---
title: The performance characteristics of async methods in C#
date: 2025-05-13 08:59:38
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

在最近的两篇博客文章中，我们深入探讨了C#异步方法的内部实现机制，并详细分析了C#编译器提供的扩展点如何调整异步方法的行为。今天，我们将重点研究异步方法的性能特征。

正如本系列第一篇文章所述，编译器进行了大量转换工作，使异步编程体验几乎与同步编程无异。但为了实现这一点，编译器需要创建状态机实例、将其传递给异步方法生成器、调用任务等待器等。显然，所有这些逻辑都会带来性能开销，但具体代价有多大呢？

在TPL（任务并行库）出现之前，异步操作通常粒度较粗，因此其开销往往可以忽略不计。但在现代应用中，即使相对简单的程序每秒也可能执行成百上千次异步操作。TPL虽然针对这种工作负载进行了优化设计，但它并非魔法，仍然存在一定开销。

为了准确测量异步方法的开销，我们将对首篇博客中的示例进行适当调整后作为测试基准。

```C#
public class StockPrices
{
    private const int Count = 100;
    private List<(string name, decimal price)> _stockPricesCache;
 
    // Async version
    public async Task<decimal> GetStockPriceForAsync(string companyId)
    {
        await InitializeMapIfNeededAsync();
        return DoGetPriceFromCache(companyId);
    }
 
    // Sync version that calls async init
    public decimal GetStockPriceFor(string companyId)
    {
        InitializeMapIfNeededAsync().GetAwaiter().GetResult();
        return DoGetPriceFromCache(companyId);
    }
 
    // Purely sync version
    public decimal GetPriceFromCacheFor(string companyId)
    {
        InitializeMapIfNeeded();
        return DoGetPriceFromCache(companyId);
    }
 
    private decimal DoGetPriceFromCache(string name)
    {
        foreach (var kvp in _stockPricesCache)
        {
            if (kvp.name == name)
            {
                return kvp.price;
            }
        }
 
        throw new InvalidOperationException($"Can't find price for '{name}'.");
    }
 
    [MethodImpl(MethodImplOptions.NoInlining)]
    private void InitializeMapIfNeeded()
    {
        // Similar initialization logic.
    }
 
    private async Task InitializeMapIfNeededAsync()
    {
        if (_stockPricesCache != null)
        {
            return;
        }
 
        await Task.Delay(42);
 
        // Getting the stock prices from the external source.
        // Generate 1000 items to make cache hit somewhat expensive
        _stockPricesCache = Enumerable.Range(1, Count)
            .Select(n => (name: n.ToString(), price: (decimal)n))
            .ToList();
        _stockPricesCache.Add((name: "MSFT", price: 42));
    }
}
```