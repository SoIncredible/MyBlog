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



需要说明的是，在

在Unity中，天空盒子是在所有不透明物体之后渲染的，而其被后使用的网格是一个立方体或一个细分后的球体。

## 创建用于环境映射的立方体纹理

除了天空盒子，立方体纹理最常见的用处就是用于环境映射。通过这种方法，我们可以模拟出具有金属质感的材质。

在Unity中创建用于环境映射的立方体纹理的方法有三种：第一种方法是直接由一些特殊布局的纹理创建；第二种方法是手动创建一个Cubemap资源，再把6张图赋给它；第三种方法是由脚本生成。

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

# 渲染纹理

## 镜子效果

## 玻璃效果

## 渲染纹理 vs. GrabPass

# 程序纹理

## 在Unity中实现简单的程序纹理

## Unity的程序材质