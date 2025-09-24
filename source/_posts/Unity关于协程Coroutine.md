---
title: Unity关于协程Coroutine
tags: C#
categories: 软技能
abbrlink: 83d7c4e7
date: 2023-04-17 15:04:49
cover: https://www.notion.so/images/page-cover/met_henri_rousseau_1907.jpg
description:
swiper_index:
sticky:
---

# Unity中的协程和真正的协程并不是一回事

Unity的协程这个概念还真有可能会让只接触过Unity的C#开发者产生误解, 比如在实际开发中, 一个资源同步加载的时间太久, 就会阻塞游戏进程, 于是我们希望异步加载这个资源, 我们的做法可能是将调用`Resources.Load`接口改为`Resources.LoadAsync`接口. 然后将资源加载的方法返回类型改为`IEnumerator`, 调用`StartCoroutine`启动这个协程, 一气呵成. 如果你从未想过上述这一切Unity和C#编译器在背后为我们做了什么, 那么你肯呢个会产生下面三个误解: 一是误以为Unity中所说的协程和操作系统级别的协程是一个概念; 二是把异步和协程甚至线程概念挂钩, 认为要实现异步必须依赖协程或者线程. 三是`yield return`关键字的误解, 认为后面只能跟协程、`WaitForSeconds`.

# 拨乱反正

Unity中的协程（Coroutines）并不是严格意义上的“真协程”，而是**基于C#迭代器（IEnumerator）和Unity引擎的帧调度机制实现的一种协程模拟**. 在游戏开发的客户端领域, 我们在日常开发中几乎不会涉及到真的多线程(Unity的Dots当然还是用到多线程的了, 但Dots不在本篇博客的讨论范围内). 

首先想一下, 为什么我们需要异步呢? 笔者认为, 使用异步的原因, 是因为我们不想让某些操作阻塞整个流程. 于是笔者简单总结了一下我们在客户端开发中会使用的异步操作场景:
- 等待一段时间后尽心某种操作: 打开一个页面、播放一个动效等等
- 异步加载资源
- 异步等待网络

你会发现, 上述这些异步场景中, CPU好像都不需要做什么事情, 比如加载资源只是一个IO操作, 如果同步加载资源, 在资源加载上来之前, CPU就只能干等着, 阻塞游戏进程. 使用异步加载的方式, CPU想IO发出加载指令之后, 就可以去执行别的操作了. 网络请求也是同理. 上述异步操作都是非CPU密集型的操作. 

而还有一些是需要CPU执行很繁重的任务而产生的异步场景, 比如涉及到大量逻辑运算的, CPU执行这些逻辑要耗费大量的时间, 这便是CPU密集型的操作. 这时我们会选择创建一个新的线程, 在新的线程上执行这段逻辑, 从而不会阻塞原来的线程. 因为CPU有多个核嘛, 我们可以简单的理解为一个线程对应一个CPU的一个核心(这是十分不严谨的, 因为线程并不是一个纯物理的概念, 是一个操作系统概念, 但在这里只是为了帮助理解), 开辟新的线程能让我们在**同一时刻**利用CPU中的多个核心. 只是游戏开发的客户端中很少会有CPU密集型的操作(游戏界的笑话: 一核有难, 八核围观), 这是Unity3D前端程序员由于开发场景的原因而对异步和协程线程概念产生误解的原因. 
因此异步是异步, 多线程是多线程, 一般来说, 只有异步场景为CPU密集型操作时, 异步操作才有可能涉及到多线程, 涉及到多线程的操作我们就会使用`await`关键字而不会用`yield return`的伪协程了. 

所以妄图使用Unity协程来做一些CPU密集型的工作是根本不可能的, 因为协程本质上还是运行在同一个进程上, Unity协程只适合那些I/O密集型的操作, 因为这类操作并不真的会占用CPU的执行时间, 这段时间的CPU能够去做别的事情.

# 揭开`yield return`的面纱

有下面的代码:
```C#
using System;
using System.Collections;
public class C {
    public void M() {
        
    }
    
    public IEnumerator A(){
        Console.WriteLine("SaySomething");
        yield return new BClass(0); 
        Console.WriteLine("SaySomething");
        yield return new BClass(1);
        Console.WriteLine("SaySomething");
    }
    
    public class BClass{
        public BClass(int idx){
            
        }
    }
}
```

在笔者发现[SharpLab](https://sharplab.io/)之前, `yield return`一直很神秘, 在`SharpLab`中处理之后, `yield return`的神秘面纱被轻易揭开:

```C#
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Security;
using System.Security.Permissions;

[assembly: CompilationRelaxations(8)]
[assembly: RuntimeCompatibility(WrapNonExceptionThrows = true)]
[assembly: Debuggable(DebuggableAttribute.DebuggingModes.Default | DebuggableAttribute.DebuggingModes.IgnoreSymbolStoreSequencePoints | DebuggableAttribute.DebuggingModes.EnableEditAndContinue | DebuggableAttribute.DebuggingModes.DisableOptimizations)]
[assembly: SecurityPermission(SecurityAction.RequestMinimum, SkipVerification = true)]
[assembly: AssemblyVersion("0.0.0.0")]
[module: UnverifiableCode]
[module: RefSafetyRules(11)]

public class C
{
    public class BClass
    {
        public BClass(int idx)
        {
        }
    }


    [CompilerGenerated]
    private sealed class <A>d__1 : IEnumerator<object>, IEnumerator, IDisposable
    {
        private int <>1__state;

        private object <>2__current;

        public C <>4__this;

        object IEnumerator<object>.Current
        {
            [DebuggerHidden]
            get
            {
                return <>2__current;
            }
        }

        object IEnumerator.Current
        {
            [DebuggerHidden]
            get
            {
                return <>2__current;
            }
        }

        [DebuggerHidden]
        public <A>d__1(int <>1__state)
        {
            this.<>1__state = <>1__state;
        }

        [DebuggerHidden]
        void IDisposable.Dispose()
        {
        }

        private bool MoveNext()
        {
            switch (<>1__state)
            {
                default:
                    return false;
                case 0:
                    <>1__state = -1;
                    Console.WriteLine("SaySomething");
                    <>2__current = new BClass(0);
                    <>1__state = 1;
                    return true;
                case 1:
                    <>1__state = -1;
                    Console.WriteLine("SaySomething");
                    <>2__current = new BClass(1);
                    <>1__state = 2;
                    return true;
                case 2:
                    <>1__state = -1;
                    Console.WriteLine("SaySomething");
                    return false;
            }
        }

        bool IEnumerator.MoveNext()
        {
            //ILSpy generated this explicit interface implementation from .override directive in MoveNext
            return this.MoveNext();
        }

        [DebuggerHidden]
        void IEnumerator.Reset()
        {
            throw new NotSupportedException();
        }
    }

    public void M()
    {
    }

    [NullableContext(1)]
    [IteratorStateMachine(typeof(<A>d__1))]
    public IEnumerator A()
    {
        <A>d__1 <A>d__ = new <A>d__1(0);
        <A>d__.<>4__this = this;
        return <A>d__;
    }
}

```

我们来总结一下`yield return`关键字的作用: 包含有`yield return`的方法经过编辑器处理之后变成了一个实现了`IEnumerator`接口的类, 我们知道`IEnumerator`是一个迭代器, 迭代器的一个作用就是迭代或者叫遍历元素, `yield return`后跟着的, 便是这个迭代器中的一个元素. `yield return`只是在状态机里面一个新增一个状态, 然后在这个状态的分支中执行从上一个`yield return`(不包含), 到这一个`yield return`(包含)之间的代码, 并且迭代器中会多一个元素. **另外, 不只是`WaitForSeconds`和`IEnumerator`可以被`yield return`, 任何数据结构都可以**. 我们编写返回类型是`IEnumerator`的方法时, 其实是在借助`yield return`构造一个包含某些元素的迭代器的过程. 
只是, 不是随便一个类型被`yield return`就可以实现异步了, 只有Unity**精心设计过的类型**, 搭配上Unity`MonoBehaviour`中的驱动逻辑, 才能实现真正的异步操作. 但是像yield return new List<int>这种操作会导致该协程挂起一帧, 就像yield return null一样

要使用`IEnumerator`实现异步, 需要MonoBehaviour和继承自UnityEngine命名空间下`YieldInstruction`类型的配合. 你调用的所有的Unity提供的异步操作接口, 其返回值的类型都包含在下图中:

![](IEnumerator与IEnumerable理解与辨析/image.png)


# 现在你知道为什么说Unity协程是“伪协程”了吗

- 被动调度：协程的恢复完全由Unity引擎控制，而非主动让出给其他协程。
- 单线程限制：无法利用多核CPU实现并行计算，仅适用于异步等待或分帧处理。
- 基于迭代器：底层实现是状态机（IEnumerator），而非真正的协程原语。

# Unity协程的局限性

- 依赖主线程：所有协程代码在主线程执行，长时间运行的操作会阻塞渲染和逻辑更新。
- 无独立栈：协程的“暂停”状态由IEnumerator保存局部变量，而非独立的执行栈。
- 无法跨线程：无法在后台线程中启动或恢复协程。

# 总结

Unity中使用迭代器来实现异步, 只是迭代器诸多使用场景的一种, `IEnumerator`和`IEnumerable`还有更广阔的天地, 在[这篇博客](https://soincredible.github.io/posts/133a9667/)中会详细探讨.

> 2025.09.13更新
# 再谈协程

前文的内容, 笔者再次回味时,经过两年半的沉淀, 笔者认为自己终于真的摸清了其中的门道. 并没有帮助笔者理解Unity协程的本质, 协程本质是一个迭代器是很空洞的一句话, 于是下文笔者将尝试换一种视角来探究协程的本质. 注意, 理解了yield return并不能帮助你理解Unity的协程异步机制, 这里面其实有两层迷雾, 第一层, 是yield return背后代表的代码逻辑怎样的, 你可以认为, 上述笔者观点中的**行为**定义, C#也给你封装了起来, 你看不到, 第二层迷雾, 是Unity把如何调用行为的逻辑也给你隐藏了起来, 因此, 你理解了yield return背后代表的是什么代码, 只是揭开了第一层迷雾, 到这里你对于Unity协程的使用程度, 才跟你对UGUI的使用等下, 要想继续了解内部如何控制流程, 那么你就需要像阅读UGUUI的EventSystem源码一样, 去阅读UnityMonoBehaviourC++代码中, 是如何调用yield return所生成的那个IEnumerator的MoveNext的. 第三层迷雾WaitForSeconds、WaitForEndOfFrame是Unity封装好的行为

## 从foreach说起

首先由于foreach和yield return语法糖的干扰. 先借助SharpLab揭开他们的面纱.
想要理解foreach本质, 笔者总结, 总共有四重迷雾, 比如, 在Unity中, 有如下代码:
```C#
private void Update(){
    foreach(var player in Players){
        player.Update();
    }
}
```

第一重迷雾, foreach本质是什么
```C#
while(Players.MoveNext()){
    var player = Players.Current;
    player.Update();
}
```

第二重迷雾, foreach是如何驱动迭代的, 这一重迷雾还是很显而易见的, 通过while关键字
```C#
while(Players.MoveNext()){ // 我们可以认为while就是迭代器的驱动者
    var player = Players.Current;
    player.Update();
}
```

第三重迷雾, MoveNext里面做了什么, 对于一个List来说, 迭代器相关的代码如下:
```C#
public bool MoveNext() {

    List<T> localList = list;

    if (version == localList._version && ((uint)index < (uint)localList._size)) 
    {                                                     
        current = localList._items[index];                    
        index++;
        return true;
    }
    return MoveNextRare();
}

private bool MoveNextRare()
{                
    if (version != list._version) {
        ThrowHelper.ThrowInvalidOperationException(ExceptionResource.InvalidOperation_EnumFailedVersion);
    }

    index = list._size + 1;
    current = default(T);
    return false;                
}

public T Current {
    get {
        return current;
    }
}

Object System.Collections.IEnumerator.Current {
    get {
        if( index == 0 || index == list._size + 1) {
                ThrowHelper.ThrowInvalidOperationException(ExceptionResource.InvalidOperation_EnumOpCantHappen);
        }
        return Current;
    }
}
```

第四重迷雾, Player.Update()里面做了什么. 
```C#
void Update(){
    UpdateMovement();
    UpdateAnimations();
    // ...
}
```

## Unity协程中的四层迷雾

比如, 有如下代码
```C#
public IEnumerator M() {
    yield return new WaitForSeconds(2f);
    yield return new WXGameCaptureProcess(null, null);
}
```

第一重迷雾, `yield return`的本质是什么
```C#
[CompilerGenerated]
private sealed class <M>d__0 : IEnumerator<object>, IEnumerator, IDisposable
{
    private int <>1__state;

    private object <>2__current;

    public C <>4__this;

    object IEnumerator<object>.Current
    {
        [DebuggerHidden]
        get
        {
            return <>2__current;
        }
    }

    object IEnumerator.Current
    {
        [DebuggerHidden]
        get
        {
            return <>2__current;
        }
    }

    [DebuggerHidden]
    public <M>d__0(int <>1__state)
    {
        this.<>1__state = <>1__state;
    }

    [DebuggerHidden]
    void IDisposable.Dispose()
    {
    }

    private bool MoveNext()
    {
        int num = <>1__state;
        if (num != 0)
        {
            if (num != 1)
            {
                return false;
            }
            <>1__state = -1;
            return false;
        }
        <>1__state = -1;
        <>2__current = new WXGameCaptureProcess(null, null);
        <>1__state = 1;
        return true;
    }

    bool IEnumerator.MoveNext()
    {
        //ILSpy generated this explicit interface implementation from .override directive in MoveNext
        return this.MoveNext();
    }

    [DebuggerHidden]
    void IEnumerator.Reset()
    {
        throw new NotSupportedException();
    }
}

[NullableContext(1)]
[IteratorStateMachine(typeof(<M>d__0))]
public IEnumerator M()
{
    <M>d__0 <M>d__ = new <M>d__0(0);
    <M>d__.<>4__this = this;
    return <M>d__;
}
```

第二重迷雾, Unity是怎么驱动这个迭代器的.

看下
```C++
ScriptingObjectPtr MonoBehaviour::StartCoroutineManaged(const char* name, ScriptingObjectPtr value)
{
    Coroutine* coroutine = StartCoroutine(name, value);
    return CreateManagedWrapperForCoroutine(coroutine);
}

Coroutine* MonoBehaviour::StartCoroutine(const char* name, ScriptingObjectPtr value)
{
    Assert(ShouldRunBehaviour());
    Assert(GetInstance() != SCRIPTING_NULL);

    if (!IsActive())
    {
        ErrorStringObject(Format("Coroutine '%s' couldn't be started because the the game object '%s' is inactive!", name, GetName()), this);
        return NULL;
    }

    ScriptingMethodPtr method = FindMethod(name);
    if (method.IsNull())
    {
        ErrorStringObject(Format("Coroutine '%s' couldn't be started!", name), this);
        return NULL;
    }

    return InvokeMethodOrCoroutineChecked(method, value);
}


static ScriptingObjectPtr CreateManagedWrapperForCoroutine(Coroutine* coroutine)
{
    if (coroutine == NULL)
        return SCRIPTING_NULL;
    Assert(!coroutine->m_IsReferencedByMono);
    coroutine->m_IsReferencedByMono = true;
    ScriptingObjectWithIntPtrField<Coroutine> wrapper = scripting_object_new(GetCoreScriptingClasses().coroutine);
    wrapper.SetPtr(coroutine, Coroutine::CleanupCoroutineGC);
    return wrapper.GetScriptingObject();
}

```

1. 依赖unity的PlayerLoop.
2. 在PlayerLoop中
3. 当有一个新的协程的时候, 向这个Mono中注册协程, 并通过回调的方式MoveNext.

第三重迷雾, MoveNext里面做了什么

```C#
private bool MoveNext()
{
    int num = <>1__state;
    if (num != 0)
    {
        if (num != 1)
        {
            return false;
        }
        <>1__state = -1;
        return false;
    }
    <>1__state = -1;
    <>2__current = new WXGameCaptureProcess(null, null);
    <>1__state = 1;
    return true;
}
```
UnityMonoBehaviour中干的事情就是这个, 它也在控制流程, 你可以理解为它在遍历这个迭代器所处理的集合, UnityMonoBehaviour的角色和foreach的角色等价. 只是foreach中的MoveNext的调用时机很简单, foreach是, 遍历一个元素, 处理一下, 紧接着遍历下一个, 而在MonoBehaviour中, 因为Unity初衷就是要用它来做一些延时操作. 因此MoveNext的调用是有说法的. 不是像foreach一样, 遍历一个紧接着调用MoveNext, 而是用DelayedManager做一下延时调用. 另外还要注意, 有一点挺反直觉的, 你想要让协程继续下去, MoveNext要是true. 而不是false. 应该是要在MoveNext里面修改Current的值的,

比如说C# List的源码, 

但比如说下面这段代码, 
只要访问一次Current, 就会返回一个新的WaitForEndOfFrame对象回去. 其实这个类中没有一个真正集合的概念.

```C#
public class WXGameCaptureProcess : IEnumerator
{
    private bool shareOver;

    public object Current
    {
        get
        {
            return new WaitForEndOfFrame();
        }
    }

    public WXGameCaptureProcess(Rect captureRect, Action<string> callback)
    {
#if DDZ_WXMINIGAME
        WXCanvas.ToTempFilePath(new WXToTempFilePathParam()
        {
            x = (int)captureRect.x,
            y = (int)(Screen.height - captureRect.y - captureRect.height),
            width = (int)captureRect.width,
            height = (int)captureRect.height,
            destWidth = (int)captureRect.width,
            destHeight = (int)captureRect.height,
            success = (ret) =>
            {
                Debug.Log("Screenshot saved: " + ret.tempFilePath);
                callback?.Invoke(ret.tempFilePath);
            },
            fail = (msg) =>
            {
                callback?.Invoke(null);
            },
            complete = (msg) =>
            {
                shareOver = true;
            }
        });
#else
        callback?.Invoke("");
#endif
    }

    public bool MoveNext()
    {
        return !shareOver;
    }

    public void Reset()
    {
        shareOver = false;
    }
}
```

使用协程的方式, 不像List, 有IEnumerable接口, 同时这种方式也没有像List一样内部有一个数组存储了某个集合, 但是协程内部其实还是有集合的概念的, 只不过集合的元素全部硬编码在了MoveNext里面, 不得不说, 协程真的是我最难以揣测设计者设计动机的一个东西了. 不管怎么说, 下面这段代码, 就是我们编写的所有的代码, 接下来把它丢给StartCoroutinue接口, 协程就可以跑起来了, 那我们来看看这段代码中我们干了什么呢? 1. 我们没有控制该协程的执行流程, 我们是将该协程通过StartCoroutine接口传递给了Unity底层 2. 我们定义了迭代器中的集合元素, 这点和List的例子还不太一样, List是通过调用Add接口向其中加入元素的. 使用协程的时候, 协程定义的迭代器中的集合的元素本身就代表了一种行为, 而不需要像List中的Player一样, 每遍历到一个Player就要执行一个Update操作. 因此协程的集合更像是一组标签集合, 驱动该协程的模块遍历该协程中迭代其中的元素, 根据这些元素身上的标签, 执行不同的操作. 那么Unity底层支持哪些标签呢?

- WaitForSeconds
- IEnumerator

所以说, Unity底层是通过判断迭代器中当前元素的对象类型, 作为标签的. 那么有那么多的类型, Unity不可能全都识别到. 所以说, 你可以用yield return返回任意的类型, 比如你可以yield return new List<int>() 但是Unity协程机制所
能识别出来的类型就那几个. 

当Unity遇到yield return new List的时候会做什么?

# 而当我们尝试梳理C#中编写的复杂yield return的时候, 我们是在梳理什么?

yield return 中嵌套yield rteurn 的含义是什么?

意思其实是, 迭代器迭代的集合里面有一个集合元素又是一个迭代器.

这样总结下来, 一共就有三种常见迭代器迭代集合的方式了, 第一种是像List那样的正统
第二种是Unity通过yield return 创建出来的
第三种是创建一个类, 继承IEnumerator. 比如WXGameCaptureProcess这个, “模拟”了一个集合



# 几种可以实现图片透明度渐变效果的方法

方法一：使用协程

```C#
using System.Collections;
using System.Collections.Generic;
using System.Net.Mime;
using Unity.Collections;
using UnityEngine;
using UnityEngine.UI;

public class TestCoroutine : MonoBehaviour
{
    private int a = 0;
    public Image testImg;
    private float colorAlpha = 1f;
    private bool flag = false;

    void Start()
    {
        StartCoroutine(Test());
    }
    IEnumerator Test()
    {
        while (colorAlpha >= 0.0f)
        {
            colorAlpha -= 0.02f;
            testImg.GetComponent<Image>().color = new Color(255, 255, 255, colorAlpha);
            yield return new WaitForSeconds(0.05f);
        }
    }
}
```

方法二：使用`Time.deltatime`

```C#
using System.Collections;
using System.Collections.Generic;
using System.Net.Mime;
using Unity.Collections;
using UnityEngine;
using UnityEngine.UI;

public class TestCoroutine : MonoBehaviour
{
    private int a = 0;
    public Image testImg;
    private float colorAlpha = 1f;
  
    void Update()
    {
        if (colorAlpha >= 0.0f)
        {
            colorAlpha -= 0.1f * Time.deltaTime;
            testImg.GetComponent<Image>().color = new Color(255, 255, 255, colorAlpha);
        }
    }
}
```

通过这个例子我有点理解Time.deltatime的含义了，在本例中，我们在`colorAlpha -= 0.1f * Time.deltaTime;`处使用了Time.deltatime，它的作用是使得`colorAlpha`变量每一秒减0.1f，如果使用打断点的方式去分步调试，每次Update的时候都会去执行这一行代码，然后可以看到每次Update后`colorAlpha`会每次减小一点点，也就是说Time.deltaTime是一个很小的数，它能够确保在1s内将colorAlpha这个变量减少0.1，而且这个过程是连续的。

使用协程的那个方式呢，它能够实现在一段时间间隔内渐变的效果是因为有`yield return new WaitForSeconds()`方法和while循环的共同配合，而且如果你WaitForSeconds方法中的数太大的话会导致变化的过程非常的生硬，不够连贯，也就是说和Time.deltatime相比，使用协程的方式实现的渐变显得像是非连续式的渐变，而且写法也不如Time.deltatime方便，所以我个人认为使用Time.deltatime的方法实现渐变效果会更好一点。

我在网上看到了这样的写法：

```C#
using System.Collections;
using System.Collections.Generic;
using System.Net.Mime;
using Unity.Collections;
using UnityEngine;
using UnityEngine.UI;

public class TestCoroutine : MonoBehaviour
{
 
    private int a = 0;
    public Image testImg;
    private float colorAlpha = 1f;
    private bool flag = false;

    void Start()
    {
        StartCoroutine(Test());
    }
    IEnumerator Test()
    {
        while (colorAlpha >= 0.0f)
        {
            colorAlpha -= 0.1f * Time.deltaTime;
            testImg.GetComponent<Image>().color = new Color(255, 255, 255, colorAlpha);
            yield return null;
        }
    }
}
```

我个人感觉这和直接写在Update中没有任何区别啊，这样写难道有什么别的好处么？🥲不过这样确实也达到了连续渐变的效果。
> 2025.5.10更新
> 这段实现方式以笔者现在的水平来看确实是要优于前两者的. 首先WaitForSecond会造成额外开销, 而协程会在每一帧Update之后、LateUpdate之前执行, 使用`Time.deltaTime`是合理的. 其次, 在`Update()`方法里面做颜色渐变很蠢, Update只要这个Mono存在就会每帧执行, 做一个渐变效果要每一次Update里都要判断简便要不要执行是很cumbersome的.

