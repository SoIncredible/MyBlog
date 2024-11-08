---
title: 实现一套完美兼容粒子系统的UI框架
tags: 
  - UI框架
abbrlink: 5e0804b8
date: 2022-08-23 21:39:03
categories: 开发日志
cover: https://www.notion.so/images/page-cover/met_gerome_1890.jpg
description: 
---

笔者在业务场景中经常遇到在UI上展示一些粒子，但是粒子和UI层级问题处理起来实在是太让人头大了，因此笔者想要实现一套支持管理粒子和UI的UI框架。


描述场景，UI和粒子通过SortingOrder进行排序
在UI中开放一套接口