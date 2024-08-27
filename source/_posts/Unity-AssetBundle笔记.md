---
title: UnityAssetBundle笔记
abbrlink: 96c99d7a
date: 2024-06-21 16:41:59
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# AssetBundle内存

使用[AssetRipper](https://github.com/AssetRipper/AssetRipper?tab=readme-ov-file)可以查看AssetBundle中的文件

![](Unity-AssetBundle笔记/image.png)

# 依赖

# Unity AssetBundle与图集Sprite Atlas


Atlas上有一个IncludeInBuild 勾选了这个之后，与这个Atlas对应的散图就建立了依赖关系，因此在构建AssetBundle的时候，查询依赖会查询到Atlas文件，并把Atlas文件和散图打在一个bundle文件中

如果勾选了IncludeInBuild选项，就不需要再把SpriteAtlas打一个Bundle了，这样会造成图集资源的冗余

在合理使用SpriteAtlas的情况下，当我们把AssetBundle包解开以后，会发现里面会包含一张Texture和若干个Sprite这两种资产。Texture是纹理，显示的文件大小较大；而Sprite可以理解为一个描述了精灵在整张纹理上的偏移量位置信息的数据文件，显示的文件大小较小。

因此这个不是冗余，是正常现象。

不过确实存在一个冗余的问题：如果Prefab1和Prefab2引用了同一个Atlas的Sprite，那么这个Atlas至少要主动包含在一个AssetBundle中，否则会被动打入两个包中，造成冗余。

Sprite和SpriteAtlas和Texture的概念重要区分

Sprite和Sprite Atlas是两个类，他们中具有一些图片信息的数据成员，他们不是真的“图片”！而Texture才是真正的图片，因此在MemoryProfiler中你可以看到SpriteAtlas和Sprite类外加真正的图片Texture被加载到内存中

> 如果不勾选IncludeInBuild，而是制定图集打Bundle，效果是一样的嘛？
> 待验证

# 参考资料

- https://blog.csdn.net/yinfourever/article/details/109493160
- https://zhuanlan.zhihu.com/p/369080940
- https://docs.unity3d.com/cn/2021.2/Manual/AssetBundles-Native.html
- https://www.jianshu.com/p/1b1527faaca2
- https://www.jianshu.com/p/0d18ac565563
- https://blog.csdn.net/sunheng_/article/details/128204386
- https://blog.uwa4d.com/archives/TechSharing_249.html