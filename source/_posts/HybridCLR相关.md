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

> 《CLR via C#》这本书帮助读者建立起了C#的知识框架, 是笔者职业生涯中功不可没的好书, 不过本书的作者Jeffrey Richter是以.Net Framework框架在Windows平台进行开发的视角上介绍.Net的一些特性的, 而笔者作为一个Unity入门C#的半吊子在最初阅读本书的很长一段时间内是没有真正理解.Net的核心特性的. 在本篇博客中, 笔者尝试基于这本书, 加上笔者对Unity的理解, 站在Unity开发者的视角上, 诠释.Net中的一些关键性概念.

# .NET往事

以在这本书里面并没有详细解释过.Net、.NetCore、.Net Standard、.Net Framework一系列的概念, 之所以有这么多的概念, 也是有历史原因的, 了解.Net的发言史能帮我们更好地理解这些概念、
.Net是.Net C#是C#, .Net是一套支持C#代码运行的框架体系, 除此之外.Net还支持F#、VB等语言 

.Net Framework、 .NetCore、 .Net这三者是对同一个对象在不同发展时期的不同称谓, 这一对象在不同发展时期具有不同的特点. 


## .Net Framework是啥
此时的.Net只能够运行在Windows上

## .Net Core是啥

.Net Core在原来.NetFramework的基础上, 增加了对于跨平台的支持

## .Net 是啥

经过一段时间的演变, .Net Core就变成了.Net

| 组件名称                                  | 作用                          | 说明                                      |   |   |   |   |   |   |   |
|---------------------------------------|-----------------------------|-----------------------------------------|---|---|---|---|---|---|---|
| CoreCLR / .NET Runtime                | 托管运行时/虚拟机，负责执行IL，中间语言JIT，GC | https://github.com/dotnet/runtime（已开源）  |   |   |   |   |   |   |   |
| CoreFX / 基类库（Base Class Library, BCL） | 常用系统API：IO、网络、集合、字符串等       | 现代叫runtime中的“libraries”                 |   |   |   |   |   |   |   |
| ASP.NET Core                          | Web开发框架                     | 支持Web服务器、API、MVC、Blazor、SignalR、gRPC等平台 |   |   |   |   |   |   |   |
| Entity Framework Core                 | 对象关系映射（ORM）数据库组件            | 支持SQLServer、SQLite、MySQL等主流数据库          |   |   |   |   |   |   |   |
| WinForms、WPF（Windows Only）            | 桌面GUI开发                     | .NET 5+支持基本移植，但仅限Windows                |   |   |   |   |   |   |   |
| MSBuild                               | 构建系统                        | dotnet build 就是用的这个                     |   |   |   |   |   |   |   |
| Roslyn                                | C#、VB.NET编译器和代码分析器          | https://github.com/dotnet/roslyn        |   |   |   |   |   |   |   |
| NuGet                                 | 包管理工具                       | 在线/本地包管理                                |   |   |   |   |   |   |   |
| CLI                                   | 命令行工具（dotnet）               | 包括dotnet、dotnet build、dotnet run等基础命令   |   |   |   |   |   |   |   |

## Mono是啥

Mono 项目始于 2001 年，由 Ximian 公司的创始人 Miguel de Icaza 领导。Ximian 是一家专注于 Linux 和开源软件的公司。Miguel de Icaza 的目标是创建一个.NET 框架的开源实现，以便在 Linux 上运行。

.NET 框架最初由微软开发，主要用于构建 Windows 应用程序。由于.NET 框架的闭源性，它无法直接在其他操作系统上运行。Mono 项目的出现打破了这一限制，使得.NET 开发者的代码能够在多种平台上运行。

随着时间的推移，Mono 项目得到了广泛的社区支持，并且不断发展和完善。它支持多种编程语言，包括 C#、Visual Basic.NET 和 F#，并且实现了许多.NET 框架的功能。Mono 项目在开源社区中的成功也促进了.NET 技术在非 Windows 平台上的普及。

还有就是.Net升级是.Net C# language升级是language
一般来说, 先是C#的语言有更新, 必须新增了某种语法或者语言特性 那么配套的.Net中就需要支持能够识别并驱动这些语法, 因此一般C#有新特性增加对应的.Net也有新的升级

说白了 .Net、.NetCore、.NetFramework、Mono每一个都是一套让C#代码能够运行跑起来的平台. 这就好比各家虚拟机平台, 而C#就像是你在微软官网上下载的Windows操作系统的镜像文件, 你可以将这个镜像文件挂载在VMWare平台上, 也可以挂载在Parallel Desktop平台上, 即便平台的底层实现不同, 但是因为它们都遵循同样的规范, 因此同样的一操作系统镜像文件可以跑在不同的虚拟机平台上, 这些平台遵循的规范, 类比到.Net中也就是`.Net Standard`

## .Net Standard

.Net Standard 是针对多个 .Net实现推出的一套正式的.Net API规范. 推出.NetStandard的背后动机是要提高.Net生态系统中的一致性. 但是 .Net5采用的不同的方法来建立一致性, 这种新方法在很多情况下都不需要使用到.Net Standard.

## Unity的发展史

## Unity何时引入的Mono

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

# DLL

> C#和C++虽然都能生成DLL, 如果DLL的调用者是C#语言, 那么这两种DLL是有分别的: C#生成的DLL类型是托管类型的DLL, C++生成的DLL是非托管的(原生的)DLL, 前者的DLL导入到C#工程中编译器就能够自动的识别DLL中的成员类型, 后者生成的DLL导入到C#工程中则需要使用`[DLLImport]`Attribute来做一些额外的处理, 并且在C++侧也需要对于要在C#侧调用的方法签名上添加`extern "C"`标识

# 一个C#工程使用的.Net版本怎么看? 
# 一个C#工程使用的.Net版本怎么修改? 

## AOT JIT

AOT和JIT是C#(当然别的基于CLR的语言也可以)代码运行的两种方式, AOT即Ahead of Time
- AOT Ahead of Time
什么是AOT?

# 代码热更新

## Lua、XLua

## ILRuntime

## HybridCLR
## .sln .csproj .dll .asmdef的区别和联系

.sln 是
.csproj 一个sln下会有多个csproj
.asmdef 这是Unity中的一个概念, 每创建一个asmdef, Unity都会自动生成这个asmdef对应的csproj
.dll dll一般有两种: 使用C#编译生成的dll, 这类dll属于托管类dll, 导入Unity能够直接被Unity编译器识别; 还有一类是使用c/c++等非托管类语言编译生成的dll, 这类dll需要C#具有能够调用原生(native)代码的能力, 需要使用`[DLLImport]`属性来导入方法

# JIT和解释器的区别是啥呢? 不都是运行时编译代码嘛?

JIT方式是有完整的Mono或者CLR虚拟机的机制, 而解释器则是一种针对Unity热更场景下的迷你版虚拟机机制.

# C#的执行机制

C#代码会被编译成IL语言, 在运行的时候, 通过一个Interpreter(解释器)逐行解释IL指令运行
在IOS上, 是不允许JIT的方式运行代码的, 只能使用AOT的方式运行
在Android等其他设备上AOT和JIT都是可以的

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

# 一直有个问题是如果我区分程序集的话 我只修改了其中某一个程序集的代码 C#的编译器会不会只编译那个被修改代码的程序集而提高编译速度呢?

# 参考
- [.net 温故知新：【2】 .Net Framework 、.Net 、 .NET Standard的概念与区别](https://www.cnblogs.com/SunSpring/p/15047424.html)
- [AOT 和 JIT、 IL2CPP和Mono、 CLR、 ILRuntime热更新原理](https://blog.csdn.net/codywangziham01/article/details/123689658)
- [Unity跨平台原理](https://www.cnblogs.com/fly-100/p/4594380.html)
- [Unity从发布到流行经历了什么重大变化（2）](https://zhuanlan.zhihu.com/p/88692056)
- [Unity将来时：IL2CPP是什么？有了Mono为什么还需要IL2CPP?](https://blog.csdn.net/gz_huangzl/article/details/52486255)
- [.NET 发展历程与未来](https://blog.csdn.net/Funniyuan/article/details/136002603)
- [Mono 软件发展历程详解](https://my.oschina.net/emacs_9244658/blog/18230886)
- [.NET 的发展简史](https://www.cnblogs.com/willick/p/15038133.html)