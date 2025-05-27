---
title: ET中的异步
abbrlink: a8cdc42b
date: 2025-03-25 16:21:26
tags: ET
categories: ET
cover: https://public.ysjf.com/content/title-image/%E5%9F%8E%E5%B8%82%E4%BA%BA%E6%96%87-%E4%BF%84%E7%BD%97%E6%96%AF%E8%8E%AB%E6%96%AF%E7%A7%91-03-%E5%85%A8%E6%99%AF.jpg
description:
swiper_index:
sticky:
---

本篇博客不止讨论ETTask如何实现, 更想探讨C#底层是如何支持异步实现的. 如果读者像笔者一样, 通过Unity接触到的C#语言, 可能对协程和异步概念的理解上有偏差, **因为我们在Unity中使用的协程并不是操作系统层次下讨论的与线程、进程、协程中的协程概念**, Unity的协程是Unity基于IEnumerator和Unity事件更新框架实现的伪协程、伪异步, Unity的协程限制非常多, 如果读者对Unity的协程、IEnumerator和`yield return`语法糖有疑惑, 欢迎参考[IEnumerator与IEnumerable辨析](https://soincredible.github.io/posts/133a9667/)和[关于协程](https://soincredible.github.io/posts/83d7c4e7/)这两篇博客, 希望能帮助你理解.

本篇博客首先会讨论C#中异步的实现思路, 然后会讨论ETTask的实现思路, UniTask和YooAsset中的异步也在本系列的讨论之中.
[对Task的概述](https://soincredible.github.io/posts/323f6783/)

另外, 如果读者对C#中的异步不是很了解, 推荐先看一下下面五篇翻译的文章:
[Dissecting the async methods in C#](https://soincredible.github.io/posts/72dba58e)
[Extending the async methods in C#](https://soincredible.github.io/posts/40aca622/)
[The performance characteristics of the async methods in C#]()
[One user scenario to rule them all]()
[A Deep Dive into C#'s Cancellation](https://soincredible.github.io/posts/7331d0f1)

在理解了上面博客中的内容后, 请思考这句话: Task是Task, Async是Async. 有Task并不一定意味着异步操作, 有Async也并不意味着一定有异步操作. 也就是说, 并不是只有在异步的场景下我们才可以使用Task, Task依然可以在同步场景下使用, 而`async`关键字也不能完全和异步绑定, 因为`async`关键字的作用只是告诉编译器对这个方法做一些特殊的处理: 每一个被标记为async的方法, Compiler在背后都会在其内部生成一个状态机.

# 为什么ET框架要设计自己的异步返回类型? 和Task相比, ET自己设计的异步返回类型有哪些优势.

> ⚠️Task有冷热之分
> 冷任务（Cold Task）不会自动执行，必须显式调用 Start()、RunSynchronously() 或通过任务调度器触发。
> 热任务（Hot Task）无需手动启动，任务在被创建时已经处于 Running 状态。

```C#
private void Start()
{
    var s = Func2();
    s.Start();
}

private async Task Func2()
{
    await Task.Delay(42);
}
```
上面代码运行会报错:
```
InvalidOperationException: Start may not be called on a promise-style task.
```
因为s是一个热任务, 在返回该任务时已经隐式Start了, 不必调用Start接口. 或者就按照它的报错信息理解, 通过非直接调用Task构造方法拿到的Task实例都是`promise-style`的, 这种Task都不能调用Start.


ET作者猫大说: ETTask说自己是单线程的, 不支持多线程, 不像Task要支持多线程 ETTask做了什么?

请读者们想一想, 自己在用Task的时候, 从来没有调用过`TaskAwaiter`或者说`AsyncTaskMethodBuilder`的SetResult接口吧? 这是因为有TaskScheduler的存在, 在背后有一套自己的调度机制.
一个返回类型为Task的方法, 返回的是一个热任务, 该任务在被创建出来的那一刻就已经要给到`TaskScheduler`进行管理了
Task可能一层一层地嵌套上来, 在业务使用上, 开发者最底层一般`Task.Run()`或者`Task.Delay`这样的接口, 上层的这些Task, 该任务在被创建出来的那一刻就已经要给到`TaskScheduler`进行管理了, Task如何调度完全不受我们开发者的控制, 我们来回想一下我们是在哪一步将控制权转交给TaskScheduler的. 

由于Task由TaskScheduler调度, 我们无法控制, 有可能涉及到多线程、出现上下文跨线程传递的开销, 因此ETTask的目标是自主控制调度、单线程作业, 你可以这么理解Task是C#的TaskScheduler, 来调用SetResult, 既然ETTask决定使用自己的异步机制, 那么就需要自己实现一个像TaskScheduler一样的调度机制, 在`ProcessInnerSender`组件中, 就有一套ETTask的调度机制, 有一个requestCallback

ET框架中一共实现了三种用于异步操作的返回类型:`ETVoid`、`ETTask`和`ETTask<T>`

> 💡ETTask既是Awaiter又是可以被await的TaskLike类型, 希望不要对各位刚接触异步或ET的读者造成困扰

# 为什么ETTask里面有一个`Coroutine`方法, 它的作用是什么?



# SynchronizationContext

SynchronizationContext和ExecutionContext有什么联系吗?
SynchronizationContext中存储了一些能够标识线程身份的信息，现在你有一个方法，你可以通过`SynchronizationContext.Send()`或者`SynchronizationContext.Post`方法把你要执行的这个方法丢给你想要让他执行的线程里面去，可以把他理解为是一种跨线程的方法调用的方式。
在一般单线程里，方法的调用都是直来直去，而在多线程里面，可以通过SynchronizationContext来实现线程间的函数调用。
要注意一下Send和Post的区别，如果使用Send的方式把一个方法丢给某一个上下文，如果这个方法恰好很耗时，那么就会卡住调用Send处之后代码的执行，而如果使用Post方法的话，则不会阻塞调用处之后代码的执行。根据需求选择用Send还是Post。示例如下👇👇

```C#
using System.Threading;
using UnityEngine;

namespace Learn
{
    public class LearnSynchronizationContext : MonoBehaviour
    {
        // SynchronizationContext的理解和使用
        private SynchronizationContext _synchronizationContext;

        private Thread _thread; // 新建一个线程 让上下文指向这个线程
        
        public void Start()
        {
            // 不能把这个上下文设置成主线程 因为下面的测试代码中要在该上下文线程里面执行while循环
            // 会卡住主线程
            // _synchronizationContext = SynchronizationContext.Current;
            this._synchronizationContext = new SynchronizationContext();
            
            // _thread = new Thread(() =>
            // {
            //     this._synchronizationContext.Post(async (obj) =>
            //     {
            //         // 让这个方法执行的久一点
            //         // 向上下文中抛出一个方法   
            //         // 执行某个方法， 这个方法要比较耗时一点 才能看出Send和Post的差距
            //         await Task.Delay(1000);
            //
            //         var str = obj as STR;
            //         str.number = 20000;
            //         var threadId = Thread.CurrentThread.ManagedThreadId;
            //         Debug.Log("执行上下文Send/Post方法的线程ID是: " + threadId);
            //     }, str);
            //     
            //     Debug.Log("str字段中的number是" + str.number);
            //     var threadId = Thread.CurrentThread.ManagedThreadId;
            //     Debug.Log("执行线程方法的线程ID是" + threadId);
            //     Debug.Log("我是调用上下文Send/Post方法之后执行的语句");
            // });

            // --------------------------------------------------
            // Send
            // --------------------------------------------------
            
            _thread = new Thread(() =>
            {
                this._synchronizationContext.Send((obj) =>
                {
                    // 让这个方法执行的久一点
                    // 向上下文中抛出一个方法   
                    // 执行某个方法， 这个方法要比较耗时一点 才能看出Send和Post的差距
                    while (true)
                    {
                        
                    }
                }, null);
                
                var threadId = Thread.CurrentThread.ManagedThreadId;
                Debug.Log("执行线程方法的线程ID是" + threadId);
                Debug.Log("我是调用上下文Send/Post方法之后执行的语句");
            });

            // --------------------------------------------------
            // Post
            // --------------------------------------------------
            
            // _thread = new Thread(() =>
            // {
            //     this._synchronizationContext.Post((obj) =>
            //     {
            //         // 让这个方法执行的久一点
            //         // 向上下文中抛出一个方法   
            //         // 执行某个方法， 这个方法要比较耗时一点 才能看出Send和Post的差距
            //         while (true)
            //         {
            //             
            //         }
            //     }, str);
            //     
            //     Debug.Log("str字段中的number是" + str.number);
            //     var threadId = Thread.CurrentThread.ManagedThreadId;
            //     Debug.Log("执行线程方法的线程ID是" + threadId);
            //     Debug.Log("我是调用上下文Send/Post方法之后执行的语句");
            // });
            
            // 开始执行这个线程 
            this._thread.Start();
        }
    }
}
```

# TaskCompletionSource是什么？

按照笔者的理解，TaskCompletionSource可以将一个基于回调的异步操作转换成一个可以被await的异步操作。

# ETTask与UniTask对比

# 扩展

- [YooAsset资源异步加载机制](https://soincredible.github.io/posts/ae5b3442)

## 参考文档
- [C#中的TaskCompletionSource](https://blog.csdn.net/q__y__L/article/details/133905192)
- [Dissecting the async methods in C#](https://devblogs.microsoft.com/premier-developer/dissecting-the-async-methods-in-c/)
- [Extending the async methods in C#](https://devblogs.microsoft.com/premier-developer/extending-the-async-methods-in-c/)
- [The performance characteristics of async methods in C#](https://devblogs.microsoft.com/premier-developer/)the-performance-characteristics-of-async-methods/
- [One user scenario to rule them all](https://devblogs.microsoft.com/premier-developer//one-user-scenario-to-rule-them-all/)
- [async 的三大返回类型](https://www.cnblogs.com/liqingwen/p/6218994.html?)tdsourcetag=s_pcqq_aiomsg
- [C# SynchronizationContext和ExecutionContext使用总结](https://www.cnblogs.com/wwkk/p/17814057.html)
- [详解 SynchronizationContext](https://blog.csdn.net/shizuguilai/article/details/121236777)
- [SynchronizationContext](https://www.cnblogs.com/peterYong/p/16328187.html)
- [Parallel Computing - It's All About the SynchronizationContext](https://learn.microsoft.com/en-us/archive/msdn-magazine/2011/february/)msdn-magazine-parallel-computing-it-s-all-about-the-synchronizationcontext
- [概述 .NET 6 ThreadPool 实现](https://www.cnblogs.com/eventhorizon/p/15316955.html#4991898)
- [.NET Task 揭秘（1）：什么是Task](https://www.cnblogs.com/eventhorizon/p/15824541.html)
