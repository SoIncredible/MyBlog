---
title: Unity事件更新顺序
abbrlink: 11d7edcb
date: 2025-04-19 06:33:36
tags:
categories:
cover: https://public.ysjf.com/mediastorm/material/material/%E8%87%AA%E7%84%B6%E9%A3%8E%E5%85%89_%E6%9C%9F%E6%9C%AB%E7%9A%84%E5%BB%B6%E6%97%B6_08_%E5%85%A8%E6%99%AF.jpg
description:
swiper_index:
sticky:
---
# Unity事件执行顺序图

[](https://docs.unity3d.com/cn/2022.3/uploads/Main/monobehaviour_flowchart.svg)

# Awake、OnDestroy、OnEnable、OnDisable四者执行时机梳理
`OnEnable`方法在AddComponent、**跟随预制体实例化(这里要十分注意挂载该脚本的GameObj的avtive状态要是true的情况下才会触发!)**、在代码中手动设置enable为true的时候会调用.
`OnDisable`方法是跟随预制体被删除、在代码中手动设置enable为false的时候会调用.

`Awake`方法在AddComponent、**跟随预制体实例化的时候会调用(这里要十分注意挂载该脚本的GameObj的avtive状态要是true的情况下才会触发!)**. 整个组件的生命周期只会调用一次
`OnDestroy`方法在跟随预制体被删除的时候调用. 整个组件的生命周期只会调用一次 **注意,如果在这个预制体被实例化到销毁的这一生命周期内,挂载该脚本的节点的active一直都是false,那么就不会触发Awake,也不会触发OnDestroy,只要该节点被active过,即便被销毁的时候该节点处在not active状态,也会触发OnDestroy**


- ExecuteAlways Property的使用 https://blog.csdn.net/alexhu2010q/article/details/105437083

- `yield WaitForEndOfFrame`是在干嘛?

在非PlayMode下,有哪些生命周期内的方法会被执行?
在Unity开发过程中，我们经常需要在编辑器启动时或脚本重新编译后执行一些操作，例如初始化数据、注册事件等。这时，我们可以使用InitializeOnLoad特性来实现这一需求。本文将详细介绍InitializeOnLoad特性的用法，并通过三个实际案例来展示其应用场景。

OnValidate方法是一个仅限编辑器的函数，在Unity加载脚本或检查器中的值更改时调用。它的调用时机非常特殊，这里总结一下。

1. OnValidate不受播放模式影响，只要其值发生变化，在非播放状态下也会被调用（可以用于非播放模式修改参数后更新）。

2. 不受enabled状态影响，即使其所在的脚本被禁用，修改值时也会被正常调用。

3. 更改脚本enabled状态时，会调用一次OnValidate。如果在Play Mode，OnValidated的调用时机在OnDisable或者OnEnable前。

4. 更改GameObject active状态不会调用OnValidate，只有OnDisable和OnEnable会被调用。

5. 初始加载时，无论enabled状态和active状态如何，都会被调用多次。其调用时机在Awake之前。

常见用法：

1. 用于Play Mode下修改参数值，实时查看效果

2. 实时更新资产文件，比如材质、shader等

# 参考资料
- https://blog.csdn.net/angry_youth/article/details/117469722