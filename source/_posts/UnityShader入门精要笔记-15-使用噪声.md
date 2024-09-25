---
title: UnityShader入门精要笔记-15-使用噪声
abbrlink: 200401f7
date: 2024-09-11 00:36:49
tags:
 - Unity
 - Shader
categories: UnityShader入门精要笔记
cover: https://www.notion.so/images/page-cover/rijksmuseum_jansz_1637.jpg
description:
swiper_index:
sticky:
---

很多时候，向规则的事物里添加一些“杂乱无章”的效果往往会有意想不到的效果。而这些“杂乱无章”的效果来源就是噪声。在本章中，我们将会学习如何使用噪声来模拟各种看似“神奇”的效果。在15.1节中，我们将会使用一张噪声纹理来模拟火焰的消融效果。15.2节则把噪声应用在模拟水面的波动上，从而产生波光粼粼的视觉系哦啊过。在15.3节中，我们会回顾13.3节中实现的全局雾效，并向其中添加噪声来模拟不均匀的飘渺雾效。

## 消融效果