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

本篇博客不止讨论ETTask如何实现, 更想探讨C#底层是如何支持异步实现的. 如果读者像笔者一样, 通过Unity接触到的C#语言, 可能对协程和异步概念的理解上有偏差, **因为我们在Unity中使用的协程并不是操作系统层次下讨论的与线程、进程、协程中的协程概念**, Unity的协程是Unity基于IEnumerator和Unity事件更新框架实现的伪协程、伪异步, Unity的协程限制非常多, 如果读者对Unity的协程、IEnumerator和`yield return`语法糖有疑惑, 欢迎参考[这篇博客](https://soincredible.github.io/posts/133a9667/), 希望能帮助你理解.

本篇博客首先会讨论C#中异步的实现思路, 然后会讨论ETTask的实现思路

# 关于异步的历史

C#开发者在第一次接触异步的概念, 应该是通过Task类型. Task是在`.Net 4.0`的时候被引入的. 一个task就是一个work单元, 且该task承诺, 在这个task未来完成的时候, 会将结果返回给task的调用者. 这个Task可能是由IO操作支持或者计算密集型操作(这正好对应了笔者在协程部分所说的CPU密集型操作和IO型操作, Unity协程只能用来做IO型的异步操作). 重要的是该操作的结果是自包含的，且具有一等公民身份。你可以自由传递这个"未来"：将其存储在变量中、从方法返回它、或传递给其他方法。你可以将两个"未来"合并形成新的任务，可以同步等待结果，也可以通过添加"延续"来"等待"结果。仅凭任务实例本身，你就能决定在操作成功、失败或被取消时采取何种处理。

任务并行库 (TPL) 改变了我们对并发编程的认知，而 C# 5 通过引入 async/await 进一步推动了这一发展。async/await 让任务的组合变得更加容易，并允许开发者使用熟悉的代码结构，如 try/catch、using 等。但是async/await也有其开销. 要理解具体开销是什么, 我们需要深入底层机制中.

# 异步方法内部

普通方法只有一个入口点和一个退出点（虽然可能有多个 return 语句，但在运行时，每次调用仅有一个实际退出点）。但 异步方法（*） 和 迭代器方法（包含 yield return 的方法）则不同。对于异步方法而言，调用方几乎可以立即获取结果（即 Task 或 Task<T>），然后通过返回的任务（Task）来 “await” 方法的实际执行结果。

（*）我们定义的 “异步方法” 是指用 async 上下文关键字标记的方法。这并不一定意味着该方法会异步执行，甚至完全不意味着它是异步的。它仅表示 编译器会对该方法进行某些特殊转换。

看一下下面这个异步方法:
```
class StockPrices
{
    private Dictionary<string, decimal> _stockPrices;
    public async Task<decimal> GetStockPriceForAsync(string companyId)
    {
        await InitializeMapIfNeededAsync();
        _stockPrices.TryGetValue(companyId, out var result);
        return result;
    }
 
    private async Task InitializeMapIfNeededAsync()
    {
        if (_stockPrices != null)
            return;
 
        await Task.Delay(42);
        // Getting the stock prices from the external source and cache in memory.
        _stockPrices = new Dictionary<string, decimal> { { "MSFT", 42 } };
    }
}
```

为了更好的理解编译器做了或者能做什么, 让我们尝试着手写一下转换过程

# 手动解构异步方法

TPL（任务并行库）提供了两大核心构建模块，帮助我们构造和组合任务：
- 任务延续（Task Continuation）-> 通过 `Task.ContinueWith`实现
- 手动构建任务 -> 通过 `TaskCompletionSource<T>` 类实现
（注：前者用于链式编排任务，后者用于手动控制任务生命周期。）
```
class GetStockPriceForAsync_StateMachine
{
    enum State { Start, Step1, }
    private readonly StockPrices @this;
    private readonly string _companyId;
    private readonly TaskCompletionSource<decimal> _tcs;
    private Task _initializeMapIfNeededTask;
    private State _state = State.Start;
 
    public GetStockPriceForAsync_StateMachine(StockPrices @this, string companyId)
    {
        this.@this = @this;
        _companyId = companyId;
    }
 
    public void Start()
    {
        try
        {
            if (_state == State.Start)
            {
                // The code from the start of the method to the first 'await'.
 
                if (string.IsNullOrEmpty(_companyId))
                    throw new ArgumentNullException();
 
                _initializeMapIfNeededTask = @this.InitializeMapIfNeeded();
 
                // Update state and schedule continuation
                _state = State.Step1;
                _initializeMapIfNeededTask.ContinueWith(_ => Start());
            }
            else if (_state == State.Step1)
            {
                // Need to check the error and the cancel case first
                if (_initializeMapIfNeededTask.Status == TaskStatus.Canceled)
                    _tcs.SetCanceled();
                else if (_initializeMapIfNeededTask.Status == TaskStatus.Faulted)
                    _tcs.SetException(_initializeMapIfNeededTask.Exception.InnerException);
                else
                {
                    // The code between first await and the rest of the method
 
                    @this._store.TryGetValue(_companyId, out var result);
                    _tcs.SetResult(result);
                }
            }
        }
        catch (Exception e)
        {
            _tcs.SetException(e);
        }
    }
 
    public Task<decimal> Task => _tcs.Task;
}
 
public Task<decimal> GetStockPriceForAsync(string companyId)
{
    var stateMachine = new GetStockPriceForAsync_StateMachine(this, companyId);
    stateMachine.Start();
    return stateMachine.Task;
}
```

这段代码虽然冗长，但逻辑相对直接。原先在 GetStockPriceForAsync 中的所有逻辑都被转移到了 GetStockPriceForAsync_StateMachine.Start 方法中，该方法采用了"延续传递风格"(continuation passing style)。异步转换的核心算法就是将原始方法在 await 边界处分割成若干代码块：

- 第一个代码块：从方法开始到第一个 await 之间的代码
- 第二个代码块：从第一个 await 到第二个 await 之间的代码
- 第三个代码块：从第二个 await 到第三个 await 或方法结束的代码 以此类推

```
// Step 1 of the generated state machine:
 
if (string.IsNullOrEmpty(_companyId)) throw new ArgumentNullException();
_initializeMapIfNeededTask = @this.InitializeMapIfNeeded();
```

每个被 await 的任务现在都变成了状态机的一个字段，而 Start 方法会将自己注册为这些任务的延续（continuation）：

```
_state = State.Step1;
_initializeMapIfNeededTask.ContinueWith(_ => Start());
```

随后，当任务完成时，Start方法会被回调，并通过检查_state字段来确定当前执行阶段。接着，状态机会判断任务是成功完成、被取消还是出现异常。如果是成功完成的情况，状态机就会继续执行下一个代码块。当所有操作都完成后，状态机会设置TaskCompletionSource<T>实例的结果值，此时从GetStockPricesForAsync返回的任务状态就会变更为已完成。

```
// The code between first await and the rest of the method
 
@this._stockPrices.TryGetValue(_companyId, out var result);
_tcs.SetResult(result); // The caller gets the result back
```

这种"实现方式"存在几个严重缺陷：

- 大量堆内存分配：
  - 状态机实例需要1次内存分配
  - TaskCompletionSource<T>需要1次分配
  - TaskCompletionSource<T>内部任务需要1次分配, 按照GPT的说法`TaskCompletionSource<decimal>`内部会自动创建一个`Task<decimal>` 实例（通过其 .Task 属性访问）
  - 延续委托(delegate)需要1次分配, 指的应该是` _initializeMapIfNeededTask.ContinueWith(_ => Start());`
- 缺少"热路径优化"：
  当被等待的任务已经完成时，完全没有必要创建延续委托
- 可扩展性不足：
  实现与Task类紧密耦合，导致无法用于其他场景，例如：
  - 等待非Task类型
  - 返回非Task/Task<T>类型


# 一个类是如何变得可以被await的？

在Sergey的[这篇博客](https://devblogs.microsoft.com/premier-developer/extending-the-async-methods-in-c/)中提到, 如果要让一个类变得"awaitable", 这个类就必须遵循一些模式:
- 编译器要能够找到一个实例或者一个扩展方法叫做`GetAwaiter`. 这个方法的返回类型需要遵守某些要求:
- 这个返回类型需要实现`INoifyCompletion`接口
- 这个返回类型需要有`bool IsCompleted{ get; }`属性和`T GetResult()`方法



# 关于异步和多线程的讨论

异步是异步, 多线程是多线程

多线程是真线程概念范围内的议题, 而异步的议题不止包含线程, 还包含网络请求、文件读取等和物理硬件相关的范畴

因为网络请求和文件读取等与物理硬件交互的过程并不占用线程, 因此在进行类似操作的时候, 线程上是可以执行别的任务的. 这种情况是异步

在多线程的范畴里, 由于多个任务确实就是并行地执行的, 我们也将其看成了异步

但是要注意的是, 我们Unity开发者调用Unity底层API的时候, 就拿网络请求和加载资源来说, Unity都会给我们提供两种类型的接口, 一种是异步, 一种是同步. 这两种接口都是I/O密集型操作, 因为Unity是单线程嘛. 如果你常常陷入纠结网络和资源加载的内部实现是怎样的 不利于你了解异步的本质, 

所以妄图使用协程来做一些CPU密集型的工作是根本不可能的, 因为协程本质上还是运行在一个进程上, 协程反而适合那些I/O密集型的操作, 因为这类操作并不真的会占用CPU的执行时间, 这段时间的CPU只是在空转而已, 或者我们希望使用协程达成一种延时的效果

yield和await都能实现异步, 区别是什么?

而多线程我们往往会用多线程来做一些CPU密集型的任务, 我们会决定这些任务的具体实现, 或许会让你更好理解

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

# ICriticalNotifyCompletion

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
