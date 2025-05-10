---
title: Extending the asynce methods in CSharp(译)
abbrlink: 40aca622
date: 2025-05-08 21:01:43
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


我们希望的是Task可以被await, 那么Task中就要有一个`GetAwaiter()`方法
那么task的GetAwaiter方法返回回来的是一个TaskAwaiter类型
所以是TaskAwaiter类型实现了INotifyCompletion接口、 且TaskAwaiter中有`bool IsComplete {get;}`属性和`T GetResult()`方法

```
public static async Task Foo()
{
    var lazy = new Lazy<int>(() => 42);
    var result = await lazy;
    Console.WriteLine(result);
}
```

其中() => 42 的含义
()：表示一个没有参数的匿名方法（类似无参函数）。

=>：Lambda 运算符，分隔参数和方法体。

42：方法的返回值（此处直接返回常量值 42）。

Lazy<T> 的构造函数接受一个 Func<T> 委托（即一个无参且返回 T 类型的方法）。Lambda 表达式 () => 42 正好匹配这个委托类型：

Func<int> 的签名是 int Func()，而 () => 42 是一个无参且返回 int 的表达式。

因此，代码可以简化为 Lambda 形式，而不需要显式定义一个单独的方法。