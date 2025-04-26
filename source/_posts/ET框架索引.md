---
title: ET框架索引
abbrlink: ff09e316
date: 2025-03-28 14:37:27
tags:
categories:
cover: https://public.ysjf.com/content/title-image/xinjiang/%E8%88%AA%E6%8B%8D-%E9%AD%94%E9%AC%BC%E5%9F%8E09-%E7%A9%BA%E9%95%9C.jpg
description:
swiper_index:
sticky: 99
---

> 本系列基于ET8.1版本

- [帧同步与状态同步](https://soincredible.github.io/posts/50d23509/)
- [ET中的网络通信模块](https://soincredible.github.io/posts/3613bb98/)
- [ET中的异步](https://soincredible.github.io/posts/a8cdc42b/)
- [ET中的Singleton](https://soincredible.github.io/posts/12f854cb/)
- [ET中反射与特性的使用](https://soincredible.github.io/posts/cd96d12/)
- [ET框架自动生成脚本](https://soincredible.github.io/posts/ef16867e/)
- [ET框架Demo启动流程梳理](https://soincredible.github.io/posts/9c5dbe31/)

ET中的SceneType, 可以这样理解: 一个由ET框架开发的游戏就是一个World, 在这个world下会有各种个样的Scene, 这些Scene扮演不同的角色, 这些角色扮演一般都是不同的职责. 在ET中,有一个名叫Demo的SceneType, 还有叫Main的

由World提供各种原材料, 供各个Scene下面的Entity使用

每一个Entity都会标记自己被创建出来的Scene
SceneType会影响Fiber的创建

ET中的Entity 有Child和Component之分