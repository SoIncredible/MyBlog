---
title: ET框架Demo启动流程梳理
categories: ET
cover: 'https://www.notion.so/images/page-cover/met_horace_pippin.jpg'
abbrlink: 9c5dbe31
date: 2025-3-20 17:50:31
tags: ET
description:
swiper_index:
sticky:
---

# ET客户端启动流程梳理

启动时, 客户端只有一个Fiber

我们就从`Entry.cs`脚本中`StartAsync`方法的最后一行`FiberManager.Create`方法开始看吧，这个方法内部有如下代码：
```
fiber.ThreadSynchronizationContext.Post(async () =>
{
    try
    {
        await EventSystem.Instance.Invoke<FiberInit, ETTask>((long)sceneType, new FiberInit() {Fiber = fiber});
        tcs.SetResult(true);
    }
    catch (Exception e)
    {
        Log.Error($"init fiber fail: {sceneType} {e}");
    }
});
```
这段代码会通过`EventSystem`触发参数为`FiberInit`、`SceneType为Main（因为调用FiberManager.Create方法的Entry传进来的SceneType就是Main)`的InvokeHandler的`Handler`方法，也就是`FiberInit_Main.cs`脚本中的的`Handle`方法，并且将`FiberInit`参数传递到了这个方法内。

因为我们这里关心的是客户端部分，所以我们看`await EventSystem.Instance.PublishAsync(root, new EntryEvent3());`这一行，也就是说它会通过`EventSystem`触发参数是`EntryEvent3`、`SceneType是Main`的Event的`Run`方法，也就是`EntryEvent3_InitClient`中的`Run`方法。

我们注意到，在该方法内先是给传进来的这个`Scene`类型的root字段添加了一些Component: `GlobalComponent`、`UIGlobalComponent`、`UIComponent`、`ResourcesLoaderComponent`、`PlayerComponent`、`CurrentScenesComponent`。然后根据加载的`GlobalConfig`中的`AppType`字段修改了传进来的`root`参数的`SceneType`字段，在`Demo`中该字段就是`Demo`。接着调用了`await EventSystem.Instance.PublishAsync(root, new AppStartInitFinish());`这一行，也就是通过`EventSystem`触发参数是`AppStartInitFinish`、`SceneType是Demo`的Event的`Run`方法，也就是`AppStartInitFinish_CreateLoginUI.cs`中的`Run`方法。

到了`AppStartInitFinish_CreateLoginUI.cs`这里就不需要说太多了，顺着代码调用路径点下去就能找到`UILoginEvent.cs`这个脚本中的`OnCreate`方法，在这个方法的`ui.AddComponent<UILoginComponent>();`这一行触发了`UILoginComponentSystem`中的`Awake`方法，在这个`Awake`方法中，给登录按钮注册了`OnLogin`方法。由`OnLogin`方法我们执行到了`LoginHelper.cs`脚本中的`Login`方法，该方法要求你传一个类型为`Scene`的字段，这个字段就是从我们最一开始说的`Entry.cs`脚本中`StartAsync`方法的最后一行`FiberManager.Create`方法创建的那个Fiber里面的`Root`字段。**`LoginHelper.cs`脚本中的`Login`方法中执行客户端向服务器发送登录请求，并等待服务器的回应继续执行之后的逻辑**，也就是这一行`long playerId = await clientSenderComponent.LoginAsync(account, password);`，到此为止，客户端所有该做的事情就都做完了，现在客户端已经把请求发送给了服务端，等待着服务端的答复。

所有的Scene都是由Fiber创建出来的 在客户端有两个Scene或者叫Fiber在跑一个是Main 另一个是NetClient
在ClientSenderComponentSystem的LoginAsync方法中, 创建了一个新的Fiber, 这个Fiber创建后, `FiberInit_NetClient`被触发

若一个Entity上挂载了一个ProcessInnerSender组件, 那么它就具备了向其他Fiber发送消息的能力


# ET服务端启动流程梳理

服务端的几个角色

- Router
- Realm
- Gate 最终处理玩家数据的就是Gate

我们回到`FiberInit_Main.cs`这个脚本，这次我们要以`EntryEvent2`为线索来看一下服务端的启动流程，我们需要找到参数为`EntryEvent2`、`SecneType为Main`的`AEvent`，也就是`EntryEvent2_InitServer`。笔者直接把该类的Run方法贴在这里：
```
protected override async ETTask Run(Scene root, EntryEvent2 args)
{
    switch (Options.Instance.AppType)
    {
        case AppType.Server:
        {
            // AppType 的默认值就是Server
            int process = root.Fiber.Process;
            StartProcessConfig startProcessConfig = StartProcessConfigCategory.Instance.Get(process);
            if (startProcessConfig.Port != 0)
            {
                await FiberManager.Instance.Create(SchedulerType.ThreadPool, ConstFiberId.NetInner, 0, SceneType.NetInner, "NetInner");
            }

            // 根据配置创建纤程
            // 应该是会创建12个Scene 这些Scene中有重复的
            var processScenes = StartSceneConfigCategory.Instance.GetByProcess(process);
            foreach (StartSceneConfig startConfig in processScenes)
            {
                await FiberManager.Instance.Create(SchedulerType.ThreadPool, startConfig.Id, startConfig.Zone, startConfig.Type, startConfig.Name);
            }

            break;
        }
        case AppType.Watcher:
        {
            root.AddComponent<WatcherComponent>();
            break;
        }
        case AppType.GameTool:
        {
            break;
        }
    }

    if (Options.Instance.Console == 1)
    {
        root.AddComponent<ConsoleComponent>();
    }
}
```

# ET服务端与客户端的通信流程

我们已经知道了客户端和服务端各自的启动流程了，客户端和服务端是从哪里建立起的连接呢？ 
就看一下 客户端怎么知道往哪个IP地址发请求

现在让我们回到`LoginHelper.cs`脚本中的`Login`方法的`long playerId = await clientSenderComponent.LoginAsync(account, password);`这一行。接下来我们要看一下，客户端是怎么把消息发出去的，服务端是怎么接收到来自客户端的消息、处理客户端的消息然后返回给客户端，客户端收到服务器返回的消息是怎么处理的以及客户端处理完服务器返回的消息之后又做了什么。本小节涉及到部分ET框架层面的实现。


流程
客户端发起连接请求->Router服务器返回Realm地址->客户端根据Realm地址向服务器发送申请->


# EnterMap

所有


# ET中的配置表


- StartProcessConfig
- StartMachineConfig
- StartSceneConfig
- StartZoneConfig

---

基本的继承结构

- ETTask<T>
- ETTask
- Object
    - SystemObject 逻辑体 这些SystemObject看起来都会自动生成一些脚本，是如何做到的？
        - AwakeSystem<T> : ISystemType 
        - AwakeSystem<T,A> : ISystemType
        - AwakeSystem<T,A,B> : ISystemType
        - AwakeSystem<T,A,B,C> : ISystemType
        - AwakeSystem<T,A,B,C,D> : ISystemType
        - DeserializeSystem<T> : ISystemType
        - DestroySystem<T> : ISystemType
        - GetComponentSysSystem<T> : ISystemType
        - LSRollbackSystem<T> : ISystemType
        - LSUpdateSystem<T> : ISystemType
        - LateUPdateSystem<T> : ISystemType
        - SerializeSystem<T> : ISystemType
        - UpdateSystem<T> : ISystemType
            - 继承自UpdateSystem的类一共有15个 看起来都是自动生成的
    - DisposeObject
        - Entity 实体组件都继承这个类
            - Component
            - Scene : IScene
            - ClientSenderComponent
            - MailBoxComponent 挂上这个组件表示该Entity是一个Actor,接收的消息将会队列处理
            - TimerComponent
            - MoveComponent : IAwake, IDestroy
            - GameObjectComponent : IAwake, IDestroy
            - NetComponent
        - ASingleton
            - Singleton<T>
                - CodeLoader
                - CodeTypes 维护着所有被标记了`EntitySystemAttribute`属性的类和方法？
                - EntitySystemSingleton 里面维护着所有继承自`ISystemType`接口的的类型,或者说，被标记为`EntitySystemAttribute`属性的类型
                - EventSystem 维护着allInvokers、allEvents
                - MessageDispatcher : ISingleAwake
                - MessageSessionDispatcher
                - TimeInfo
    - ProtoObject : ISupportInitialize 继承该接口能够实现序列化Bson
        - MessageObject : IMessage
    - HandlerObject
        - AAIHanddler
        - AInvokeHandler<A>
            - MailBoxType_OrderedMessageHandler
            - ATimer
                - MoveTimer
        - AInvokeHandler<A,T>
        - MessageHandler<E,Message>
            - C2M_PathfindingResultHandler
        - MessageHandler<E,Request,Response>
        - MessageLocationHandler<E,Message>
        - MessageLocationHandler<E,Request,Response>
        - MessageSessionHandler<Message>
        - MessageSessionHandler<Request,Response>
- AEvent<S,A> : IEvent
    - ChangePosition_SyncGameObjectPos
- AChanel
  - KChannel
  - TChannel
  - WChannel
- AService
  - KService
  - TService
  - WService
- IKcpTransport
  - TcpTransport
  - UdpTransport


- Fiber
- TypeSystems
- OneTypeSystems 里面维护着所有继承自`SystemObject`基类的类型

BaseAttribute (搞清楚这些Attribute的含义)
- AIHandlerAttribute
- CodeAttribute
- ConfigAttribute
- ConsoleHandlerAttribute
- EnableClassAttribute
- EntitySystemAttribute
- EntitySystemOfAttribute
- EventAttribute
- HttpHandlerAttribute
- InvokeAttribute
- LSEntitySystemAttribute
- LSEntitySystemOfAttribute
- MessageAttribute
- MessageHandlerAttribute
    - MessageLocationHandlerAttribute
- MessageSessionHandlerAttribute
- NumericWatcherAttribute
- ResponseTypeAttribute
- UIEventAttribute

EntitySystemSingleton


# interface
- ISystemType
    - IAwakeSystem
    - IAwakeSystem<A>
    - IAwakeSystem<A,B>
    - IAwakeSystem<A,B,C>
    - IAwakeSystem<A,B,C,D>
    - IDeserializeSystem
    - IGetComponentSysSystem
    - ILSRollbackSystem
    - ILSUpdateSystem
    - ILateUpdateSystem
    - ISerializeSystem
    - IUpdateSystem
- IInvoke的继承类的数量和Invoker属性的标记数量是一样多的,所有的Invoker被EventSystem中的allInvokers管理，allInvoker根据Invoker属性加入字典中
- IAwake
- IAwake<A>
- IAwake<A,B>
- IAwake<A,B,C>
- IAwake<A,B,C,D>
- IUpdate
- ISerialize
- IScene
- IMessage
- IRequest
- IResponse
- ISingletonAwake
- ISingletonawake<A>
- ISingletonAwake<A,B>
- ISingletonAwake<A,B,C>



环境为ET8.1的Demo，梳理点击地板控制角色移动的全流程

客户端发送给服务端的消息体是`C2M_PathfindingResult`

服务端返回给客户端的消息体是`C2M_PathfindingResult`

负责发送消息体的类是ProcessInnerSender，但是将消息体传递给ProcessInnerSender之前，需要先用A2NetClient_Message类包装一下，通过ProcessInnerSender类，将要发送的消息体装载到MessageQueue中

ProcessInnerSenderSystem脚本中的Reply方法

MessageQueue负责各个纤程之间的通讯，在这个Demo中看起来并没有区分客户端和服务端，或者说客户端和服务端在两个不同的纤程中，模拟了服务端和客户端分离的效果。

MessageObject是纤程（客户端、服务端）之间通信的消息体， 



SystemObject 
AwakeSystem
UpdateSystem

# ICriticalNotifyCompletion接口的作用

在Init的Update中执行着

FiberManager.Instance.Update()
    - this.mainThreadScheduler.Update()
        -  fiber.Update();
            - this.EntitySystem.Update();
                - iUpdateSystem.Run(component); -> 继承IUpdateSystem接口的UpdateSystem中实现了该Run方法
                    - this.Update((T)o); -> 再由继承了UpdateSystem的对象实现Update方法


PathfindComponnetSystem

寻路算法的实现是在MoveHelper中的FindPathMoveToAsync中，该方法由C2M_PathfindingResultHandler中的Run方法调用，这些继承自MessageLocationHandler的Run方法统一由MessageLocationHandler的Handle方法调用，Handle方法又由MessageDispatcher中的Handle调用

驱动客户端Unit移动的逻辑看起来在`MoveComponentSystem`类中的`MoveForward`方法中,该方法由MoveTimer类中的Run方法调用

真正在前端做表现的是通过`ChangePosition_SyncGameObjectPos`类


## FiberManager 
这是ET中一个比较重要的模块,


在`CodeTypes`脚本的Awake方法中
```
public void Awake(Assembly[] assemblies)
{
    Dictionary<string, Type> addTypes = AssemblyHelper.GetAssemblyTypes(assemblies);
    foreach ((string fullName, Type type) in addTypes)
    {
        this.allTypes[fullName] = type;
        
        if (type.IsAbstract)
        {
            continue;
        }
        
        // 记录所有的有BaseAttribute标记的的类型
        object[] objects = type.GetCustomAttributes(typeof(BaseAttribute), true);

        foreach (object o in objects)
        {
            this.types.Add(o.GetType(), type);
        }
    }
}
```

CodeLoader中的Start方法如下:
```
public void Start()
{
    if (!Define.IsEditor)
    {
        byte[] modelAssBytes = this.dlls["Unity.Model.dll"].bytes;
        byte[] modelPdbBytes = this.dlls["Unity.Model.pdb"].bytes;
        byte[] modelViewAssBytes = this.dlls["Unity.ModelView.dll"].bytes;
        byte[] modelViewPdbBytes = this.dlls["Unity.ModelView.pdb"].bytes;
        // 如果需要测试，可替换成下面注释的代码直接加载Assets/Bundles/Code/Unity.Model.dll.bytes，但真正打包时必须使用上面的代码
        //modelAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.dll.bytes"));
        //modelPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.pdb.bytes"));
        //modelViewAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.dll.bytes"));
        //modelViewPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.pdb.bytes"));

        if (Define.EnableIL2CPP)
        {
            foreach (var kv in this.aotDlls)
            {
                TextAsset textAsset = kv.Value;
                RuntimeApi.LoadMetadataForAOTAssembly(textAsset.bytes, HomologousImageMode.SuperSet);
            }
        }
        this.modelAssembly = Assembly.Load(modelAssBytes, modelPdbBytes);
        this.modelViewAssembly = Assembly.Load(modelViewAssBytes, modelViewPdbBytes);
    }
    else
    {
        if (this.enableDll)
        {
            byte[] modelAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.dll.bytes"));
            byte[] modelPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.pdb.bytes"));
            byte[] modelViewAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.dll.bytes"));
            byte[] modelViewPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.pdb.bytes"));
            this.modelAssembly = Assembly.Load(modelAssBytes, modelPdbBytes);
            this.modelViewAssembly = Assembly.Load(modelViewAssBytes, modelViewPdbBytes);
        }
        else
        {
            Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();
            foreach (Assembly ass in assemblies)
            {
                string name = ass.GetName().Name;
                if (name == "Unity.Model")
                {
                    this.modelAssembly = ass;
                }
                else if (name == "Unity.ModelView")
                {
                    this.modelViewAssembly = ass;
                }

                if (this.modelAssembly != null && this.modelViewAssembly != null)
                {
                    break;
                }
            }
        }
    }
    
    (Assembly hotfixAssembly, Assembly hotfixViewAssembly) = this.LoadHotfix();

    World.Instance.AddSingleton<CodeTypes, Assembly[]>(new[]
    {
        typeof (World).Assembly, typeof (Init).Assembly, this.modelAssembly, this.modelViewAssembly, hotfixAssembly,
        hotfixViewAssembly
    });

    IStaticMethod start = new StaticMethod(this.modelAssembly, "ET.Entry", "Start");
    start.Run();
}
```

如果你是Editor模式下的话,你应该会走到`this.enableDll`为true的分支
通过执行CodeLoader中的Start方法,被遍历到的程序集有`World`类所在的程序集`Unity.Core`、`Init`类所在的程序集`Unity.Loader`、modelAssemBly`Unity.Model`,modelViewAssembly`Unity.ModelView`,hotfixAssembly`Unity.Hotfix`,hotfixViewAssembly`Unity.HotfixView`

代入一下就是,通过AddSingleton方法,创建了CodeTypes实例,并将上面提到的程序集作为参数传入CodeType的Awake方法中.

然后在modelAssembly`Unity.Model`中,找到`ET.Entry`类中的Start方法,并执行该方法,然后在该方法中,执行了`CodeTypes.Instance.CreateCode`方法,该方法会在上面收集到的程序集中,找到所有被标记了CodeAttribute属性的类,并将这些类实例化出来,这些类分别是
- EntitySystemSingleton
- MessageDispatcher MessagePatcher中的Awake方法中实例化了所有被标记为MessageHandlerAttribute属性的类
- EventSystem
- HttpDispatcher
- LSEntitySystemSingleton
- AIDispatcherComponent
- ConsoleDispatcher
- MessageSessionDispatcher
- NumericWatcherComponent
- UIEventComponent
也就是说,上面这十个类,通过执行ET的Entry方法之后就已经被创建出来了. 




# 项目组织

基本上重要的脚本都是在Unity工程下面,在ET.sln视角下,看到的Unity外面几个目录下有很多代码,这些代码都是类似超链接的东西连接到Unity工程中的.

ET中的HotFix、HotFixView、Model、ModelView四个程序集都是以dll的方式加载到内存中运行的，因此如果你修改了这四个程序集里面的代码，你可能需要重新遍历一下才能把更新的内容放进程序集中。

# 

# 参考资料

[ET8框架的讲解视频](https://www.bilibili.com/video/BV1rhYyeKExP/?spm_id_from=333.337.search-card.all.click&vd_source=83f4165825ce9df46cf4fd576ccb1102)
[一篇将服务器架构历史的博客](https://blog.csdn.net/Q540670228/article/details/123385080?spm=1001.2014.3001.5502)
[一篇介绍C#和ET异步方法的帖子](https://et-framework.cn/d/2067-fiber)

