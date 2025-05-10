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

另外, 如果读者对C#中的异步不是很了解, 推荐先看一下下面四篇翻译的文章:
[Dissecting the async methods in C#](https://soincredible.github.io/posts/72dba58e)
[Extending the async methods in C#](https://soincredible.github.io/posts/40aca622/)
[The performance characteristics of the async methods in C#]()
[One user scenario to rule them all]()

请思考这句话: Task是Task, Async是Async. 有Task并不一定意味着异步操作, 有Async也并不意味着一定有异步操作. 也就是说, 并不是只有在异步的场景下我们才可以使用Task, Task依然可以在同步场景下使用, 而`async`关键字也不能完全和异步绑定, 因为`async`关键字的作用只是告诉编译器对这个方法做一些特殊的处理: 每一个被标记为async的方法, Compiler在背后都会在其内部生成一个状态机.


# 关于异步和多线程的讨论

我们看一下 这个例子

ThreadSynchronizationContext的作用?

# C#中要实现异步需要哪些角色

YooAsset、ETTask、UniTask三者异步的实现方式的区别是什么？

# SynchronizationContext

SynchronizationContext中存储了一些能够标识线程身份的信息，现在你有一个方法，你可以通过`SynchronizationContext.Send()`或者`SynchronizationContext.Post`方法把你要执行的这个方法丢给你想要让他执行的线程里面去，可以把他理解为是一种跨线程的方法调用的方式。
在一般单线程里，方法的调用都是直来直去，而在多线程里面，可以通过SynchronizationContext来实现线程间的函数调用。
要注意一下Send和Post的区别，如果使用Send的方式把一个方法丢给某一个上下文，如果这个方法恰好很耗时，那么就会卡住调用Send处之后代码的执行，而如果使用Post方法的话，则不会阻塞调用处之后代码的执行。根据需求选择用Send还是Post。示例如下👇👇

```
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

# C# 中几种异步的返回类型

C#中有三种比较常用的返回类型: void、Task<TResult>和Task

# TaskCompletionSource是什么？

按照笔者的理解，TaskCompletionSource可以将一个基于回调的异步操作转换成一个可以被await的异步操作。


抛开ET的一个例子，比如协程 协程是不可以被await的

## 参考文档
- https://blog.csdn.net/q__y__L/article/details/133905192
- https://devblogs.microsoft.com/premier-developer/dissecting-the-async-methods-in-c/
- https://devblogs.microsoft.com/premier-developer/extending-the-async-methods-in-c/
- https://devblogs.microsoft.com/premier-developer/the-performance-characteristics-of-async-methods/
- https://devblogs.microsoft.com/premier-developer//one-user-scenario-to-rule-them-all/
- https://www.cnblogs.com/liqingwen/p/6218994.html?tdsourcetag=s_pcqq_aiomsg
- https://www.cnblogs.com/wwkk/p/17814057.html
- https://blog.csdn.net/shizuguilai/article/details/121236777
- https://www.cnblogs.com/peterYong/p/16328187.html
- https://learn.microsoft.com/en-us/archive/msdn-magazine/2011/february/msdn-magazine-parallel-computing-it-s-all-about-the-synchronizationcontext
- https://www.cnblogs.com/eventhorizon/p/15316955.html#4991898
- https://www.cnblogs.com/eventhorizon/p/15824541.html
