---
title: UnityAssetDatabase问题整理
abbrlink: 1a407a45
date: 2024-09-11 17:51:04
tags:
  - 项目经验总结
categories: UnityEditor开发
cover: https://www.notion.so/images/page-cover/met_emanuel_leutze.jpg
description: 
swiper_index: 
sticky:
---

# AssetDataBase接口使用遇到的坑

`AssetDatabase.Refresh`方法用于刷新Unity编辑器的资产数据库。这个方法会同步磁盘和Unity编辑器之间的资产状态，包括添加、删除、修改文件等。如果你在Unity编辑器外部（比如在文件浏览器中或通过脚本）对项目中的文件进行了更改，使用AssetDatabase.Refresh可以让Unity编辑器识别这些更改。

`AssetDatabase.SaveAssets`方法用于将所有未保存的资产更改持久化到磁盘。这包括对预制体、场景、材质等任何在编辑器中做出的更改。如果你在脚本中修改了任何资产（比如更改了一个材质的颜色，或者添加了一个新的游戏对象到一个预制体中），并且想要确保这些更改被保存，就需要调用这个方法。

1. 脚本中修改完资源里面的属性的时候，调用一次AssetDataBase.Save() 必须要在AssetDataBase.Load()方法之间调用，不然修改就白做了
2. File接口相关的操作Directory相关的操作后必须后面跟一个AssetDataBase.Refresh调用

`AssetDatabase.MoveAsset`调用的时候,如果目标位置的路径不存在的话移动Asset的操作是不会成功的.会提示你`Could not find parent directory GUID`,所以在移动Asset之前需要先确定目标路径是否存在,你可以使用`AssetDataBase.CreateFolder`接口来创建目标路径,或者使用`Directory`相关接口来创建,注意使用`Directory`接口创建路径后需要调用`AssetDatabase.Refresh`接口来同步

# 查询资源引用

AssetDataBase.FindAssets接口 对于预制体来说, 必须传进去的是预制体所在的目录, 是文件夹, 不能是具体的预制体, 但是对于Texture来说, 传入Texture路径却是没问题的.


# 案例

需要做热更,将原来混在一起的资源隔离出来,

资源的分类

- Material
- Texture
- Animation
- Font
- Prefab
- Audioin

# 对Prefab的处理可以全部用PrefabUtility

