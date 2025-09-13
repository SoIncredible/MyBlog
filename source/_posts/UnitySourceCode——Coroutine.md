---
title: UnitySourceCode——Coroutine
abbrlink: da596cf9
date: 2025-09-11 19:52:27
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


翻一下这段代码
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

在Unity的MonoBehaviour中, 会获得IEnumerator的Current的类型, 然后根据Current的类型判断要执行什么处理

你问得很有针对性，来看你的问题：

---
**问题简述：**  
你定义了一个 `WXGameCaptureProcess` 继承自 `IEnumerator`。  
在 Unity 协程中，  
```csharp
yield return new WXGameCaptureProcess(...);
```
和
```csharp
yield return new WaitForEndOfFrame();
```
有什么关系？二者等价吗？它们行为有何区别？

---

## 1. yield return new WaitForEndOfFrame()

- **行为：** 等待 **本帧渲染** 结束后，协程继续进行。
- **效果**：只“暂停”一帧，马上进入下一步代码。

---

## 2. yield return new WXGameCaptureProcess(...)

这是个自定义的 IEnumerator，分析其实现：

### 关键实现

- `MoveNext`：只要 `!shareOver` 为真，就会在协程里不停“等待”。
- `Current`：每次等待，返回的对象是 `new WaitForEndOfFrame()`
- 只有当你传入的 `callback` 的 success、fail、complete 被触发（间接导致 `shareOver = true`），`MoveNext` 返回 false，协程才开始执行下一条语句。

### 表现

这个自定义迭代器，在业务事件没完成之前（比如截图还没保存），每一帧返回 `WaitForEndOfFrame()`，**让协程等待“若干帧”**，直到业务事件完成。完成后协程才往下走。

---

### 直观说明

- `yield return new WaitForEndOfFrame();`
    - 等一帧。

- `yield return new WXGameCaptureProcess(...);`
    - **其实是“每帧都 yield return new WaitForEndOfFrame()”，直到捕获（截图操作）状态 shareOver==true，才继续协程下去。等待的是一个“异步事件”的完成！**

---

## 3. 是否等价？

**结论：**

- `yield return new WXGameCaptureProcess(...)` **绝不等价于** 只 `yield return new WaitForEndOfFrame();`
    - 前者是：**等待“多帧”——每帧结尾都 yield — 直到外部异步事件结束**
    - 后者是：**只暂停一帧**
- **WXGameCaptureProcess** 可以等待一个异步回调，比如截图、网络、动画等，适合等待“非立即完成”的业务。

---

## 4. 形式总结

实际上，你的
```csharp
yield return new WXGameCaptureProcess(...);
```

等价于
```csharp
// 伪代码示意，效果类似于：
while (!shareOver) {
    yield return new WaitForEndOfFrame();
}
// shareOver==true 时继续协程
```

而不是
```csharp
yield return new WaitForEndOfFrame();
```

---

### 【专业英文结论】

> `yield return new WXGameCaptureProcess(...)` is **not equivalent** to `yield return new WaitForEndOfFrame();`. The first form will keep yielding `WaitForEndOfFrame` each frame until the async capture process is finished (`shareOver == true`), while the latter only waits for one frame.

---

如果你想等待一个异步操作的完成而不是只暂停一帧，这种自定义 `IEnumerator` 写法是业界常用套路。这种写法扩展性也很强。

---

Coroutine中会持有m_Current字段, 然后通过如下的接口判断m_Current的类型
代码如下:

```c++
#include "UnityPrefix.h"

#include "Coroutine.h"

#include "Runtime/Misc/AsyncOperation.h"
#include "Runtime/Mono/MonoBehaviour.h"
#include "Runtime/Mono/MonoScript.h"
#include "Runtime/Mono/MonoIncludes.h"
#include "Runtime/Mono/MonoManager.h"
#include "Runtime/GameCode/CallDelayed.h"
#include "Runtime/Scripting/ScriptingUtility.h"
#include "Runtime/ScriptingBackend/ScriptingApi.h"
#include "Runtime/Scripting/ScriptingObjectWithIntPtrField.h"

#if UNITY_IPHONE_API
# include "Runtime/Input/OnScreenKeyboard.h"
#endif

#include "Runtime/ScriptingBackend/ScriptingApi.h"

// copied from MonoBehaviour.cpp
// if they should be synced - blame the author not me ;-)
#define DEBUG_COROUTINE 0
#define DEBUG_COROUTINE_LEAK 0

Coroutine::Coroutine()
    : m_DoneRunning(false)
    , m_CoroutineEnumeratorGCHandle()
    , m_IsIEnumeratorCoroutine(false)
{
    #if DEBUG_COROUTINE
    static int coroutineCounter = 0;
    coroutineCounter++;
    printf_console("Allocating coroutine %d --- %d : 0x%x\n", this, coroutineCounter, &m_RefCount);
    #endif
}

Coroutine::~Coroutine()
{
    Assert(!m_CoroutineEnumeratorGCHandle.HasTarget());

    #if DEBUG_COROUTINE
    printf_console("Deconstructor coroutine %d\n", this);
    #endif
}

void Coroutine::SetMoveNextMethod(ScriptingMethodPtr method)
{
    m_MoveNext = method;
}

void Coroutine::SetCurrentMethod(ScriptingMethodPtr method)
{
    m_Current = method;
}

CallObjectState Coroutine::ContinueCoroutine(Object* o, void* userData)
{
    Coroutine* coroutine = (Coroutine*)userData;
    Assert(coroutine->m_RefCount > 0 && coroutine->m_RefCount < 1000000);

    if ((Object*)coroutine->m_Behaviour != o)
    {
        ErrorString("Coroutine continue failure");
        #if DEBUG_COROUTINE
        if ((Object*)coroutine->m_Behaviour != o)
        {
            printf_console("continue Coroutine corruption %d refcount: %d behaviour: %d \n", coroutine, coroutine->m_RefCount, coroutine->m_Behaviour);
            printf_console("continue Coroutine corruption name: %s methodname\n", ((MonoBehaviour*)(o))->GetScript()->GetName());
            if (!coroutine->m_CoroutineMethod.IsNull())
                printf_console("continue Coroutine methodname: %s\n", scripting_method_get_name(coroutine->m_CoroutineMethod));
        }
        #endif
        return kCallObjectAlive;
    }

    return coroutine->Run();
}

CallObjectState Coroutine::CleanupCoroutine(void* userData)
{
    Coroutine* coroutine = (Coroutine*)userData;
    Assert(coroutine->m_RefCount > 0);
    Assert(coroutine->m_RefCount <= 1000000);
    coroutine->m_RefCount--;

    #if DEBUG_COROUTINE
    printf_console("decrease refcount %d - active: %d \n", coroutine, coroutine->m_RefCount);
    #endif

    if (coroutine->m_RefCount > 0)
        return kCallObjectAlive;

    coroutine->m_DoneRunning = true;

    #if DEBUG_COROUTINE
    printf_console("CleanupCoroutine %d\n", coroutine);
    if (coroutine->m_Behaviour && GetDelayedCallManager().HasDelayedCall(coroutine->m_Behaviour, Coroutine::ContinueCoroutine, CompareCoroutineMethodName, coroutine))
    {
        printf_console("CORRUPTION is still in delayed call manager%d!\n", coroutine->m_Behaviour);
    }
    #endif

    if (coroutine->m_ContinueWhenFinished)
    {
        CleanupCoroutine(coroutine->m_ContinueWhenFinished);
        coroutine->m_ContinueWhenFinished = NULL;
    }

    if (coroutine->m_WaitingFor)
    {
        coroutine->m_WaitingFor->m_ContinueWhenFinished = NULL;
        //Set the WaitingFor variable to NULL. Otherwise it might happen that a coroutine is cleaned up because another coroutine has it set as its m_ContinueWhenFinished.
        //If then this coroutine is cleaned up using MonoBehaviour::StopCoroutine, it will be cleaned up again and trigger asserts
        coroutine->m_WaitingFor = NULL;
    }

    coroutine->RemoveFromList();

    if (coroutine->m_AsyncOperation)
    {
        coroutine->m_AsyncOperation->SetCoroutineCallback(NULL, NULL, NULL, NULL);
        coroutine->m_AsyncOperation->Release();
        coroutine->m_AsyncOperation = NULL;
    }

    Assert(coroutine->m_CoroutineEnumeratorGCHandle.HasTarget());
    coroutine->m_CoroutineEnumeratorGCHandle.ReleaseAndClear();

    if (!coroutine->m_IsReferencedByMono)
    {
        delete coroutine;

        #if DEBUG_COROUTINE_LEAK
        gCoroutineCounter--;
        #endif
        return kCallObjectDestroyed;
    }

    return kCallObjectAlive;
}

void Coroutine::CleanupCoroutineGC(void* userData)
{
    Coroutine* coroutine = (Coroutine*)userData;
    if (!coroutine->m_IsReferencedByMono)
        return;

    if (coroutine->m_RefCount != 0)
    {
        coroutine->m_IsReferencedByMono = false;
        return;
    }

    ErrorIf(coroutine->IsInList());

    delete coroutine;

    #if DEBUG_COROUTINE
    printf_console("GC free coroutine: %d\n", coroutine);
    #endif

    #if DEBUG_COROUTINE_LEAK
    gCoroutineCounter--;
    #endif
}

bool Coroutine::CompareCoroutineMethodName(void* callBackUserData, void* cancelUserdata)
{
    Coroutine* coroutine = (Coroutine*)callBackUserData;
    if (coroutine->m_CoroutineMethod.IsNull())
        return false;

    return strcmp(scripting_method_get_name(coroutine->m_CoroutineMethod), (const char*)cancelUserdata) == 0;
}

static bool ScriptingObjectPtrsEqual(ScriptingObjectPtr o1, ScriptingObjectPtr o2)
{
#if ENABLE_DOTNET
    return WinRT::Bridge::GetUtils()->CompareObjects(o1, o2);
#else
    return o1 == o2;
#endif
}

bool Coroutine::CompareCoroutineEnumerator(void* callBackUserData, void* cancelUserdata)
{
    Coroutine* coroutine = (Coroutine*)callBackUserData;
    if (!coroutine->m_CoroutineEnumeratorGCHandle.HasTarget())
        return false;

    ScriptingObjectPtr* handle = (ScriptingObjectPtr*)cancelUserdata;
    if (!handle)
        return false;

    if (ScriptingObjectPtrsEqual(coroutine->m_CoroutineEnumeratorGCHandle.Resolve(), *handle))
        return true;

    if (!coroutine->m_IsIEnumeratorCoroutine)
        return false;

    if (coroutine->m_ContinueWhenFinished == NULL)
        return false;

    return ScriptingObjectPtrsEqual(coroutine->m_ContinueWhenFinished->m_CoroutineEnumeratorGCHandle.Resolve(), *handle);
}

bool Coroutine::CompareCoroutineDirect(void* callBackUserData, void* cancelUserdata)
{
    Coroutine* callBackCoroutine = (Coroutine*)callBackUserData;
    Coroutine* cancelCoroutine = (Coroutine*)cancelUserdata;

    if (callBackCoroutine == cancelCoroutine)
        return true;

    if (!callBackCoroutine->m_IsIEnumeratorCoroutine)
        return false;

    if (callBackCoroutine->m_ContinueWhenFinished == NULL)
        return false;

    return callBackCoroutine->m_ContinueWhenFinished == cancelCoroutine;
}

bool Coroutine::InvokeMoveNext(ScriptingExceptionPtr* exception)
{
    bool result = false;
    ScriptingInvocation invocation(GetCoreScriptingClasses().invokeMoveNext);
    invocation.AddObject(m_CoroutineEnumeratorGCHandle.Resolve());
    invocation.AddIntPtr(&result);
    invocation.classContextForProfiler = m_Behaviour->GetClass();
    invocation.methodContextForProfiler = m_MoveNext;
    invocation.objectInstanceIDContextForException = m_Behaviour->GetInstanceID();
    invocation.Invoke(exception);
    return result && *exception == SCRIPTING_NULL;
}

CallObjectState Coroutine::Run(bool *exceptionThrown)
{
    Assert(m_RefCount != 0);
    Assert(m_Behaviour != NULL);

    #if DEBUG_COROUTINE
    Assert(!GetDelayedCallManager().HasDelayedCall(m_Behaviour, Coroutine::ContinueCoroutine, CompareCoroutineMethodName, this));
    if (m_Behaviour == NULL)
    {
        printf_console("Coroutine error %d refcount: %d behaviour%d\n", this, m_RefCount, m_Behaviour);
    }
    #endif

    // - Call MoveNext (This processes the function until the next yield!)
    // - Call Current (This returns condition when to continue the coroutine next.)
    //   -> Queue it based on the continue condition

    // Temporarily increase refcount so the object will not get destroyed during the m_MoveNext call
    m_RefCount++;
    ScriptingExceptionPtr exception = SCRIPTING_NULL;
    bool keepLooping = InvokeMoveNext(&exception);
    Assert(m_RefCount > 0 && m_RefCount <= 10000000);

    bool coroutineWasDestroyedDuringMoveNext = m_RefCount == 1;
    Coroutine* continueWhenFinished = m_ContinueWhenFinished;
    // Decrease temporary refcount so the object will not get destroyed during the m_MoveNext call
    CleanupCoroutine(this);

    // The coroutine has been destroyed in the mean time, probably due to a call to StopAllCoroutines, stop executing further
    if (coroutineWasDestroyedDuringMoveNext)
    {
        Assert(continueWhenFinished == NULL);
        return kCallObjectDestroyed;
    }

    if (exceptionThrown)
        *exceptionThrown = exception != SCRIPTING_NULL;

    if (exception != SCRIPTING_NULL)
        return kCallObjectAlive;

    // Are we done with this coroutine?
    if (!keepLooping)
    {
        // If there is a coroutine waiting for this one to finish Run it!
        if (m_ContinueWhenFinished)
        {
            Assert(this == m_ContinueWhenFinished->m_WaitingFor);
            Coroutine* continueWhenFinished = m_ContinueWhenFinished;
            m_ContinueWhenFinished->m_WaitingFor = NULL;
            m_ContinueWhenFinished = NULL;
            // The coroutine might have been stopped inside of the last coroutine invokation
            if (continueWhenFinished->m_Behaviour)
            {
                // If the continuation coroutine yields waiting for this coroutine, it must know that this coroutine is finished
                m_DoneRunning = true;
                continueWhenFinished->Run();
            }
            CleanupCoroutine(continueWhenFinished);
        }

        return kCallObjectAlive;
    }

    if (m_Behaviour == NULL)
        return kCallObjectAlive;

    ProcessCoroutineCurrent();

    return kCallObjectAlive;
}

void Coroutine::ProcessCoroutineCurrent()
{
    ScriptingExceptionPtr exception = SCRIPTING_NULL;

    ScriptingInvocation invocation(m_Current);
    invocation.objectInstanceIDContextForException = m_Behaviour->GetInstanceID();
    invocation.classContextForProfiler = m_Behaviour->GetClass();

#if !ENABLE_DOTNET
    ScriptingClassPtr methodKlass = scripting_method_get_class(m_Current);
    if (scripting_class_is_valuetype(methodKlass))
        invocation.SetTarget(scripting_object_unbox(m_CoroutineEnumeratorGCHandle.Resolve()));
    else
#endif
    {
        invocation.SetTarget(m_CoroutineEnumeratorGCHandle.Resolve());
    }

    ScriptingObjectPtr monoWait = invocation.Invoke(&exception);

    Assert(m_RefCount > 0 && m_RefCount <= 10000000);

    if (exception != SCRIPTING_NULL)
        return;

    if (monoWait == SCRIPTING_NULL)
    {
        m_RefCount++;
        CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
        return;
    }

    HandleIEnumerableCurrentReturnValue(monoWait);
}

void Coroutine::HandleIEnumerableCurrentReturnValue(ScriptingObjectPtr monoWait)
{
    AsyncOperation* async = NULL;
    ScriptingClassPtr waitClass = scripting_object_get_class(monoWait);
    const CommonScriptingClasses& classes = GetMonoManager().GetCommonClasses();

    // Continue the coroutine in 'wait' seconds
    if (scripting_class_is_subclass_of(waitClass, GetCoreScriptingClasses().waitForSeconds))
    {
        m_RefCount++;

        ScriptingWaitForSeconds wait;
        MarshallManagedStructIntoNative(monoWait, &wait);
        if (IsNAN(wait.m_Seconds))
            ErrorString("float.NaN has been passed into WaitForSeconds which will result in an infinite wait time.");
        CallDelayed(ContinueCoroutine, m_Behaviour, wait.m_Seconds, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
        return;
    }

    // Continue the coroutine on the next fixed update
    if (scripting_class_is_subclass_of(waitClass, GetCoreScriptingClasses().waitForFixedUpdate))
    {
        m_RefCount++;
        CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunFixedFrameRate);
        return;
    }

    // Continue the coroutine at the end of frame
    if (scripting_class_is_subclass_of(waitClass, GetCoreScriptingClasses().waitForEndOfFrame))
    {
        m_RefCount++;
        CallDelayed(ContinueCoroutine, m_Behaviour, -1.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kEndOfFrame);
        return;
    }

    if (scripting_class_is_subclass_of(waitClass, classes.iEnumerator))
    {
#if UNITY_EDITOR
        if (m_CoroutineEnumeratorGCHandle.Resolve() == monoWait)
        {
            const char* className = scripting_class_get_name(waitClass);
            WarningStringMsg("IEnumerator class %s is returning this (itself) in %s.Current, which can lead to infinite recursion.", className, className);
        }
#endif
        m_RefCount++;

        Coroutine* waitForCoroutine;
        if (!m_Behaviour->TryCreateAndRunCoroutine(monoWait, NULL, &waitForCoroutine))
            return;

        // coroutine has already finished (empty enumerator block); just reschedulle current one..
        if (waitForCoroutine == NULL)
        {
            CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
            return;
        }

        AssertMsg(!waitForCoroutine->m_DoneRunning, "Coroutine initialized incorrectly");
        AssertMsg(waitForCoroutine->m_ContinueWhenFinished == NULL, "Coroutine initialized incorrectly");

        waitForCoroutine->m_IsIEnumeratorCoroutine = true;
        waitForCoroutine->m_ContinueWhenFinished = this;
        m_IsIEnumeratorCoroutine = true;
        m_WaitingFor = waitForCoroutine;

        return;
    }

    // Continue after another coroutine is finished
    if (scripting_class_is_subclass_of(waitClass, GetCoreScriptingClasses().coroutine))
    {
        Coroutine* waitForCoroutine;
        MarshallManagedStructIntoNative(monoWait, &waitForCoroutine);
        if (waitForCoroutine->m_DoneRunning)
        {
            // continue executing.
            ContinueCoroutine(m_Behaviour, this);
            return;
        }

        if (waitForCoroutine->m_ContinueWhenFinished != NULL)
        {
            LogStringObject("Another coroutine is already waiting for this coroutine!\nCurrently only one coroutine can wait for another coroutine!", m_Behaviour);
            return;
        }

        m_RefCount++;
        waitForCoroutine->m_ContinueWhenFinished = this;
        m_WaitingFor = waitForCoroutine;
        return;
    }

    if ((scripting_class_is_subclass_of(waitClass, GetCoreScriptingClasses().asyncOperation)) && (async = ScriptingObjectWithIntPtrField<AsyncOperation>(monoWait).GetPtr()) != NULL)
    {
        m_RefCount++;

        if (async->IsDone())
        {
            CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
            return;
        }

        // Use AysncOperation ContinueCoroutine - default path
        if (async->HasCoroutineCallback())
        {
            ////@TODO: Throw exception?
            ErrorString("This asynchronous operation is already being yielded from another coroutine. An asynchronous operation can only be yielded once.");
            CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
            return;
        }

        async->SetCoroutineCallback(ContinueCoroutine, m_Behaviour, this, CleanupCoroutine);

        if (m_AsyncOperation != NULL)
        {
            m_AsyncOperation->Release();
        }

        m_AsyncOperation = async;
        m_AsyncOperation->AddRef();

        return;
    }

    // Continue the coroutine on the next dynamic frame update
    m_RefCount++;
    CallDelayed(ContinueCoroutine, m_Behaviour, 0.0F, this, 0.0F, CleanupCoroutine, DelayedCallManager::kRunDynamicFrameRate | DelayedCallManager::kWaitForNextFrame);
    //Ext_MarshalMap_Release_ScriptingObject(monoWait);//RH TODO : RELEASE THE MONOWAIT OBJECTS SOMEWHERE
}

```

C++到C#的理解有些难度, 我们可以实现一个纯C#的协程:
