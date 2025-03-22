---
title: ET框架
categories: 硬技能
cover: 'https://www.notion.so/images/page-cover/met_horace_pippin.jpg'
abbrlink: 9c5dbe31
date: 2025-3-20 17:50:31
tags:
description:
swiper_index:
sticky:
---
这段时间笔者工作清闲，在工位也没有摸鱼，先是废寝忘食般速通了YooAsset的源码之后，感觉自己在看别人代码这件事上摸到了一些门路, 不论ET、YooAsset还是UniTask它们都各自实现了自己的异步操作,对于异步操作更详细的介绍在[UniTask框架梳理]()这篇博客中.

一上来先不用管什么帧同步状态同步之类的，先把Demo的源码看明白.笔者尝试从游戏启动时，ET执行的第一行代码开始，历经代码加载、资源加载、登录、跳转逻辑、一直梳理到Demo中点击地板移动角色操作的所有逻辑。ET在初始化过程中涉及到了反射和属性的知识，那么结合ET与反射、属性的详细内容在[这篇博客](https://soincredible.github.io/posts/cd96d12/)中

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

# Fiber

Fiber是ET中比较重要的一个角色

ActorId
Address
Fiber

# Entity

目前我没有找到ET是如何往这个componnets里面添加东西的
Entity中维护了一个`component`字典,表示这个entity上挂载的Entity组件,而且Entity中还维护了一套Entity的父子关系

# ET中用到的其他Attribute

[BsonIgnore]
[UnityEngine.HideInInspector]
[MemoryPackIgnore]

有的Attribute比如ChildOf属性，由[ChildOf]和[ChildOf(SomeClass)]这两者有什么区别？

# Actor模型
调用Activator.CreateInstance(type);难道不需要考虑带参数的构造方法吗?

# ET框架看起来可以自动创建一些System的脚本,这是如何做到的 看起来并不是在Unity侧进行的操作


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

# 流程梳理

# 角色梳理

- World 
    - singletons : Dictionary<Type,ASingleton> Demo中所有的Singleton都被存入这个字典中，笔者认为该字段存在的目的主要是在World被销毁时统一将所有singleton Dispose，毕竟都是Singleton了，任何地方都可以通过Instance直接访问到这些单例。
- FiberManager 
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

# 如何让一个类变得可以await？

ThreadSynchronizationContext的作用?
  - https://learn.microsoft.com/en-us/archive/msdn-magazine/2011/february/msdn-magazine-parallel-computing-it-s-all-about-the-synchronizationcontext


# 

Invoke由EventSystem维护,Invoker的Handle在EventSystem中调用,**MailBoxType_OrderedMessageHandler**属于Invoker,因此MailBox在Event System中被Invoke,然后
在**MailBox**中驱动MessageDispatcher调用Handle

-----
登录流程大概如下
ClientSenderComponentSystem中的LoginAsync中的
`self.fiberId = await FiberManager.Instance.Create(SchedulerType.ThreadPool, 0, SceneType.NetClient, "");`
调用了FiberManager中的`public async ETTask<int> Create(SchedulerType schedulerType, int fiberId, int zone, SceneType sceneType, string name)`方法，这个方法中执行了`await EventSystem.Instance.Invoke<FiberInit, ETTask>((long)sceneType, new FiberInit() {Fiber = fiber});` 然后**EventSystem驱动Invoker， Invoker驱动Dispatcher， Dispatcher驱动Handler** 登录流程处理完毕
----

MessageDispatcher维护messageHandlers,服务器处理登录逻辑的类是Main2NetClient_LoginHandler, 也就是由

ProcessInnerSenderSystem
MessageQueue
**ET_ProcessInnerSender_UpdateSystem**被标记为了**EntitySystem**,由EntitySystemSingleton管理
**ET_ProcessInnerSender_UpdateSystem**的Update中执行Fetch操作,看起来是要处理发送来的请求.
UpdateSystem在每一帧都会执行, UpdateSystem由谁驱动? 由**EntitySystem**


大概看明白了 登录的时候把登录请求放到一个队列里面 然后在Update的时候去这个队列里面拿登录请求进行处理. 但是对于上层 什么时候Update还需要再看一下,应该就是每一帧都会Update,但是因为这个登录算是同步的,
就得看发起登录的 **ET_Client_UILoginComponent_AwakeSystem**和处理登录的**Main2NetClient_LoginHandler**谁先执行谁后执行了
也就是看EntitySystem或者就叫AwakeSystem的Awake和MessageHandler的Handle在同一帧中的处理顺序了

前一帧登录请求 

紧接着在Update中

