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