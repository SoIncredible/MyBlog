---
title: Unity问题杂记
tags: 
  - 问题杂记
categories: 硬技能
abbrlink: 127bc3c9
date: 2022-12-19 18:13:50
cover: https://www.notion.so/images/page-cover/met_joseph_hidley_1870.jpg
description: 
swiper_index: 3
sticky: 3
---

> 本篇博客记录笔者在Unity开发中遇到的各种小问题，有可能是Unity奇怪的bug、Unity Editor的使用小技巧或者是一些不值得开一篇新博客的小知识点

# Unity Scriptable Object踩坑

下面是一个名为`EntityBaseProperty.cs`的脚本，但是在创建一个`PlayerEntityProperty`SO的时候会提示你找不到这个脚本。你必须让脚本的名字和SO的类名保持一致，所以每一个SO都要新建一个和SO类命一样的脚本来写。
```C#
public class EntityBaseProperty : ScriptableObject
{
    
}

[CreateAssetMenu(fileName = "PlayerEntityProperty", menuName = "CreatePlayerEntityProperty", order = 0)]
public class PlayerEntityProperty : CharacterProperty
{
    
}

```

# Unity项目代码正常运行但是在Rider中大量报红

Rider的`solution wide analysis`组件有问题，在Unity的`Settings -> External Tools -> Regenerate project files`解决。
也有可能是Unity编译项目的dll出问题了, 删除掉Unity工程中`Library/ScriptAssemblies`目录, 重新打开Unity, 让它重新编译一下

# GetComponentsInChildren接口

GetComponentsInChildren方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# GetComponentInParent接口

GetComponentInParent方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# DoTween

DoTween不仅可以用来做动画，也可以实现音频减弱、图片的渐显操作。

# Spine动画的使用

Spine动画有一个专门针对UI的组件叫做SkeletonGraphic，SkeletonGraphic组件是基于UICanvas绘制的，因此它的渲染层级可以被Canvas管理
另外还有专门为非UI播放动画的SkeletonAnimation组件，该组件的渲染是基于MeshRenderer的，因此不受Canvas的管理，当要在UI上展示Spine动画的时候，要选择使用SkeletonGraphic组件。

SkeletonGraphic相关接口
```C#
// 停止当前正在播放的动画
// heartBeatFlower.AnimationState.ClearTrack(0);    
heartBeatFlower.AnimationState.SetEmptyAnimation(0,0); 
// 播放新的动画
heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  
```
> 2024.11.8更新
> 使用SkeletonGraphic.AnimationState.ClearTrack(0)遇到坑了，目前笔者还不清楚Spine的作用原理，从表现上看，调用该接口会将Spine动画从轨道上移除，之后想要再次播放该动画的话就无法在轨道上找到这个动画，所以如果有切换播放动画的需求，只需要调用heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  就可以了

SkeletonAnimation相关接口
```C#
// 停止当前正在播放的动画
heartBeatFlower.state.SetEmptyAnimation(0,0);            
// 播放新的动画
heartBeatFlower.state.SetAnimation(0, "loop", true);  
```

# Animator使用

Animator中必须设置一个从Entry进入的默认状态，这个从Entry进入默认状态的操作会在Animator所挂载的游戏物体的Active状态变为true或者Animator组件自身的enable状态变为true的时候自动执行，无法控制。如果在默认状态设置了某些动画，在其他开发同事不知情的情况下设置了这个Animator节点的Active状态，就会导致动画的自动触发，可能会给别人留坑。更好的使用Animator的方法是将默认状态设置为一个空状态。让真正的动画状态指向默认状态，如下图：

![](Unity问题杂记/image.png)

在我们需要播放动画的时机可以通过[`Animator.Play()`](https://docs.unity3d.com/cn/current/ScriptReference/Animator.Play.html)接口，并且在动画播放完之后会自动的进入默认状态，这样的好处是不用通过控制Active状态来控制动画的播放，而且在代码中我们也是通过`Animator.Play`接口控制动画的播放，比通过Active状态来控制的方法更让人知道这行代码在做什么。

接口的具体参数如下，其中第三个参数normalizedTime是一个归一化的时间，[0,1]指从动画的什么时刻开始播放
```C#
public void Play (string stateName, int layer= -1, float normalizedTime= float.NegativeInfinity);
public void Play (int stateNameHash, int layer= -1, float normalizedTime= float.NegativeInfinity);
```
> 2024.12.18更新
> 最近尝试了一种新的延时方法,以前在处理当某一动画播放完毕后,执行一段逻辑的时候,都是用
> 下面这两种方法都可以对当前的Animator播放动画的normalizedTime做调整
> ![](Unity问题杂记/image-1.png)
> 2025.6.18更新
> 更健壮的版本 TODO Eddie 这个问题能不能抽成
> Unity的Animator太难用了 笔者是希望在动画播放完成之后 执行某个操作 Animator只需要给我一个回调的接口就可以 这样就不需要我自己来实现了.
> Animacer插件应该是有这个接口的, 需要验证一下
```C#
m_ddz_zhounianqing_JY12_zhuanchang_GameObject.SetActiveEx(true);
yield return null;
m_ddz_zhounianqing_JY12_zhuanchang_Animator.Play("ddz_jiyang_12nian_zhuanchang", 0, 0);

// 等待进入动画状态
float waitAnimStateTimeout = 2f, timer = 0f;
while (!m_ddz_zhounianqing_JY12_zhuanchang_Animator.GetCurrentAnimatorStateInfo(0)
            .IsName("ddz_jiyang_12nian_zhuanchang"))
{
    yield return null;
    timer += Time.deltaTime;
    if (timer > waitAnimStateTimeout) {
        QDebug.LogError("动画没有切换到目标状态！");
        break;
    }
}

// 等待动画非循环情况下正常播放结束
timer = 0f;
float waitAnimPlayTimeout = 10f;
while (m_ddz_zhounianqing_JY12_zhuanchang_Animator.GetCurrentAnimatorStateInfo(0)
            .normalizedTime < 1.0f)
{
    yield return null;
    timer += Time.deltaTime;
    if (timer > waitAnimPlayTimeout) {
        QDebug.LogError("动画播放超时，可能动画clip循环/没切到/速度很慢！");
        break;
    }
}
QDebug.Log("转场结束");
m_ddz_zhounianqing_JY12_zhuanchang_GameObject.SetActiveEx(false);
```

# Animation体积优化的问题
https://blog.uwa4d.com/archives/UWA_Pipeline22.html

# DOTweenAnimation组件使用

DOTweenAnimation组件挂载在节点上，调用播放的时候只播第一次，之后不播了，需要搞清楚为什么

# 协程的坑

- 协程中的等待一秒并不是真正的一秒有可能会有误差
- 提示要执行Coroutine的物体的状态是inactive的
一个GameObject,在同一帧内,先被SetActive了,然后紧接着用这个GameObject上的一个Mono执行StartCoroutine,会提示GameObject无法被执行,验证是否是这样的.

# Unity的Animation的使用的坑

Animation中有一个Legacy字段，如果不勾选这个选项，在Animation中是没有办法通过Animation.Play()接口播放该动画的。
有兴趣可以做一个实验，创建一个勾选了Legacy和一个未勾选Legacy的动画，将这两个动画都添加到一个Animation组件上。在代码中获取这个Animation组件的引用，调用`Animation.GetClipCount()`接口得到的值为2，但是如果使用`foreach(AnimationState state in Animation)`去遍历却只会遍历到勾选了Legacy的AnimationClip

# XML文件读取逻辑

定义需要从XML中读取的数据结构

```C#
[XmlRoot("AssetBundleConfig")]
public class AssetBundleConfig
{
    public List<BundleRule> Bundles;
    
    public List<AtlasRule> Atlas;
}


public class BundleRule
{
    public string Relative;
    public string Path;
    public string Type;
    public bool Recursion;
}

public class AtlasRule
{
    public string Path;
    public bool Recursion;
}
```
定义XML文件内容
```xml
<?xml version="1.0" encoding="utf-8" ?>
<AssetBundleConfig xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:noNamespaceSchemaLocation="AssetBundleConfigSchema.xsd">
    <!--带有平台名称的bundle只会出现名称所代表的平台-->
    <Bundles>
        <BundleRule name="Art_Animation">
            <Path>Assets/Art/Animation</Path>
            <Type>SubFolder</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_Audio">
            <Path>Assets/Art/Audio</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_CustomShaders">
            <Path>Assets/Art/CustomShaders</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_Fonts">
            <Path>Assets/Art/Fonts</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>
        
        <BundleRule name="Art_Fonts8x8">
            <Relative>Assets/Art/Fonts</Relative>
            <Path>Assets/Art/Fonts/Fonts8x8</Path>
            <Type>SubFolder</Type>
            <Recursion>true</Recursion>
        </BundleRule>

    </Bundles>
    
    <Atlas>
        <AtlasRule name="Art_Atlas">
            <Path>Assets/Art/Texture/Atlas</Path>
            <Recursion>true</Recursion>
        </AtlasRule>
    </Atlas>
</AssetBundleConfig>
```

C#脚本读取该XML文件
```C#
public static T LoadXmlConfig<T>(string path) where T : class
{
    XmlSerializer xmlSerializer = new XmlSerializer(typeof(T));
    T result;
    using (var reader = XmlReader.Create(path))
    {
        result = (T) xmlSerializer.Deserialize(reader);
    }

    return result;
}
```
调用时只需要把`AssetBundleConfig`作为T传入该方法，就可以返回XML的内容


# 写一个假的进度条

```C#
using System;
using UnityEngine;
using UnityEngine.UI;

namespace UI
{
    public class LoadingUICartoon : LoadingUI
    {
        [SerializeField] private Text progressText;
        
        private float currentValue = 0f; // 当前值
        private float targetValue = 100f; // 目标值
        private float speed = 1f; // 增长速度
        private bool endLoading = false;
        
        protected override void Start()
        {
            canvas.sortingLayerID = R.SortingLayers.Default;
            progressText.text = "0%";
        }

        private void Update()
        {
            if (endLoading)
            {
                return;
            }
            
            // 使用指数衰减的方式逐渐接近目标值
            currentValue += (targetValue - currentValue) * speed * Time.deltaTime;

            // 更新Text组件的显示内容
            progressText.text = Mathf.FloorToInt(currentValue).ToString() + "%";
        }

        public void SetProgress(float progress)
        {
            endLoading = true;
            progressText.text = progress.ToString("0") + "%";
        }
        
    }
}
```

# Unity中对导入的资产进行自动化的导入格式设置


# Unity预制体中的Image组件内容不显示

https://blog.csdn.net/WGYHAPPY/article/details/116161817

# 使用摄像机渲染一个RenderTexture 以及 我想要把一个Canvas放到世界坐标下,即不会在上层嵌套一个UICanvas

https://blog.csdn.net/qq_37179591/article/details/118148818

# Mac下如何打出乘号
https://www.zhihu.com/question/20367435

# 查询资源引用

对该工具进行扩充, 变成一个资产库工具, 能够方便开发者快速的管理资源
见[GitHub](https://github.com/SoIncredible/UnityUtils)工具库
Unity插件[FR2](https://docs.google.com/document/d/1M3drHsRSCNKk-colYnCYECM_TR2XgtGB_8GFc_La19E/edit?tab=t.0#heading=h.z6ne0og04bp5)

# Unity中合并两个Mesh

https://blog.csdn.net/qq_42980269/article/details/123064307

# 在Unity工程中使用相对路径

如果你在C#中看到这样的目录:` configFilePath = $"../Config/Excel/s/{Options.Instance.StartConfig}/{configType.Name}.bytes"; `,它代表的是跟Unity工程的根目录同级下有一个Config目录:
```
Root
 - Unity工程目录
 - Config目录
```
所以如果你要使用AssetDatabase加载某一个资源的话,资源的路径一定是"Assets/.."起手的

# Unity关闭自动编译

在2020.3.48f1中`Preference`->General一栏中取消勾选`Auto Refresh`,但是在2022.3.15f1版本中笔者并没有找到该字段. 

# Unity 颜色十六进制和RGB之间的转换方式
https://blog.csdn.net/f_957995490/article/details/120727626


# 让一段代码只在Editor模式下运行

两种方式, 一种方式是将代码脚本放到Editor目录下, 另一种是将脚本放在Runtime下, 但是用宏包裹起来, 如果放在Editor下的话则不能挂载到节点上.

# GUID与FileID

一个fbx文件中可能有多个子模型 或者说的更普遍的情况: 一个被AssetDatabase收录的资产中可能包含多个部分, 需要通过guid+fileId的方式才能定位到一个资产

FileID中并没有被序列化在meta文件中, 在翻阅的Unity的源码之后, 找到了FileID的生成逻辑:

在`YAMLNode.cpp`脚本里面, YAMLMapping方法, 用来向meta文件中添加fileID, FileId被PersistentManager管理
```c++
YAMLMapping::YAMLMapping(const PPtr<Object>& value) : useInlineStyle(true)
{
    GetPersistentManager().Lock();

    SerializedObjectIdentifier identifier;
    if (GetPersistentManager().InstanceIDToSerializedObjectIdentifier(value.GetInstanceID(), identifier))
    {
        FileIdentifier id = GetPersistentManager().PathIDToFileIdentifierInternal(identifier.serializedFileIndex);
        Append("fileID", identifier.localIdentifierInFile);
        Append("guid", id.guid);
        Append("type", id.type);
    }
    GetPersistentManager().Unlock();
}
```

上面是取的, 在`AssetImporter.cpp`中, 根据导入资产的名字和资产的类型生成一个id

```c++
LocalIdentifierInFileType AssetImporter::GenerateFileIDHashBased(const Unity::Type* type, const core::string& name)
{
    MdFourGenerator mdfourGen;
    mdfourGen.Feed(static_cast<int>(type->GetPersistentTypeID()));
    mdfourGen.Feed(name);
    Hash128 Hash128 = mdfourGen.Finish();
    LocalIdentifierInFileType fileID = (SInt32)(*reinterpret_cast<UInt32*>(&Hash128));
    Assert(m_FileIDToRecycleName.empty());

    if (m_UsedFileIDs.count(fileID) == 1 || fileID == kAssetImporterFileID || fileID == kAssetMetaDataFileID)
        return 0;
    else
        return fileID;
}
```
- [Unity的序列化中的几个概念：“GUID”、“Local ID”、“ Instance ID”](https://blog.csdn.net/qq_33060405/article/details/147315678)
- [unity fileID vs GUID](https://zhuanlan.zhihu.com/p/654506392)
- [Unity文件、文件引用、Meta详解](https://blog.csdn.net/qq_17758883/article/details/105345454)

# Unity的序列化中的几个概念：“GUID”、“Local ID”、“ Instance ID”

[文档](https://blog.csdn.net/qq_33060405/article/details/147315678)


# Unity中的PhysicsRaycaster为什么要继承UIBehaviour

RectTransformUtility.ScreenPointToLocalPointInRectangle接口

# Unity中的对称按钮

有一类需求, 会出一个按钮, 向左的, 然后要你水平翻转一下变成向右的 注意要用RectTransform的scale 不要用Rotation 因为Rotation会把这个图片的正面反转的背离摄像机 这样射线就检测不到这个图片了



[Unity控制台输出过多不显示的解决方案](https://www.cnblogs.com/tian98/p/15623292.html)


# Unity中的Awake

如果一个节点实例化出来被设置为了active为false, 其Awake方法就不会执行, 所以不应该使用Unity提供的生命周期


Unity也有Assert功能



# Unity中的OffScreenRender离屏渲染

在 **Unity** 或图形学开发中，“**Off Screen Render**” 或 “**Offscreen Rendering**” 通常指的是“**离屏渲染**”。

---

## 1. 直观解释

**OffScreen Render（离屏渲染）**：  
不是直接将画面渲染到屏幕（monitor）上，而是渲染到某个内存区域（如 RenderTexture、Frame Buffer）的过程。

- **On Screen** 渲染：把画面绘制到显示设备上用户直接看到
- **Off Screen** 渲染：画面先渲染到一个**纹理**或缓冲区，之后你可以对它做后处理、保存、合成、变换，最后再决定怎么展示或用

---

## 2. Unity中的典型用法

在 Unity 里，**离屏渲染**主要体现在以下场景：

### a. RenderTexture
最常用的离屏渲染就是**渲染到 RenderTexture**，比如：

- **摄像机（Camera）输出**不直接显示，而是输出到一个 RenderTexture
- 后期处理（Post Process），比如模糊、特效
- Mini Map（小地图）、分屏、投影、镜子、监控摄像头

**示例**：

```csharp
public Camera cam;
public RenderTexture myRT;

void Start()
{
    cam.targetTexture = myRT;
}
```
这样，摄像机渲染内容就**不会直接显示在屏幕上**，而是画到 myRT 里，你可以贴到 UI、另一个物体等任意想用的地方。

### b. Texture合成或图片保存
- 把场景渲染到 RenderTexture，再通过 `Texture2D.ReadPixels` 等读取数据，保存为 PNG、异步上传等

### c. 特殊效果
- 画面截图、动态环境贴图、实时反射球等都要离屏渲染

---

## 3. 图形原理解释

离屏渲染在底层其实是：
- 创建一个“帧缓冲区”（Frame Buffer Object, FBO），或者用 RenderTexture 作为目标
- 将渲染管线的输出写入内存而不是实际屏幕
- 后续可以把这块内存的数据用于其它用途（二次渲染、后期处理、特效合成等）

---

## 4. 为什么要 OffScreen Render?

- 方便实现后处理特效（例如 HDR, Bloom, VFX 类效果先渲染到纹理，再处理再显示）
- 实现多视角展示（分屏、监控、全景图、小地图）
- 做动态贴图，比如车漆反射、摄像头画面、视频等
- 截图及保存画面
- 编辑器工具、UI复杂效果

---

## 5. 总结一句话

**OffScreen Render 在 Unity 就是“把渲染结果输出到一个纹理或者缓冲区内存”，而不是直接输出到屏幕，便于实现特效、截图、合成等各种高级功能。**

--
如果你需要具体离屏渲染场景的代码例子或不同平台的兼容问题，可以告诉我你的需求！

# Unity中的LOD


在 Unity 中，**LOD** 指的是 "**Level of Detail**" —— 细节层次（或称多层次细节）。  
它是一种常见的三维图形渲染优化技术，用于提高渲染效率、减少资源消耗，同时保证画面质量。

---

## LOD（Level of Detail）是什么概念？

- **简单说**：  
  对同一个模型，准备多组不同复杂度的版本（高、中、低多边形数），距离近时渲染高细节版本，距离远时自动切换为低细节版本。
- **为什么用 LOD？**  
  在大场景或有大量模型时，如果都用高细节模型画面会卡顿、瞬间渲染三角形数太多。  
  远处的小物体细节肉眼看不清，多边形再多也没意义，因此远距离渲染低细节模型可以提升性能。
- **Unity LOD 系统**就是帮你自动做模型切换的机制。

---

## Unity LOD 的具体用法

1. 准备不同复杂度的模型（一般3个版本，如 LOD0、LOD1、LOD2）
   - LOD0：最近距离、最高细节
   - LOD1：中等距离、中细节
   - LOD2：最远距离、最低细节（甚至可用一个小盒子、或直接隐藏）

2. 使用 **LOD Group 组件**
   - 在 Unity 的 Hierarchy 里选中物体，点击“Add Component” → “LOD Group”
   - 在组件中可以设置每个 LOD 使用的模型、每个层级切换的阈值（距离/屏幕百分比）

3. 运行时，Unity 会自动根据摄像机距离切换不同层级，提升性能又不影响近距离效果。

---

## 图示（简化版）

| 层级 | 距离与你远近  | 显示模型      | 多边形数量         |
|------|------------|------------|--------------------|
| LOD0 | 很近        | 高细节模型  | 20000（三角形）    |
| LOD1 | 一般        | 中细节模型  | 4000（三角形）     |
| LOD2 | 很远        | 简单/隐藏   | 500（三角形）      |

---

## 应用场景

- 大场景的山峰、建筑、树木、角色等远景物体
- VR、手游等对性能特别敏感的场景

---

## 拓展

- LOD 不仅限于 3D mesh，还可以用在贴图、特效等其他需要多细度方案的地方。
- Unity LOD Group 可以和 Static Batching、Occlusion Culling、GPU Instancing 搭配进一步优化性能。

---

**总结：**
> Unity中的LOD是一种自动切换模型精度的优化技术，让复杂模型在远距离时用简化版本渲染，以提高性能，使用LOD Group组件管理模型的各个层级细节与距离切换。