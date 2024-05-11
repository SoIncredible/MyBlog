---
title: Unity渲染顺序
date: 2024-04-29 08:57:16
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

Unity中一个场景下的所有物体，不论是UI、2D物体还是3D物体，都是在一个三维空间中进行渲染的，然后我们透过场景中的一个或多个摄像机观察场景中物体渲染后的遮挡关系。当我们看到物体之间存在某种遮挡关系时，我们一般会直观地认为这是由于不同物体与摄像机之间的距离不同导致的。Unity确实会根据物体距离摄像机的距离来决定渲染结果，除此之外Unity中还提供了其他方式来影响渲染结果，进而改变物体之间的遮挡关系，比如对于两个Canvas物体，我们可以设置离摄像机远的Cavas的SortingOrder大于离摄像机近的Canvas的SortingOrder，然后我们就会看到离摄像机远的Canvas反而渲染在了离摄像机近的Canvas上面。本文记录笔者个人对Unity中渲染顺序的分析理解。

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





# 参考文章
[Unity渲染顺序](https://zhuanlan.zhihu.com/p/473875401)
[UI和模型的层级处理](https://zhuanlan.zhihu.com/p/673810066)
[Rendering Order In Build-in Render Pipeline](https://docs.unity3d.com/2022.3/Documentation/Manual/built-in-rendering-order.html)
[2D Sorting](https://docs.unity3d.com/2021.3/Documentation/Manual/2DSorting.html)
[Unity CanvasScaler](https://docs.unity3d.com/Packages/com.unity.ugui@2.0/manual/script-CanvasScaler.html)