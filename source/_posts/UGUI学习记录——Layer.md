---
title: UGUI学习记录——Layer
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
