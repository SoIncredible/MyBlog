---
title: UnityShader入门精要笔记（七）——基础纹理
abbrlink: 5fb55013
date: 2024-07-05 21:41:19
tags:
 - Unity
 - Shader
categories: Shader入门精要
cover: https://www.notion.so/images/page-cover/woodcuts_4.jpg
description:
swiper_index:
sticky:
---

纹理的作用像是盖在模型表面的衣服，纹理决定了模型表面看起来是什么样子的。

# 单张纹理

我们通常会使用一张纹理来代替物体的漫反射颜色。

```
Shader "unityShaderBook/Chapter 7/Single Texture"
{
    Properties
    {
        _MainTex ("MainTex", 2D) = "white" {}
        _Color ("Color", Color) = (1,1,1,1)
        _Gloss ("Gloss", Range(8.0, 256)) = 20
        _Specular ("Specular", Color) = (1,1,1,1)
    }
    
    SubShader
    {
        Pass
        {
            
            Tags
            {
                "LightMode"="ForwardBase"
            }
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag
            
            #include"Lighting.cginc"
            
            sampler2D _MainTex;
            float4 _MainTex_ST;
            fixed4 _Color;
            fixed4 _Specular;
            float _Gloss;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float3 worldNormal : TEXCOORD0;
                float3 worldPos : TEXCOORD1;
                float2 uv : TEXCOORD2;
            };

            v2f vert(a2v v)
            {
                v2f o;
                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;
                o.worldNormal = UnityObjectToWorldNormal(v.normal);
                o.uv = v.texcoord.xy * _MainTex_ST.xy + _MainTex_ST.zw;
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed3 worldNormal = normalize(i.worldNormal);
                fixed3 worldLightDir = normalize(UnityWorldSpaceLightDir(i.worldPos));
                
                fixed3 albedo = tex2D(_MainTex, i.uv).rgb * _Color.rgb;
                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz * albedo;
                
                fixed3 diffuse = _LightColor0.rgb * albedo.rgb * saturate(dot(worldNormal, worldLightDir));
                
                fixed3 viewDir = normalize(UnityWorldSpaceViewDir(i.worldPos));
                fixed3 halfDir = normalize(viewDir+worldLightDir);
                fixed3 specular = _LightColor0.rgb * _Specular.rgb * pow(saturate(dot(worldNormal, halfDir)), _Gloss);
                
                return fixed4(ambient + diffuse + specular, 1.0);
            }
            
            
            ENDCG
        }
    }

    Fallback "Specular"
}
```




# 凹凸映射

纹理的另一种常见的应用就是凹凸映射(bump mapping)。凹凸映射的目的是使用一张纹理来修改模型表面的法线，以便为模型提供更多的细节。这种方法不会真的改变模型的顶点位置，只是让模型看起来好像是凹凸不平的，但可以从模型的轮廓处看出破绽。

有两种主要的方法可以用来进行凹凸映射：一种方法是使用一张高度纹理(height map)来模拟表面位移(displacement),然后得到一个修改后的法线值，这种方法也被称为高度映射(height mapping)；另一种方法则是使用一张法线纹理(normal map)来直接存储表面法线，这种方法又被称为法线映射(normal mapping)。尽管我们常常将凹凸映射和法线映射当成是相同的技术，但是我们还是需要知道它们之间的不同。

## 高度纹理 

我们首先来看第一种技术，即使用一张高度图来实现凹凸映射。高度图中存储的是强度值(intensity)，它用于表示模型表面局部的海拔高度。因此，颜色越浅的地方表明该位置的表面越向外凸起，而颜色越深的地方表明该位置越想里凹。这种方法的好处是非常直观，我们可以从高度图中明确地知道一个模型表面的凹凸情况，但缺点是计算更加复杂，在实时计算的时候不能直接得到表面法线，而是需要由像素的灰度值计算而得，因此需要消耗更多的性能。

高度图通常会和法线映射一起使用，用于给出表面凹凸的额外信息，也就是说，我们经常会使用法线映射来修改光照。

## 法线纹理

而法线纹理中存储的就是表面法线的方向。由于法线方向的分量范围在[-1,1]，而像素的分量范围为[0,1]，因此我们需要做一个映射，通常使用的映射就是：
$$pixel = \frac{normal + 1}{2}$$

这就要求，我们在Shader中对法线纹理进行纹理采样后，还需要对结果进行一次反映射的过程，以得到原先的法线方向。反映射的过程实际上就是使用上面的映射函数的逆函数:
$$normal = pixel \times 2 - 1$$

然而，由于方向是相对于坐标空间来说的，那么法线纹理中存储的法线方向在哪个坐标空间中呢？对于模型顶点自带的法线，它们是定义在模型空间中的，因此一种直接的想法就是将修改后的模型空间中的表面法线存储在一张纹理中，这种纹理被称为是模型空间的法线纹理(object-space normal map)。然而，在实际制作中，我们往往会采用另一种坐标空间，即模型顶点的切线空间(tangent space)来存储法线。对于模型的每个顶点，它都有属于自己的一个切线空间，这个切线空间的原点就是该顶点本身，而z轴是顶点的法线方向(n),x轴是顶点的切线方向(t),而y轴则可由法线和切线的叉乘积而得，也被称为是副切线(bitangent,b)或者副法线。切线空间坐标系是右手坐标系。

这种纹理被称为是切线空间的法线纹理(tangent-space normal map)

从图中可以看出，模型空间下的法线纹理看起来是五颜六色的，这是因为，所有法线所在的坐标空间是同一个坐标空间，即模型空间，而每个点存储的法线方向是各异的，有的是(0,1,0)，经过映射后存储到纹理中就对应了RGB(0.5,1,0.5)浅绿色，有的是(0,-1,0)，经过映射后存储到纹理中就对应了(0.5,0,0.5)紫色。而切线空间下的法线纹理看起来几乎全部是浅蓝色。这是因为，每个法线方向所在的坐标空间是不一样的，即是表难免每个点各自的切线空间。这种法线纹理其实就是存储了每个点在各自的且前空间中的法线扰动方向。也就是说，如果一个点的法线方向不变，那么在它的切线空间中，新的法线方向就是z轴方向，即值为(0,0,1)，经过映射后存储在纹理中就对应了RGB(0.5,0.5,1)浅蓝色。而这个颜色就是法线纹理中大片的蓝色。这些蓝色实际上说明顶点的大部分法线是和模型本身法线一样的，不需要改变。

总体来说，模型空间下的法线纹理更加符合人类的直观认识，而法线纹理本身也很直观，容易调整，因为不同的法线方向就代表了不同的颜色。但美术人员往往更喜欢使用切线空间下的法线纹理。那么，为什么他们更喜欢使用这个看起来很蹩脚的切线空间呢？

其实，发现本身存储在哪个坐标系中都是可以的，我们甚至可以选择存储在世界空间下。但问题是，我们并不是单纯地想要得到法线，后续的光照计算才是我们的目的。而选择哪个坐标系意味着我们需要把不同信息转换到相应的坐标系中。例如，如果选择了切线空间，我们需要把从法线纹理中得到的法线方向从切线空间转换到世界空间(或其他空间)中。

总体来说，使用模型空间来存储法线的优点如下。
- 实现简单，更加直观。我们甚至都不需要模型初始的法线和切线等信息，也就是说，计算更少。生成它也非常简单，而如果要生成切线空间下的法线纹理，由于模型的切线一般是和UV方向相同，因此想要得到效果比较好的法线映射就要求纹理映射也是连续的。
- 在纹理坐标的缝合处和尖锐的边角部分，可见的突变(缝隙)较少，即可以提供平滑的边界。这是因为模型空间下的法线纹理存储的是同一坐标系下的发现信息，因此在边界处通过插值得到的法线可以平滑变化。而切线空间下的法线纹理中的法线信息是依靠纹理坐标的方向得到的结果，因此可能会在边缘处或者尖锐的部分造成更多可见的缝合迹象。

但是使用切线空间有更多的优点
- 自由度很高。模型空间下的法线纹理记录的是**绝对法线信息**，仅可以用于创建它时的那个模型，而应用到其他模型上的效果就完全错误了。而切线空间下的法线纹理记录的是相对法线信息，这意味着，即便是把该纹理应用到一个完全不同的网格上，也可以得到一个合理的结果。
- 可进行UV动画。比如，我们可以移动一个纹理的UV坐标来实现一个凹凸移动的效果，但使用模型空间下的法线纹理会得到完全错误的结果。原因同上。这种UV动画在水或者火山熔岩这种类型的物体上会经常用到。
- 可以重用法线纹理。比如，一个砖块，我们仅使用一张发现纹理就可以用到所有的6个面上，原因同上。
- 可压缩。由于切线空间下的法线纹理中法线Z方向总是正方向，因此我们可以仅存储XY方向，进而推到Z方向。而模型空间下的法线纹理由于每个方向都是可能的，因此必须存储3个方向的值，不可压缩。

切线空间下的法线纹理的前两个优点足以让很多人放弃模型空间下的法线纹理而选择它。从上面的优点可以看出，切线空间在很多情况下都优于模型空间，而且可以节省美术人员的工作，因此我们也会主要使用切线空间下的法线纹理。

### 实践

我们需要在计算光照模型中统一各个方向矢量所在的坐标空间。由于法线纹理中存储的法线是切线空间下的方向，因此我们通常有两种选择：一种选择是在切线空间下进行光照计算，此时我们需要把光照方向、视角方向变换到坐标空间下；另一种选择是在切线空间下进行光照计算，此时我们需要把采样得到的法线方向变换到切线空间下，再和世界空间下的光照方向和视角方向进行计算。从效率上来说，第一种方式要由于第二种方式，因为我们可以在顶点着色器中完成对光照方向和视角方向的转变，而第二种方法由于要先对法线纹理进行采样，所以变换过程必须在片元着色器中实现，这就意味着我们在片元着色器中要进行一次矩阵操作。但从通用性角度来说，第二种方法要优于第一种方法，因为有的时候我们需要在世界空间下进行一些计算，例如在使用Cubemap进行环境映射，我们需要使用世界空间下的反射方向对Cubmap进行采样。如果同时需要进行法线映射，我们就需要把法线方向变换到世界空间下。当然我们也可以选择其他坐标空间进行计算，例如模型空间，但是切线空间和世界空间是最为常用的两种空间，本节会依次实现上面两种方法。

1. 在切线空间下计算

我们首先来实现第一种方法，即在切线空间下计算光照模型。基本思路是：在片元着色器中通过纹理采样得到切线空间下的法线，然后再与切线空间下的视角方向、光照方向等进行计算，得到最终的光照结果。

为此，我们首先需要在顶点着色器中把视角方向和光照方向从模型空间变换到切线空间中，即我们需要知道从模型空间到切线空间的变换矩阵。这个变换矩阵的逆矩阵，即从切线空间到模型空间的变换矩阵是非常容易求的，我们在顶点着色器中按切线(x轴)、副切线(y轴)、法线(z轴)的顺序按**列**排列即可。我们已经知道，如果一个变换中仅有平移和旋转变换，那么这个变换的逆变换矩阵就等于它的转置矩阵，而从切线空间到模型空间的变换正是符合这样要求的变换。因此，从模型空间到切线空间的变换矩阵就是从切线空间到模型空间变换矩阵的转置矩阵，我们把切线(x轴)、副切线(y轴)、法线(z轴)的顺序按**行**排列即可。
```
Shader "UnityShaderBook/Chapter 7/NormalMapInTangentSpace"
{
    Properties
    {
        _Color ("Colcor Tint", Color) = (1,1,1,1)
        _MainTex ("MainTex",2D) = "white" {}
        _BumpMap ("Normal Map", 2D) = "bump" {}
        _BumpScale ("Bump Scale", Float) = 1.0
        _Specular ("Specular", Color) = (1,1,1,1)
        _Gloss ("Gloss", Range(8.0,256)) = 20
    }
    
    SubShader
    {
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"  
            }
             
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag
            #include"Lighting.cginc"

            fixed4 _Color;
            sampler2D _MainTex;
            fixed4 _MainTex_ST;
            sampler2D _BumpMap;
            float4 _BumpMap_ST;
            float _BumpScale;
            fixed4 _Specular;
            float _Gloss;

            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float4 tangent : TANGENT;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float4 uv : TEXCOORD0;
                float3 lightDir : TEXCOORD1;
                float3 viewDir : TEXCOORD2;
            };

            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv.xy = v.texcoord.xy * _MainTex_ST.xy + _MainTex_ST.zw;
                o.uv.zw = v.texcoord.xy * _BumpMap_ST.xy + _BumpMap_ST.zw;

                TANGENT_SPACE_ROTATION;

                o.lightDir = mul(rotation, ObjSpaceLightDir(v.vertex)).xyz;

                o.viewDir = mul(rotation, ObjSpaceViewDir(v.vertex)).xyz;
                
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {

                fixed3 tangentLightDir = normalize(i.lightDir);
                fixed3 tangentViewDir = normalize(i.viewDir);

                fixed4 packedNormal = tex2D(_BumpMap, i.uv.zw);
                fixed3 tangentNormal;

                tangentNormal = UnpackNormal(packedNormal);
                tangentNormal.xy *= _BumpScale;
                tangentNormal.z = sqrt(1.0 - saturate(dot(tangentNormal.xy,tangentNormal.xy)));

                fixed3 albedo = tex2D(_MainTex,i.uv.xy).xyz * _Color.rgb;

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz * albedo;

                fixed3 diffuse = _LightColor0.rgb * albedo * saturate(dot(tangentNormal, tangentLightDir));

                fixed3 halfDir = normalize(tangentViewDir + tangentLightDir);
                fixed3 specular = _LightColor0.rgb * _Specular.rgb * pow(saturate(dot(tangentNormal, halfDir)), _Gloss);
                
                return fixed4(ambient + diffuse + specular,1);
            }
            
            ENDCG
        }
    }
    Fallback "Specular"
}

```


2. 在世界空间下计算
现在我们来实现第二种方法，即在世界空间下计算光照模型。我们需要在片元着色器中把法线方向从切线空间变换到世界空间下。这种方法的基本思想是：在顶点着色器中
```
Shader "Unity Shader Book/Chapter 7/NormalMapWorldSpace"
{
    Properties
    {
        _MainTex ("MainTex", 2D) = "white" {}
        _BumpMap ("BumpMap", 2D) = "bump" {}
        _Specular ("Specular", Color) = (1,1,1,1)
        _BumpScale ("BumpScale", Float) = 1.0
        _Gloss ("Gloss", Range(8.0, 256)) = 20
        _Color ("Color Tint", Color) = (1,1,1,1)
    }
    
    SubShader
    {
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag
            #include "Lighting.cginc"

            sampler2D _MainTex;
            fixed4 _MainTex_ST;
            sampler2D _BumpMap;
            fixed4 _BumpMap_ST;
            fixed4 _Color;
            fixed4 _Specular;
            float _Gloss;
            float _BumpScale;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float4 tangent : TANGENT;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : POSITION;
                float4 uv : TEXCOORD0;
                float4 TtoW0 : TEXCOORD1;
                float4 TtoW1 : TEXCOORD2;
                float4 TtoW2 : TEXCOORD3;
            };

            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv.xy = v.texcoord.xy * _MainTex_ST.xy + _MainTex_ST.zw;
                o.uv.zw = v.texcoord.xy * _BumpMap_ST.xy + _BumpMap_ST.zw;

                // 求变换矩阵
                float3 worldPos = mul(unity_ObjectToWorld, v.vertex);

                fixed3 worldNormal = UnityObjectToWorldNormal(v.normal);
                fixed3 worldTangent = UnityObjectToWorldDir(v.tangent);
                fixed3 worldBinormal = cross(worldNormal, worldTangent) * v.tangent.w;

                o.TtoW0 = float4(worldTangent.x, worldBinormal.x, worldNormal.x, worldPos.x);
                o.TtoW1 = float4(worldTangent.y, worldBinormal.y, worldNormal.y, worldPos.y);
                o.TtoW2 = float4(worldTangent.z, worldBinormal.z, worldNormal.z, worldPos.z);
                
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {

                float3 worldPos = float3(i.TtoW0.w, i.TtoW1.w, i.TtoW2.w);
                fixed3 worldLightDir = normalize(UnityWorldSpaceLightDir(worldPos));
                fixed3 viewDir = normalize(UnityWorldSpaceViewDir(worldPos));

                fixed3 bump = UnpackNormal(tex2D(_BumpMap, i.uv.zw));
                bump.xy *= _BumpScale;
                bump.z = sqrt(1.0 - saturate(dot(bump.xy, bump.xy)));
                bump = normalize(half3(dot(i.TtoW0.xyz, bump), dot(i.TtoW1.xyz, bump), dot(i.TtoW2.xyz, bump)));

                fixed3 albedo = tex2D(_MainTex, i.uv.xy).rgb * _Color.rgb;

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz * albedo;

                fixed3 diffuse = _LightColor0.rgb * albedo.rgb * saturate(dot(bump, worldLightDir));

                fixed3 halfDir = normalize(bump + worldLightDir);
                fixed3 specular = _LightColor0.rgb * _Specular.rgb * pow(saturate(dot(bump, halfDir)), _Gloss);
                
                return fixed4(ambient + diffuse + specular, 1.0);
            }
            
            ENDCG
        }
        
        
    }
    
    
}
```

## Unity中的法线纹理类型

将法线纹理的纹理类型标识成Normal Map时，可以使用Unity的内置函数UnpackNormal来得到正确的法线方向。
当我们需要使用那些包含了法线映射的内置的Unity Shader时，必须把使用的法线纹理按照上面的方式标识成Normal map才能得到正确的结果(即使你忘了这么做，Unity也会在材质面板中提醒你修正这个问题)，这是因为这些Unity Shader都使用了内置的unpackNormal函数来采样法线方向。那么，当我们把纹理类型设置成Normal map的时候到底发生了什么呢？为什么要这么做呢？
简单来说，这么做可以让Unity根据不同平台对纹理进行压缩，例如使用DXT5nm格式，再通过UnpackNormal函数来针对不同的压缩格式对法线纹理进行正确的采样。我们可以再UnityCG.cginc里面找到UnpackNormal函数的内部实现：
```
inline fixed3 UnpackNormalDXT5nm(fixed4 packednormal){
    fixed3 normal;
    normal.xy = packednormal.wy *2 - 1;
    normal.z = sqrt(1 - saturate(dot(normal.xy, normal.xy)));
    return normal; 
}

inline fixed3 UnpackNormal(fixed4 packnormal){
#if defined(UNITY_NO_DXT5nm)
    return packednormal.xyz * 2 - 1;
#else
    return UnpackNormalDXT5nm(packednormal);
#endif
}
```

从代码中可以看出，在某些平台上由于使用了DXT5nm的压缩格式，因此需要针对这种格式对法线进行解码。在DXT5nm格式的法线纹理中，纹素的a通道(即w分量)对应了法线的x分量，g通道对应了法线的y分量，而纹理的r和b通道则会被舍弃法线的z分量可以由xy分量推导而得。为什么之前的普通纹理不能按照这种方式压缩，而法线就需要使用DXT5nm格式来进行压缩呢？这是因为，按照我们之前的处理方式，法线纹理被当成一个和普通纹理无异的图，但实际上，它只有两个通道是真正必不可少的，因为第三个通道的值可以用另外两个推导出来(法线是单位向量，并且切线空间下的法线方向的z分量始终为正)。使用这种压缩方法就可以减少法线纹理占用的内存空间。

当我们把纹理类型设置成normal map之后，还有一个复选框是Creat from Grayscale，那么它是做什么用的呢？还记得我们在本节一开始提到的另一种凹凸映射的方法，即使用高度图，而这个复选框就是用于从高度图中生成法线纹理的。高度图本身记录的是相对高度，是一张灰度图，白色部分相对更高，黑色表示相对更低。当我们把一张高度图导入Unity后，除了需要把它的纹理类型设置成Normal map外，还需要勾选Create from Grayscale，这样就可以得到类似下图的结果。然后，我们就可以把它和切线空间下的法线纹理等同对待了。

# 渐变纹理

尽管在一开始，我们在渲染中使用纹理是为了定义一个物体的颜色，但是后来人们发现，纹理其实可以用于存储任何表面属性。一种常见的用法就是使用渐变纹理来控制漫反射的光照结果。
```
Shader "Unity Shader Book/Chapter 7/Ramp Texture"
{
    Properties
    {
        _RampTex ("Ramp", 2D) = "white"{}
        _Color ("Color Tint", Color) = (1,1,1,1)git
        _Specular ("Specular", Color) = (1,1,1,1)
        _Gloss ("Gloss", Range(8.0, 256)) = 20
    }
    
    SubShader
    {
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }
               
            CGPROGRAM
            
            #include "Lighting.cginc"
            #pragma vertex vert
            #pragma fragment frag

            fixed4 _Color;
            sampler2D _RampTex;
            fixed4 _Specular;
            fixed4 _RampTex_ST;
            float _Gloss;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float4 texcoord : TEXCOORD0;
            };
            
            struct v2f
            {
                float4 pos : POSITION;
                float3 worldNormal : TEXCOORD0;
                float3 worldPos : TEXCOORD1;
                float2 uv : TEXCOORD2;
            };

            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.worldNormal = UnityObjectToWorldNormal(v.normal);
                o.worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;
                // 把UV传过去也不用
                // 可以把uv这个字段移除掉
                o.uv = TRANSFORM_TEX(v.texcoord, _RampTex);
                
                return o;
            }

            fixed4 frag(v2f i) :SV_Target{

                fixed3 worldNormal = normalize(i.worldNormal);
                fixed3 lightDir = normalize(UnityWorldSpaceLightDir(i.worldPos));

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz;

                fixed halfLambert = 0.5 * dot(worldNormal, lightDir) + 0.5;

                fixed3 diffuseColor = tex2D(_RampTex, fixed2(halfLambert, halfLambert)).rgb * _Color.rgb;

                fixed3 diffuse = _LightColor0.rgb * diffuseColor;

                fixed3 viewDir = UnityWorldSpaceViewDir(i.worldPos);

                fixed3 halfDir = normalize(viewDir + lightDir);
                fixed3 specular = _LightColor0.rgb * _Specular.rgb * pow(saturate(dot(worldNormal, halfDir)), _Gloss);

                return fixed4(diffuse + ambient + specular,1.0);
            }
            
            ENDCG
            
        }
    }
    
    Fallback "Specular"
}
```

需要注意的是，我们需要把渐变纹理的Wrap Mode设为`Clamp`模式，以防止对纹理进行采样时由于浮点数的精度而造成的问题。

# 遮罩纹理

遮罩纹理(mask texture)是本章要介绍的最后一种纹理，它非常有用，在很多商业游戏中都可以见到它的身影。那么什么是遮罩呢？简单来讲，遮罩允许我们可以保护某些区域，使它们免于某些修改。

```
Shader "ShaderBook/Chapter7/MaskTexture"
{
    Properties
    {
        _Color ("Color Tint", Color) = (1,1,1,1)
        _MainTex ("Main Tex", 2D) = "white" {}
        _BumpMap ("Normal Map", 2D) = "bump" {}
        _BumpScale ("Bump Scale", Float) = 1.0
        
        _SpecularMask ("Specular Mask", 2D) = "white" {}
        _SpecularScale ("SpecularScale", Float) = 1.0
        _Specular ("Specular", Color) = (1,1,1,1)
        
        _Gloss ("Gloss", Range(8.0, 256)) = 8.0
        
    }
    
    SubShader
    {
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }            
            
            CGPROGRAM

            #include "Lighting.cginc"
            #pragma vertex vert
            #pragma fragment frag
            
            fixed4 _Color;
            sampler2D _MainTex;
            fixed4 _MainTex_ST;

            sampler2D _BumpMap;
            float _BumpScale;

            sampler2D _SpecularMask;
            float _SpecularScale;

            fixed4 _Specular;

            float _Gloss;
            
            struct a2v
            {****
                float4 vertex : POSITION;
                float3 normal : NORMAL;
                float4 tangent : TANGENT;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float2 uv : TEXCOORD0;
                float3 lightDir : TEXCOORD1;
                float3 viewDir : TEXCOORD2;
            };

            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv.xy = v.texcoord.xy * _MainTex_ST.xy + _MainTex_ST.zw;

                TANGENT_SPACE_ROTATION;
                o.lightDir = mul(rotation, ObjSpaceLightDir(v.vertex)).xyz;
                o.viewDir = mul(rotation, ObjSpaceViewDir(v.vertex)).xyz;

                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed3 tangentLightDir = normalize(i.lightDir);
                fixed3 tangentViewDir = normalize(i.viewDir);

                fixed3 tangentNormal = UnpackNormal(tex2D(_BumpMap, i.uv));
                tangentNormal.xy *= _BumpScale;
                tangentNormal.z = sqrt(1 - saturate(dot(tangentNormal.xy, tangentNormal.xy)));

                fixed3 albedo = tex2D(_MainTex, i.uv).rgb * _Color.rgb;

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz * albedo;;

                fixed3 diffuse = _LightColor0.rgb * albedo * saturate(dot(tangentNormal, tangentLightDir));
                
                fixed3 halfDir = normalize(tangentViewDir + tangentLightDir);

                fixed specularMask = tex2D(_SpecularMask, i.uv).r * _SpecularScale;

                fixed3 specular = _LightColor0.rgb * _Specular.rgb * pow(saturate(dot(tangentNormal, halfDir)), _Gloss) * specularMask;

                return fixed4(diffuse + specular + ambient, 1.0);
            }
            
            ENDCG
        }
        
    }

    Fallback "Specular"
    
}
```