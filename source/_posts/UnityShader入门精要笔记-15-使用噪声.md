---
title: UnityShader入门精要笔记-（十五）-使用噪声
abbrlink: 200401f7
date: 2024-09-11 00:36:49
tags:
 - Unity
 - Shader
categories: UnityShader入门精要笔记
cover: https://www.notion.so/images/page-cover/rijksmuseum_jansz_1637.jpg
description:
swiper_index:
sticky:
---

很多时候，向规则的事物里添加一些“杂乱无章”的效果往往会有意想不到的效果。而这些“杂乱无章”的效果来源就是噪声。在本章中，我们将会学习如何使用噪声来模拟各种看似“神奇”的效果。在15.1节中，我们将会使用一张噪声纹理来模拟火焰的消融效果。15.2节则把噪声应用在模拟水面的波动上，从而产生波光粼粼的视觉感受。在15.3节中，我们会回顾13.3节中实现的全局雾效，并向其中添加噪声来模拟不均匀的飘渺雾效。

## 消融效果

消融效果常见于游戏中的角色死亡、地图烧毁等效果.在这些小锅中,消融往往从不同的区域开始,并向看似随机的方向扩张,最后整个物体都将消失不见.在本节中,我们将学习如何在Unity中实现这种效果.
要实现这种效果,原理非常简单,概括来说,就是使用噪声纹理+透明度测试.我们使用对噪声纹理采样的结果和某个控制消融程度的阈值比较,如果小于阈值,就使用clip函数把它对应的像素裁剪掉,这些部分就对应了图中被“烧毁”的区域.而镂空区域边缘的烧焦效果则是将两种颜色混合,再用pow函数处理之后,与原纹理颜色混合后的结果.

1. 首先,声明消融效果需要的各个属性
```
Properties
{
    _BurnAmount ("Burn Amount", Range(0.0, 1.0)) = 0.0
    _LineWidth("Burn Line Width", Range(0.0, 0.2)) = 0.1
    _MainTex ("Base (RGB)", 2D) = "white" {}
    _BumpMap ("Normal Map", 2D) = "bump" {}
    _BurnFirstColor("Burn First Color", Color) = (1, 0, 0, 1)
    _BurnSecondColor("Burn Second Color", Color) = (1, 0, 0, 1)
    _BurnMap("Burn Map", 2D) = "white" {}
}
```

_BurnAmount属性用于控制消融程度,当值为0时,物体为正常效果,当值为1时,物体会完全消融._LineWidth属性用于控制模拟烧焦效果时的线宽,它的值越大,火焰边缘的蔓延范围越广._MainTex和_BumpMap分别对应了物体原本的漫反射纹理和法线纹理._BurnFirstColor和_BurnSecondColor对应了火焰边缘的两种颜色值._BurnMap则是关键的噪声纹理.

2. 我们在SubShader块中定义消融所需要的Pass:
```
SubShader
    {
        Pass
        {
            Tags {  "LightMode"="ForwardBase" }
            Cull Off
            
            CGPROGRAM

            #include "Lighting.cginc"
            #include "AutoLight.cginc"

            #pragma multi_compile_fwdbase
            
            ENDCG
        
        }    
    }
```
为了得到正确的光照,我们设置了Pass的LightMode和multi_compile_fwdbase的编译指令.值得注意的是,我们还使用Cull命令关闭了该Shader的面片剔除,也就是说,模型的正面和背面都会被渲染.这是因为,消融会导致裸露内部的构造,如果只渲染正面会出现错误的结果.

3. 定义顶点着色器  

顶点着色器中的代码很常规.我们使用宏TRANSFORM_TEX计算了三张纹理对应的纹理坐标,再把光源方向从模型空间变换到了切线空间.最后,为了得到阴影信息,计算了世界空间下的顶点位置和阴影纹理的采样坐标(使用了TRANSFER_SHADOW宏). 

```
v2f vert(a2v v)
{
    v2f o;
    o.pos = UnityObjectToClipPos(v.vertex);

    o.uvMainTex = TRANSFORM_TEX(v.texcoord, _MainTex);
    o.uvBumpMap = TRANSFORM_TEX(v.texcoord, _BumpMap);
    o.uvBurnMap = TRANSFORM_TEX(v.texcoord, _BurnMap);

    TANGENT_SPACE_ROTATION;

    o.lightDir = mul(rotation, ObjSpaceLightDir(v.vertex)).xyz;

    o.worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;

    TRANSFER_SHADOW(o);

    return o;
}
```
使用TANGENT_SPACE_ROTATION实现了从坐标空间到切线空间的变换矩阵.
4. 我们还需要片元着色器来模拟消融的效果
   