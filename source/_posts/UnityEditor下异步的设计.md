---
title: UnityEditor下异步的设计
categories: UnityEditor
abbrlink: 1302a241
date: 2025-09-09 08:27:17
tags:
cover: https://www.notion.so/images/page-cover/woodcuts_8.jpg
description:
swiper_index:
sticky:
---


参考框架: FigmaConvertToUnity、FindReference2、I2


Editor模式下没办法使用MonoBehaviour那套协程来实现异步, I2用了点奇技淫巧, 在需要执行异步的时候, 在场景上挂载一个Mono的脚本, 用这个Mono脚本驱动协程的执行


# FR2
 
使用的是伪异步, 在EditorUpdate里面做

# Figma插件

这个用到的是C#的Task, 因为它里面的一些异步操作都是向网络请求, 下载数据, 不是操作Unity里面的一些东西, 所以可以放心放到别的线程里面去做.