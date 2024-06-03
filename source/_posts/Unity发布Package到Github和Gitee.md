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

准备编写自己的框架，想要在之后开发新的小游戏的时候可以直接移植，所以计划将通用的部分抽离出来拆分成框架，像Unity Package一样部署在云上，这样


# 实操


注意在配置package.json的时候
一定要在git repository的url前面添加git+,并且在url的最后添加.git
而且在其他项目中引用这个包的时候也要在url的最后添加.git






# 参考资料
[Unity官方创建自定义包](https://docs.unity3d.com/cn/2022.3/Manual/CustomPackages.html)

[语义版本控制介绍](https://semver.org/lang/zh-CN/)

[深入理解Unity的Asmdef](https://blog.csdn.net/qq_42672770/article/details/131193440)

[【教程】使用git通过ump发布Unity插件包（PackageManager）](https://zhuanlan.zhihu.com/p/258129649)

[Tutorial: How to develop a package for UnityPackageManager](https://www.patreon.com/posts/25070968)

[使用OpenUPM发布自己的Unity项目](https://yomunchan.moe/Post/582)

[NPM Doc: package.json](https://docs.npmjs.com/cli/v10/configuring-npm/package-json)