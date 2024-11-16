---
title: UnityAndroid工程包体优化
abbrlink: 279644bd
date: 2023-02-18 11:44:23
tags:
  - 性能优化
categories: 开发日志
cover: https://www.notion.so/images/page-cover/met_cezanne_1890.jpg
description:
---

# 包体分析

可以使用Android Studio中的`Build -> ApkAnalyzer`来对包体内容进行分析

Resources目录下的文件都会被打包进`assets/bin/Data/unity default resources`文件中，游戏启动的时候会一次性把这个文件加载到内存中，因此应该要尽可能地减小放在resources目录下的文件体积。

# libil2cpp.so内容分析

https://blog.csdn.net/linxinfa/article/details/116572369


# AssetRipper使用

AssetRipper的GitHub链接👉👉👉 https://github.com/AssetRipper/AssetRipper

下载到本地解压后在根目录打开终端，运行`./AssetRipper.GUI.Free`打开AssetRipper


# AssetBundle打包问题