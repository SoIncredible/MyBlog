---
title: Unity渲染顺序
abbrlink: fff34a27
date: 2024-04-29 08:57:16
tags:
categories:
cover: https://www.notion.so/images/page-cover/woodcuts_10.jpg
description:
swiper_index:
sticky:
---

Unity中一个场景下的所有物体，不论是UI、2D物体还是3D物体，都是在一个三维空间中进行渲染的，然后我们透过场景中的一个或多个摄像机观察场景中物体渲染后的遮挡关系。当我们看到物体之间存在某种遮挡关系时，我们一般会直观地认为这是由于不同物体与摄像机之间的距离不同导致的。Unity确实会根据物体距离摄像机的距离来决定渲染结果，除此之外Unity中还提供了其他方式来影响渲染结果，进而改变物体之间的遮挡关系，比如对于两个Canvas物体，我们可以设置离摄像机远的Canvas的SortingOrder大于离摄像机近的Canvas的SortingOrder，然后我们就会看到离摄像机远的Canvas反而渲染在了离摄像机近的Canvas上面。本文记录笔者个人对Unity中渲染顺序的分析理解。

# Canvas组件
Canvas组件具有一个名为RenderMode属性
ScreenSpace-Overlay模式下最简单 但是局限性也比较高 不能处理粒子和UI的层级关系
ScreenSpace-Camera 模式可以处理粒子和UI的层级关系
WorldSpace 


# 渲染顺序
所有需要显示的物体都会有Renderer组件，MeshRenderer、SpriteRenderer、CanvasRenderer、PaticleSystemRenderer
SortingOrder SortingLayerID


# 摄像机的Size
找到场景中的MainCamera节点上的Camera组件，设置组件上的Projection属性为`Orthographic`，Camera上的就出现了Size属性，Size定义了透过该摄像机可以看到的视野高度的一半。
CanvasScaler和CameraSize搭配使用
Canvas只要不是ScreenOverLay模式， 就可以将Canvas看作一个3D场景中的物体，该物体和场景中的其他物体距离摄像机的远近不同，会直接影响最后的显示效果

Canvas下的所有组件都相当于是在一个新的坐标空间下进行计算，将Canvas中的元素由Canvas坐标空间转换到世界坐标空间下时需要有一个比例换算
粒子系统会经常和UI系统放在一起使用，但是由于UI的重叠等原因 粒子经常会出现显示层级不正确的问题，因此还需要手动去管理粒子和UI的层级

# SortingLayer

# 粒子和UI元素的渲染顺序

距离摄像机的距离、LayerID、OrderInLayer和粒子UI的渲染顺序对最终屏幕上显示效果的影响
在Unity默认的渲染管线下同LayerID、同OrderInLayer的UI和粒子是先渲染粒子还是先UI

# CanvasScaler

按照标准分辨率来设计的 

Unity粒子的Render和Canvas的OrderInLayer是一样的 用来处理渲染层级的问题

常规模型和粒子满足谁距离摄像机近谁后渲染
在Unity中创建一个场景，创建两个Image，然后创建一个Sphere，将Sphere放置在两个Image中间，会发生奇怪的事情
UI和常规模型满足谁距离摄像机近谁后渲染
UI和粒子就不满足谁距离摄像机近谁后渲染 为什么？ 给粒子系统挂载RectTransform就会这样
移除RectTransform后就没有问题了 RectTransform改变了什么？
RectTransform组件上的Pos Z字段可以设置游戏对象的Z轴位置，但是不影响显示顺序，在同一个Canvas画布中，游戏对象的显示顺序由Hierarchy视图中的顺序决定
即便是在不同的Canvas中想通过调整PosZ字段也不能影响显示顺序 处理不同Canvas的显示顺序是通过修改Canvas的 OrderInLayer值来实现的


疑惑场景
两个Canvas，每个Canvas下面都有一个Image组件，Hierarchy窗口中Canvas1在Canvas2上面，无论如何调整Canvas1和Canvas2的PosZ属性，只要不动Canvas的OrderInLayer属性，Canvas2就一直会显示在Canvas1上面
Canvas1
Canvas2

如果在场景中创建一个Sphere 3D物体，物体上挂载的是Transform不是RectTransform，调整Sphere、Canvas1、Canvas2的PosZ属性使得距离摄像机有远到近的关系为：Canvas2、Sphere、Canvas1，会发现Sphere会潜入Canvas2中，而Canvas1本应该被Canvas2盖住，但是现在也会在Sphere遮挡Canvas2的区域中显示出来，原因是什么？


就算只有一个Canvas 下添加两个Image组件，Hierarchy中Image1在Image2的上面 就算Image1的PosZ比Image2的PosZ要小 Image1还是应该被Image2挡住，但是将Sphere放在两个Image中间的时候也会出现上述情况，为什么？

RectTransform的PosZ不能用来处理UI元素之间的层级关系 但是可以用来处理UI和3D物体之间的层级关系

Unity是如何渲染UI的？

# Unity的渲染顺序
如果开启了深度测试永远都是离摄像机近的物体该在离摄像机远的物体之前



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

Canvas Scaler组件用来控制UI元素在Canvas上的整体缩放和像素密度。这个组件会影响所有的位于该Canvas组件下的UI元素，包括字体大小和图片的边界

## 属性

- UIScaleMode:  决定在该Canvas上的UI元素该如何进行缩放
  - ConstantPixelSize: 让UI元素保持相同的像素不论屏幕的尺寸
    - Scale Factor: 该Canvas中的所有UI元素的缩放受该factor影响
    - ReferencePixelsPerUnit: 如果一个sprite进行了Pixel Per Unity的设置，那么在该sprite中的一个像素将会覆盖一个单元
  - ScaleWithScreenSize: 屏幕的尺寸越大，UI元素也越大
    - Reference Resolution: UI布局是基于此分辨率设计的，如果屏幕分辨率更大，那么UI会被放大，反之UI会被缩小
    - ScreenMatchMode: 一种模式用来适应不同分辨率UI的缩放方式
      - Match Width or Height: 如果设备的分辨率和默认分辨率不匹配的话，那么是保证宽和原分辨率成比例还是高与原分辨率成比例
      - Expand: 
      - Shrink: 
    - Match: 决定
    - Reference Pixels Per Unit
  - ConstantPhysicalSize: 让UI元素保持同样的物理尺寸的大小而不管屏幕的尺寸和分辨率
    - Physical Unit:
    - Fallback Screen DPI
    - Default Sprite DPI
    - Reference Pixels Per Unit
- 当Canvas的RenderMode被设置为WorldSpace的时候
  - Dynamic Pixels Per Unit： 表示UI中动态创建的位图，比如Text，中单个单元的像素数量
  - Reference Pixels Per Unit:  如果一个sprite有这个Pixels Per Unity的设置，那么在Sprite中的一个像素将会覆盖世界场景中韩的一个单元，如果Reference Pixel Per Unit 被设置为了1，那么在sprite中的Pixels Per Unit设置将会被直接使用

## 细节

对于被设置为Screen Space - Overlay 或者 Screen Space - Camera 的 Canvas，Canvas Scaler UI 的 Scale Mode 可以被设置为Constant PixelSize， Scale With Screen 或者 Constant Physical Size

## Constant Pixel Size

## Scale With Screen Size

## Constant Physical Size

## World Space

# CanvasGroup

 CanvasGroup用来控制某些方面

# CanvasRenderer

Canvas Renderer 组件将一个Canvas中的一个图像UI渲染出来，该组件需要挂载在每一个需要显示在屏幕上的UI组件上，Canvas Renderer组件没有任何属性暴露在inspector窗口中

## 细节

在Unity的菜单中可以创建的标准的UI组件上不论你需不需要都挂载有Canvas Renderer组件，但是当我们开发我们自定义的UI组件的时候，我们需要手动将该组件挂载到自定义UI上，尽管在Inspector窗口中没有任何的属性，但是有一些属性和功能还是可以通过代码的形式来控制

# GraphicRaycaster




# 参考文章
[Unity渲染顺序](https://zhuanlan.zhihu.com/p/473875401)
[UI和模型的层级处理](https://zhuanlan.zhihu.com/p/673810066)
[Rendering Order In Build-in Render Pipeline](https://docs.unity3d.com/2022.3/Documentation/Manual/built-in-rendering-order.html)
[2D Sorting](https://docs.unity3d.com/2021.3/Documentation/Manual/2DSorting.html)
[Unity CanvasScaler](https://docs.unity3d.com/Packages/com.unity.ugui@2.0/manual/script-CanvasScaler.html)