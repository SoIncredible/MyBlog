---
title: UnityShader入门精要笔记-11-让画面动起来
date: 2024-07-28 12:36:13
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


没有动画的画面往往会让人觉得很无趣，在本章中，我们将会学习如何向UnityShader中引入时间变量，以实现各种动画效果。

# Unity Shader中的内置变量(时间篇)

动画效果往往是把时间添加到一些变量的计算中，以便在时间变化时画面也可以随之变化。UnityShader提供了一些列关于时间的内置变量来允许我们方便地在Shader中访问运行时间，实现各种动画效果。下表是Unity中内置地时间变量。

| 名称            | 类型   | 描述                                                            |
| --------------- | ------ | --------------------------------------------------------------- |
| _Time           | float4 | t是自该场景加载开始所经过地时间，4个分量分别是(t/20, t, 2t, 3t) |
| _SinTime        | float4 | t是时间的正弦值，4个分量的值分别是(t/8, t/4, t/2, t)            |
| _CosTime        | float4 | t是时间的余弦值 ，4个分量分别是(t/8,t/4, t/2, t)                |
| unity_DeltaTime | float4 | dt是时间增量，4个分量的值分别是(dt, 1/dt, smoothDt, 1/smoothDt) |

在后面的章节中，我们会使用上述的时间变量来实现纹理动画和顶点动画。

# 纹理动画

纹理动画在游戏中非常广泛。尤其在各种资源都比较局限的移动平台上，我们往往会使用纹理动画来代替复杂的粒子系统等模拟各种动画效果。

## 序列帧动画

最常见的纹理动画之一就是序列帧动画。序列帧动画的原理十分简单，它像放电影一样，依次播放一系列关键帧图像，当播放速度达到一定数值时，看起来就像是一个连续的动画化。它的优点在于灵活性强，我们不需要进行任何的物理计算就可以得到非常细腻的动画效果。而它的缺点也很明显，由于序列帧中每张关键帧图像都不一样，因此要制作一张出色的序列帧纹理所需要的工程量很大。

想要实现序列帧动画，我们首先要提供一张包含关键帧图像的图像。
序列帧动画的精髓在于，我们需要在每个时刻计算下个应该播放的关键帧的位置，并对关键帧进行采样。

```
Shader "UnityShaderBook/Chapter11/ImageSequenceAnimation"
{
    Properties
    {
        _Color ("Color Tint", Color) = (1,1,1,1)
        _MainTex ("Image Sequence", 2D) = "white" {}
        _HorizontalAmount ("Horizontal Amount", Float) = 4
        _VerticalAmount ("Vertical Amount", Float) = 4
        _Speed ("Speed", Range(1, 100)) = 30
    }
    
    SubShader
    {
        Tags{
            "Queue"="TransParent"
            "IgnoreProjector"="True"
            "RenderType"="Transparent"
        }
        
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }
            
            ZWrite Off
            Blend SrcAlpha OneMinusSrcAlpha
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            #include "UnityCG.cginc"
            
            fixed4 _Color;
            sampler2D _MainTex;
            float4 _MainTex_ST;
            float _HorizontalAmount;
            float _VerticalAmount;
            float _Speed;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float2 uv : TEXCOORD0;
            };

            v2f vert(a2v v)
            {
                v2f o;
                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv = TRANSFORM_TEX(v.texcoord, _MainTex);
                return  o;
            }

            fixed4 frag(v2f i) : SV_Target{

                float time = floor(_Time.y * _Speed);
                float row = floor(time / _HorizontalAmount);
                float column = time - row * _HorizontalAmount;

                half2 uv = i.uv + half2(column, -row);
                uv.x /= _HorizontalAmount;
                uv.y /= _VerticalAmount;

                fixed4 c = tex2D(_MainTex, uv);

                c.rgb *= _Color;
                
                return c;
            }
            
            ENDCG
        }
    }

Fallback "Transparent/VertexLit"

}
```

由于序列帧图像通常是透明纹理，我们需要设置Pass的相关状态，以渲染透明效果，由于序列帧图像通常包含了透明通道，因此可以被当成是一个半透明对象，在这里我们使用半透明的“标配”来设置它的SubShader标签，即把Queue和RenderType设置成Transparent，把IgnoreProjector设置为True。在Pass中，我们使用Blend命令来开启并设置混合模式，同时关闭了深度写入。

顶点着色器的代码十分简单，我们进行了基本的顶点变换，并把顶点纹理坐标存储到了v2f结构体中。

要播放帧动画，从本质上来说，我们需要计算出每个时刻需要播放的关键帧在纹理中的位置，而由于序列帧纹理都是按行按列进行排列的，因此这个位置可以认为时该关键帧所在的行列索引数。因此，在上面的代码的前3行中我们计算计算行列式，其中使用了Unity的内置时间变量_Time。由上表知道，_Time.y就是自该场景加载后经过的时间。我们首先把_Time.y和速度属性_Speed相乘来得到模拟的时间，并使用CG的floor函数对结果值取整来得到整数时间time。然后，我们使用time除以_HorizontalAmount的结果的商来作为当前对应的行索引，除法结果的余数对应的则时列索引。接下来，我们需要使用行列索引值来构建真正的采样坐标。由于序列帧图像包含了许多关键帧图像，这意味着采样坐标需要映射到每个关键帧图像的坐标范围内。我们可以首先把原纹理坐标i.uv按行数和列数进行等分，得到每个子图像的纹理坐标范围。然后，我们使用当前行列数对上面的结果进行偏移，得到当前子图像的纹理坐标范围。需要注意的时，对竖直方向上的坐标偏移要使用减法，这是因为在Unity中纹理坐标竖直方向的顺序和序列帧纹理中的顺序是相反的。这样，我们就得到了真正的纹理采样坐标。

代码中的`half2 uv = i.uv + half2(column, -row);`这一行让我迷惑了很久，我迷惑的点在于，i中的v最大值是1，而row的最小值为1，也就是说uv最终的结果里的v的值是一个小于等于0的值，为什么通过这个值能够得到正确的UV采样坐标呢？好了现在我们来解答这个问题，我们回顾一下在UnityShader入门精要第七章的7.1.2节，介绍纹理属性那一部分，书中给我们演示了使用Tilling和offset对纹理的表现造成的影响，我们把这一部分搞懂了，上面这行代码也就自然而然搞懂了。

## 滚动的背景

很多2D游戏都使用了不断滚动的背景来模拟游戏角色在场景中的穿梭，这些背景往往包含了多个层(layer)来模拟一种视差效果。而这些背景的实现往往就是利用了纹理动画。在本节中，我们将实现一个包含了两层的无限滚动的2D游戏背景。


# 顶点动画

如果一个游戏中所有的物体都是静止的，这样枯燥的世界恐怕很难引起玩家的兴趣。顶点动画可以让我们的场景变得更加生动有趣。在游戏中，我们常常使用顶点动画来模拟飘动的旗帜、湍流的小溪等效果。本节中，我们将学习两种常见的顶点动画的应用——流动的河流以及广告牌技术。在本节的最后，我们还将给出一些顶点动画中的注意事项以及解决方法。

## 流动的河流

河流的模拟是顶点动画最常见的应用之一。它的原理通常是使用正弦函数等来模拟水流的波动特效

## 广告牌

另一种常见的顶点动画就是广告牌技术(Billboarding)。广告牌技术会根据视角方向来旋转一个被纹理着色的多边形(通常就是简单的四边形，这个多边形就是广告牌)，使得多边形看起来好像总是面对着摄像机。广告牌技术被用于很多应用，比如渲染烟雾、云朵、闪光效果等。

广告牌技术的本质就是构建旋转矩阵，而我们知道一个变换矩阵需要3个基向量。广告牌技术使用的基向量通常就是表面法线(normal)、指向上的方向(up)以及指向右的方向。除此之外，我们还需要一个锚点，这个锚点在旋转过程中是固定不变的，以此来确定多边形在空间中的位置。

广告牌技术的难点在于如何根据需求来构建3个相互正交的基向量。计算过程通常是，我们首先会通过初始计算得到目标的表面法线(例如就是视角方向)和指向上的方向，而两者往往是不垂直的。但是，两者其中之一是固定的，例如当模拟草丛时，我们希望广告牌指向上的方向永远是(0,1,0)，而法线方向应该随视角变化；而当模拟粒子效果时，我们希望广告牌的法线方向是固定的，即总是指向视角方向，指向上的方向则可以发生变化。我们假设法线方向是固定的，首先我们根据初始的表面法线和指向上的方向来计算出目标方向的指向右的方向(通过叉积操作):
$$right = up \times normal$$

对其归一化后，再由法线方向和指向右的方向计算出正交的指向上的方向即可

$$up' = normal \times right$$

至此，我们就可以得到用于旋转的3个正交基了。

```
Shader "UnityShaderBook/Chapter11/BillBoard"
{
    Properties
    {
        _MainTex ("MainTex", 2D) = "white" {}
        _Color ("Color", Color) = (1,1,1,1)
        _VerticalBillboarding ("Vertical Restraints", Range(0,1)) = 1
    }
    
    SubShader
    {
        Tags
        {
            "Queue"="Transparent"
            "IgnoreProjector"="True"
            "RenderType"="Transparent"
            "DisableBatching"="True"
        }
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }
            
            ZWrite Off
            Blend SrcAlpha OneMinusSrcAlpha
            Cull Off
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            #include "UnityCG.cginc"
            
            sampler2D _MainTex;
            float4 _MainTex_ST;
            float4 _Color;
            float _VerticalBillboarding;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float2 uv : TEXCOORD0;
            };

            v2f vert(a2v v)
            {
                v2f o;

                float3 center = float3(0,0,0);
                float3 viewer = mul(unity_WorldToObject, float4(_WorldSpaceCameraPos, 1));

                float3 normalDir = viewer - center;
                normalDir.y = normalDir.y * _VerticalBillboarding;
                normalDir = normalize(normalDir);

                float3 upDir = abs(normalDir.y) > 0.999 ? float3(0,0,1) : float3(0,1,0);
                float3 rightDir = normalize(cross(upDir, normalDir));
                upDir = normalize(cross(normalDir, rightDir));

                float3 centerOffs = v.vertex.xyz - center;
                float3 localPos = center + rightDir * centerOffs.x + upDir * centerOffs.y + normalDir * centerOffs.z;

                o.pos = mul(unity_MatrixMVP, float4(localPos,1));

                o.uv = TRANSFORM_TEX(v.texcoord, _MainTex);
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed4 c = tex2D(_MainTex, i.uv);
                c.rgb *= _Color.rgb;
                return c;
            }
            
            ENDCG
        }
    }   
    
    Fallback "Transparent/VertexLit"
}
```

## 注意事项