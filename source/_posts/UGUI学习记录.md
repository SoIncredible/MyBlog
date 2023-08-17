---
title: UGUI学习记录
date: 2023-08-17 14:41:01
tags:
 - UGUI
categories:
cover:
description:
swiper_index:
sticky:
---

# Canvas

Canvas组件提供了一个供UI布局和渲染的抽象空间。所有的UI元素必须是一个挂载有Canvas组件的GameObject的子。当我们创建一个UI元素的时候，如果场景中还没有一个带有Canvas的物体，那么一个Canvas的Object将会自动被创建出来。

## 属性

- Render Mode: UI渲染到屏幕上或者是作为一个Object被渲染到3D空间中的方式。共有三种选项 Screen Space - Overlay， Screen Space - Camera和World Space
- Pixel Pefect(Screen Space modes only): 为了精度UI是否应该关闭抗锯齿并渲染
- Render Camera(Screen Space - Camera mode onle): UI要被渲染到哪个Camera上
- Sort Order: 用于控制不通过Canvas之间的绘制顺序，数值越大，该canvas的绘制优先级越高，会绘制在其他Canvas的上方
- Plane Distance(Screen Space - Camera mode only): 被渲染的UI放置在渲染摄像机前的距离
- Event Camera(World Space mode only): 用来运行UI Event的Camera
- Receives Events: UIEvent是否会被该Canvas驱动？

## 细节

对于所有的UI元素单个Canvas已经是足够的了，但是在场景中放置多个Canvas也是可以的。同样地，使用嵌套Canvas也是可以的，一般我们出于优化的目的会将一个Canvas设置为另一个Canvas的子节点。嵌套Canvas的Canvas采用和父Canvas相同的渲染模式。

传统方式中，所有的UI就像它们是简单的图形设计一样被直接地渲染到了屏幕上。这就是说，在Camera的视角下，它们是没有3D空间的概念的。Unity支持这种屏幕空间的渲染并且同时支持UI作为Object被渲染到3D空间中，这取决于Render Mode的值。

## Screen Space - Overlay

这种模式下，Canvas会被缩放以适应屏幕然后在没有场景或者Camera的参照下直接渲染出来（UI会直接渲染出来，即使场景中根本就没有Camera）。如果屏幕的尺寸或者分辨率发生了改变，那么UI会自动地重新缩放来适应新的尺寸。UI会绘制在任何其他图形比如摄像机视角的上层

注意：Screen Space - Overlay模式下，挂载canvas的节点必须是在hierarchy窗口中的最上级的节点。如果没有这样使用canvas的话可能会导致UI从视野里消失。这是从Unity引擎设计上的局限，因此保持ScreenSpace - Overlay canvas在hierarchy的最顶层以保证能够获得期望的效果

## Screen Space - Camera

在这种模式下，Canvas会被渲染在一个平面的物体上，该平面距离给定的摄像机有一定的距离，这种模式下的UI显示在屏幕上的尺寸并不会随着距离而变化，因为它总是会重新缩放来适应Camera的平截头体中，如果屏幕的尺寸或者分辨率或者Camera的平截头体发生了变化，那么该UI会自动的缩放来适应改变话。任何在场景中的3D物体如果距离摄像机比该UI近的话，那么就会被渲染在该UI的前面，而在UI后面的物体就会被遮挡

## World Space

这种模式下的UI会想一个平面的物体一样被渲染在场景中，不想Screen Space - Camera一样，这种模式下的UI不需要面向摄像机并且可以朝向任何方向，这种模式下的Canvas的尺寸可以通过该节点上的RectTransform组件进行设置。而它的显示效果取决于观看角度和该UI距离Camera的距离，其他场景中的物体可以穿过该UI，或者在它的前面，或者在它的后面

# CanvasScaler
