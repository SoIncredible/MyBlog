---
title: YooAsset
abbrlink: ae5b3442
date: 2025-03-17 14:09:58
tags:
categories:
cover: https://public.ysjf.com/mediastorm/material/material/%E8%B4%B9%E5%B0%94%E7%8F%AD%E5%85%8B%E6%96%AF-11-%E8%BF%91%E6%99%AF-20250107.JPG
description:
swiper_index:
sticky:
---

# 资源信息热更新流程

- 游戏启动
- 获取当前资源的Manifest信息
- 获取远端最新的Manifest的信息
- 比较当前和远端Manifest的信息，如果可以更新，则需要更新到最新的Manifest
- 创建ResourcePackage，根据最新的Manifest创建运行时的AssetSystem

分帧加载是什么意思?

Persistent下的
BuildinRoot-> streamingAssets/YooAssetSettingsData.Setting.DefaultYooFolderName
BuildinPackageRoot
SandboxRoot->UnityEngine.Application.persistentDataPath, YooAssetSettingsData.Setting.DefaultYooFolderName
SandboxPackageRoot

ManifestFileName PackageManifest
DefaultYooFolderName yoo


# 启动流程

启动游戏
获得游戏的版本号
初始化YooAsset
有一个内置路径和一个沙盒路径
内置路径是包含在底包中的, 沙盒路径是persistent路径,要么是沙盒路径中已经有一份某个Verrsion的Manifest文件了, 要么就是第一次启动游戏, persistent路径下是干净的, 从buildIn也就是streamingasset下面把跟随底包的Version的Manifest文件写入到沙盒路径下. 到此YooAsset持有了一份正确的Manifeset了.

然后再是下面的流程👇

1. 要请求当前这个游戏版本在CDN上最新的资源包的**版本**
2. 拿到远端最新的资源版本之后, 向CDN请求这个版本对应的**资源清单Manifest**
   1. 如果本地的资源清单不是空的, 并且清单的版本和1中拿到的版本一样, 就不用再向远端请求清单Manifest了
   2. 否则,
      1. 先尝试从本地加载1中拿到的资源包版本, 看看是不是远端CDN把最新的那个资源版本给删除了, 然后本地其实已经下载过CDN上最新的那个版本 这时候也不需要去远端请求 即先尝试从本地加载
         1. 本地加载要经过一些校验 MD5之类的, 稍后整理
      2. 如果本地都没有这个版本的资源 那么这次真的要去远端下载了 至此Manifest检查与加载流程完毕
3. 更新被打上特殊标记的资源

问题: 在启动的时候, 只是从底包中也就是streaming目录下把Manifest拷贝过来了啊, 底包中带的那些bundle有没有拷贝过来呢? 结论是并没有拷贝过来, 

底包中的资源也是可以热更的, 但是热更的底包资源是放在persistent沙盒目录下的, 首先是YooAsset拿到的最新的manifest, 在最新的manifest中 如果底包中的资源发生了变化, 那么这个资源的bundle名以为有hash值, hash值是会变的, 所以在底包中就找不到这个资源. 所以就会触发热更逻辑.

1、Unity中，StreamingAssetsPath称为buildin路径，persistentPath称为Sandbox路径，可以吗？

结论：可以，这是一种常见的理解方式。

详细解释

StreamingAssetsPath（Application.streamingAssetsPath）

它指向包内的 StreamingAssets 目录， 打包后内容只读 ，无法运行时修改，内容随包发布。
开发中常常被称为 “内置资源路径” 或 “包内资源路径”，英文有时叫 builtin path 或 package path。
buildin 或 builtin 本身不是Unity官方术语，但在行业里大量用来指只读/包内/原生自带资源。
persistentPath（Application.persistentDataPath）

指向设备上的可读可写目录，运行时可以写入，常用于下载、存档等。
这个目录通常是应用“沙盒”中的一个子目录，**沙盒（sandbox）**就是指每个应用私有且受系统保护的存储空间。
因此，很多开发者将其叫做 Sandbox路径 或 沙盒路径（即用户数据/可持久化路径）。
Sandbox 目录是操作系统层面对应用私有存储的通用叫法。
所以你这样对应：

StreamingAssetsPath = Buildin Path / 包内路径 / 内置资源路径
PersistentPath = Sandbox Path / 沙盒路径 / 可读写路径
是完全OK的！

2、Buildin 和 Sandbox 是什么开发术语？

Buildin / Built-in

Built-in（或 buildin，实际拼写应为 built-in，意思是「内建的」）
泛指应用程序自带的、不需要动态下载的资源或功能，通常只读。
在Unity，往往指 editor自带的shader、资源，也被开发者泛指 StreamingAssets 和 Resources 这些「随包就有」的资源目录。
不是Unity的官方API术语，但业界用得多。
Sandbox

Sandbox（沙盒）
在移动端和桌面操作系统（iOS/Android/Windows/macOS）开发中，都有“沙盒”概念：
每个应用有自己的私有目录，不能随意访问其他应用的数据，受OS保护。
Unity的 Application.persistentDataPath 目录 就是在各平台沙盒中的一个可读写子目录。
沙盒路径就是这种应用专属、可读写的数据目录。
总结

你这样叫法可以，业界很常见！
官方文档可能不用这两个单词，但实际开发交流时常用：
Buildin Path，指内置只读资源。
Sandbox Path，指应用私有可读写目录。
参考链接

Unity Application.streamingAssetsPath
Unity Application.persistentDataPath
如果你在文档、配置、代码注释里这样使用，一般团队成员都能理解。如果要求更官方，可用“StreamingAssets Path”、“Persistent Data Path”的全称表述。


有一些资源是被打入底包的, 同时这些资源也会被热更, 在游戏启动流程中, YooAsset首先跟远端请求最新的资源版本, 有两种可能, 1本地的资源就是最新的, 这里指的是Buildin的资源, 2本地资源不是最新的, 那么就会先去CDN上下载最新的BuildIn资源, 把这些资源下载到Persistent沙盒目录下, 在加载资源的时候, YooAsset会先判断这个资源在Persistent沙盒路径下有没有, 然后再去BuildInstreamingPath下去找.
根据BudleInfo的LoadMode字段


```C#
private BundleInfo CreateBundleInfo(PackageBundle packageBundle)
{
   if (packageBundle == null)
      throw new Exception("Should never get here !");

   // 查询分发资源
   if (IsDeliveryPackageBundle(packageBundle))
   {
      DeliveryFileInfo deliveryFileInfo = GetDeiveryFileInfo(packageBundle);
      BundleInfo bundleInfo = new BundleInfo(packageBundle, BundleInfo.ELoadMode.LoadFromDelivery, deliveryFileInfo.DeliveryFilePath, deliveryFileInfo.DeliveryFileOffset);
      return bundleInfo;
   }

   // 查询沙盒资源
   if (IsCachedPackageBundle(packageBundle))
   {
      BundleInfo bundleInfo = new BundleInfo(packageBundle, BundleInfo.ELoadMode.LoadFromCache);
      return bundleInfo;
   }

   // 查询APP资源
   if (IsBuildinPackageBundle(packageBundle))
   {
      BundleInfo bundleInfo = new BundleInfo(packageBundle, BundleInfo.ELoadMode.LoadFromStreaming);
      return bundleInfo;
   }

   // 从服务端下载
   return ConvertToDownloadInfo(packageBundle);
}
```

# 复述一下YooAsset构建bundle的过程


# YooAssetCollector里面的Group的含义是什么? 已经有collector了, 已经可以实现针对不同的路径使用不同的打bundle策略了, 我能想到的Group的作用是在不同的平台上使用不同的Group


# 下面两者的区别 同步和异步的接口都是一样的, 只是传入的参数不一样 怎么做到的

```C#
/// <summary>
/// 同步加载资源对象
/// </summary>
/// <typeparam name="TObject">资源类型</typeparam>
/// <param name="location">资源的定位地址</param>
public AssetHandle LoadAssetSync<TObject>(string location) where TObject : UnityEngine.Object
{
   DebugCheckInitialize();
   AssetInfo assetInfo = ConvertLocationToAssetInfo(location, typeof(TObject));
   return LoadAssetInternal(assetInfo, true, 0);
}


/// <summary>
/// 异步加载资源对象
/// </summary>
/// <typeparam name="TObject">资源类型</typeparam>
/// <param name="location">资源的定位地址</param>
/// <param name="priority">加载的优先级</param>
public AssetHandle LoadAssetAsync<TObject>(string location, uint priority = 0) where TObject : UnityEngine.Object
{
   DebugCheckInitialize();
   AssetInfo assetInfo = ConvertLocationToAssetInfo(location, typeof(TObject));
   return LoadAssetInternal(assetInfo, false, priority);
}
```