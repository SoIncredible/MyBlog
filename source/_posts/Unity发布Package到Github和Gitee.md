---
title: Unity发布Package到Github和Gitee
date: 2024-06-03 11:37:54
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 背景

笔者计划搭建自己的游戏框架，并希望在开发新游戏时能快速移植，于是笔者计划将游戏框架以Unity Package的方式部署在Github和Gtee上。


# 实践



## 准备工作

- 在Github/Gitee上创建仓库
- 本地新建一个Unity项目
- 

## 分支策略
我们需要用到至少两个分支：
- master 用来开发我们的框架
- upm 用来作为安装我们框架的分支


我们在master分支上进行开发，当一个版本开发完成后，我们将提交合并到upm分支并发布
我们需要编写

## 编写代码

## 发布到Github/Gitee

注意在配置package.json的时候
一定要在git repository的url前面添加git+,并且在url的最后添加.git
而且在其他项目中引用这个包的时候也要在url的最后添加.git

## 安装我们的Package

打开Packages下面的manifest.json文件，在"depencies"中输入我们的包名和Git仓库的地址，这里注意，从浏览器上复制的Git仓库地址是不带.git的，如果不带.git Unity会提示你找不到这个仓库
另外我们也可以添加#1.0.0这样的数字在末尾来安装指定版本的Package版本

之后开发新版本的时候，我们就可以继续按照上面的步骤开发了



# 参考资料
[Unity官方创建自定义包](https://docs.unity3d.com/cn/2022.3/Manual/CustomPackages.html)

[语义版本控制介绍](https://semver.org/lang/zh-CN/)

[深入理解Unity的Asmdef](https://blog.csdn.net/qq_42672770/article/details/131193440)

[【教程】使用git通过ump发布Unity插件包（PackageManager）](https://zhuanlan.zhihu.com/p/258129649)

[Tutorial: How to develop a package for UnityPackageManager](https://www.patreon.com/posts/25070968)

[使用OpenUPM发布自己的Unity项目](https://yomunchan.moe/Post/582)

[NPM Doc: package.json](https://docs.npmjs.com/cli/v10/configuring-npm/package-json)