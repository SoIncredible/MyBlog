---
title: UnityShader入门精要笔记-（十五）-使用噪声
abbrlink: 200401f7
date: 2024-09-11 00:36:49
tags:
 - Unity
 - Shader
categories: UnityShader
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

```
fixed4 frag(v2f i) : SV_Target
{
    fixed3 burn = tex2D(_BurnMap, i.uvBurnMap).rgb;
    clip(burn.r - _BurnAmount);

    float3 tangentLightDir = normalize(i.lightDir);
    fixed3 tangentNormal = UnpackNormal(tex2D(_BumpMap, i.uvBumpMap));

    fixed3 albedo = tex2D(_MainTex, i.uvMainTex).rgb;

    fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz * albedo;
    fixed3 diffuse = _LightColor0.rgb * albedo * max(0, dot(tangentNormal, tangentLightDir));

    fixed t = 1 - smoothstep(0.0, _LineWidth, burn.r - _BurnAmount);
    fixed3 burnColor = lerp(_BurnFirstColor, _BurnSecondColor, t);
    burnColor = pow(burnColor, 5);

    UNITY_LIGHT_ATTENUATION(atten, i, i.worldPos);

    fixed3 finalColor = lerp(ambient + diffuse * atten, burnColor, t * step(0.0001l, _BurnAmount));

    return fixed4(finalColor, 1);
}
```
我们首先对噪声纹理进行采样,并将采样结果和用于控制消融程度的属性_BurnAmount相减,传递给clip函数. 当结果小于0时,该像素将会被剔除,从而不会显示到屏幕上.如果通过了测试,则进行正常的光照计算.我们首先根据漫反射纹理得到材质的反照率albedo,并由此计算得到环境光照, 进而得到漫反射光照.然后,我们计算了烧焦颜色burnColor. 我们想要在宽度为_LineWidth的范围内模拟一个烧焦的颜色变化,第一步就是用了smoothstep函数来计算混合系数t.当t的值为1时,表明该像素位于消融的边界处,当t值为0时,表明该像素为正常的模型颜色,而中间的插值则表示需要模拟一个烧焦效果.我们首先用t来混合两种火焰颜色_BurnFirstColor和_BurnSecondColor,为了让效果更佳接近烧焦的痕迹,我们还使用pow函数对结果进行处理.然后,我们再次使用t来混合正常的光照颜色(环境光+漫反射)和烧焦颜色.我们这里又使用了step函数来报称当_BurnAmount为0时,不显示任何消融效果.最后,返回混合后的颜色值finalColor

与之前的实现不同,我们在本例中还定义了一个用于投射阴影的Pass.正如我们在之前解释过的一样:使用透明度测试的物体的阴影需要特别处理,如果仍然使用普通的阴影Pass,那么被剔除的区域仍然会向其他物体投射阴影,造成穿帮.为了让物体的阴影也能配合透明度测试产生正确的效果,我们需要定义一个投射阴影的Pass.

```
Pass

{
    Tags{ "LightMode"="ShadowCaster" }
    CGPROGRAM

    #pragma vertex vert
    #pragma fragment frag

    #pragma multi_compile_shadowcaster

    struct v2f
    {
        V2F_SHADOW_CASTER;
        float2 uvBurnMap : TEXCOORD1;
    };

    v2f vert(appdata_base v)
    {
        v2f o;

        TRANSFER_SHADOW_CASTER_NORMALOFFSET(o)

        o.uvBurnMap = TRANSFORM_TEX(v.texcoord, _BurnMap);
        return o;
    }

    fixed4 frag(v2f i) : SV_Target{
        fixed3 burn = tex2D(_BurnMap,i.uvBurnMap).rgb;

        clip(burn.r - _BurnAmount);

        SHADOW_CASTER_FRAGMENT(i)
    }
    
    ENDCG
}
```

阴影投射的重点在于我们需要按正常Pass的处理来剔除片元或进行定点动画,以便可以和物体正常渲染的结果相匹配.在自定义的阴影投射的Pass中,我们通常会使用Unity提供的内置宏`V2F_SHADOW_CASTER`、`TRANSFER_SHADOW_CASTER_NORMALOFFSET`和`SHADOW_CASTER_FRAGMENT`来帮助我们计算阴影投射时所需要的各种变量,而我们可以只关注自定义计算的部分.在上面的代码中,我们首先在v2f结构体中利用V2F_SHADOW_CASTER来定义阴影投射需要定义的变量.随后,在顶点着色器中,我们使用TRANSFER_SHADOW_CASTER_NORMALOFFSET来填充V2F_SHADOW_CASTER在背后声明的一些变量,这是由Unity在背后为我们完成的. 我们需要在顶点着色器中关注自定义的计算部分,这里指的就是我们需要计算噪声纹理的采样坐标uvBurnMap. 在片元着色器中,我们首先按之前的处理方法使用噪声纹理的采样结果来剔除片元,最后再利用SHADOW_CASTER_FRAGMENT来让Unity为我们完成阴影投射的部分,把结果输出到深度图和阴影映射纹理中.
通过Unity提供的这三个内置宏,我们可以方便地定义需要阴影投射的Pass, 但由于这些宏需要使用一些特定的输入变量,因此我们需要保证为它们提供了这些变量. 例如TRANSFER_SHADOW_CASTER_NORMALOFFSET会使用名称v作为输入结构体,v中需要包含顶点位置v.vertex和顶点法线v.normal的信息,我们可以直接使用内置的appdata_base结构体,它包含了这些必须的顶点变量. 如果我们需要进行定点动画,可以在顶点着色器中直接修改v.vertex,再传递给TRANSFER_SHADOW_CASTER_NORMALOFFSET.


投射阴影“三剑客” 想投射阴影除了编写Shader代码还要把MeshRenderer中的`CastShadows`选项打开 `V2F_SHADOW_CASTER`、`TRANSFER_SHADOW_CASTER_NORMALOFFSET`和`SHADOW_CASTER_FRAGMENT`

接收阴影“三剑客” `SHADOW_COORDS` `TRANSFER_SHADOW` `SHADOW_ATTENUATION`

统一管理光照衰减(Attenuation)和接收阴影`SHADOW_COORDS` `TRANSFER_SHADOW` 将`SHADOW_ATTENUATION`替换为`UNITY_LIGHT_ATTENUATION`


## 水波效果

在模拟实时水面的过程中,我们往往也会使用噪声纹理.此时,噪声纹理通常会用作一个高度图, 以不断修改水面的法线方向. 为了模拟水不断流动的效果,我们会使用和时间相关的变量来对噪声纹理进行采样,当得到法线信息后,再进行正常的反射和折射计算,最后得到水面波动的效果.

我们将会使用一个由噪声纹理得到的法线贴图,实现一个包含菲涅耳反射的水面效果.

我们使用一张立方体纹理作为环境纹理,模拟反射.为了模拟折射效果,我们使用GrabPass来获取当前屏幕的渲染纹理,并使用切线空间下的法线方向对像素的屏幕坐标进行偏移,再使用该坐标对渲染纹理进行纹理采样,从而模拟近似折射的效果. 与之前实现不同的是,水波的法线纹理是由一张噪声纹理生成而得,而且会随着时间变化不断平移,模拟波光粼粼的效果.除此之外,我们没有使用一个定值来混合反射和折射的颜色,而是使用之前提到的菲涅耳系数来动态决定混合系数.我们使用如下公式来计算菲涅耳系数:
$$ fresnel=pow(1 - max(0, v \cdot n), 4)$$
其中,v和n分别对应了视角方向和法线方向. 它们之间的夹角越小,fresnel值越小, 反射越弱, 折射越强. 菲涅耳系数还经常会用于边缘光照的计算中. 