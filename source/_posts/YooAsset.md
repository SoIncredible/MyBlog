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

# 角色
OperationSystem类，管理、驱动所有异步操作

AsyncOperationBase类，所有异步操作要继承的积累

IPlayModeServices 下载相关的接口
IBundleServices Bundle信息相关的接口
IRemoteServices 服务器URL相关


PackageManifest 资源信息 资源包的版本、名称、资源列表等
PackageAsset 
AssetSystem
ProviderBase

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

问题: 在启动的时候, 只是从底包中也就是streaming目录下把Manifest拷贝过来了啊, 底包中带的那些bundle有没有拷贝过来呢?

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
