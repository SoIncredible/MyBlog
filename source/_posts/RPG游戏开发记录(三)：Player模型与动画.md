---
title: RPG游戏开发记录(三)：Player模型与动画
abbrlink: 8601cc5
date: 2024-10-4 18:29:24
tags: 开发日志
categories: 硬技能
cover: https://www.notion.so/images/page-cover/met_emanuel_leutze.jpg
description:
swiper_index:
sticky:
---

# 状态机

Unity的Animator系统本质上也是一个状态机，笔者认为在此引入状态机有利于功能的实现。在表现上，人物会跑、跳、飞等动作，而与之对应的是与这些动作相关的数据发生了变化，注意这里面的先后关系：先是由于数据发生了变化，然后才导致了表现上角色动画行为的变化。秉承着上述数据驱动表现的原则，我们类比Animator状态机，为我们的数据层也建立一个状态机。其实任何逻辑你都可以去用状态机进行组织，只要你能够抽象出几种互斥的状态，然后在每种状态内部去实现该状态下的所有逻辑。我们先来看一下状态机的基本写法。

状态机可以被分成两部分，第一部分是状态，第二部分是管理这些状态的部分。这两个部分的具体表现都是类，上面我们说要在



本节将我们控制的Player由圆柱体替换成有人形骨骼的模型

# 模型

# Unity中的动画系统

实现一个可以静止、移动切换的动画状态

静止状态下随机播放动画

## 要写一个状态机

设计一个主角的状态机

主角有Idle、Move、Jump等状态

```
sm.AddState(StateEnum, StateClass(Host, Animation ....))
```
类似上面的状态注册，将Animator传递进去，在每个状态的OnTick中执行音频、动画等模块的更新

PlayerController是状态机的Host

## 

