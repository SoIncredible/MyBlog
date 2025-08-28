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

AssetDataBase.FindAssets接口 **对于预制体来说, 必须传进去的是预制体所在的目录, 是文件夹, 不能是具体的预制体, 但是对于Texture来说, 传入Texture路径却是没问题的.**

# 使用AssetDatabse创建文件夹


# 对Prefab的处理可以全部用PrefabUtility

加载一个预制体, 如果只是对一个预制体 只读, 那么可以使用AssetDatabase的Load接口加载, 但是如果是要对预制体的内容进行修改, 则要使用PrefabUtility中的Load接口,


# AssetPostProcessor
# AssetModificationProcessor
# AssetImporter
# AudioImporter

思考一下 Unity里面有几种类型的资源:

模型、Texture、Audio类 他们都会有一个ImportSettings, 这类资源不是Unity可以Create的.


预制体

材质呢?

你的理解很接近真实的Unity资源体系！我帮你进一步理清和补充：

---

## 你总结的三种类型资源

### 1. **外部导入资源（有Importer处理）**
**特征：**  
- 源文件本身不是Unity创建，比如 `.png`、`.jpg`、`.fbx`、`.wav`、`.mp3` 等
- 需要导入器（Importer），比如 TextureImporter、ModelImporter、AudioImporter
- 在Unity导入流程中，Importer负责转换参数、导入设置、生成meta数据和最终的Unity对象（如Texture、Mesh、AudioClip等）
- 你可以通过Importer批量修改这些资源的导入参数

**例子：**  
- 外部图片 → TextureImporter → Texture2D
- 外部模型 → ModelImporter → Mesh, Material, AnimationClip
- 外部音频 → AudioImporter → AudioClip

---

### 2. **Unity创建且有专门管理模块/接口的资源**
**特征：**  
- 这些资源类型由Unity原生创建和管理
- 编辑器暴露了专门的管理API或工具类用于批量处理、特殊保存
- 典型代表如Prefabs（PrefabUtility）、场景Scene（EditorSceneManager），还有Timeline Asset、AnimatorController等等
- 这类资源一般有特别的生命周期或内容变换机制

**例子：**  
- Prefab（通过PrefabUtility管理加载/保存/变更）
- 场景（通过EditorSceneManager管理保存/加载/脏标记）
- AnimatorController（部分模块有专用API，但不全）

---

### 3. **Unity创建但没有专用管理模块的资源**
**特征：**  
- Unity里手动创建的资产，但官方没有专门的批量接口或"导入管理器"
- 通常直接通过AssetDatabase和EditorUtility.SetDirty来批量处理
- 典型如Material、ScriptableObject、普通.asset文件、PhysicMaterial等

**例子：**  
- Material
- ScriptableObject（自定义游戏数据）
- PhysicMaterial
- LightingDataAsset等

---

## 补充细节

- 有些资源类型可能同时涉及多种管理方式，比如模型文件（.fbx）导入，既由ModelImporter处理，也会自动生成Material和Prefab资源。
- Unity的资源归类实际还和“如何生成/管理/编辑/导入/保存/批量变更”有关，而不是单纯“能不能创建”。
- 一些资源有编辑器扩展，但没有专门管理器（比如LightingSettings之类）。
- 场景、Prefab等是少数拥有专门流程和API的资产；绝大多数Unity资产都是“你需要自己查找、编辑、SetDirty然后SaveAssets。”

---

## 总结归纳表

| 类型              | 来源              | 管理方式                        | 例子                    |
|-------------------|-------------------|---------------------------------|-------------------------|
| 外部导入资源      | Unity外部         | Importer/AssetPostprocessor     | .png, .fbx, .wav        |
| Unity专有模块资源 | Unity内部创建     | 专用API（PrefabUtility等）      | Prefab, Scene           |
| 普通Unity资产     | Unity内部创建     | AssetDatabase+EditorUtility     | Material, ScriptableObject |

---

### 你的理解是正确的！

- 可完全理解为：“Importer类仅服务于外部导入的资源，部分Unity资产（Prefab/Scene）有专门工具模块管理，剩下绝大多数Unity自己创建的资产（如Material）则没有专门模块，需要自己脚本批量处理。”

---

如需更精细的分类表、具体批量方案、API对比可以继续问！

