---
title: RPG开发记录(零)：场景搭建
abbrlink: eb0ca922
date: 2024-09-13 00:38:54
tags: 开发日志
categories: 硬技能
cover: https://www.notion.so/images/page-cover/met_vincent_van_gogh_oleanders.jpg
description:
swiper_index:
sticky:
---

# 在开发中加入AI的协助


![](RPG开发记录(零)：场景搭建/image-2.png)

在开始进入正题之前，需要先搭建一个可以供角色站立、移动、跳跃并且还有边界的测试场景。笔者希望搭建的场景要遵循一些规范：
1. 统一所有模型资源的单位长度，极特殊情况，所有模型的缩放都设置为1
2. 将场景中的地形预制体化，比如墙、地面、台阶等
3. ...

下面说一个可以提高我们搭建场景效率的技巧：在Unity的Scene窗口中，点击下图位置的图标可以设置物体移动的固定步长。按住`Control/Command`键拖拽Scene中选中物体的Object坐标系的坐标轴，就可以以固定的步长移动物体了。按住`V`键拖拽坐标轴就可以让选中物体的选中顶点吸附到另一个场景中的物体上。

![](RPG开发记录(零)：场景搭建/image.png)

# 创建地形

## Unity中自带的模型

Unity中自带的用来表示平面的是`Plane`模型，用来表示墙面的是`Quad`模型，在场景中分别创建`Plane`和`Quad`模型，查看它们的详细信息，你会发现`Plane`模型的尺寸是10x10的(此处不考虑模型的厚度)，也就是长宽各为10个单位长度；而`Quad`模型的尺寸是1x1，长宽各1个单位长度。也就是说需要使用100个`Quad`模型才能拼出一个和`Plane`面积一样大的模型。

## 创建材质

为了让地形的尺寸展示更直观，我们使用Unity内置的一张名为`Default-Checker-Gray`贴图来装饰我们的地形，我们会为Plane和Quad各自生成一个材质，这两个材质的区别只有上面的Tilling参数，一个设置成10，而另一个设置为1。这样的设置主要和这两种模型的尺寸有关系。然后将材质分别赋给这两个模型。

![](RPG开发记录(零)：场景搭建/image-1.png)

至此，这样我们就有了搭建场景的预制地形了。



# 参考资料
- [Unity 实用教程 之 物体固定值移动调整顶点吸附](https://jingyan.baidu.com/article/1612d500968440e20e1eeebd.html)



