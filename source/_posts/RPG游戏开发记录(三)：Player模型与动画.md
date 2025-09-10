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

# 这个系列快要被我自己做成一个科普系列了, 有太多不知道的东西了

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

我从Mixamo网站上下载了一个动画, 
我以为为了要让下载的动画适配我在Unity中使用的角色, 我需要把我角色的Avatar给到这个动画, 但实际上是让这个动画生成一个Avatar, 然后将这个动画给到我使用的角色就可以正常播放了, 这是为什么?

你的理解**现在是正确的**，而且你问得非常好，很多Unity新手都会有这样的疑问！让我们详细说一说**原因**。

---

# 1. Unity Animator与Avatar的核心原理

- **Avatar**，本质上是一个“人形骨骼结构的映射关系”，把每块骨头（hips、spine、head、arm等）和Unity内建的人体部分一一对齐。
- **Animation Clip**并不直接关心你角色模型的骨骼细节，只需要你在角色上有一个“Humanoid Avatar”。
- **Animator Controller**引用了Animation Clip，Animator组件再关联你的角色和Avatar。

---

# 2. “让动画Clip生成自己的Avatar”是什么意思

- 在Mixamo动画FBX导入时，Inspector设置为`Animation Type: Humanoid`，`Avatar Definition: Create From This Model`，Unity会按照FBX里记录的Mixamo骨骼结构，**自动生成一个Humanoid Avatar**。
- 这个操作的**目的是告诉Unity：这个动画是以什么人为标准骨骼录的动。**

> **只要动画的Avatar和人为目标的Avatar都属于Humanoid体系，Unity就能帮你自动"重定位"骨骼，让任意通用动作动画作用到任意"符合Humanoid要求"的模型上。**

---

# 3. **Humanoid ("人形") 的最大价值：自动化骨骼重定向**

- Unity为人形做了大量底层映射抽象，**无论骨头名字怎么叫、具体怎么分支，只要Mapping过程正确，动画的Avatar和你角色的Avatar就可以互换动画！**
- 动画Clip作用时，Unity会自动把“动画数据的骨骼变换”映射到“你角色的骨骼变换”上，不需要它们骨骼结构100%一样，名字也可以不同，只要都按照Humanoid头肩腿手的逻辑映射过即可。

---

### 你最初的误区在哪里？

> 你以为“要让动作Clip转到我屁股角色上用，动画资源必须用我角色的Avatar”<br>
实际上：只需要每个动画和每个人物都各自有一个合法的Humanoid Avatar，**动画资源和目标角色模型的骨骼并不是一一对齐、名字也不必一致**（但都需要在人形映射里有对应的“身体部位”）。

### 为什么不能直接“Copy From Other Avatar”？
- `Copy From Other Avatar`要求动画FBX和你主角FBX的骨骼结构一模一样。这更多用于模型LOD、模型换装、同一套骨架绑定。
- 通用Mixamo下载的动画和你主角的模型一般骨骼结构不同，因此不能copy主角的那个Avatar。

---

# 4. 总结为一句话：

> **正确用法就是让每个FBX的Rig生成自己的Avatar（Create From This Model）！只要都设为Humanoid，无需“copy”别人的Avatar。这样Mixamo动画可以直接作用到你的主角身上——Unity底层帮你自动转换骨骼！**

---

## 类比：
就像Unicode编码让全世界不同语言都可以互相通信一样，Humanoid Avatar让不同骨骼结构的角色/动画能够互相“理解”动作~

---

## 最后，流程建议

1. 你的主角FBX：Inspector > Rig > Animation Type=Humanoid > AvatarDefinition=CreateFromThisModel
2. Mixamo动画FBX：Inspector > Rig > Animation Type=Humanoid > AvatarDefinition=CreateFromThisModel
3. 在Animator Controller里assign动画，拖主角进场景，挂Animator即可通用！

---

### **只要都是Humanoid Avatar，动作clip可以通用，和Clip是从哪个骨骼导出的关系不大，Unity自动帮你完成“动作对齐”！**

---

如还想了解更底层的“映射原理”或动画采样，可以再问我！



你遇到的错误是：

> **Copied Avatar Rig Configuration mis-match Transform hierarchy does not match Transform spine1_loResSpine1 for human bone Hips not found**

这说明**动画和你选的Source Avatar（即你的角色）之间骨架结构不同**。  
Mixamo动画的骨骼名字和你的角色的骨骼名字/层级对不上，导致Avatar无法copy，对应人体部位(`Hips`)在你角色的骨骼里找不到`spine1_loResSpine1`这个Transform。

---

## 常见根本原因

1. **你用的动画和你用的角色骨骼结构或者骨骼名字对不上。**
2. **Mixamo的骨骼层级和你主角的骨骼层级有额外节点、命名不同或顺序不同。**
3. **你的主角模型没有用Mixamo的AutoRigger绑定过（最好用Mixamo人形骨骼的标准）。**
4. **动画FBX（带动画）和角色FBX中骨骼树结构不是一份标准。**

---

## 解决办法

### 方案一：动画与模型都用Mixamo骨骼

最“保险”的做法是：
1. **把你的主角FBX上传到Mixamo官网，用其Auto-Rigger进行Humanoid绑定，然后在Mixamo下动画并下载。**
2. 这样动画和模型骨骼结构100%兼容（Mixamo标准）。
3. 再把下载下来的带动画的FBX导入Unity，动作不会报错。

---

### 方案二：单独下载动画只带动画数据

- 在Mixamo下载动画时**勾选"Without Skin"**，这样FBX只包含动画和Mixamo骨骼、不带模型体。
- 在Unity里，将动画的**Animation Type 设为Humanoid**，Avatar Definition 选 **Create From This Model**，不要选Copy，直接用动画自己的骨骼Avatar。

此时只要你的主角也是Humanoid，动画就可以直接拖拽到主角的Animator Controller里用了。

---

### 方案三：用自己的骨骼重新绑定动画

如果你的主角不是Mixamo骨骼，而且改不了，也不想送去Mixamo“Rigger”，可以：
- 只将Mixamo动画设置为Humanoid，AvatarDefinition别用CopyFromOtherAvatar，直接Create。
- 只要你的角色是Humanoid，且Mapping和Mixamo骨骼能够自动匹配人体（很多次是可以的），Unity就能自动重定向动画→你的主角。

> **关键点：用“Copy From Other Avatar”时，两个FBX骨骼结构必须一模一样；如果只是标准Humanoid结构，不必Copy，只需要都设为Humanoid，动画就可以通用。**

---

## 总结口诀

- **Copy From Other Avatar → 要求骨骼100%一致！**（谨慎用，仅用于同一套骨骼结构的不同资源）
- **Create From This Model → Unity会自动识别骨骼并适配，一般兼容大部分标准人形骨骼。**
- **把你的主角送进Mixamo Auto-Rigger来获得Mixamo标准骨骼结构，兼容性最佳。**
- **Mixamo动画下载时选Without Skin，只需动画。**

---

## 你的操作建议

1. **把Mixamo动画FBX的Avatar Definition改为"Create From This Model"。**  
   不要选Copy From Other Avatar，Apply看看有没有警告！
2. **主角FBX/Rig Inspector里确认Animation Type是Humanoid，Configure无红色警告。**
3. **如此设置后，Mixamo动画clip可以挂载在Animator Controller里（针对humanoid），主角能正确播放动作。**

---

## 常见问题答疑

- Q: 为什么用“Copy From Other Avatar”报错？  
  A: 此方式只适用于完全相同的骨骼结构，用于模型LOD、换装等定制角色，不适合通用Mixamo动画。

- Q: 为什么Create模式会兼容？  
  A: Unity底层会自动匹配标准Human骨架结构（如Hips、Spine、Head、Hand等），使大多数动作互通。

---

如还遇到“肢体扭曲”或动画播放失败，可以进一步帖出你的角色和Mixamo动画的Rig骨骼Inspector截图，我可帮你逐步分析！


你的理解非常接近准确，说明你已经抓住了**Avatar的作用与意义**！我帮你再梳理和精炼一下，让你的认知更准确一些：

---

## 你的原话拆解确认

- **每一个Avatar都是独一无二的**  
  ✔️ 是的，每个Avatar记录了一套模型骨骼是怎样映射到Unity标准“人形骨架（Humanoid）”各部位的。

- **它是一套映射规则**  
  ✔️ 没错，就是把模型内的骨骼名字/层级（比如mixamo:Hips、Bip001-Hips、Root/Hip、HumanPelvis等）映射到Unity的标准人体骨骼“Hips”、“Spine”、“LeftUpperArm”等。

- **角色的Avatar就是角色的骨骼与Unity标准骨骼的映射规则**
  ✔️ 正确！角色的Avatar帮Unity把“你这个具体角色”翻译成“Unity标准人形骨架”。

- **动画的Avatar就是动画原始骨骼与Unity标准骨骼的映射规则**
  ✔️ 也正确！动画来源的FBX里也有自己的骨架，Avatar就记录了“这个动画录的时候的骨骼映射是怎样的”。

- **想让动画在角色上正确表现，Unity底层会把两者都转成标准骨骼表示，然后翻译过去**
  ✔️ 正是这样！只要两边都能翻译到Unity标准骨骼，Unity就可以把动画描述的“左腿抬高30°”套用到你的角色的“左大腿”上，无需关心双方骨骼名字/结构是否相同。

---

## 精炼的准确答案

### 1. **Avatar的本质：连接“具体骨架”与“抽象标准人形骨架”的桥梁**

- 每个Avatar告诉Unity：“我的骨骼A是你的Head、我的B是你的LeftFoot”等。
- **角色Avatar**让任何动作都能套用到这个模型（只要该动作是Humanoid）。
- **动画Avatar**让Unity知道动画Clip的关键帧是在什么骨骼结构上录下来的。

### 2. **动画与角色通信的底层机制**

- Unity第一次播放时，把动画Clip的骨骼数据通过动画的Avatar，映射到Unity的标准人形骨架（“中间层”）。
- 然后Unity把这些动作转换给角色的Avatar，再应用到实际模型骨骼上。
- 因此，**只要求双方“都能正确地映射到标准人形骨架”即可，结构、名字无所谓。**

---

## 3. 总结图示（概念模型）

```plaintext
动画Clip骨骼 -----动画的Avatar映射----> Unity标准人形骨架 <---角色的Avatar映射----- 角色骨骼
    |                                                                      |
    +--------------------- Animator播放时自动转译 ------------------------+
```

---

## 你的话进一步精炼表达一下就是：

> **每个Avatar本质是一套“骨骼与Unity标准人体的对照表”，角色Avatar和动画Avatar都起桥梁作用。任何来源的AnimationClip，只要它能映射为标准人形骨架，Unity都能把这套动作“翻译”给目标角色的骨架，让动作在任何合格的人形角色上重现。**

---

### 你说的“独一无二”也没错，不过只要映射结果都做到标准对齐，不同Avatar之间也能互通！！

---

## 扩展理解

- 这套机制让动画资产极具复用性（比如Mixamo、Assetstore动画资源等等！）。
- 如果模型缺少某些标准人体部位（比如没头、没左臂），那Avatar映射就会出错或动作不完整。