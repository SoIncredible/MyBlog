---
title: VSCode配置UnityC#开发环境
tags: 
  - Unity
  - C#
  - VSCode
abbrlink: e57f243c
categories: 技术文档
date: 2022-07-06 00:07:34
cover: https://www.notion.so/images/page-cover/rijksmuseum_jansz_1636.jpg
description: 
---

# 背景

笔者大学期间用过一段时间VSCode+Unity的开发方案，工作之后使用了Rider配合Unity开发之后，虽然Rider是一个十分强大的IDE，有强大的补全功能，强大的Debug功能，但是也让我对Rider产生了依赖。另外Rider没有免费的社区版，因此还是想要使用一个更加轻量级的保持自己对代码的熟悉程度
另外VSCode的插件也让人眼前一亮

![](VSCode配置UnityC#开发环境/image-20220707015057151.png)

[麦扣的视频链接](https://www.bilibili.com/video/BV19741167zU?spm_id_from=333.999.0.0&vd_source=83f4165825ce9df46cf4fd576ccb1102)

[VS code 官方文档](https://code.visualstudio.com/docs/other/unity)

## 前置准备 Prerequisites

1. 安装[.NET SDK](https://dotnet.microsoft.com/zh-cn/download)，执行 dotnet --version 查看是否安装成功

2. （这一步只针对Windows用户）重启Windows让更改的设置生效

3. （这一步只针对Mac用户)安装长期支持版[Mono](https://www.mono-project.com/download/stable/)，避免遇到 "Some projects have trouble loading. Please review the output for more details" 的问题 执行 mono --version 查看是否安装成功

   Note: 额外安装的这个Mono，不会干预Unity内置安装的Mono

4. 在VSCode中安装C#的扩展

5. 在VSCode的设置中，取消勾选 C# extension's Omnisharp: Use Modern Net setting("omnisharp.useModernNet" : false)

   ![](VSCode配置UnityC#开发环境/image-20220707013823965.png)

## 在Unity中设置VSCode为默认的脚本编辑器

选择 Unity Preferences -> External Tools，选择VSCode.

![](VSCode配置UnityC#开发环境/截屏2022-07-0701.43.12.png)



## 小结

With the solution file selected, you are now ready to start editing with VS Code. Here is a list of some of the things you can expect:

- Syntax Highlighting
- Bracket matching
- IntelliSense
- Snippets
- CodeLens
- Peek
- Go-to Definition
- Code Actions/Lightbulbs
- Go to symbol
- Hover

**巨大的坑**：我在安装完成dotnet和mono组件并且设置vscode为默认编辑器之后，在VSCode中输入rigidbody时并没有语法提示，但**耐心等待一段时间**后，语法提示就出现了……这个本不存在的问题困扰了我两天……

![](VSCode配置UnityC#开发环境/958EBB7CEA0FF5AF41A9A3FECC90845C.jpg)

晚安。

# 背景

本文主要记录如何配置.NET环境，以及如何创建最简单的C# application。

# 在Mac上配置dotNet开发环境

这里是.Net7的[下载地址](https://download.visualstudio.microsoft.com/download/pr/86bb5988-5fb5-4e22-8f21-de5992e1a689/f8f616f84dc38100a8464c4644a371ce/dotnet-sdk-7.0.201-osx-arm64.pkg)，打开下载的pkg，按照指引完成安装。在终端中输入`dotnet`，如果安装成功，会弹出如下内容：

![](在Mac和Windows上配置dotNet开发环境/image-20230310161307972.png)

# 使用Terminal创建一个最简单的C#命令

在终端中输入：

```shell
dotnet new console -o MyApp -f net7.0
cd MyApp
```

上述各命令的解释：

- `dotnet new conslole` 命令用来创建新的console APP 
- -`o`参数会创建名为`MyApp`的目录，用于存储应用并使用所需文件进行填充
- `-f`参数指示console我们创建的是.NET7的应用程序

创建的MyApp文件夹中的主文件称为`Program..cs`，默认情况下，它包含的内容如下：

```c#
Console.WriteLine("Hello World!");
```

然后在终端中运行`dotnet run`来运行`.NET`应用，终端中会输出如下结果：

![](在Mac和Windows上配置dotNet开发环境/截屏2023-03-10 16.41.32.png)

## 使用Rider打开并运行C#项目

使用Rider打开并运行C#项目不像使用IDEA打开并运行java代码那样繁琐，直接在Rider中选择`File`->`Open`，选择你使用Terminal创建的C# application打开，就可以在Rider中运行项目了。

# 在Windows上配置dotNet开发环境

Windows上安装dotNet的方式多种多样，而且官方的教程也很详细，可以直接看[官网教程](https://learn.microsoft.com/zh-cn/dotnet/core/install/windows?tabs=net70)

