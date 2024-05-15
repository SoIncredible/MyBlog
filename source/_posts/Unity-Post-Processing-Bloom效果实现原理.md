---
title: Unity Post Processing Bloom效果实现原理
date: 2024-05-13 07:04:40
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 前置知识

## CommandBuffer

## CameraEvent

配合CommandBuffer使用，允许我们在UnityRenderLoop的一些节点添加我们自定义的渲染操作

# PostProcess 代码框架

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

# Bloom效果

## Blomm效果实现原理
提取图片的高亮部分进行高斯模糊，将模糊后的结果叠加回原图片

采样

## Bloom着色器
```
Shader "Hidden/PostProcessing/Bloom"
{
    HLSLINCLUDE

        #include "Packages/com.unity.postprocessing/PostProcessing/Shaders/StdLib.hlsl"
        #include "Packages/com.unity.postprocessing/PostProcessing/Shaders/Colors.hlsl"
        #include "Packages/com.unity.postprocessing/PostProcessing/Shaders/Sampling.hlsl"

        TEXTURE2D_SAMPLER2D(_MainTex, sampler_MainTex);
        TEXTURE2D_SAMPLER2D(_BloomTex, sampler_BloomTex);
        TEXTURE2D_SAMPLER2D(_AutoExposureTex, sampler_AutoExposureTex);

        float4 _MainTex_TexelSize;
        float  _SampleScale;
        float4 _ColorIntensity;
        float4 _Threshold; // x: threshold value (linear), y: threshold - knee, z: knee * 2, w: 0.25 / knee
        float4 _Params; // x: clamp, yzw: unused

        // ----------------------------------------------------------------------------------------
        // Prefilter

        half4 Prefilter(half4 color, float2 uv)
        {
            half autoExposure = SAMPLE_TEXTURE2D(_AutoExposureTex, sampler_AutoExposureTex, uv).r;
            color *= autoExposure;
            color = min(_Params.x, color); // clamp to max
            color = QuadraticThreshold(color, _Threshold.x, _Threshold.yzw);
            return color;
        }

        half4 FragPrefilter13(VaryingsDefault i) : SV_Target
        {
            half4 color = DownsampleBox13Tap(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy);
            return Prefilter(SafeHDR(color), i.texcoord);
        }

        half4 FragPrefilter4(VaryingsDefault i) : SV_Target
        {
            half4 color = DownsampleBox4Tap(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy);
            return Prefilter(SafeHDR(color), i.texcoord);
        }

        // ----------------------------------------------------------------------------------------
        // Downsample

        half4 FragDownsample13(VaryingsDefault i) : SV_Target
        {
            half4 color = DownsampleBox13Tap(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy);
            return color;
        }

        half4 FragDownsample4(VaryingsDefault i) : SV_Target
        {
            half4 color = DownsampleBox4Tap(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy);
            return color;
        }

        // ----------------------------------------------------------------------------------------
        // Upsample & combine

        half4 Combine(half4 bloom, float2 uv)
        {
            half4 color = SAMPLE_TEXTURE2D(_BloomTex, sampler_BloomTex, uv);
            return bloom + color;
        }

        half4 FragUpsampleTent(VaryingsDefault i) : SV_Target
        {
            half4 bloom = UpsampleTent(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy, _SampleScale);
            return Combine(bloom, i.texcoordStereo);
        }

        half4 FragUpsampleBox(VaryingsDefault i) : SV_Target
        {
            half4 bloom = UpsampleBox(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy, _SampleScale);
            return Combine(bloom, i.texcoordStereo);
        }

        // ----------------------------------------------------------------------------------------
        // Debug overlays

        half4 FragDebugOverlayThreshold(VaryingsDefault i) : SV_Target
        {
            half4 color = SAMPLE_TEXTURE2D(_MainTex, sampler_MainTex, i.texcoordStereo);
            return half4(Prefilter(SafeHDR(color), i.texcoord).rgb, 1.0);
        }

        half4 FragDebugOverlayTent(VaryingsDefault i) : SV_Target
        {
            half4 bloom = UpsampleTent(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy, _SampleScale);
            return half4(bloom.rgb * _ColorIntensity.w * _ColorIntensity.rgb, 1.0);
        }

        half4 FragDebugOverlayBox(VaryingsDefault i) : SV_Target
        {
            half4 bloom = UpsampleBox(TEXTURE2D_PARAM(_MainTex, sampler_MainTex), i.texcoord, UnityStereoAdjustedTexelSize(_MainTex_TexelSize).xy, _SampleScale);
            return half4(bloom.rgb * _ColorIntensity.w * _ColorIntensity.rgb, 1.0);
        }

    ENDHLSL

    SubShader
    {
        Cull Off ZWrite Off ZTest Always

        // 0: Prefilter 13 taps
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragPrefilter13

            ENDHLSL
        }

        // 1: Prefilter 4 taps
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragPrefilter4

            ENDHLSL
        }

        // 2: Downsample 13 taps
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragDownsample13

            ENDHLSL
        }

        // 3: Downsample 4 taps
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragDownsample4

            ENDHLSL
        }

        // 4: Upsample tent filter
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragUpsampleTent

            ENDHLSL
        }

        // 5: Upsample box filter
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragUpsampleBox

            ENDHLSL
        }

        // 6: Debug overlay (threshold)
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragDebugOverlayThreshold

            ENDHLSL
        }

        // 7: Debug overlay (tent filter)
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragDebugOverlayTent

            ENDHLSL
        }

        // 8: Debug overlay (box filter)
        Pass
        {
            HLSLPROGRAM

                #pragma vertex VertDefault
                #pragma fragment FragDebugOverlayBox

            ENDHLSL
        }
    }
}

```

# 参考资料
[CommandBuffer官方文档](https://docs.unity3d.com/ScriptReference/Rendering.CommandBuffer.html)

[Extending the Built-in Render Pipeline with CommandBuffers](https://docs.unity3d.com/Manual/GraphicsCommandBuffers.html)

[Extending Unity 5 rendering pipeline: Command buffers](https://blog.unity.com/engine-platform/extending-unity-5-rendering-pipeline-command-buffers)