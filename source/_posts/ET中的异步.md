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

主要是一些异步的操作看起来比较吃力，因此在这边博客中对C#中的异步操作做一个简单的介绍。

# 多线程系列
- https://www.cnblogs.com/eventhorizon/p/15316955.html#4991898
- https://www.cnblogs.com/eventhorizon/p/15824541.html

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

## 参考资料
- https://www.cnblogs.com/wwkk/p/17814057.html
- https://blog.csdn.net/shizuguilai/article/details/121236777
- https://www.cnblogs.com/peterYong/p/16328187.html

# C# 中几种异步的返回类型

C#中有三种比较常用的返回类型: void、Task<TResult>和Task

## 参考资料
- https://www.cnblogs.com/liqingwen/p/6218994.html?tdsourcetag=s_pcqq_aiomsg

# TaskCompletionSource是什么？

按照笔者的理解，TaskCompletionSource可以将一个基于回调的异步操作转换成一个可以被await的异步操作。

在ET中


抛开ET的一个例子，比如协程 协程是不可以被await的

## 参考文档
- https://blog.csdn.net/q__y__L/article/details/133905192


# 一个类是如何变得可以被await的？

ETTask是可以被await的，如何做到的？