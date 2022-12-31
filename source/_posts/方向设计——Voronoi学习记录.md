---
title: 方向设计——Voronoi学习记录
date: 2022-12-24 18:12:09
tags:
updated:
top_img:
categories:
cover:
description:
---

# 问题描述：

快速高效地实现虚拟世界的大规模场景布局在游戏领域中具有广泛的应用价值，因此研究空间布局的自动生成以替代传统手动建模也成为迫切需求。本课题基于Power图，兼顾自动化设计与用户设计控制，研究高效的室内空间布局生成算法，设计并实现基于Power图的室内空间布局系统。实现要求：（1）通过文献调研，了解住室内空间布局问题；（2）通过文献调研，了解Power图的基本概念和基本算法；（3）设计并实现基于Power图的室内空间布局系统。参考文献：Zheng L, Yao Y, Wu W, et al. A novel computation method of hybrid capacity constrained centroidal power diagram[J]. Computers & Graphics, 2021, 97: 108-116.



# 解决办法

因为最近在学习前端的知识,所以本次课设决定使用JS来完成,先来到这个[网站](https://rosettacode.org/wiki/Voronoi_diagram#JavaScript)上面,这里有JS生成Voronoi图的源码,







以下是我及其蹩脚的翻译

# Introduction

Voronoi图在计算几何中有着广泛的应用和扩充,Power图作为Voronoi图的重要扩展,它给站点site引入了权重这个特性,并且重新定义了距离.通过利用容量限制给普通的power图们就可以得到容量限制的Power图(CCPD),通过在二次基础上(secondary basis)引入质心约束(centroidal constraint),就可以得到质心容量限制的Power图(CCCPD).相较于Vorionoi图,由于权重的特性,Power图有更精确的容量约束的特性.因此Power图有着更加广泛的应用比如 blue noise sampling(蓝噪声采样), mesh optimization(网格优化), fluid simulation, computer animation, location-allocation proeblem, sector division, and grain structure representation of polucrystyalline materials, etc.

Aurenhammer等人首先引入了power图的概念性质计算方法和应用.Imai等人总结了平面Power图的理论和应用.Gavrilova等人构建了基于二维power图的power图.然而,在这些研究中,容量优化的问题被忽略了.

容量是Power图和重要的一个特性,最近,大量的研究者聚焦在研究能够得到满足容量限制的power图的计算方法上.Balzer等人首先提出了针对离散空间和连续空间的容量限制Power图的生成算法.这个算法结合了错误位置逐个迭代和Lloy方法得到稳定的CCCPD(质心容量限制Power图).然而,由于这种逐个点的迭代策略这个算法从计算CCCPD过程的角度来看是非常的耗费时间的.

de Gose等人使用牛顿算法优结合Mullen等人为优化站点提出的合适步长梯度下降方法和可替代的迭代获得的CCCPD的优化权重.然而在优化过程中的权重优化和站点优化界面只有线性集中.Bourne等人提出了一种广义的方法来计算质心Power图(CPD_并且理论上证明了他的线性约束.Xin等人发展了一种有着超级线性约束的L-BFGS方法来计算具有一般距离的CCCPD,并且将它应用在blue noise sampling, displacement interpolation and polygon convex decomposition(多边形凸分解).

最近几年,郑等人提出了一个GPU-CPU算法来加速计算power图,这个算法用基于GPU的JFA算法渲染和构建POWER图,然后联系L-BFGS方法来得到CCCPD,相较于目前对现今的纯CPU功率图计算方法相比，该方法在Power图的计算效率上有了显著的提高．

在现有的Power研究中,所有站点的容量约束预设值都是固定的,每个站点的容量域CCCPD中各自的预设值近似,然而,由于难以为所有站点设置精确的容量约束值,在一些实际应用中可能存在一些具有容量约束间隔的站点,如位置分配问题(LAP),其中混合容量约束是必须考虑的,据我们所知.本文是对具有混合容量约束Power图的首次研究,为此,我们提出了一种计算混合容量约束下CPD的迭代算法,本工作的主要贡献如下:

结合固定值的容量约束和间隔容量约束,得到一种新的power图,成为混合容量约束Power图(HCCPD),通过在二次基础上施加质心约束,得到混合质心容量约束Power图(HCCCPD)

通过间隔容量约束发展出一种权重评估方法用来优化站点的权重,并提出了一种可变容量约束Power图(VCCPD)

通过牛顿和Lloyd方法的耦合,提出了高效的用以计算HCCCPD的方法.

# Priliminary

在本节中我们介绍一些Power图的相关概念







CCCPD

由于每个站点都引入了权重,Power图具有精确容量约束的特征,通过
