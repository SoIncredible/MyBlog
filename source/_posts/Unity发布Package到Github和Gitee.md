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

笔者计划搭建自己的游戏框架，并希望基于Github和Gitee等代码托管平台维护和迭代，以便在开发新游戏时能够快速移植，本文记录笔者将代码发布到Gitee上的过程。

# 制定规范

一个标准的UnityPackage，它们内部的结构大致如下：

```
.
├── CHANGELOG.md
├── README.md
├── LICENSE.md
├── Code
└── package.json
```

README用来介绍Package的功能和用法；CHANGELOG用来记录Package所有历史版本功能修改、新增和维护日志；README、CHANGELOG虽然不是必须的，但是它们可以作为对package的辅助文档，帮助使用者更好的理解和使用Package；另外持续地维护README和CHANGELOG可以督促你养成良好的开发习惯。
LICENSE是开源许可，如果你对你的代码的版权不是很重视的话，这也是非必需的；或者你也可以使用最宽松的[MIT开源许可](https://opensource.org/license/mit)。
Code就是Package中实现功能的代码部分了，这一部分的细节会在实践章节中阐述。
package.json是Package中最重要的文件，它记录了Package的版本、url等各种能够标识该Package的信息，模板可以参考[npm官方文档](https://docs.npmjs.com/cli/v10/configuring-npm/package-json)，笔者在实践章节也会给出自己使用到的模版中的字段。

# 实践

- 首先创建好本地项目和远端的仓库，在Asset下创建一个文件夹作为Package的根路径，依次创建好：`README`、`CHANGELOG`、`LICENSE`和`package.json`文件.
- 编写Package代码
  
   在Unity中我们开发的代码要么是在Runtime环境下使用，要么是在Editor环境使用。在Editor环境下使用的代码，是不应该在打包的时候被打进包内导致包体变大的，为了实现不把Editor脚本打到包内的效果，我们需要[划分程序集](https://docs.unity3d.com/Manual/ScriptCompilationAssemblyDefinitionFiles.html)。当然我们也可以不划分程序集，所有的代码都会归属名为`Assembly-CSharp`的程序集，你可以在Unity项目的根目录下看到这个文件。不过通过划分程序集可以让代码结构更加清晰，且更易于管理和维护。
   笔者在根目录下创建`Editor`和`Runtime`两个目录来分别代表在Editor和Runtime下运行的代码，然后分别在这两个目录下执行`Assets -> Create -> Assembly Definition`, 就会在对应路径创建一个`asmdef`文件。注意最后程序集的名字和该`asmdef`文件名是没有关系的，而是和该程序集文件的Inspector窗口中的Name字段有关。另外Runtime目录下的程序集要在Platforms部分勾选除了Editor以外的其他所有平台都使用，Editor目录下的程序集勾选只在Editor环境下使用。

 - 编写好代码之后我们就可以将我们的代码上传了。按顺序输入以下指令
    ```
    git subtree split --prefix=Assets/yourPackageName --branch upm

    git tag 1.0.0 upm

    git push origin upm --tags
    ```
完成以上操作我们就完成了UnityPackage的发布。

# 安装我们的Package

打开Packages下面的manifest.json文件，在"dependencies"中输入我们的包名和Git仓库的地址，注意，从浏览器上复制的Git仓库地址是不带.git的，如果不带.git Unity会提示你找不到这个仓库
另外我们也可以添加#1.0.0这样的数字在末尾来安装指定版本的Package版本

开发后续版本的时候，记得更新ChangeLog、Readme还有package.json中的信息。

## 注意

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

[开源许可介绍](https://www.ruanyifeng.com/blog/2017/10/open-source-license-tutorial.html)