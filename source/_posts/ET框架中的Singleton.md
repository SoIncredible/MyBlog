---
title: ET框架中的Singleton
abbrlink: 12f854cb
date: 2025-03-28 14:23:01
tags:
categories:
cover: https://public.ysjf.com/content/title-image/%E8%88%AA%E6%8B%8D-%E9%98%BF%E8%A5%BF%E9%87%8C%E8%A5%BF%E5%A4%A7%E8%8D%89%E5%8E%9F12-%E7%A9%BA%E9%95%9C.jpg
description:
swiper_index:
sticky:
---

本文笔者会按照ET中所有Singleton创建的顺利，尝试梳理一下设计到的模块。

# Singleton的创建

> 疑问 已经有一个ASingleton了，为什么还要有一个ASingletonAwake？
所有的Singleton都会继承ASingleton基类。但是不同的Singleton还会继承不同的ISingletonAwake。 一是使用泛型，二是声明几个参数数量不同的ISingletonAwake接口，开发者可以根据需求选择具体的Singleton继承哪个接口，但是所有的Singleton又都会继承ASingleton被Word中的singletons管理起来，相当于把Singleton的初始化和管理的职能拆分交给了两个角色去处理，还是比较巧妙的。

> 疑问 为什么有的Singleton会被打上Code特性，有的不会？

虽然大家都是Singleton，但是各个Singleton之间的优先级不一定一样，单例之间会存在依赖。比如CodeLoader、CodeTypes这两个单例，它们两个需要在其他单例之前先被Awake，然后在CodeType单例内部，再去创建出其他单例出来。因此，如果有一个单例没有被标记为Code特性，那么说明这个单例是一个优先级比较高的单例，它应该在某一比较早的时刻被我们开发者手动调用并创建出来。也就是ET框架通过给不给单例标记CodeAttribute的方式来对单例的创建进行手动或者自动的管理。

# World

接着我们来说一下World中的AddSingleton接口，这个接口不仅仅是被World内部的几个泛型AddSingleton接口调用，还被下面几个地方调用了：
- CodeTypes
- ConfigLoader
- EditorLogHelper

# CodeTypes
# ConfigLoader
# EditorLogHelper

# AddSingleton的顺序
- Logger
- TimeInfo
- FiberManager
- ResourcesComponent
- CodeLoader
- 接着调用CodeLoader的Start方法创建CodeTypes单例
- 接着通过反射的方式调用Entry中的Start方法创建下面单例：
  - IdGenerater
  - OpcodeType
  - ObjectPool
  - MessageQueue
  - NetServices
  - NavmeshComponent
  - LogMsg
  - 然后调用CodeTypes单例的CreateCode方法，该方法拿到所有被标记了CodeAttribute的类，自动的创建这些类的单例：
    - EntitySystemSingleton
    - MessageDispatcher
    - EventSystem
    - HttpDispatcher
    - LSEntitySystemSingleton
    - AIDispatcherComponent
    - ConsoleDispatcher
    - MessageSessionDispatcher
    - NumericWatcherComponent
    - UIEventComponent
  - ConfigLoader


# CodeTypes



# EventSystem

EventSystem中维护了两个字段`allEvents`和`allInvokers`
EventSystem在Invoke的时候会初始化这两个字段的内容
allEvents字段中 记为Event特性的所有类型，创建这些类型的实例，被标记了Event特性的类必须要继承IEvent接口。

[`MemberInfo.GetCustomAttributes(Type, bool);`](https://learn.microsoft.com/en-us/dotnet/api/system.reflection.memberinfo.getcustomattributes?view=net-9.0)。

看一下AEvent中提供了一个Run接口，

InvokeHandler和MessageHandler的区别是什么

Event和Invoker的区别是什么

# MessageHandler 感觉是另外一套逻辑


# EntitySystemSingleton

## EntitySystem

EntitySystemSingleton里面维护了一个TypeSystems字段

AwakeSystem和Component的IAwake接口之间的关系.