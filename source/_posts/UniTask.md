---
title: UniTask
abbrlink: 9955c97f
date: 2025-03-20 22:03:33
tags:
categories:
cover: https://public.ysjf.com/mediastorm/material/material/%E5%AE%89%E5%85%8B%E9%9B%B7%E5%A5%87-03-%E8%BF%9C%E6%99%AF-20250107.JPG
description:
swiper_index:
sticky:
---

（摘抄自CLR Via C#）异步函数存在以下限制：

- 不能将应用程序的Main方法转变成异步函数。另外构造器、属性访问器方法和事件访问器方法不能转变成异步函数
- 异步函数不能使用任何out或者ref参数
- 不能在catch，finally或unsafe块中使用await操作符（注意在try中可以使用await，笔者记得UniTask的文档中说过是可以在try catch中使用UniTask并被捕获的，是因为UniTask实现了自己的`AsyncMethodBuilderAttribute`吗？ 记得看一下）。
- 不能在await操作符之前获得一个支持线程所有权或递归的锁，并在await操作符之后释放它。这是因为await之前的代码由一个线程执行，之后的代码则可能由另一个线程执行。（使用了await就一定会涉及到多线程吗？那UniTask是怎么保证它使用await的时候是在同一个线程内？）在C#的lock语句中使用await，编译器会报错。如果显式调用Monitor的Enter和Exit方法，那么代码虽然可以编译，但Monitor.Exit会在运行时抛出一个SynchronizationLockException。
- 在查询表达式中，await操作符只能在初始from子句的第一个集合表达式中使用，或者在join子句的集合表达式中使用。

- 关于（使用了await就一定会涉及到多线程吗？那UniTask是怎么保证它使用await的时候是在同一个线程内？）GPT的回复：
    是的，如果你使用的是Unity专用的异步API（如UnityWebRequest），await这些API的操作通常会在主线程上执行。
    如果开发者显式创建Task或Thread并await这些任务，这些任务可能在非主线程上执行，但await后续的代码依然会回到主线程上执行。
    因此，理解上可以简化为：只要不是开发者主动使用多线程机制，一般所有的await操作都会确保在主线程上安全地执行后续操作。