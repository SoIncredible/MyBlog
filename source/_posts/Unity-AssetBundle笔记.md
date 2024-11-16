---
title: UnityAssetBundle笔记
abbrlink: 96c99d7a
date: 2024-06-21 16:41:59
tags:
categories:
cover: https://www.notion.so/images/page-cover/woodcuts_13.jpg
description:
swiper_index:
sticky:
---

# AssetBundle内存

使用[AssetRipper](https://github.com/AssetRipper/AssetRipper?tab=readme-ov-file)可以查看AssetBundle中的文件

![](Unity-AssetBundle笔记/image.png)

# 依赖

如果两个ab A和B中的一些资源都依赖了一个没有被指定要打包的资源C，那么C就会同时被打进ab A和B中，造成资源的冗余，增大ab和安装包的体积。而这个被A，B依赖的资源C又可以分为两种类型，一种是Assets下外部导入的资源，即开发者导入或创建的资源；另一种则是Unity内置的资源，例如内置的Shader，Default-Material和UGUI一些组件如Image用的一些纹理资源等等。因此要解决资源冗余的问题，就要分别对这两种被依赖的资源进行处理。

也就是说，只有我们自己手动把一些资源打进Bundle，
想要打Bundle，最终都要调用`BuildPipeline.BuildAssetBundles`接口，

`public static AssetBundleManifest BuildAssetBundles(string outputPath,AssetBundleBuild[] builds,BuildAssetBundleOptions assetBundleOptions,BuildTarget targetPlatform)`接口支持传入AssetBundleBuild数组

` public static AssetBundleManifest BuildAssetBundles(string outputPath,BuildAssetBundleOptions assetBundleOptions,BuildTarget targetPlatform)`
通过给AssetBundleBuild显示传入打包的依赖关系，确保打包资源不会冗余
因此 重点在于AssetBundleBuild数组的构建。

在AssetBundle的工作流中，必须有一个环节指定有哪些资源是要打进Bundle的，这个操作可以由Unity内置的AssetBundle工具执行，也可以自己编写脚本执行。

# Unity AssetBundle与图集Sprite Atlas

Atlas资产的Inspector上有一个IncludeInBuild选项。勾选该选项之后，与这个Atlas对应的散图就建立了依赖关系，因此在构建AssetBundle的时候，查询依赖会查询到Atlas文件，并把Atlas文件和散图打在一个bundle文件中

如果勾选了IncludeInBuild选项，就不需要再把SpriteAtlas打一个Bundle了，这样会造成图集资源的冗余

在合理使用SpriteAtlas的情况下，当我们把AssetBundle包解开以后，会发现里面会包含一张Texture和若干个Sprite这两种资产。Texture是纹理，显示的文件大小较大；而Sprite可以理解为一个描述了精灵在整张纹理上的偏移量位置信息的数据文件，显示的文件大小较小。

因此这个不是冗余，是正常现象。

不过确实存在一个冗余的问题：如果Prefab1和Prefab2引用了同一个Atlas的Sprite，那么这个Atlas至少要主动包含在一个AssetBundle中，否则会被动打入两个包中，造成冗余。

Sprite和SpriteAtlas和Texture的概念重要区分

Sprite和Sprite Atlas是两个类，他们中具有一些图片信息的数据成员，他们不是真的“图片”！而Texture才是真正的图片，因此在MemoryProfiler中你可以看到SpriteAtlas和Sprite类外加真正的图片Texture被加载到内存中

> 如果不勾选IncludeInBuild，而是指定图集打Bundle，效果是一样的嘛？
> 


# 案例



# 参考资料

- https://blog.csdn.net/yinfourever/article/details/109493160
- https://zhuanlan.zhihu.com/p/369080940
- https://docs.unity3d.com/cn/2021.2/Manual/AssetBundles-Native.html
- https://www.jianshu.com/p/1b1527faaca2
- https://www.jianshu.com/p/0d18ac565563
- https://blog.csdn.net/sunheng_/article/details/128204386
- https://blog.uwa4d.com/archives/TechSharing_249.html