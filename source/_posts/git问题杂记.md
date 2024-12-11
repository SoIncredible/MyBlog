---
title: git问题杂记
tags: 
  - Git
abbrlink: a72e4bb6
categories: 硬技能
date: 2022-07-10 11:11:26
cover: https://www.notion.so/images/page-cover/met_frederic_edwin_church_1871.jpg
description: 
sticky: 2
swiper_index: 2
---

# 终端中git相关指令执行的结果中的中文乱码问题

- 在终端中使用`git status`命令时，结果显示为乱码
  解决方法是在终端中输入以下两行命令
  ```
   git config --global core.quotepath true
   git config --global core.quotepath false
  ```
  重启终端，如果仍然显示乱码，执行下面两行命令
  ```
   git config --global core.quotepath true
   git config --global core.quotepath false
  ```
  再次重启终端


# Clone GitHub仓库失败

有可能是因为本地使用了代理工具，比如Clash修改了网络端口，解决方案如下👇👇👇
- https://blog.csdn.net/qq_27281247/article/details/135925956

# 参考资料

[官方参考书](https://git-scm.com/book/zh/v2)
