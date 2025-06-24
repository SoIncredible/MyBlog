---
title: HybridCLR相关
abbrlink: '20505312'
date: 2025-06-22 08:31:38
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 先讲点历史

> CLR via C#是本好书, 可以在这本书里面并没有详细解释过.Net、.NetCore、.Net Standard、.Net Framework一系列的概念, 之所以有这么多的概念, 也是有历史原因的, 了解.Net的发言史能帮我们更好地理解这些概念

## 先来介绍一下C#

## Unity的发展史

## Mono

Mono自2001年启动, Mono是第一个在Android、IOS、Linux等其他非Windows平台上实现的.Net项目. 现在Mono已经渐渐退出历史舞台了

Mono官网描述如下:Mono由微软赞助，是微软.NET框架的开源实现，是.NET基金会的一部分，基于C#和通用语言运行时的ECMA标准。不断发展壮大的解决方案系列和积极热情的贡献社区正帮助 Mono 成为开发跨平台应用程序的主要选择。

Unity最初选择的是Unity Script语言作为Editor的开发语言, 但是后来考虑跨平台, 选择了C#语言, 但是又因为微软本身提供的C#的运行环境(或者叫CLR)是闭源的, 有授权和跨平台的各种限制, 于是就选择了Mono作为C#的运行环境.

2010年之前, C#的这一套东西叫做.Net Framework, 这个.Net Framework是只支持Windows平台的.
2010年之后, .Net开源, 并且舍弃了之前的.Net Framework, 并且支持了跨平台, 现在就叫.Net或者.NetCore了

# .Net构成

.Net由以下几个部分构成
- CorCLR/.NET Runtime运行时环境, 托管运行时/虚拟机, 负责执行IL 中间语言JIT GC
- 基类库
- ASP.Net Core Web开发框架
- WinForms、WPF（Windows Only）

# C#代码是怎么跑起来的?

在CLR via C#这本书中, 作者只提及了JIT Compiler这个概念, 笔者认为在一开始.Net可能就只支持JIT的方式执行C#代码, 而AOT和解释器方式则是跨平台的概念引入到.Net中后, 才出现的概念.

## AOT模式

我们就拿Unity开发举例子, 如果使用AOT的方式打包, 那么在打包阶段, 我们编写的C#代码就会全部被编译成机器码, 也就是.so文件, 这种代码就是原生的代码, 丢到机器上就就能立刻运行, 不需要编译, 但是机器码都是01, 存储效率低, 由我们的C#脚本转成机器码会造成代码膨胀, 听起来是不是很像IL2CPP?

## JIT模式

JIT方式打包, 在打包阶段, 我们编写的C#代码会被编译成IL, 打入一个.dll的文件, 这就是Mono的打包方式, 使用这种方式打的包, 包体小, 但是由于其代码不是原生的, 所以运行时需要依赖CLR将IL代码编译成原生代码执行, 比起AOT方式 在启动时间上会久一点, 因为需要编译, 但是只要编译了一次之后, 执行效率和AOT是没什么区别的

## Interpreter模式

解释器, 笔者第一次看到这个概念出现在C#中时, 是非常疑惑的, 因为笔者理解的解释器, 是像Python那种解释型语言才会使用到的东西, 而C#作为一个编译型语言, 其实C#并不是传统意义上的编译型语言, 因为C#是先编译成IL中间语言, 然后在运行时通过JITCompiler将中间语言编译成机器码执行的方式.

也就是, C#不解释型语言那样直接解释源代码执行, 也不像C++那样一次性把源代码编译成机器码执行.

解释器模式可以理解为是在Unity开发中代码热更新场景下, 业内开发者为了能够执行热更代码(dll)而开发的一种迷你版的CLR(虚拟机).
我再理解一下解释器的角色, 解释器就是在AOT打包方式下, 主包的.Net的虚拟机被剔除丧失了解析编译IL(dll)的能力, 需要一个迷你版的虚拟机(也就是解释器)来执行IL代码

# DLL种类

> C#和C++虽然都能生成DLL, 如果DLL的调用者是C#语言, 那么这两种DLL是有分别的: C#生成的DLL类型是托管类型的DLL, C++生成的DLL是非托管的(原生的)DLL, 前者的DLL导入到C#工程中编译器就能够自动的识别DLL中的成员类型, 后者生成的DLL导入到C#工程中则需要使用`[DLLImport]`Attribute来做一些额外的处理, 并且在C++侧也需要对于要在C#侧调用的方法签名上添加`extern "C"`标识

# 一个C#工程使用的.Net版本怎么看? 
# 一个C#工程使用的.Net版本怎么修改? 

# 前置知识


## AOT JIT

AOT和JIT是C#(当然别的基于CLR的语言也可以)代码运行的两种方式, AOT即Ahead of Time
- AOT Ahead of Time
什么是AOT?


ILRuntime中提到了它的机制是Interpret代码, 为什么会是Interpret呢?

# 而Unity中 Mono方式打包对应的就是JIT的方式, IL2CPP方式打包对应的就是AOT的方式, 
正是由于IOS系统上不允许使用JIT的方式运行App, 所以Unity团队才给出了IL2CPP的解决方案

# .Net .NetFramework .NetStandard C#版本之间的关系


# JIT和解释器的区别是啥呢? 不都是运行时编译代码嘛?

JIT方式是有完整的Mono或者CLR虚拟机的机制, 而解释器则是一种针对Unity热更场景下的迷你版虚拟机机制.

# C#的执行机制

C#代码会被编译成IL语言, 在运行的时候, 通过一个Interpreter(解释器)逐行解释IL指令运行
在IOS上, 是不允许JIT的方式运行代码的, 只能使用AOT的方式运行
在Android等其他设备上AOT和JIT都是可以的

# Mono是什么 扮演什么角色

由于.Net最初并不开源, 而且跨平台支持不好, 因此有一个团队根据.Net开放的接口, 一比一还原了CLR运行时, 这是真的造轮子, 也就是说, C#这个代码既可以跑在CLR环境中, Mono也提供了一个运行C#脚本的环境. 

# Unity PlayerSettings中有一个APICompatibilityLevel选项 是干嘛的

你的Unity工程，允许代码能用（兼容）的 .NET（或Mono）API集合的标准程度。

Unity的脚本运行环境（Scripting Backend）是基于Mono（或IL2CPP）来实现C#环境的；
但Mono和微软官方.NET实现的API/标准并不是完全一致的，有新有旧、有全有残；
API Compatibility Level，就是让你指定用哪一套标准的API接口子集：
比如：.NET 2.0 Subset
又比如：.NET 4.x / .NET Standard 2.0
或者更高（随着Unity版本演进而变化）
这些选项，实际上背后对应着一组Unity定制的.NET Base Class Library (BCL)，决定你在C#脚本里能不能引用、编译、反射、调用某些标准库功能。

`API Compatibility Level`会根据你所使用的Unity Editor版本变化而变化, 比如在`2020.3.48`版本中, `API Compatibility Level`的选项是`.Net 4.X`和`.Net Standard 2.0`, 而在`2022.3.15`版本中, `API Compatibility Level`的选项则变成了`.Net Standard 2.1`和`.Net Framework`

## 实际开发中如何选择?

新项目：优先选“.NET 4.x”或“.NET Standard 2.0”。
项目中有现代C#语法（如async/await、LINQ等）：必须选新标准。
做插件/SDK跨Unity兼容：优先考虑“.NET Standard 2.0”。
有老DLL、Asset Store资产只支持2.0，且不想升级：临时用“.NET 2.0”。
手机版本极度精简、追求最小包体：用Subset，但很少有这种极端需求。

# 如今行业内主流的热更方案究竟是怎么形成的?

- 由于IOS对于JIT的限制, 导致主包必须使用AOT的方式打包, 而AOT打包会直接把.Net Mono虚拟机剔除掉, 将C#直接转成C++原生代码. 这就会导致主包不具备识别dll能力
- 接着, 由于各个操作系统平台对于原生代码的动态加载是有限制的（比如 dlopen 加新 so/dll）, 所以想要在运行时动态的加载一段逻辑进来, 只能使用非原生代码, 于是热更通常选择DLL（IL）
- 要让底包能认识和运行“热更dll”里的 C# 代码，就得有.NET虚拟机。但AOT打包让虚拟机相关能力被移除/阉割，不认dll。 但是虚拟机在AOT打包的时候已经几乎被完全剔除掉了, 因此我们必须让它恢复识别dll的功能, 
- 要想恢复识别dll的能力，就得集成新的虚拟机或解释能力（比如ILRuntime/HybridCLR）。HybridCLR的意义就是让AOT包恢复充分甚至很大程度原生的DLL识别/运行能力。这就是HybridCLR所做的事情在AOT打包模式剔除掉Mono虚拟机识别dll代码能力的情况下, 重写AOT的打包方式, 让其保留能够识别dll代码的能力.

# 代码热更新

为什么代码热更新都选择使用JIT(将热更代码编译成dll)的方式, 而不选择AOT原生方式呢?

# 热更的代码和热更的资源 哪个应该先加载?
热更代码的调用方式
有一种很取巧的方式加载热更代码, 那就是把热更代码挂载到某一个预制体上, 通过Unity的Awake方法调用热更代码的入口
AOT程序集 就是底包程序集 AOT是一种代码编译方式 它会

什么是streaming path? 什么是persistent path?

# 参考
- [AOT 和 JIT、 IL2CPP和Mono、 CLR、 ILRuntime热更新原理](https://blog.csdn.net/codywangziham01/article/details/123689658)
- [Unity跨平台原理](https://www.cnblogs.com/fly-100/p/4594380.html)