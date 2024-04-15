---
title: UGUI学习记录——Layer
abbrlink: 3f14f139
date: 2023-08-17 17:20:56
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# Unity的渲染规则

先渲染的会被后渲染的覆盖，仅当前一个条件相同时，才会比较下一个条件

- 物体的SortingLayer，根据Project Setting - Tags & Layers 中的顺序，越靠上的SortingLayer越先渲染
- 物体的OrderInLayer。数字越小越先渲染
- 物体距离摄像机的距离，距离越远的越先渲染，在2D游戏中（默认设置下摄像机朝向z轴正方向时），这通常等价于z坐标，越大的越先渲染

Unity中的Layer允许你将场景中的物体分开，你可以在UI中或者在脚本中运用Layer来驱动你场景中的物体是如何彼此交互的。

# Unity中Layers的使用

你可以使用Layer来优化你的项目和工作流。Layer通常的使用场景有：基于Layer的渲染，基于Layer的碰撞

## 摄像机遮罩剔除结合layers使用

如果你使用了摄像机的遮罩剔除，你可以只渲染特定layer或者一组选定layer中的物体。如果要更改遮罩剔除，选择你想要使用的camera，在inspector窗口的Camera组件上会看到一个CullingMask的下拉框，如果在下拉框中你没有选中任何的layer，那么该摄像机不会渲染任何场景中的物体

注意：当一个Canvas被设置为Screen Space - Overlay模式，那么位于该Canvas下的所有UI元素在Culling Mask没有选中任何layer时仍然会被渲染出来，如果是Screen Space - Camera模式的话，仍然什么都不会被渲染出来



## Layer和LayerMask



## Ray cast 和 Layers

