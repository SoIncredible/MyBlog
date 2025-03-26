---
title: ET框架前置知识总结
date: 2025-03-25 16:21:26
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

主要是一些异步的操作看起来比较吃力，因此在这边博客中对C#中的异步操作做一个简单的介绍。

# C# SynchronizationContext和ExecutionContext使用总结 
https://www.cnblogs.com/wwkk/p/17814057.html


https://blog.csdn.net/shizuguilai/article/details/121236777

SynchronizationContext中的Post方法是干什么的？ Post和Send方法是不是也会有异步的效果，需要验证一下

# C# 中几种异步的返回类型

C#中有三种比较常用的返回类型: void、Task<TResult>和Task

https://www.cnblogs.com/liqingwen/p/6218994.html?tdsourcetag=s_pcqq_aiomsg

# TaskCompletionSource是什么？

按照笔者的理解，TaskCompletionSource可以将一个基于回调的异步操作转换成一个可以被await的异步操作。

在ET中


抛开ET的一个例子，比如协程 协程是不可以被await的

参考文档：https://blog.csdn.net/q__y__L/article/details/133905192