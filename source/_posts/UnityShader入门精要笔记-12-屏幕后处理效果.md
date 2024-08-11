---
title: UnityShader入门精要笔记（十二）——屏幕后处理效果
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


```
using System;
using UnityEngine;

public class BrightnessSaturationAndContrast : PostEffectsBase
{
    public Shader briSatConShader;
    public Material briSatConMat;
    
    private Material _material
    {
        get
        {
            briSatConMat = CheckShaderAndCreateMaterial(briSatConShader, briSatConMat);
            return briSatConMat;
        }
    }
    
    [Range(0.0f, 3.0f)] public float brightness = 1.0f;
    [Range(0.0f, 3.0f)] public float saturation = 1.0f;
    [Range(0.0f, 3.0f)] public float contrast = 1.0f;


    private void OnRenderImage(RenderTexture source, RenderTexture destination)
    {
        if (_material != null)
        {
            _material.SetFloat("_Brightness", brightness);
            _material.SetFloat("_Saturation", saturation);
            _material.SetFloat("_Contrast", contrast);
            
            Graphics.Blit(source, destination, _material);
        }
        else
        {
            Graphics.Blit(source, destination);
        }
    }
}
```

```
Shader "UnityShaderBook/Chapter 12/BrightnessSaturationAndContrast"
{
    Properties
    {
        _MainTex ("MainTex", 2D) = "white" {}
        _Brightness ("Brightness", Float) = 1
        _Saturation ("Saturation", Float) = 1
        _Contrast ("Contrast", Float) = 1
    }
    
    SubShader
    {
        Tags
        {
            
        }
        
        Pass
        {
            ZTest Always
            Cull Off
            ZWrite Off

            CGPROGRAM

            #pragma vertex vert;
            #pragma fragment frag;

            #include "UnityCG.cginc"
            
            sampler2D _MainTex;
            half _Brightness;
            half _Saturation;
            half _Contrast;

            struct v2f
            {
                float4 pos : SV_POSITION;
                half2 uv : TEXCOORD0;
            };

            v2f vert(appdata_img v)
            {
                v2f o;
                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv = v.texcoord;
                return o;
            }

            fixed4 frag(v2f i) : SV_Target{
                fixed4 renderTex = tex2D(_MainTex, i.uv);

                fixed3 finalColor = renderTex.rgb * _Brightness;

                fixed luminance = 0.2125 * renderTex.r + 0.7154 * renderTex.g + 0.0721 * renderTex.b;
                fixed3 luminanceColor = fixed3(luminance, luminance, luminance);
                finalColor = lerp(luminanceColor, finalColor, _Saturation);

                fixed3 avgColor = fixed3(0.5, 0.5, 0.5);
                finalColor = lerp(avgColor, finalColor, _Contrast);

                return fixed4(finalColor, renderTex.a);
            }
            
            ENDCG
        }   
    }
}
```

首先，我们得到对原屏幕图像（存储在_MainTex）中的采样结果renderTex。然后，利用_Brightness属性来计算来调整亮度。亮度的调整非常简单，我们只需要把原颜色乘以亮度系数_Brightness即可。然后，我们计算该像素值对应的亮度值（luminance），这是通过对每个颜色分量乘以一个特定的系数
# 边缘检测

边缘检测的原理是利用一些边缘检测算子对图像进行`卷积(convolution)`操作，我们首先来了解一下什么是卷积。

## 什么是卷积

在图像处理中，卷积操作指的就是使用一个`卷积核(kernel)`对一张图片中的每个像素进行一系列的操作。卷积核通常是一个四方形的网格结构(比如 $2 \times 2$、$3 \times 3$的方形区域)，该区域内每个方格都有一个权重值。当对图像中某个像素进行卷积时，我们会把卷积核的中心放置于该像素上，翻转核之后再以此计算核中每个元素和其覆盖的图像像素值的乘积并求和，得到的结果就是该位置的新像素值。

这样的计算过程虽然简单，但是可以实现很多常见的图像处理效果，例如图像模糊、边缘检测等。例如，如果我们想要对图像进行均值模糊，可以使用一个$3 \times 3$的卷积核，核内每个元素的值均为1/9。

## 常见的边缘检测算子

卷积操作的神奇之处在于选择的卷积。那么，用于边缘检测的卷积核应该长什么样呢？在回答这个问题前，我们可以先回想一下边到底是如何形成的。如果相邻像素之间存在差别明显的颜色、亮度、纹理等属性，我们就会认为它们之间应该有一条边界。这种相邻像素之间的差值可以用梯度(gradient)来表示，可以想象到，边缘处的梯度绝对值会比较大。

3种常见的边缘算子如图所示，它们都包含了两个方向的卷积核，分别用于检测水平方向和竖直方向上的边缘信息。在进行边缘检测时，我们需要对每个像素分别进行一次卷积计算，得到两个方向上的梯度值$G_{x}$和$G_{y}$ ，而整体的梯度公式可以按照下面的公式计算而得：
$$G=\sqrt{G^2_{x} + G^2_{y}}$$

由于上面的计算包含了开根号的操作，出于性能考虑。我们有时会使用绝对值操作来代替开根号操作:
$$G = |G_{x}| + |G_{y}|$$

当我们得到梯度G后，我们就可以据此来判断哪些像素对应了边缘（梯度值越大越有可能是边缘点）。

# 高斯模糊

# Bloom效果

Bloom效果是游戏中常见的一种效果。这种特效可以模拟真实摄像机的一种图像效果，它让画面中较亮的区域“扩散”到周围区域中，造成一种朦胧的效果。

Bloom效果的实现原理非常简单：我们首先根据一个阈值提取出图像中较亮的区域，把它们存储到一张渲染纹理中，再利用高斯模糊对这样渲染纹理进行模糊处理，模拟光线扩散的效果，最后再将其和原图像混合，得到最终的效果。


# 运动模糊

运动模糊是真实世界中的摄像机的一种效果。如果在摄像机曝光时，拍摄场景发生了变化，就会产生模糊的画面。运动模糊在我们的日常生活中是非常常见的，只要留心观察，就可以发现无论是体育报道还是各个电影中，都有运动模糊的身影。运动模糊的效果可以让物体运动起来更加真实平滑，但在计算机产生图像的过程中，由于不存在曝光这一物理现象，渲染出来的图像往往都是棱角分明，缺少运动模糊。在一些注入赛车类型的游戏中，为画面添加运动模糊是一种常见的处理方法。在这一节中，我们将学习如何在屏幕后处理中实现运动模糊的效果。

运动模糊的实现有很多种方法。一种实现方法是利用一块累计缓存(accumulation buffer)来混合多张连续的图像。当物体快速移动产生多张图向后，我们取它们之间的平均值作为最后的运动模糊图像。然而这种暴力的方法对性能的消耗很大，因为想要获得多张帧图像往往意味着我们要在同一帧内渲染多次场景。另一种应用广泛的方法是创建和使用速度缓存(velocity buffer)，这个缓存种存储了各个像素当前的运动速度，然后利用该值来决定模糊的方向和大小