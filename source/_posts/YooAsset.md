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