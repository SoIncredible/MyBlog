---
title: UnityShader入门精要笔记-8-透明效果
date: 2024-07-07 10:24:49
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


Unity中实现透明效果有两种方式，透明度测试(Aplha Test)和透明度混合(Alpha Blend)

透明度测试的原理是 
因此透明度测试是无法实现真正的半透明效果的，


透明度混合的原理是
这种方法可以得到真正的半透明效果



# 渲染顺序很重要


我们要在不透明物体渲染完成之后再渲染半透明物体

对两个都是半透明的物体来说，