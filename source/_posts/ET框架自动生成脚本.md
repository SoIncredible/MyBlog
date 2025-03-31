---
title: ET框架自动生成脚本
abbrlink: ef16867e
date: 2025-03-28 19:09:03
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

ET框架仓库下其实是有两个工程的，虽然这两个工程里面的脚本有可能是用的同一份

在Share这个模块中`ETSystemGenerator.cs`这个脚本里面！

> 问题 这些代码自动生成的时机是什么时候？

编译的时候自动生成，无需开发者手动管理ETSystemGenerator这个类实现了ISourceGenerator接口，但是需要注意的是，在Unity中自动生成脚本的功能是是通过dll插件的方式接入进来的，还得看一下怎么导出dll，如果对SourceGenerator的源码有修改，需要重新导入dll到Unity中


# GeneratorExecutionContext