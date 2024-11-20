---
title: Unity问题杂记
tags: 
  - Unity
categories: 学习笔记
abbrlink: 127bc3c9
date: 2022-12-19 18:13:50
cover: https://www.notion.so/images/page-cover/met_joseph_hidley_1870.jpg
description: 
---

> 本篇博客记录笔者在Unity开发中遇到的各种小问题，有可能是Unity奇怪的bug、Unity Editor的使用小技巧或者是一些不值得开一篇新博客的小知识点

# Unity Scriptable Object踩坑

下面是一个名为`EntityBaseProperty.cs`的脚本，但是在创建一个`PlayerEntityProperty`SO的时候会提示你找不到这个脚本。你必须让脚本的名字和SO的类名保持一致，所以每一个SO都要新建一个和SO类命一样的脚本来写。
```
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


# AssetDataBase接口使用遇到的坑

`AssetDatabase.Refresh`方法用于刷新Unity编辑器的资产数据库。这个方法会同步磁盘和Unity编辑器之间的资产状态，包括添加、删除、修改文件等。如果你在Unity编辑器外部（比如在文件浏览器中或通过脚本）对项目中的文件进行了更改，使用AssetDatabase.Refresh可以让Unity编辑器识别这些更改。

`AssetDatabase.SaveAssets`方法用于将所有未保存的资产更改持久化到磁盘。这包括对预制体、场景、材质等任何在编辑器中做出的更改。如果你在脚本中修改了任何资产（比如更改了一个材质的颜色，或者添加了一个新的游戏对象到一个预制体中），并且想要确保这些更改被保存，就需要调用这个方法。

1. 脚本中修改完资源里面的属性的时候，调用一次AssetDataBase.Save() 必须要在AssetDataBase.Load()方法之间调用，不然修改就白做了
2. File接口相关的操作Directory相关的操作后必须后面跟一个AssetDataBase.Refresh调用

# GetComponentsInChildren接口

GetComponentsInChildren方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# GetComponentInParent接口

GetComponentInParent方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# DoTween

DoTween不仅可以用来做动画，也可以实现音频减弱、图片的渐显操作。

# 解除帧率限制(Android)

对一些安卓高刷屏来说，进游戏时屏幕刷新率会被设置成60，这应该和Android系统的策略有关，因此通过`Screen.currentResolution.refreshRateRatio`接口拿到的屏幕刷新率是不准确的，
如果要开启高刷，则需要关闭 ProjectSettings -> Player -> Resolution and Presentation 下的 [Optimized Frame Pacing](https://docs.unity3d.com/ScriptReference/PlayerSettings.Android-optimizedFramePacing.html)，并且在脚本中设置 `Application.targetFrameRate = 120;`

以上的解决方案过于粗糙了，更完善的解决方案需要参考Android的[官方文档](https://developer.android.com/media/optimize/performance/frame-rate?hl=zh-cn)去到Android层实现帧率的设置，一篇[实践的帖子](https://blog.csdn.net/a310989583/article/details/135771394?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-135771394-blog-118787844.235%5Ev43%5Epc_blog_bottom_relevance_base8&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-135771394-blog-118787844.235%5Ev43%5Epc_blog_bottom_relevance_base8&utm_relevant_index=9)

# Spine动画的使用

Spine动画有一个专门针对UI的组件叫做SkeletonGraphic，SkeletonGraphic组件是基于UICanvas绘制的，因此它的渲染层级可以被Canvas管理
另外还有专门为非UI播放动画的SkeletonAnimation组件，该组件的渲染是基于MeshRenderer的，因此不受Canvas的管理，当要在UI上展示Spine动画的时候，要选择使用SkeletonGraphic组件。

SkeletonGraphic相关接口
```
// 停止当前正在播放的动画
// heartBeatFlower.AnimationState.ClearTrack(0);    
heartBeatFlower.AnimationState.SetEmptyAnimation(0,0); 
// 播放新的动画
heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  
```
> 2024.11.8更新
> 使用SkeletonGraphic.AnimationState.ClearTrack(0)遇到坑了，目前笔者还不清楚Spine的作用原理，从表现上看，调用该接口会将Spine动画从轨道上移除，之后想要再次播放该动画的话就无法在轨道上找到这个动画，所以如果有切换播放动画的需求，只需要调用heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  就可以了

SkeletonAnimation相关接口
```
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
```
public void Play (string stateName, int layer= -1, float normalizedTime= float.NegativeInfinity);
public void Play (int stateNameHash, int layer= -1, float normalizedTime= float.NegativeInfinity);
```

# 协程的坑

协程中的等待一秒并不是真正的一秒有可能会有误差

# Unity的Animation的使用的坑

Animation中有一个Legacy字段，如果不勾选这个选项，在Animation中是没有办法通过Animation.Play()接口播放该动画的。
有兴趣可以做一个实验，创建一个勾选了Legacy和一个未勾选Legacy的动画，将这两个动画都添加到一个Animation组件上。在代码中获取这个Animation组件的引用，调用`Animation.GetClipCount()`接口得到的值为2，但是如果使用`foreach(AnimationState state in Animation)`去遍历却只会遍历到勾选了Legacy的AnimationClip

# XML文件读取逻辑

定义需要从XML中读取的数据结构

```
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
```
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
```
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
