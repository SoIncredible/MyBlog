---
title: Unity问题杂记
tags: 
  - Unity
categories: 学习笔记
abbrlink: 127bc3c9
date: 2022-12-19 18:13:50
cover: https://www.notion.so/images/page-cover/met_joseph_hidley_1870.jpg
description: 
---

> 本篇博客记录笔者在Unity开发中遇到的各种小问题，有可能是Unity奇怪的bug、Unity Editor的使用小技巧或者是一些不值得开一篇新博客的小知识点

# Unity Scriptable Object踩坑

下面是一个名为`EntityBaseProperty.cs`的脚本，但是在创建一个`PlayerEntityProperty`SO的时候会提示你找不到这个脚本。你必须让脚本的名字和SO的类名保持一致，所以每一个SO都要新建一个和SO类命一样的脚本来写。
```
public class EntityBaseProperty : ScriptableObject
{
    
}

[CreateAssetMenu(fileName = "PlayerEntityProperty", menuName = "CreatePlayerEntityProperty", order = 0)]
public class PlayerEntityProperty : CharacterProperty
{
    
}

```

# Unity项目代码正常运行但是在Rider中大量报红

Rider的`solution wide analysis`组件有问题，在Unity的`Settings -> External Tools -> Regenerate project files`解决。


# AssetDataBase接口使用遇到的坑

`AssetDatabase.Refresh`方法用于刷新Unity编辑器的资产数据库。这个方法会同步磁盘和Unity编辑器之间的资产状态，包括添加、删除、修改文件等。如果你在Unity编辑器外部（比如在文件浏览器中或通过脚本）对项目中的文件进行了更改，使用AssetDatabase.Refresh可以让Unity编辑器识别这些更改。

`AssetDatabase.SaveAssets`方法用于将所有未保存的资产更改持久化到磁盘。这包括对预制体、场景、材质等任何在编辑器中做出的更改。如果你在脚本中修改了任何资产（比如更改了一个材质的颜色，或者添加了一个新的游戏对象到一个预制体中），并且想要确保这些更改被保存，就需要调用这个方法。

1. 脚本中修改完资源里面的属性的时候，调用一次AssetDataBase.Save() 必须要在AssetDataBase.Load()方法之间调用，不然修改就白做了
2. File接口相关的操作Directory相关的操作后必须后面跟一个AssetDataBase.Refresh调用

# GetComponentsInChildren接口

GetComponentsInChildren方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# GetComponentInParent接口

GetComponentInParent方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false