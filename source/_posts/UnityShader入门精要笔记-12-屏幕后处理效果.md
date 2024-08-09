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

# 高斯模糊

# Bloom效果

Bloom效果是游戏中常见的一种效果。这种特效可以模拟真实摄像机的一种图像效果，它让画面中较亮的区域“扩散”到周围区域中，造成一种朦胧的效果。

Bloom效果的实现原理非常简单：我们首先根据一个阈值提取出图像中较亮的区域，把它们存储到一张渲染纹理中，再利用高斯模糊对这样渲染纹理进行模糊处理，模拟光线扩散的效果，最后再将其和原图像混合，得到最终的效果。