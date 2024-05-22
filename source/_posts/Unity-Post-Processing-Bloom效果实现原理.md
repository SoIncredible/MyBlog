---
title: 通过Bloom效果了解Unity Post Processing代码框架
date: 2024-05-13 07:04:40
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 前置知识

## 从GPU如何将数据绘制到屏幕上说起

### Frame Buffer




### Render Buffer


## 从Unity Render Pipeline

Render Pipeline中包括三个主要步骤

1. Culling
2. Rendering
3. PostProcessing

后处理将场景渲染到一个或多个Render Texture上，而不是直接渲染到屏幕上，后续的后处理效果都会在这一个或者多个Render Texture上进行


## CommandBuffer

## CameraEvent

配合CommandBuffer使用，允许我们在UnityRenderLoop的一些节点添加我们自定义的渲染操作

# Post Processing后处理

后处理值得是在渲染完整个场景得到屏幕图像后，再对这个图像进行一系列的操作，实现各种屏幕特效。
要实现后处理，需要用到Unity提供的接口：OnRenderImage函数，这个函数的调用时机是在所有的Shader中不透明和透明的Pass全部执行完毕后调用的

# Bloom效果

Bloom效果模拟真实摄像机，让图片中的较亮区域“扩散”到周围区域，造成一种朦胧的效果。

## Bloom效果实现原理

Bloom的实现原理比较简单：首先根据一个阈值提取出图像中较亮的部分，把它们存储在一张渲染纹理(RenderTexture)中；再利用一些模糊算法对这张纹理进行模糊处理，模拟光线扩散的效果；最后再将其和原图像进行混合，得到最终的效果。

## Bloom效果实现

### Bloom的Shader实现

上文提到实现Bloom效果需要三个环节：提取高亮、进行模糊、与原图像混合。那么我们就要在Shader中去编写三个Pass分别处理这三个阶段。

**阶段一：提取图片高亮部分**



**阶段二：将高亮部分模糊处理**



**阶段三：将模糊处理后的图像与原图混合**



### 提取高亮部分 结合Shader入门精要



Shader部分
```

```




# PostProcess 代码框架

整个代码框架包括

整个后处理都是基于CommandBuffer来实现的，也就是说后处理通过CommandBuffer，自定义Camera不同绘制阶段的行为，来达到效果实现
在Unity中，渲染管线负责处理所有的图形渲染任务，从场景中的对象到最终的图像输出。内置渲染管线是Unity提供的一种标准的渲染流程，它允许开发者通过编写脚本和着色器来定制和扩展其功能。而CommandBuffer正是内置渲染管线中的一个强大工具，用于在渲染过程中插入自定义的渲染命令。
OnPreRender
OnPreCull
OnRenderImage

后效通过Camera.AddCommandBuffer接口自定义要在整个Camera渲染过程中插入的阶段
Graphics.Blit()方法


后效自定义了如下几个Camera渲染阶段

```
m_Camera.AddCommandBuffer(CameraEvent.BeforeReflections, m_LegacyCmdBufferBeforeReflections);
m_Camera.AddCommandBuffer(CameraEvent.BeforeLighting, m_LegacyCmdBufferBeforeLighting);
m_Camera.AddCommandBuffer(CameraEvent.BeforeImageEffectsOpaque, m_LegacyCmdBufferOpaque);
m_Camera.AddCommandBuffer(CameraEvent.BeforeImageEffects, m_LegacyCmdBuffer);
```

理清后处理的关键线索 **PostProcessRenderContext**: A context object passed around all post-processing effects in a frame.
数据成员


基本流程
先看到PostProcessLayer
在每一帧的OnPreCull和OnPreRender中都会调用BuildCommandBuffers但是本文只讨论在Androud、MacOS和Windows平台上的Bloom效果的原理 OnPreRender方法中的BuildCommandBuffer判不过去


# 参考资料
[Unity PostProcessing后处理官方文档]()

[Unity Render Pipeline官方文档](https://docs.unity3d.com/Manual/render-pipelines-overview.html)

[CommandBuffer官方文档](https://docs.unity3d.com/ScriptReference/Rendering.CommandBuffer.html)

[Extending the Built-in Render Pipeline with CommandBuffers](https://docs.unity3d.com/Manual/GraphicsCommandBuffers.html)

[Extending Unity 5 rendering pipeline: Command buffers](https://blog.unity.com/engine-platform/extending-unity-5-rendering-pipeline-command-buffers)