---
title: UnityShader入门精要笔记-12-屏幕后处理效果
abbrlink: a07e7969
date: 2024-07-31 23:13:20
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

屏幕后处理效果是游戏中实现屏幕特效的常见方法。在本章中我们将学习如何在Unity中利用渲染纹理来实现各种常见的屏幕后处理效果。我们首先要建立一个基本的屏幕后处理脚本系统，随后我们会使用这个系统实现一个简单的调整画面亮度、饱和度和对比度的屏幕特效。然后我们会接触到图像滤波的概念，并利用Sobel算子在屏幕空间中对图像进行边缘检测，实现描边效果。在此基础上，我们会实现一个高斯模糊的屏幕特效、Bloom和运动模糊的效果。


# 建立一个基本的屏幕后处理脚本系统




```
using UnityEngine;

[ExecuteInEditMode]
[RequireComponent (typeof(Camera))]
public class PostEffectsBase : MonoBehaviour {

    // Called when start
    protected void CheckResources() {
        bool isSupported = CheckSupport();
		
        if (isSupported == false) {
            NotSupported();
        }
    }

    // Called in CheckResources to check support on this platform
    protected bool CheckSupport() {
        return true;
    }

    // Called when the platform doesn't support this effect
    protected void NotSupported() {
        enabled = false;
    }
	
    protected void Start() {
        CheckResources();
    }

    // Called when need to create the material used by this effect
    protected Material CheckShaderAndCreateMaterial(Shader shader, Material material) {
        if (shader == null) {
            return null;
        }
		
        if (shader.isSupported && material && material.shader == shader)
            return material;
		
        if (!shader.isSupported) {
            return null;
        }
        else {
            material = new Material(shader);
            material.hideFlags = HideFlags.DontSave;
            if (material)
                return material;
            else 
                return null;
        }
    }
}
```


# 调整屏幕的亮度、饱和度和对比度


# 边缘检测

# 高斯模糊

# Bloom效果

Bloom效果是游戏中常见的一种效果。这种特效可以模拟真实摄像机的一种图像效果，它让画面中较亮的区域“扩散”到周围区域中，造成一种朦胧的效果。

Bloom效果的实现原理非常简单：我们首先根据一个阈值提取出图像中较亮的区域，把它们存储到一张渲染纹理中，再利用高斯模糊对这样渲染纹理进行模糊处理，模拟光线扩散的效果，最后再将其和原图像混合，得到最终的效果。