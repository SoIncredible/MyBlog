---
title: Dissecting the async methods in CSharp
tags: 异步
categories: C#
abbrlink: 72dba58e
date: 2025-05-07 20:44:20
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
    // 被标记了async的方法, Compiler会在背后将其内部的逻辑转调用一个状态机
    // 而这个异步方法中原来的逻辑会全部转移到Compiler自动生成的状态机的MoveNext方法中 
    public async Task<decimal> GetStockPriceForAsync(string companyId)
    {
        await InitializeMapIfNeededAsync();
        _stockPrices.TryGetValue(companyId, out var result);
        return result;
    }
 
    // 被标记了async的方法, Compiler会在背后将其转换成状态机
    // 而这个异步方法中原来的逻辑会全部转移到Compiler自动生成的状态机的MoveNext方法中 
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

但`async/await`模式专为异步操作设计，这类操作通常最多只会产生一个错误。因此语言设计者认为，若`awaiter.GetResult()`能对AggregateException进行解包并仅抛出首个异常，将更符合使用场景。这一设计并非完美，我们将在后续文章中看到这种抽象方案可能存在的漏洞。

异步状态机只是整个拼图的一部分。要完整理解其运作机制，我们还需了解状态机实例如何与`TaskAwaiter<T>`和`AsyncTaskMethodBuilder<T>`进行交互。

# 这些模块是如何被联系到一起的呢?

[](https://devblogs.microsoft.com/wp-content/uploads/sites/31/2019/06/Async_sequence_state_machine_thumb.png)

图表看似过于复杂，但每个组件都经过精心设计且扮演着重要角色。最有趣的协作发生在等待的任务尚未完成时（图中用棕色矩形标记）：

状态机注册任务延续

状态机调用 `__builder.AwaitUnsafeOnCompleted(ref awaiter, ref this)`，将自身注册为任务的延续。
`AsyncTaskMethodBuilder` 确保任务完成后调用 `IAsyncStateMachine.MoveNext` 方法：
捕获当前 执行上下文（ExecutionContext），创建一个 MoveNextRunner 实例，将其与当前状态机实例关联。
从 MoveNextRunner.Run 创建一个 Action 委托，用于在捕获的执行上下文中推进状态机。
调用 TaskAwaiter.UnsafeOnCompleted(action)，将上述 Action 委托调度为等待任务的延续。
任务完成后的恢复

当等待的任务完成时，注册的回调（Action 委托）被触发，状态机继续执行异步方法的下一段代码块。

# 执行上下文
问题：什么是执行上下文？为何需要这种复杂的设计？
在同步代码中，每个线程通过 `线程本地存储（Thread-Local Storage）` 维护环境信息，例如：安全凭据（如 SecurityContext）区域性设置（如 CultureInfo）或者其他上下文数据（如 AsyncLocal<T> 的值）
当三个方法在同一个线程中依次调用时，这些信息会自动在方法间流动。但是对于异步方法来说就不再是这样了. 异步方法的每个代码段（如 await 前后的代码）可能在不同线程上执行，`线程本地存储(Thread-Local Storage`失效。
因此执行上下文派上用场, 它为 一个逻辑控制流 维护上下文信息，即使该控制流跨越多线程。
例如，Task.Run 或 ThreadPool.QueueUserWorkItem 会 自动捕获调用线程的执行上下文，并将其与任务绑定。
当任务执行时，调度器（如 TaskScheduler）通过 ExecutionContext.Run 在捕获的上下文中运行委托，确保环境信息（如安全凭据）无缝延续。

通过下面这个例子理解一下:
```
static Task ExecutionContextInAction()
{
    var li = new AsyncLocal<int>();
    li.Value = 42;
 
    return Task.Run(() =>
    {
        // Task.Run restores the execution context
        Console.WriteLine("In Task.Run: " + li.Value);
    }).ContinueWith(_ =>
    {
        // The continuation restores the execution context as well
        Console.WriteLine("In Task.ContinueWith: " + li.Value);
    });
}
```

在这个例子中, 执行上下文通过`Task.Run`流向了`Task.ContinueWith`. 因此上面这段代码的执行结果如下:
```
In Task.Run: 42
In Task.ContinueWith: 42
```

但是不是所有在BCL中的方法都会自动捕获并恢复执行上下文. 两个例外是: `TaskAwaiter<T>.UnsafeOnCompledte`和`AsyncMethodBuilder<T>.AwaitUnsafeOnComplete`. 这看起来十分奇怪, 语言设计者决定添加不安全的方法手动使用`AsyncMethodBuilder<T>`和`MoveNetRunner`而不是依赖于内建的类似`AwaitTaskContinuation`这样的机制驱动执行上下文, 猜测这是出于性能问题考虑或者是对现有实现的另一个妥协.

```
static async Task ExecutionContextInAsyncMethod()
{
    var li = new AsyncLocal<int>();
    li.Value = 42;
    await Task.Delay(42);

    // The context is implicitely captured. li.Value is 42
    Console.WriteLine("After first await: " + li.Value);

    var tsk2 = Task.Yield();
    tsk2.GetAwaiter().UnsafeOnCompleted(() =>
    {
        // The context is not captured: li.Value is 0
        Console.WriteLine("Inside UnsafeOnCompleted: " + li.Value);
    });

    await tsk2;

    // The context is captured: li.Value is 42
    Console.WriteLine("After second await: " + li.Value);
}
```

输出结果是:
```
After first await: 42
Inside UnsafeOnCompleted: 0
After second await: 42
```

# 结论

- 异步方法（async）的底层行为与同步方法截然不同，其核心机制依赖于编译器生成的 状态机（State Machine）：

- 编译器会为每个异步方法生成一个独立的状态机，将原方法的全部逻辑转移至状态机中。 状态机负责跟踪执行进度（通过状态值）、挂起（await 时）与恢复（任务完成时）的逻辑流转。
- 对同步完成的深度优化. 若所有等待的任务（await 的任务）已同步完成（如缓存命中、内存计算等无阻塞操作），异步方法的性能开销极低，几乎与同步方法无异。
此优化避免了不必要的上下文切换或调度，是异步编程高性能的关键保障。
异步场景的复杂性

- 当等待的任务未完成（需异步等待）时，状态机依赖一系列辅助类型（如 AsyncTaskMethodBuilder<T>、TaskAwaiter<T>、MoveNextRunner 等）协作完成：
  - 注册任务延续（Continuation）。
  - 维护执行上下文（ExecutionContext）。
跨线程调度时的状态安全流转。