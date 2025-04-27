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

在开始学习Unity渲染顺序之前, 我们最好对Unity事件执行顺序有一定的了解, 也就是下面这张图

# 如何理解UI的渲染 

# 如何理解Mesh的渲染

无论UI还是模型, 都是要先更新顶点的位置等信息, 然后再渲染出来, 实际渲染都是在RenderObject的时候, UI的顶点信息更新也是知道的, 模型顶点等数据的更新在什么时候呢?
除了UI以外, 普通的模型, 每一帧里面模型的顶点也都会发生变化, 那模型的

Unity中一个场景下的所有物体，不论是UI、2D物体还是3D物体，都是在一个三维空间中进行渲染的，然后我们透过场景中的一个或多个摄像机观察场景中物体渲染后的遮挡关系。当我们看到物体之间存在某种遮挡关系时，我们一般会直观地认为这是由于不同物体与摄像机之间的距离不同导致的。Unity确实会根据物体距离摄像机的距离来决定渲染结果，除此之外Unity中还提供了其他方式来影响渲染结果，进而改变物体之间的遮挡关系，比如对于两个Canvas物体，我们可以设置离摄像机远的Canvas的SortingOrder大于离摄像机近的Canvas的SortingOrder，然后我们就会看到离摄像机远的Canvas反而渲染在了离摄像机近的Canvas上面。本文记录笔者个人对Unity中渲染顺序的分析理解。

# Canvas组件
Canvas组件具有一个名为RenderMode属性
ScreenSpace-Overlay模式下最简单 但是局限性也比较高 不能处理粒子和UI的层级关系
ScreenSpace-Camera 模式可以处理粒子和UI的层级关系
WorldSpace 


# 渲染顺序
所有需要显示的物体都会有Renderer组件，MeshRenderer、SpriteRenderer、CanvasRenderer、PaticleSystemRenderer
SortingOrder SortingLayerID

**sortingLayer和order in layer(c#中属性名为sortingOrder)的属性只适用于Shader中没有ZWrite的，一旦Shader有进行ZTest、并且ZWrite有写入，那么Shader的ZTest优先级就是最高的反之，一旦没有ZWrite，那么控制Renderer的sortingLayer和order in layer(c#中属性名为sortingOrder)的属性就有效果**


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


# 参考文章
[Unity渲染顺序](https://zhuanlan.zhihu.com/p/473875401)
[UI和模型的层级处理](https://zhuanlan.zhihu.com/p/673810066)
[Rendering Order In Build-in Render Pipeline](https://docs.unity3d.com/2022.3/Documentation/Manual/built-in-rendering-order.html)
[2D Sorting](https://docs.unity3d.com/2021.3/Documentation/Manual/2DSorting.html)
[Unity CanvasScaler](https://docs.unity3d.com/Packages/com.unity.ugui@2.0/manual/script-CanvasScaler.html)
[关于UGUI和3D物体渲染顺序](https://blog.csdn.net/wjp494754224/article/details/105813042)