---
title: ET框架
categories: 硬技能
cover: 'https://www.notion.so/images/page-cover/met_horace_pippin.jpg'
abbrlink: 9c5dbe31
date: 2024-12-14 17:50:31
tags:
description:
swiper_index:
sticky:
---
这段时间笔者工作清闲，在工位也没有摸鱼，先是废寝忘食般速通了YooAsset的源码之后，感觉自己在看别人代码这件事上摸到了一些门路

一上来先不用管什么帧同步状态同步之类的，先把Demo的源码看明白.
基本的继承结构

- Object
  - SystemObject 逻辑体
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
  - DisposeObject
      - Entity 实体组件都继承这个类
          - Component
          - Scene : IScene
          - ClientSenderComponent
      - ASingleton
          - Singleton<T>
              - CodeLoader
              - CodeTypes 维护着所有被标记了`EntitySystemAttribute`属性的类和方法？
              - EntitySystemSingleton 里面维护着所有继承自`ISystemType`接口的的类型
              - EventSystem
  - ProtoObject : ISupportInitialize 继承该接口能够实现序列化Bson
      - MessageObject


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


# Actor模型
调用Activator.CreateInstance(type);难道不需要考虑带参数的构造方法吗?

# ET框架看起来可以自动创建一些System的脚本,这是如何做到的 看起来并不是在Unity侧进行的操作
