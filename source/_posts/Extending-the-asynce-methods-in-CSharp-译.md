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


首先要搞清楚, 为什么要实现我们自己的异步, 我们可以使用Task来完成我们的异步操作, 我们将需要异步的操作用Task包装, Task(Awaiter)何时结束、如何调度都是TaskScheduler做的, 我们没有太多权限干预 我们能做的只是告诉Task 等你的任务完成之后 你需要调用stateMachiner的MoveNext方法, 如果我要实现自己的类似ETTask的功能, 我希望我能自己决定如何调度这些ETTask, 要不然实现自己的ETTask的意义就没有了. C#中异步操作到这里就已经揭示地比较清楚了, 只是Task的调度目前对我们来说还是黑盒, 最好它也只是黑盒

因此, 在笔者看来, 要实现自己的异步, 就是要实现如何调度这些异步操作, 即我们要实现TaskScheduler的功能.

我们使用Task包装一些操作, 这些操作可能是`ComputeBound`类型或者`IOBound`类型, 也可能只是一个延时`Task.Delay`操作, 甚至可能就是一个简单的同步方法. 不论是什么操作, 你只要将该操作使用Task包装, 并且调用了该Task, 那么你就可以通过获取`Task.GetAwaiter()`接口获取到该Task的Awaiter, 然后通过调用Awaiter.IsComplete来判断包装进该Task的操作有没有完成.

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