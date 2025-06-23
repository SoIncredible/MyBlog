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

# 前置知识

什么是AOT?

AOT程序集 就是底包程序集 AOT是一种代码编译方式 它会

什么是streaming path? 什么是persistent path?

分帧加载是什么意思?

热更代码的调用方式

有一种很取巧的方式加载热更代码, 那就是把热更代码挂载到某一个预制体上, 通过Unity的Awake方法调用热更代码的入口


# C#的执行机制

C#代码会被编译成IL语言, 在运行的时候, 通过一个Interpreter(解释器)逐行解释IL指令运行
在IOS上, 是不允许JIT的方式运行代码的, 只能使用AOT的方式运行
在Android等其他设备上AOT和JIT都是可以的

Mono是什么 扮演什么角色
Interpreter和Compiler 扮演什么角色

# 热更的代码和热更的资源 哪个应该先加载?



# 参考
- [AOT 和 JIT、 IL2CPP和Mono、 CLR、 ILRuntime热更新原理](https://blog.csdn.net/codywangziham01/article/details/123689658)
- [Unity跨平台原理](https://www.cnblogs.com/fly-100/p/4594380.html)