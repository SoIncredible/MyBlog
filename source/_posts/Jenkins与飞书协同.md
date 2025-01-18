---
title: Jenkins与飞书协同
date: 2025-01-12 21:30:16
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# Jenkins安装

在Mac上,使用Homebrew命令安装Jenkins
```
brew install jenkins-lts
```
安装成功后使用下面命令启动Jenkins服务器
```
brew services start jenkins-lts
```
在本地


其他常用命令
```
brew services restart jenkins-lts // 重启Jenkins
brew upgrade jenkins-lts // 升级Jenkins
```

Jenkins中的其他概念.

Node
Agent
创建Pipeline类型的Work无法看到workspace

通过新建一个Node的方式,并在pipeline流水线中制定,

# 遇到的问题

使用homebrew安装了java,在终端中运行Java提示`The operation couldn’t be completed. Unable to locate a Java Runtime.
Please visit http://www.java.com for information on installing Java.`
原因可能是使用homebrew安装的java没有jvm,需要去[java官网](https://www.oracle.com/java/technologies/downloads/#jdk23-mac)手动安装


# Jenkins执行成功/失败上报飞书


