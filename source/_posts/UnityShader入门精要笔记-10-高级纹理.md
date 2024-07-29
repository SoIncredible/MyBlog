---
title: UnityShader入门精要笔记_10_高级纹理
date: 2024-07-15 12:35:28
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


# 立方体纹理

在图形学中，立方体纹理(Cubemap)是**环境映射(Environment Mapping)**的一种实现方法。环境映射可以模拟物体周围的环境，而使用了环境映射的物体可以看起来像是镀了一层金属一样反射处周围的环境。

和之前见到的纹理不同，立方体纹理总共包含六张图片，这些图像对应了一个立方体的六个面，立方体纹理的名称也由此而来。立方体的每个面表示沿着世界空间下的轴向（上、下、左、右、前、后）观察所得的图像。那么我们如何对这样的一种纹理进行采样呢？和之前使用的二维纹理坐标不同，对立方体纹理采样我们需要提供一个三维的纹理坐标，这个三维纹理坐标表示了我们在世界空间下的一个3D方向。这个方向矢量从立方体的中心出发，当它向外部延伸时就会和立方体的6个纹理之一发生相交，而采样得到的结果就是由该交点计算而来的。

使用立方体纹理的好处在于，它的实现简单快速，而且得到的效果也是比较好的。但是它也有一些缺点，例如，当场景中引入了新的物体、光源，或者物体发生移动时，我们就需要重新生成立方体纹理。除此之外，立方体纹理也仅可以反射环境，但不能反射使用了该立方体纹理的物体本身。这是因为，立方体纹理不能模拟多次反射的结果，例如两个金属球互相反射的情况，不过Unity中引入的全局光照系统允许我们实现这样的自反射效果。由于这样的原因，想要得到令人信服的渲染结果，我们应该尽量对凸面体而不要对凹面体使用立方体纹理，因为凹面体会反射自身。

立方体纹理在实时渲染中有很多应用，最常见的是用于天空盒子以及环境映射。

## 天空盒子


需要说明的是，在Window -> Lighting -> Skybox中设置的天空盒子会应用于该场景中所有的摄像机。如果我们希望某些摄像机可以使用不同的天空盒子，可以通过向该摄像机添加Skybox组件来覆盖掉之前的设置。也就是说，在挂载有摄像机组件的物体上点击Component -> Rendering -> Skybox来完成对场景默认天空盒子的覆盖。

在Unity中，天空盒子是在所有不透明物体之后渲染的，而其被后使用的网格是一个立方体或一个细分后的球体。

## 创建用于环境映射的立方体纹理

除了天空盒子，立方体纹理最常见的用处就是用于环境映射。通过这种方法，我们可以模拟出具有金属质感的材质。

在Unity中创建用于环境映射的立方体纹理的方法有三种：第一种方法是直接由一些特殊布局的纹理创建；第二种方法是手动创建一个Cubemap资源，再把6张图赋给它；第三种方法是由脚本生成。

如果使用第一种方法，我们需要提供一张具有特殊布局的纹理，例如类似正方体展开图的交叉布局、全景布局等。然后，我们只需要把该纹理的Texture Type设置成Cubemap即可，Unity会为我们做好剩下的事情。在基于物理的渲染中，我们通常会使用一张HDR图像来生成高质量的Cubemap。

第二种方法是Unity5之前的方法，我们首先需要在项目资源中创建一个Cubemap，然后把6张纹理拖拽到它的面板中。在Unity5中，官方推荐使用第一种方法来创建立方体纹理，这是因为第一种方法可以对纹理数据进行压缩，而且可以支持边缘修正、光滑反射(Glossy Reflection)和HDR等功能。

前面两种方法都需要我们提前准备好立方体纹理的图像，它们得到的立方体纹理往往是被场景中的物体所共用的。但在理想情况下，我们希望根据物体所在场景中位置的不同，生成它们各自不同的立方体纹理。这时我们就可以在Unity中通过脚本来创建。这是通过利用Unity提供的`Camera.RenderToCubemap`函数来实现的。这个函数可以把从任意位置观察到的场景图像存储在6张图片中，从而创建出该位置上对应的立方体纹理。


我们先来使用第三种方法，

## 反射

使用了反射效果的物体通常看起来就像是镀了一层金属。想要模拟反射效果十分简单，我们只需要通过入射光线的方向和表面法线方向来计算反射方向，再利用反射方向对立方体纹理进行采样即可。

我们需要两个C#脚本
```
using UnityEngine;

[ExecuteInEditMode]
public class ProceduralTextureGeneration : MonoBehaviour {

	public Material material = null;

	#region Material properties
	[SerializeField]
	private int m_textureWidth = 512;
	public int textureWidth {
		get {
			return m_textureWidth;
		}
		set {
			m_textureWidth = value;
			_UpdateMaterial();
		}
	}

	[SerializeField]
	private Color m_backgroundColor = Color.white;
	public Color backgroundColor {
		get {
			return m_backgroundColor;
		}
		set {
			m_backgroundColor = value;
			_UpdateMaterial();
		}
	}

	[SerializeField]
	private Color m_circleColor = Color.yellow;
	public Color circleColor {
		get {
			return m_circleColor;
		}
		set {
			m_circleColor = value;
			_UpdateMaterial();
		}
	}

	[SerializeField]
	private float m_blurFactor = 2.0f;
	public float blurFactor {
		get {
			return m_blurFactor;
		}
		set {
			m_blurFactor = value;
			_UpdateMaterial();
		}
	}
	#endregion

	private Texture2D m_generatedTexture = null;

	// Use this for initialization
	void Start () {
		if (material == null) {
			Renderer renderer = gameObject.GetComponent<Renderer>();
			if (renderer == null) {
				Debug.LogWarning("Cannot find a renderer.");
				return;
			}

			material = renderer.sharedMaterial;
		}

		_UpdateMaterial();
	}

	private void _UpdateMaterial() {
		if (material != null) {
			m_generatedTexture = _GenerateProceduralTexture();
			material.SetTexture("_MainTex", m_generatedTexture);
		}
	}

	private Color _MixColor(Color color0, Color color1, float mixFactor) {
		Color mixColor = Color.white;
		mixColor.r = Mathf.Lerp(color0.r, color1.r, mixFactor);
		mixColor.g = Mathf.Lerp(color0.g, color1.g, mixFactor);
		mixColor.b = Mathf.Lerp(color0.b, color1.b, mixFactor);
		mixColor.a = Mathf.Lerp(color0.a, color1.a, mixFactor);
		return mixColor;
	}

	private Texture2D _GenerateProceduralTexture() {
		Texture2D proceduralTexture = new Texture2D(textureWidth, textureWidth);

		// The interval between circles
		float circleInterval = textureWidth / 4.0f;
		// The radius of circles
		float radius = textureWidth / 10.0f;
		// The blur factor
		float edgeBlur = 1.0f / blurFactor;

		for (int w = 0; w < textureWidth; w++) {
			for (int h = 0; h < textureWidth; h++) {
				// Initalize the pixel with background color
				Color pixel = backgroundColor;

				// Draw nine circles one by one
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						// Compute the center of current circle
						Vector2 circleCenter = new Vector2(circleInterval * (i + 1), circleInterval * (j + 1));

						// Compute the distance between the pixel and the center
						float dist = Vector2.Distance(new Vector2(w, h), circleCenter) - radius;

						// Blur the edge of the circle
						Color color = _MixColor(circleColor, new Color(pixel.r, pixel.g, pixel.b, 0.0f), Mathf.SmoothStep(0f, 1.0f, dist * edgeBlur));

						// Mix the current color with the previous color
						pixel = _MixColor(pixel, color, color.a);
					}
				}

				proceduralTexture.SetPixel(w, h, pixel);
			}
		}

		proceduralTexture.Apply();

		return proceduralTexture;
	}
}
```

```
using UnityEngine;
using UnityEditor;
using System.Collections;

public class RenderCubemapWizard : ScriptableWizard {
	
	public Transform renderFromPosition;
	public Cubemap cubemap;
	
	void OnWizardUpdate () {
		helpString = "Select transform to render from and cubemap to render into";
		isValid = (renderFromPosition != null) && (cubemap != null);
	}
	
	void OnWizardCreate () {
		// create temporary camera for rendering
		GameObject go = new GameObject( "CubemapCamera");
		go.AddComponent<Camera>();
		// place it on the object
		go.transform.position = renderFromPosition.position;
		// render into cubemap		
		go.GetComponent<Camera>().RenderToCubemap(cubemap);
		
		// destroy temporary camera
		DestroyImmediate( go );
	}
	
	[MenuItem("GameObject/Render into Cubemap")]
	static void RenderCubemap () {
		ScriptableWizard.DisplayWizard<RenderCubemapWizard>(
			"Render cubemap", "Render!");
	}
}
```

物体反射到摄像机中的光线方向，可以由光路可逆的原则来反向求得。也就是说，我们可以计算视角方向关于顶点法线的反射方向来求得入射光线的方向。


```
Shader "Unity Shader Book/Chapter 10/Reflection"
{
    
    Properties
    {
        _Color ("Color Tint", Color) = (1,1,1,1)
        _ReflectColor ("Reflect Color", Color) = (1,1,1,1)
        _ReflectAmount ("Relect Amount", Range(0,1)) = 1
        _Cubemap ("Reflecttion Cubmap", Cube)  = "_Skybox" {}
    }
    
    SubShader
    {
        Tags
        {
            "RenderType"="Opaque"
            "Queue"="Geometry"
        }
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"    
            }
            
            CGPROGRAM

            #pragma multi_compile_fwdbase
            
            #pragma vertex vert
            #pragma fragment frag
            #include "Lighting.cginc"
            #include "AutoLight.cginc"
            
            fixed4 _Color;
            fixed4 _ReflectColor;
            fixed _ReflectAmount;
            samplerCUBE _Cubemap;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float3 worldNormal : TEXCOORD0;
                float3 worldPos : TEXCOORD1;
                float3 worldViewDir : TEXCOORD2;
                float3 worldRefl : TEXCPPRD3;
                SHADOW_COORDS(4)
            };


            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.worldNormal = UnityObjectToWorldNormal(v.normal);
                o.worldPos = mul(unity_ObjectToWorld, v.vertex);
                o.worldViewDir = UnityWorldSpaceViewDir(o.worldPos);
                o.worldRefl = reflect(-o.worldViewDir, o.worldNormal);
                
                TRANSFER_SHADOW(o);
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed3 worldNormal = normalize(i.worldNormal);
                fixed3 worldLightDir = normalize(UnityWorldSpaceLightDir(i.worldPos));
                fixed3 worldViewDir = normalize(i.worldNormal);

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz;

                fixed3 diffuse = _LightColor0.rgb * _Color.rgb * saturate(dot(worldNormal, worldLightDir));

                fixed3 reflection = texCUBE(_Cubemap, i.worldRefl).rgb * _ReflectColor.rgb;

                UNITY_LIGHT_ATTENUATION(atten, i,i.worldPos)

                fixed3 color = ambient + lerp(diffuse, reflection, _ReflectAmount) * atten;

                
                return fixed4(color, 1.0);
            }
            
            ENDCG
        }
    }
    Fallback "Reflective/VertexLit"
}
```

## 折射

在这一节中，我们将学习如何在Unity中模拟另一种环境映射常见的应用——折射。
折射的物理原理比反射更复杂一些。我们在初中物理就已经接触过折射的定义：当光线从一种介质（比如空气）斜射进入另一种介质（比如玻璃）时，传播方向一般会发生变化。当给定入射角时，我们可以用斯涅耳定律(Snell'a Law)来计算反射角。公式如下：

$$\eta_1\sin\theta_1 =\eta_2\sin\theta_2 $$

其中，$\eta_1$和$\eta_2$分别是两个介质的折射率，折射率是一项十分重要的物理常数，例如真空的折射率是1，而玻璃的折射率一般是1.5.

通常来说，当得到折射方向后我们就会直接使用它来对立方体纹理进行采样，但是这是不符合物理规律的。对于一个透明物体来说，一种更准确的模拟方法需要计算两次折射——一次是当光线进入它的内部时，而另一次则是它从内部射出的时候。但是想要在实时渲染中模拟出第二次折射方向是比较复杂的，而且仅仅模拟一次得到的效果从视觉上看起来也挺像那么回事的。正如我们之前提到的图形学第一准则如果它看起来是对的，那么它就是对的。因此在实时渲染中，我们通常仅模拟一次折射。

```
Shader "UnityShaderBook/Chapter10/Refraction"
{
    
    Properties
    {
      	_Color ("Color Tint", Color) = (1, 1, 1, 1)
		_RefractColor ("Refraction Color", Color) = (1, 1, 1, 1)
		_RefractAmount ("Refraction Amount", Range(0, 1)) = 1
		_RefractRatio ("Refraction Ratio", Range(0.1, 1)) = 0.5
		_Cubemap ("Refraction Cubemap", Cube) = "_Skybox" {}
    }
    
    SubShader
    {
        Tags
        {
            "RenderType"="Opaque"
            "Queue"="Geometry"
        }
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
            }
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            #pragma multi_compile_fwdbase
            
            #include "Lighting.cginc"
            #include "AutoLight.cginc"

			fixed4 _Color;
			fixed4 _RefractColor;
			float _RefractAmount;
			fixed _RefractRatio;
			samplerCUBE _Cubemap;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
            };

          	struct v2f {
				float4 pos : SV_POSITION;
				float3 worldPos : TEXCOORD0;
				fixed3 worldNormal : TEXCOORD1;
				fixed3 worldViewDir : TEXCOORD2;
				fixed3 worldRefr : TEXCOORD3;
				SHADOW_COORDS(4)
			};
			

            v2f vert(a2v v)
            {
                v2f o;
                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.worldNormal = UnityObjectToWorldNormal(v.normal);
                o.worldPos = mul(unity_ObjectToWorld, v.vertex);
                o.worldViewDir = UnityWorldSpaceViewDir(o.worldPos);

                o.worldRefr = refract(-normalize(o.worldViewDir), normalize(o.worldNormal), _RefractRatio);

                TRANSFER_SHADOW(o);
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed3 worldNormal = normalize(i.worldNormal);
                fixed3 worldLightDir = normalize(UnityWorldSpaceLightDir(i.worldPos));

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz;

                fixed3 diffuse = _LightColor0.rgb * _Color.rgb * saturate(dot(worldNormal, worldLightDir));

                fixed3 refraction = texCUBE(_Cubemap, i.worldRefr).rgb * _RefractColor.rgb;

				UNITY_LIGHT_ATTENUATION(atten, i, i.worldPos);

            	fixed3 color = ambient + lerp(diffuse, refraction, _RefractAmount) * atten;
            	
                return fixed4(color, 1.0);   
            }
            
            ENDCG
        }
    }
    
    Fallback "Reflecive/VertexLit"
}
```

我们使用了CG的refract函数来计算折射方向。它的第一个参数即为入射光线的方向，它必须是归一化之后的矢量；第二个参数是表面法线，法线方向童谣需要是归一化之后的；第三个参数是入射光线所在介质的折射率和折射光线所在介质的折射率之间的比值，例如如果光是从空气射到玻璃表面，那么这个参数应该是空气的折射率和玻璃的折射率之间的比值，即1/1.5.它的返回值就是计算而得的折射方向，它的模则对应入射光线的模。

然后我们在片元着色器中使用折射方向对立方体纹理进行采样。同样，我们也没有对i.worldRefr进行归一化操作，因为对立方体纹理的采样只需要提供方向即可。最后，我们使用_RefractAmount来混合漫反射颜色和折射颜色，并和环境光照相加后返回。


## 菲涅尔反射

在实时渲染中，我们经常会使用菲涅尔反射(Fresnel reflection)来根据视角方向控制反射程度。通俗来讲，菲涅尔反射描述了一种光学现象，即当光线照射到物体表面上时，一部分发生反射，一部分进入物体内部发生折射或散射。被反射的光和入射光之间存在一定的比率关系，这个比率关系可以通过菲涅尔等式进行计算。一个常用的例子就是，当你站在湖边，直接低头看脚边的水面时，你会发现水几乎是透明的，你可以直接看到水底下的小鱼和石子；但是，当你抬头看向远处的水面时，会发现几乎看不到水下的情景，只能看到水表面反射的环境。这就是所谓的菲涅尔效果，事实上，不只是水、玻璃这样的反光物体具有菲涅尔效果，几乎任何物体都或多或少地包含了菲涅尔效果，这是基于物理的渲染中非常重要的一项高光反射计算因子。

那么，我们如何计算菲涅尔反射呢？这就需要使用菲涅尔等式进行计算。真实世界的菲涅尔等式是十分复杂的，但在实时渲染中，我们通常会使用一些近似的公式来计算。其中一个著名的近似公式就是Schlick菲涅尔近似等式：

$$F_{Schlink}(v,n) = F_0 + (1 - F_0)(1 - v \cdot n)^5$$

其中，$F_0$是一个反射系数，用于控制菲涅尔反射的强度，v是视角方向，n是表面法线。另一个应用比较广泛的等式是Empricial菲涅尔近似等式：

$$F_{Empricial}(v,n) = max(0, min(1, bias + scale \times (1 - v \cdot n)^{power}))$$

其中，bias、scale和power是控制项。

使用上面的菲涅尔近似等式，我们可以在边界处模拟反射光强度和折射光强度/漫反射光强之间的变化，在许多车漆、水面等材质的渲染中，我们经常会使用菲涅尔反射来模拟更加真实的反射效果。

```
Shader "UnityShaderBook/Chapter10/Fresnel"{
    Properties
    {
        _Color ("Color Tint", Color) = (1,1,1,1)
        _FresnelScale ("Fresnel Scale", Range(0,1)) = 0.5
        _Cubemap ("Reflection Cubemap", Cube) = "_Skybox" {}
    }
    
    SubShader
    {
        Tags
        {
            "Queue"="Geometry"
            "RenderType"="Opaque"
        }
        
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"   
            }
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            #pragma multi_compile_fwdbase
            
            #include "Lighting.cginc"
            #include "AutoLight.cginc"

            fixed4 _Color;
            float _FresnelScale;
            samplerCUBE _Cubemap;

            struct a2v
            {
                float4 vertex : POSITION;
                float3 normal : NORMAL;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                float3 worldNormal : TEXCOORD0;
                float3 worldPos : TEXCOORD1;
                float3 worldViewDir : TEXCOORD2;
                float3 worldRefl : TEXCOORD3;
                SHADOW_COORDS(4)
            };
            
            v2f vert(a2v v)
            {
                v2f o;

                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.worldNormal = UnityObjectToWorldNormal(v.normal);
                o.worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;
                o.worldViewDir = UnityWorldSpaceViewDir(o.worldPos);
                o.worldRefl = reflect(-o.worldViewDir, o.worldNormal);
                TRANSFER_SHADOW(o);
                
                return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
                fixed3 worldNormal = normalize(i.worldNormal);
                fixed3 worldLightDir = normalize(UnityWorldSpaceLightDir(i.worldPos));
                fixed3 worldViewDir = normalize(i.worldViewDir);

                fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.xyz;

                UNITY_LIGHT_ATTENUATION(atten, i, i.worldPos)
                
                fixed3 reflection = texCUBE(_Cubemap, i.worldRefl).rgb;

                fixed fresnel = _FresnelScale + (1 - _FresnelScale) * pow(1 - dot(worldViewDir, worldNormal), 5);
                
                fixed3 diffuse = _LightColor0.rgb * _Color.rgb * saturate(dot(worldNormal, worldLightDir));

                fixed3 color = ambient + lerp(diffuse, reflection, saturate(fresnel)) * atten;

                return fixed4(color, 1.0);
            }
            
            ENDCG
        }
    }
Fallback "Reflective/VertexLit"
}
```

# 渲染纹理

在之前的学习中，一个摄像机的渲染结果会输出到颜色缓冲中，并显示到我们的屏幕上。现在的GPU允许我们把整个三维场景渲染到一个中间缓冲中，即渲染目标纹理，而不是传统的帧缓冲或后备缓冲。与之相关的是多重渲染目标，这种技术指的是GPU允许我们把场景同时渲染到多个渲染目标纹理中，而不再需要为每个渲染目标纹理单独渲染完整的场景。延迟渲染就是使用多重渲染目标的一个应用。

Unity为渲染目标纹理定义了一种专门的纹理类型——渲染纹理(Render Texture)。在Unity中使用渲染纹理通常有两种方式：一种方式是在Project目录下创建一个渲染纹理，然后把某个摄像机的渲染目标设置成该渲染纹理，这样一来该摄像机的渲染结果就会实时更新到渲染纹理中，而不会显示在屏幕上。使用这种方法，我们还可以选择渲染纹理的分辨率、滤波模式等纹理属性。另一种方式是在屏幕后处理时使用GrabPass命令或者OnRenderImage函数来获取当前屏幕图像，Unity会把这个屏幕图像放到一张和屏幕分辨率等同的渲染纹理中，下面我们可以在自定义的Pass中把它们当作普通的纹理来进行处理，从而实现各种屏幕的特效。我们将依次学习这两种方法在Unity中的实现。

## 镜子效果

在本节中我们将来学习一下如何使用渲染纹理来模拟镜子效果。

最后，为了得到从镜子出发观察到的场景图像，我们还需要创建一个摄像机，并调整它的位置、裁剪平面、视角等，使得它的显示图像是我们希望的镜子图像。由于这个摄像机不需要直接显示在屏幕上，而是用于渲染纹理。因此，我们把上一步中创建的MirrorTexture拖拽到该摄像机的TargetTexture。

镜子实现的原理很简单，它使用一个渲染纹理作为输入属性，并把该渲染纹理在水平方向上翻转后直接显示到物体上即可。在新建的Shader文件中实现以下代码：

```
Shader "UnityShaderBook/Chapter10/Mirror"
{
    Properties
    {
        _MainTex ("MainTex", 2D) = "white" {}
    }
    
    SubShader
    {
        Pass
        {
            Tags
            {
                "LightMode"="ForwardBase"
                "RenderType"="Opaque"
                "Queue"="Geometry"
            }
            
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            sampler2D _MainTex;
            
            struct a2v
            {
                float4 vertex : POSITION;
                float4 texcoord : TEXCOORD0;
            };

            struct v2f
            {
                float4 pos : SV_POSITION;
                fixed4 uv : TEXCOORD0;
            };
            
            v2f vert(a2v v)
            {
                v2f o;
                o.pos = mul(unity_MatrixMVP, v.vertex);
                o.uv = v.texcoord;

                // 镜子效果需要反转x轴
                o.uv.x = 1 - o.uv.x;
                return o;
            }

            fixed4 frag(v2f i) : SV_Target{
                return tex2D(_MainTex, i.uv);
            }
            
            ENDCG
        }
    }
}
```
在上面的代码中我们反转了x分量的纹理坐标，这是因为，镜子里显示的图像都是左右相反的。
保存后返回场景，并把我们创建的MirrorTexture渲染纹理拖拽到材质的MainTex属性中。就可以得到镜子效果了。
在上面的实现中，我们把渲染纹理的分辨率大小设置成256 $\times$ 256。有时，这样的分辨率会使图像模糊不清，此时我们可以使用更高的分辨率或更多的抗锯齿采样等。但是需要注意更高的分辨率会影响带宽和性能，我们应当尽量使用较小的分辨率。

## 玻璃效果

在Unity中，我们还可以在UnityShader中使用一种特殊的Pass来完成获取屏幕图像的目的，这就是GrabPass。当我们在Shader中定义了一个GrabPass后，Unity会把当前屏幕的图像绘制在一张纹理中，以便我们在后续的Pass中访问它。我们通常会使用GrabPass来实现诸如玻璃等透明材质的模拟，与使用简单的透明混合不同，使用grabPass可以让我们对该物体后面的图像进行更加复杂的处理，例如使用法线来模拟折射效果，而不再是简单的屏幕颜色进行混合。

需要注意的时，在使用GrabPass的时候，我们需要额外小心物体的渲染队列的设置。正如之前所说，GrabPass通常用于渲染透明物体。尽管代码里面并不包含混合指令，但我们往往仍然需要把所有的物体的渲染队列设置成透明队列。这样才可以保证，当渲染该物体的时候，所有的不透明物体都已经被绘制在屏幕上，从而获得正确的屏幕图像。

本节中，我们会使用一个GrabPass来模拟玻璃的效果，这种效果的实现非常简单，我们首先使用一张法线纹理来修改模型的法线信息，然后使用了10.1节介绍的反射方法，通过一个Cubmap来模拟玻璃的反射，而在模拟偏移后，再对屏幕图像进行采样模拟近似的折射效果。


我们首先在SubShader的标签中将渲染队列设置成Transparent，尽管在后面的RenderType被设置成了Opaque。这两者看似矛盾，但实际上服务于不同的需求。我们在之前说过，把Queue设置成Transparent可以确保物体渲染时，其他所有的不透明物体都已经被渲染到屏幕上了，否则就可能无法正确得到"透过玻璃看到的图像"。而设置RenderType则是为了在使用着色器替换(Shader Replacement)时，该物体可以在需要时被正确渲染。这通常发生在我们需要得到摄像机的深度和法线纹理时，这会在十三章的时候讲到。

随后，我们通过关键词GrabPass定义了一个抓取屏幕图像的Pass，在这个pass中我们定义了一个字符串，这个字符串内部的名称决定了抓取得到的屏幕图像会被存入哪个纹理中。实际上，我们可以省略声明该字符串，但直接声明纹理名称的方法往往可以得到更高的性能，具体原因见本节最后的部分。


```
// Upgrade NOTE: replaced '_Object2World' with 'unity_ObjectToWorld'

Shader "UnityShaderBook/Chapter10/GlassRefraction"
{
    Properties
    {
       	_MainTex ("Main Tex", 2D) = "white" {}
		_BumpMap ("Normal Map", 2D) = "bump" {}
		_Cubemap ("Environment Cubemap", Cube) = "_Skybox" {}
		_Distortion ("Distortion", Range(0, 100)) = 10
		_RefractAmount ("Refract Amount", Range(0.0, 1.0)) = 1.0
    }
    
    SubShader
    {
        Tags
        {
            "Queue"="Transparent"
            "RenderType"="Opaque"
        }
        
        GrabPass{"_RefractionTex"}
        
        Pass
        {
            CGPROGRAM

            #pragma vertex vert
            #pragma fragment frag

            #include "UnityCG.cginc"
			sampler2D _MainTex;
			float4 _MainTex_ST;
			sampler2D _BumpMap;
			float4 _BumpMap_ST;
			samplerCUBE _Cubemap;
			float _Distortion;
			fixed _RefractAmount;
			sampler2D _RefractionTex;
			float4 _RefractionTex_TexelSize;
            
           	struct a2v {
				float4 vertex : POSITION;
				float3 normal : NORMAL;
				float4 tangent : TANGENT; 
				float2 texcoord: TEXCOORD0;
			};
			
			struct v2f {
				float4 pos : SV_POSITION;
				float4 scrPos : TEXCOORD0;
				float4 uv : TEXCOORD1;
				float4 TtoW0 : TEXCOORD2;  
			    float4 TtoW1 : TEXCOORD3;  
			    float4 TtoW2 : TEXCOORD4; 
			};

            v2f vert(a2v v)
            {
              	v2f o;
				o.pos = UnityObjectToClipPos(v.vertex);
				
				o.scrPos = ComputeGrabScreenPos(o.pos);
				
				o.uv.xy = TRANSFORM_TEX(v.texcoord, _MainTex);
				o.uv.zw = TRANSFORM_TEX(v.texcoord, _BumpMap);
				
				float3 worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;  
				fixed3 worldNormal = UnityObjectToWorldNormal(v.normal);  
				fixed3 worldTangent = UnityObjectToWorldDir(v.tangent.xyz);  
				fixed3 worldBinormal = cross(worldNormal, worldTangent) * v.tangent.w; 
				
				o.TtoW0 = float4(worldTangent.x, worldBinormal.x, worldNormal.x, worldPos.x);  
				o.TtoW1 = float4(worldTangent.y, worldBinormal.y, worldNormal.y, worldPos.y);  
				o.TtoW2 = float4(worldTangent.z, worldBinormal.z, worldNormal.z, worldPos.z);  
				
				return o;
            }

            fixed4 frag(v2f i) : SV_Target
            {
				float3 worldPos = float3(i.TtoW0.w, i.TtoW1.w, i.TtoW2.w);
				fixed3 worldViewDir = normalize(UnityWorldSpaceViewDir(worldPos));
				
				// Get the normal in tangent space
				fixed3 bump = UnpackNormal(tex2D(_BumpMap, i.uv.zw));	
				
				// Compute the offset in tangent space
				float2 offset = bump.xy * _Distortion * _RefractionTex_TexelSize.xy;
				i.scrPos.xy = offset * i.scrPos.z + i.scrPos.xy;
				fixed3 refrCol = tex2D(_RefractionTex, i.scrPos.xy/i.scrPos.w).rgb;
				
				// Convert the normal to world space
				bump = normalize(half3(dot(i.TtoW0.xyz, bump), dot(i.TtoW1.xyz, bump), dot(i.TtoW2.xyz, bump)));
				fixed3 reflDir = reflect(-worldViewDir, bump);
				fixed4 texColor = tex2D(_MainTex, i.uv.xy);
				fixed3 reflCol = texCUBE(_Cubemap, reflDir).rgb * texColor.rgb;
				
				fixed3 finalColor = reflCol * (1 - _RefractAmount) + refrCol * _RefractAmount;
				
				return fixed4(finalColor, 1);
            }
            
            ENDCG
        }
    }
Fallback "Diffuse"
}
```

需要注意的是，我们还定义了`_RefactionTex`和`_RefactionTex_TexelSize`变量，这对应了用GrabPass时指定的纹理名称，`_RefactionTex_TexelSize`可以让我们得到该纹理的纹素大小。我们需要在对屏幕图像的采样坐标进行偏移时使用该变量。

然后，我们在顶点着色器中，进行了必要的顶点坐标变换后，我们通过调用内置的ComputeGrabScreenPos函数来得到对应被抓取的屏幕图像的采样坐标。我们可以在UnityCG.cginc文件中找到它的声明，它的主要代码和ComputeScreenPos基本类似，最大的不同时针对平台差异造成的采样坐标问题进行了处理。接着，我们计算了_MainTex和_BumpMap的采样坐标，并把它们分别存储在了一个float4类型变量的xy和zw中。由于我们需要在片元着色器中把法线方向从切线空间变换到世界空间下，以便对Cubemap进行采样，因此我们需要在这里计算该顶点对应的从切线空间到世界空间的变换矩阵，并把该矩阵的每一行分别存储在TtoW0、TtoW1和TtoW2的xyz分两种。这里面使用的数学方法就是，得到切线空间下的3个坐标轴(xyz轴分别对应了切线、副切线和法线的方向)在世界坐标空间下的表示，再把它们依次按列组成一个变换矩阵即可。TtoW0等值的w轴被同样利用起来，用于存储世界空间下的顶点坐标。

然后，在片元着色器中，我们首先通过TtoW0等变量的w分量得到世界坐标，并用该值得到该片元对应的视角方向。随后，我们对法线纹理进行采样，得到切线空间下的法线方向。我们使用该值和_Distortion属性以及_RefractionTex_TexelSize来对屏幕图像的采样坐标进行偏移，模拟折射效果。_Distortion值越大，偏移量越大，玻璃背后的物体看起来变形程度就越大。在这里，我们选择使用切线空间下的法线方向来进行偏移，这是因为该空间下的法线可以反映顶点局部空间下的法线方向。随后，我们对scrPos透视除法得到真正的屏幕坐标，再使用该坐标对抓取的屏幕图像_RefractionTex进行采样，得到模拟的折射颜色。

之后，我们把法线方向从切线空间变换到了世界空间下(使用变换矩阵的每一行，即TtoW0、TtoW1和TtoW2，分别和法线方向点乘，构成新的法线方向)，并据此得到视角方向相对于法线方向的反射方向。随后使用反射方向对Cubemap进行采样，并把结果和主纹理颜色相乘后得到反射颜色。

最后，我们使用_RefratAmount属性对反射和折射颜色进行混合，作为最终的输出颜色。

在前面的实现中，我们在GrabPass中使用一个字符串指明了被抓取的屏幕图像将会被存储在哪个名称的纹理中。实际上，GrabPass支持两种形式。
- 直接使用GrabPass{}，虽然后在后续的Pass中直接使用_GrabTexture来访问屏幕图像。但是，当场景中有多个物体都使用了这样的形式来抓取屏幕时，这种方法的性能消耗比较大，因为对于每一个使用它的物体，Unity都会为它单独进行一次昂贵的屏幕抓取操作。但这种方法可以让每个物体得到不同的瓶木图像，这取决于它们的渲染队列以渲染它们时当前的屏幕缓冲中的颜色。
- 使用GrabPass{"TextureName"}，正如本节中的实现，我们可以在后续的Pass中使用TextureName来访问屏幕图像，使用这种方法同样可以抓取屏幕，但是Unity只会在每一帧时为第一个使用名为TextureName的纹理的物体进行依次抓取屏幕的操作，这个纹理同样可以在其他的Pass中被访问。这种方法更加高效，因为不管场景中有多少个物体使了该命令，每一帧中Unity都会执行一次抓取工作，但这也意味着所有物体都会使用同一张屏幕图像。不过在大多数情况下这已经足够了。

## 渲染纹理 vs. GrabPass

尽管GrabPass和10.2.1节中使用的渲染纹理 + 额外摄像机的方式都可以抓取屏幕图像，但是它们之间还是有一些不同的。GrabPass的好处在于实现简单，我们只需要在Shader中写几行代码就可以实现抓取屏幕的目的。而要渲染纹理的话，我们首先需要创建一个渲染纹理和一个额外的摄像机，再把摄像机的RenderTarget设置为新建的渲染纹理对象，最后把该渲染纹理传递给相应的Shader。

但是从效率上来讲，使用渲染纹理的效果往往要好于GrabPass，尤其是在移动设备上。使用渲染纹理我们可以自定义渲染纹理的大小，尽管这种方法需要把部分场景再渲染一次，但我们可以通过调整摄像机额渲染层来减少二次渲染时的场景大小，或使用其他方法来控制摄像机是否开启。而GrabPass获取到的图像分辨率和显示屏幕是一致的，这意味着再一些高分辨率的设备上可能会造成严重的带宽影响。而再移动设备上，GrabPass虽然不会重新渲染场景，但它往往需要CPU直接读取后备缓冲中的数据，破坏了CPU和GPU之间的并行性，这样做是比较耗时的，甚至在一些移动设备上这是不支持的。

现在的Unity引入了CommandBuffer来允许我们扩展Unity的渲染流水线。使用命令缓冲我们也可以得到类似抓屏的效果，它可以在不透明的物体渲染后把当前的图像复制到一个临时的渲染目标纹理中，然后在那里进行一些额外的操作，例如模糊等，最后把图像传递给需要使用它的物体进行处理和显示。当然CommandBuffer能够实现的效果还远远不止这些。

# 程序纹理

程序纹理是指那些用由计算机生成的图像，我们通常使用一些特定的算法来创建个性化图案或者非常真实的自然元素，例如木头、石子等。使用程序纹理的好处在于我们可以使用各种参数来控制纹理的外观，而这些属性不仅仅是那些颜色属性，甚至可以是完全不同类属性的图案属性，这使得我们可以得到更加丰富的动画和视觉效果。在本节中，我们会首先尝试使用算法来实现一个非常简单的程序材质，然后我们会介绍Unity中一类专门使用程序纹理的材质——程序材质。
## 在Unity中实现简单的程序纹理

首先，我们新建一个材质，使用一个第7章中我们编写的SingleTexture的Shader，我们不会给这个材质添加纹理，这是因为我们希望使用脚本来创建程序纹理。为此，我们要再创建一个脚本ProceduralTextureGeneration.cs，并把这个脚本添加得到我们在场景中创建的一个Cube的模型上。

ProceduralTextureGeneration.cs脚本内容如下：
```
using UnityEngine;

[ExecuteInEditMode]
public class ProceduralTextureGeneration : MonoBehaviour
{
    public Material Material = null;

    #region Material properties

    [SerializeField] private int m_textureWidth = 512;

    private int TextureWidth => m_textureWidth;

    [SerializeField] private Color m_backgroundColor = Color.white;

    public Color backgroundColor
    {
        get
        {
            return m_backgroundColor;
        }

        set
        {
            m_backgroundColor = value;
            UpdateMaterial();
        }
    }

    [SerializeField] private Color m_circleColor = Color.yellow;
    private Color circleColor
    {
        get
        {
            return m_circleColor;
        }
        set
        {
            m_circleColor = value;
            UpdateMaterial();
        }
    }

    [SerializeField] private float m_blurFactor = 2.0f;

    public float blurFactor
    {
        get
        {
            return m_blurFactor;
        }

        set
        {
            m_blurFactor = value;
            UpdateMaterial();
        }
    }
    #endregion


    private Texture2D m_generatedTexture = null;
    private static readonly int MainTex = Shader.PropertyToID("_MainTex");

    private void Start()
    {
        if (Material == null)
        {
            Renderer renderer = gameObject.GetComponent<Renderer>();
            if (renderer == null)
            {
                Debug.LogError("Can not find a renderer");
                return;
            }

            Material = renderer.material;
        }
        
        UpdateMaterial();
    }

    private void UpdateMaterial()
    {
        if (Material == null)
        {
            return;
        }

        m_generatedTexture = _GenerateProceduralTexture();
        Material.SetTexture(MainTex, m_generatedTexture);
    }
    
    private Texture2D _GenerateProceduralTexture()
    {
        Texture2D proceduralTexture = new Texture2D(TextureWidth, TextureWidth);
        
        var circleInterval = TextureWidth / 4.0f;
        var radius = TextureWidth / 10.0f;
        var edgeBlur = 1.0f / blurFactor;

        for (int w = 0; w < TextureWidth; w++)
        {
            for (int h = 0; h < TextureWidth; h++)
            {
                Color pixel = backgroundColor;
                
                // 依次画九个圆
                for (int i = 0; i < 3; i++)
                {
                    for (int j = 0; j < 3; j++)
                    {
                        Vector2 circleCenter = new Vector2(circleInterval * (i + 1), circleInterval * (j + 1));

                        float dist = Vector2.Distance(new Vector2(w, h), circleCenter) - radius;

                        Color color = _MixColor(circleColor, new Color(pixel.r, pixel.g, pixel.b, 0f),
                            Mathf.SmoothStep(0f, 1.0f, dist * edgeBlur));

                        pixel = _MixColor(pixel, color, color.a);

                    }
                }
                proceduralTexture.SetPixel(w,h, pixel);
            }
        }
        proceduralTexture.Apply();
        return proceduralTexture;
    }
    
    private Color _MixColor(Color color0, Color color1, float mixFactor) {
        Color mixColor = Color.white;
        mixColor.r = Mathf.Lerp(color0.r, color1.r, mixFactor);
        mixColor.g = Mathf.Lerp(color0.g, color1.g, mixFactor);
        mixColor.b = Mathf.Lerp(color0.b, color1.b, mixFactor);
        mixColor.a = Mathf.Lerp(color0.a, color1.a, mixFactor);
        return mixColor;
    }

}

```

## Unity的程序材质

在Unity中，有一类 专门使用程序纹理的材质，叫做程序材质(Procedural Materials)。这类材质和我们之前使用的那些材质在本质上是一样的，不同的是，它们使用的纹理不是普通的纹理，而是程序纹理。需要注意的是，程序材质和它使用的程序纹理并不是在Unity中创建的，而是使用一个叫Substance Designer的软件在Unity外部生成的。

[【Unity3D】程序纹理简单应用](https://www.cnblogs.com/zhyan8/p/17760973.html)