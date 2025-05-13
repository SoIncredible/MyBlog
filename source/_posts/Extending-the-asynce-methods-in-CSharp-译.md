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

在之前的博客文章中，我们讨论了C#编译器如何转换异步方法。本文将重点介绍C#编译器提供的扩展点，用于自定义异步方法的行为。

有三种方式可以控制异步方法的运行机制：

- 在System.Runtime.CompilerServices命名空间中提供自定义的异步方法生成器
- 使用自定义任务等待器
- 定义自己的类任务类型

# System.Runtime.CompilerServices命名空间中的自定义类型
正如我们在上一篇文章中所了解的，C#编译器将异步方法转换为一个生成的状态机，该状态机依赖于一些预定义类型。但C#编译器并不要求这些已知类型必须来自特定程序集。例如，您可以在项目中提供自己的AsyncVoidMethodBuilder实现，C#编译器会将异步机制"绑定"到您的自定义类型。

这是探索底层转换原理和了解运行时情况的绝佳方式：

```C#
namespace System.Runtime.CompilerServices
{
    // AsyncVoidMethodBuilder.cs in your project
    public class AsyncVoidMethodBuilder
    {
        public AsyncVoidMethodBuilder()
            => Console.WriteLine(".ctor");
 
        public static AsyncVoidMethodBuilder Create()
            => new AsyncVoidMethodBuilder();
 
        public void SetResult() => Console.WriteLine("SetResult");
 
        public void Start<TStateMachine>(ref TStateMachine stateMachine)
            where TStateMachine : IAsyncStateMachine
        {
            Console.WriteLine("Start");
            stateMachine.MoveNext();
        }
 
        // AwaitOnCompleted, AwaitUnsafeOnCompleted, SetException 
        // and SetStateMachine are empty
    }   
}
```

现在, 你项目中所有的异步方法都会使用这个自定义版本的`AsyncVoidMethodBuilder`. 我们可以用下面的异步方法简单测试一下:
```C#
[Test]
public void RunAsyncVoid()
{
    Console.WriteLine("Before VoidAsync");
    VoidAsync();
    Console.WriteLine("After VoidAsync");
 
    async void VoidAsync() { }
}
```

输出结果是:
```
Before VoidAsync
.ctor
Start
SetResult
After VoidAsync
```

您可以实现 UnsafeAwaitOnComplete 方法来测试带有 await 子句的异步方法在返回未完成任务时的行为。完整示例可以在 GitHub 上找到。

要修改 async Task 和 async Task<T> 方法的行为，您需要提供自己的 AsyncTaskMethodBuilder 和 AsyncTaskMethodBuilder<T> 实现。完整的实现示例可以在我的 GitHub 项目 EduAsync(*) 中找到，分别对应文件 AsyncTaskBuilder.cs 和 AsyncTaskMethodBuilderOfT.cs。

(*) 特别感谢 Jon Skeet 对这个项目的启发。这是深入理解异步机制的绝佳方式。

# Custom awaiters

前面的示例属于"黑客手段"，并不适合生产环境。虽然我们可以通过这种方式学习异步机制，但您肯定不希望在自己的代码库中看到这样的代码。C# 语言设计者在编译器中内置了正确的扩展点，允许在异步方法中"await"不同的类型。

要使某个类型成为"可等待的"（即可以在 await 表达式中使用），该类型需要遵循特定模式：

编译器必须能够找到一个名为 GetAwaiter 的实例方法或扩展方法。该方法的返回类型需要满足以下要求：

- 必须实现 INotifyCompletion 接口
- 必须包含 bool IsCompleted {get;} 属性
- 必须提供 T GetResult() 方法

这意味着我们可以轻易的将`Lazy<T>`变得awaitable:
```C#
public struct LazyAwaiter<T> : INotifyCompletion
{
    private readonly Lazy<T> _lazy;
 
    public LazyAwaiter(Lazy<T> lazy) => _lazy = lazy;
 
    public T GetResult() => _lazy.Value;
 
    public bool IsCompleted => true;
 
    public void OnCompleted(Action continuation) { }
}
 
public static class LazyAwaiterExtensions
{
    public static LazyAwaiter<T> GetAwaiter<T>(this Lazy<T> lazy)
    {
        return new LazyAwaiter<T>(lazy);
    }
}
```

```C#
public static async Task Foo()
{
    var lazy = new Lazy<int>(() => 42);
    var result = await lazy;
    Console.WriteLine(result);
}
```

这个示例可能看起来有些刻意，但这种扩展机制实际上非常实用，并且已被广泛应用。例如：

.NET 的 Reactive Extensions 就提供了自定义等待器，允许在异步方法中等待 IObservable<T> 实例

基础类库(BCL)本身也包含多个实现：

YieldAwaitable（由 Task.Yield 使用）

HopToThreadPoolAwaitable

```C#
public struct HopToThreadPoolAwaitable : INotifyCompletion
{
    public HopToThreadPoolAwaitable GetAwaiter() => this;
    public bool IsCompleted => false;
 
    public void OnCompleted(Action continuation) => Task.Run(continuation);
    public void GetResult() { }
}
```
以下单元测试展示了最后一个等待器的实际应用：

```C#
[Test]
public async Task Test()
{
    var testThreadId = Thread.CurrentThread.ManagedThreadId;
    await Sample();
 
    async Task Sample()
    {
        Assert.AreEqual(Thread.CurrentThread.ManagedThreadId, testThreadId);
 
        await default(HopToThreadPoolAwaitable);
        Assert.AreNotEqual(Thread.CurrentThread.ManagedThreadId, testThreadId);
    }
}
```

任何异步方法的第一部分（在首个 await 语句之前）都会同步执行。在大多数情况下，这种机制非常适合用于立即进行参数验证，但有时我们需要确保方法体不会阻塞调用者线程。HopToThreadPoolAwaitable 的作用就是确保方法的剩余部分在线程池线程而非调用者线程中执行。

# Task-Like Types

自定义等待器（custom awaiters）从支持 async/await 的编译器最初版本（即 C# 5）就已存在。这种扩展机制虽然很有用，但存在限制——所有异步方法都必须返回 void、Task 或 Task<T>。从 C# 7.2 开始，编译器开始支持类任务类型。

类任务类型是指具有关联构建器类型的类或结构体，该构建器类型通过 AsyncMethodBuilderAttribute(**) 标识。要使类任务类型真正有用，它必须满足前一节描述的"可等待"条件。本质上，类任务类型整合了前文所述的两种扩展机制，并将第一种方式转化为官方支持方案。

(**) 目前您需要自行定义此特性，示例可在我的 GitHub 代码库中找到。

以下是一个定义为结构体的自定义类任务类型简单示例：

```C#
public sealed class TaskLikeMethodBuilder
{
    public TaskLikeMethodBuilder()
        => Console.WriteLine(".ctor");
 
    public static TaskLikeMethodBuilder Create()
        => new TaskLikeMethodBuilder();
 
    public void SetResult() => Console.WriteLine("SetResult");
 
    public void Start<TStateMachine>(ref TStateMachine stateMachine)
        where TStateMachine : IAsyncStateMachine
    {
        Console.WriteLine("Start");
        stateMachine.MoveNext();
    }
 
    public TaskLike Task => default(TaskLike);
 
    // AwaitOnCompleted, AwaitUnsafeOnCompleted, SetException 
    // and SetStateMachine are empty

}
 
[System.Runtime.CompilerServices.AsyncMethodBuilder(typeof(TaskLikeMethodBuilder))]
public struct TaskLike
{
    public TaskLikeAwaiter GetAwaiter() => default(TaskLikeAwaiter);
}
 
public struct TaskLikeAwaiter : INotifyCompletion
{
    public void GetResult() { }
 
    public bool IsCompleted => true;
 
    public void OnCompleted(Action continuation) { }
}
```

现在，我们可以定义返回类任务类型（TaskLike）的方法，甚至可以在方法体内使用不同的类任务类型：

```C#
public async TaskLike FooAsync()
{
    await Task.Yield();
    await default(TaskLike);
}
```

引入类任务类型（task-like types）的主要目的是减少异步操作的开销。每个返回 Task<T> 的异步操作至少会在托管堆上分配一个对象——即任务本身。对于绝大多数应用程序来说，这完全不是问题，尤其是处理粗粒度异步操作时。但对于基础设施级别的代码（每秒可能涉及数千个小型任务）而言，情况则不同。在这类场景下，减少每次调用的内存分配可以显著提升性能。

# 异步模式扩展基础
C# 编译器为扩展异步方法提供了多种方式：
- 修改现有基于 Task 的异步方法, 通过提供自定义的 AsyncTaskMethodBuilder 类型实现来改变行为
- 实现"可等待模式", 通过实现 "awaitable pattern" 使类型支持 await 操作
- 构建自定义类任务类型（C# 7+）, 从 C# 7 开始支持创建自己的类任务类型

# 扩展阅读

[深度解析 C# 异步方法](Dissecting the async methods in C#)

[EduAsync 代码库（GitHub）](EduAsync repo on github)

[类任务类型详解](Task-like types)

下一篇博客, 我们将探讨异步方法的性能特征，并分析新型值类型 System.ValueTask 如何影响性能表现。