---
title: Unity学习记录——Raycasters
abbrlink: 5823067b
date: 2023-08-17 17:55:02
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

事件系统需要一个方法用来检测当前输入的事件需要被发送到什么地方，并且这是由，通过点击屏幕来获得一个潜在的位置，找出是否这些物体在被给的这个位置上，然后返回最靠近屏幕（摄像机的物体信息），有一下三种主要的Raycaster被提供

- Graphic Raycaster: 应用于UI
- Physics 2D Raycaster: 应用于2D物体
- Physics Raycaster: 应用于3D物体
