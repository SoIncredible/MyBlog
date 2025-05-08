---
title: Dissecting the async methods in CSharp
date: 2025-05-07 20:44:20
tags: 异步
categories: C#
cover:
description:
swiper_index:
sticky:
---

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

下面我们就来看一下上述提到的缺陷在真正的异步机制中是如何被解决的.

# 异步机制

编译器执行异步方法转换的整个过程, 其实已经和上面我们手动实现的方式十分接近了. 为了得到预期的行为, 编译器依赖于以下的类型:

- 对于一个异步方法的生成的状态机, 其行为会像栈帧一样, 并且该状态机包含原始异步方法中的全部逻辑
- `AsyncTaskMethodBuilder<T>`持有完成的task（与 TaskCompletionSource<T> 类型非常相似）, 并且管理状态机的转换.
- `TaskAwaiter<T>`负责包装task, 并在需要时调度其延续
- `MoveNetRunner`负责在正确的执行上下文中调用`IAsyncStateMachine.MoveNext`方法

生成的状态机在Debug模式下是一个类, 在Release模式下则是一个struct. 其他的类型(除了`MoveNextRunner`类)在BCL中均被定义为了struct.

编译器生成的状态机类型名称通常类似于`<YourMethodNameAsync>d_1`, 为了避免名称冲突, 生成的名字中包含了一些无效的标识符字符, 这些字符无法被用户直接定义或引用. 但是为了简化说明, 在后续的所有示例中, 我们还是将使用有效的标识符来替换掉`<`、`>`、`_`这些字符, 并且使用更容易理解的名字.

# 原始方法

原始的异步方法会创建一个状态机实例, 用捕获的状态（如果方法非静态，则包括 this 指针）初始化该实例, 通过传递状态机实例的引用并调用`AsyncTaskMethodBuilder<T>.Start`来启动执行

```
[AsyncStateMachine(typeof(_GetStockPriceForAsync_d__1))]
public Task<decimal> GetStockPriceFor(string companyId)
{
    _GetStockPriceForAsync_d__1 _GetStockPriceFor_d__;
    _GetStockPriceFor_d__.__this = this;
    _GetStockPriceFor_d__.companyId = companyId;
    _GetStockPriceFor_d__.__builder = AsyncTaskMethodBuilder<decimal>.Create();
    _GetStockPriceFor_d__.__state = -1;
    var __t__builder = _GetStockPriceFor_d__.__builder;
    __t__builder.Start<_GetStockPriceForAsync_d__1>(ref _GetStockPriceFor_d__);
    return _GetStockPriceFor_d__.__builder.Task;
}
```

传递引用是一个重要的优化, 因为一个状态机往往是相对大的结构(大于100bytes), 因此传递引用会避免冗余拷贝.
状态机代码如下:
```
struct _GetStockPriceForAsync_d__1 : IAsyncStateMachine
{
    public StockPrices __this;
    public string companyId;
    public AsyncTaskMethodBuilder<decimal> __builder;
    public int __state;
    private TaskAwaiter __task1Awaiter;
 
    public void MoveNext()
    {
        decimal result;
        try
        {
            TaskAwaiter awaiter;
            if (__state != 0)
            {
                // State 1 of the generated state machine:
                if (string.IsNullOrEmpty(companyId))
                    throw new ArgumentNullException();
 
                awaiter = __this.InitializeLocalStoreIfNeededAsync().GetAwaiter();
 
                // Hot path optimization: if the task is completed,
                // the state machine automatically moves to the next step
                if (!awaiter.IsCompleted)
                {
                    __state = 0;
                    __task1Awaiter = awaiter;
 
                    // The following call will eventually cause boxing of the state machine.
                    __builder.AwaitUnsafeOnCompleted(ref awaiter, ref this);
                    return;
                }
            }
            else
            {
                awaiter = __task1Awaiter;
                __task1Awaiter = default(TaskAwaiter);
                __state = -1;
            }
 
            // GetResult returns void, but it'll throw if the awaited task failed.
            // This exception is catched later and changes the resulting task.
            awaiter.GetResult();
            __this._stocks.TryGetValue(companyId, out result);
        }
        catch (Exception exception)
        {
            // Final state: failure
            __state = -2;
            __builder.SetException(exception);
            return;
        }
 
        // Final state: success
        __state = -2;
        __builder.SetResult(result);
    }
 
    void IAsyncStateMachine.SetStateMachine(IAsyncStateMachine stateMachine)
    {
        __builder.SetStateMachine(stateMachine);
    }
}
```

生成的状态机代码看起来比较复杂, 但是本质上, 它和我们手写的那一版的代码是十分相似的.
即使两个版本十分相似, 但是还是有一些重要的不同的.

1. "Hot Path" 优化

不像我们原生的方式, 生成的状态机知道一个被awaited的task可能已经完成了
```
awaiter = __this.InitializeLocalStoreIfNeededAsync().GetAwaiter();
 
// Hot path optimization: if the task is completed,
// the state machine automatically moves to the next step
if (!awaiter.IsCompleted)
{
    // Irrelevant stuff
 
    // The following call will eventually cause boxing of the state machine.
    __builder.AwaitUnsafeOnCompleted(ref awaiter, ref this);
    return;
}
```

如果这个被await的task已经完成了(无论成功与否), 状态机会向前直行下一步
```
// GetResult returns void, but it'll throw if the awaited task failed.
// This exception is catched later and changes the resulting task.
awaiter.GetResult();
__this._stocks.TryGetValue(companyId, out result);
```

这就意味着, 如果所有awaited的task都已经完成了, 整个状态机将只会停留在栈上. 一个异步方法甚至在今天如果所有awaited的task都已经完成或者同步完成了, 那么这个异步方法是可以只会造成很小的内存开销的. 仅剩的开销只是这个task本身.

2. 错误处理

当前逻辑并未专门处理任务处于故障状态或已取消状态的情况。状态机通过调用awaiter.GetResult()方法，当任务被取消时将抛出TaskCancelledException，若任务失败则抛出其他异常。这种设计十分优雅，因为GetResult()在错误处理机制上与task.Wait()或task.Result有本质区别。

无论是task.Wait()还是task.Result，即便任务因单一异常导致失败，它们都会抛出AggregateException。这背后的逻辑很简单：任务不仅可能代表通常只有单一故障的IO操作，也可能是并行计算的结果。后者可能产生多个错误，而AggregateException正是为聚合所有错误而设计。

但async/await模式专为异步操作设计，这类操作通常最多只会产生一个错误。因此语言设计者认为，若awaiter.GetResult()能对AggregateException进行解包并仅抛出首个异常，将更符合使用场景。这一设计并非完美，我们将在后续文章中看到这种抽象方案可能存在的漏洞。

异步状态机只是整个拼图的一部分。要完整理解其运作机制，我们还需了解状态机实例如何与TaskAwaiter<T>和AsyncTaskMethodBuilder<T>进行交互。

# 这些模块是如何被联系到一起的呢?

[](https://devblogs.microsoft.com/wp-content/uploads/sites/31/2019/06/Async_sequence_state_machine_thumb.png)