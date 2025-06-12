---
title: UnityEditor开发中Singleton的设计
date: 2025-06-11 16:14:28
tags: 
categories: UnityEditor开发
cover:
description:
swiper_index:
sticky:
---

# 在Editor下创建单例

灵感来自Unity的[MonKey插件](https://sites.google.com/view/monkey-user-guide/getting-started)

## 使用场景

我有一些由`ScriptableObject`、`json`或者别的格式组织的一组数据, 我希望在Editor模式下开发一套工具, 能让我对这一组数据进行增删改查, 根据MVC架构的思想, 必须有一个`Manager`来维护这一组数据的增删改查, 并且该`Manager`的生命周期需要和Unity Editor的生命周期保持一致.

## 关键角色

- [`InitializeOnLoad`](https://docs.unity3d.com/ScriptReference/InitializeOnLoadAttribute.html)属性, 根据官方的描述, 该属性会在UnityEditor打开和代码重新编译的时候初始化Editor脚本. 你需要给这个Editor脚本提供一个静态的构造函数, **注意** 官方提到了 应该避免在InitialzeOnLoad中进行资产加载的操作, 因为InitialzeOnLoad是在资产导入完成前被调用的, 该操作有可能会导致资产加载失败. 要在需要进行资产操作的域重载后进行初始化, 可以使用`AssetPostprocessor.OnPostprocessAllAssets`回调. 但Unity2020版本中是没有这个接口的, 该回调支持所有资产操作, 并有一个参数提示是否进行了域重载. Unity不能保证 因此使用懒汉模式, 将在初始化操作中不能加载资源的操作变为了在初始化的过程中不能访问Instance的操作, 将加载资源的操作放在首次访问Instance之后 对于一个Editor单例, 如果该单例需要做一些加载操作, 那么就让这个单例实现IAssetProcess接口,

EditorSingleton的实现
```C#
```