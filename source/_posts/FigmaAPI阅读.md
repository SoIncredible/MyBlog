---
title: FigmaAPI阅读
abbrlink: 8d32db40
date: 2025-08-10 16:29:45
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# Figma工作流概览

建立一个资产数据库系统, 打通Figma侧与Unity侧的双向通信, 减少UnityDeveloper花费在拼UI上的时间, 提高开发效率


- Figma对外暴露的API都是只读的, 外部无法对Figma设计稿进行写入操作. 因此, Figma侧的同步Unity侧页面结构改动必须在Figma侧进行操作.

要实现同步, 必须要在unity侧和Figma侧协同配合



# 参考资料
- https://www.figma.com/developers/api#intro
- https://developers.figma.com/docs/rest-api/ 之前有一个FCU插件里面的Figma API的文档找不见了, 这个文档可能内容与找不到的那篇文档差不多