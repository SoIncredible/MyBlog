---
title: ET中的网络通信模块
abbrlink: 3613bb98
date: 2025-04-02 18:14:58
tags:
categories:
cover: https://public.ysjf.com/mediastorm/material/material/%E8%87%AA%E7%84%B6%E9%A3%8E%E5%85%89_%E6%9C%9F%E6%9C%AB%E7%9A%84%E5%BB%B6%E6%97%B6_10_%E5%85%A8%E6%99%AF.jpg
description:
swiper_index:
sticky:
---


ET框架中的协议可以分成三种类型:
- 客户端内部的通讯协议
- 客户端和服务器之间的通讯协议
- 服务器内部的通讯协议

ET中负责模块间通信的组件有哪些?

ClientSenderComponent

ProcessInnerSender
ProcessOuterSender
Session

MessageSender

MessageLocationSenderComponent
MessageLocationSenderOneType
MessageLocationSender

看起来有很多Sender, 可以归根结底只有ProcessInnerSender和MessageQueueSingleton这几个Sender


Realm Session
Router Session
Gate Session

通过Root节点上的RouterAddressComponent组件可以获得服务器的地址, 并将这个地址给到Session

[内网和外网之间的通信（端口映射原理）](https://blog.csdn.net/u011041241/article/details/109574509)
[KCP协议详解](https://luyuhuang.tech/2020/12/09/kcp.html)
[Unity+ET6.0网络框架的网络开发基础理论](https://blog.csdn.net/Q540670228/article/details/123385080?spm=1001.2014.3001.5502)