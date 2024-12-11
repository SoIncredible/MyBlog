---
title: 基于CommandBuffer的Bloom效果的实现
abbrlink: 257b5b2c
date: 2024-05-13 07:04:40
tags: 
    - Shader
    - 
categories: 硬技能
cover: https://www.notion.so/images/page-cover/met_edgar_degas_1874.jpg
description:
swiper_index:
sticky:
---

# 背景

最近在配合动效同事使用后处理技术实现Bloom辉光的效果。首先如果要实现Bloom效果是必须开启HDR的，开启HDR之后内存里会增加两个RenderTexture，并且随着屏幕分辨率的增加这两个Render Texture会越来越大，这是无法避免的一部分开销。一开始我们使用的是Unity官方提供的兼容Build-in渲染管线的[Post Processing stack V2](https://docs.unity3d.com/Packages/com.unity.postprocessing@3.4/manual/index.html)后处理插件，这个插件十分强大，除了Bloom效果还能实现很多其他的后处理效果。但同时这个插件的开销实在是太大了，在使用了这个插件之后我们只是为了实现一个Bloom效果而造成这么大的内存效是得不偿失的。比起我们要实现的Bloom效果，使用这个插件造成的开销是无法接受的。
为了做优化打出包来做了性能对比分析，发现整个插件造成的主要开销在内存占用上。
在网上找到了一个Bloom插件，但是插件已经比较老了，还是基于Graphics.Blit的方式实现的后处理效果，因此笔者在此插件的基础上实现了基于CommandBuffer的Bloom效果。

# 前置知识

## 大概方案

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
另外考虑到移动平台的性能瓶颈，因此使用一些可选的操作项

**阶段一：提取图片高亮部分**



**阶段二：将高亮部分模糊处理**



**阶段三：将模糊处理后的图像与原图混合**



### 提取高亮部分 结合Shader入门精要


首先获得相机的屏幕纹理





对SetGlobalTexture的理解
CommandBuffer和Shader类中都可以执行一个SetGlobalTexture的方法，这个方法的使用还是要说一下

使用SetGlo















# 参考资料
[Unity PostProcessing后处理官方文档]()

[Unity Render Pipeline官方文档](https://docs.unity3d.com/Manual/render-pipelines-overview.html)

[CommandBuffer官方文档](https://docs.unity3d.com/ScriptReference/Rendering.CommandBuffer.html)

[Extending the Built-in Render Pipeline with CommandBuffers](https://docs.unity3d.com/Manual/GraphicsCommandBuffers.html)

[Extending Unity 5 rendering pipeline: Command buffers](https://blog.unity.com/engine-platform/extending-unity-5-rendering-pipeline-command-buffers)