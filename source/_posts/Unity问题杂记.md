---
title: Unity问题杂记
tags: 
  - 问题杂记
categories: 硬技能
abbrlink: 127bc3c9
date: 2022-12-19 18:13:50
cover: https://www.notion.so/images/page-cover/met_joseph_hidley_1870.jpg
description: 
---

> 本篇博客记录笔者在Unity开发中遇到的各种小问题，有可能是Unity奇怪的bug、Unity Editor的使用小技巧或者是一些不值得开一篇新博客的小知识点

# Unity Scriptable Object踩坑

下面是一个名为`EntityBaseProperty.cs`的脚本，但是在创建一个`PlayerEntityProperty`SO的时候会提示你找不到这个脚本。你必须让脚本的名字和SO的类名保持一致，所以每一个SO都要新建一个和SO类命一样的脚本来写。
```
public class EntityBaseProperty : ScriptableObject
{
    
}

[CreateAssetMenu(fileName = "PlayerEntityProperty", menuName = "CreatePlayerEntityProperty", order = 0)]
public class PlayerEntityProperty : CharacterProperty
{
    
}

```

# Unity项目代码正常运行但是在Rider中大量报红

Rider的`solution wide analysis`组件有问题，在Unity的`Settings -> External Tools -> Regenerate project files`解决。


# AssetDataBase接口使用遇到的坑

`AssetDatabase.Refresh`方法用于刷新Unity编辑器的资产数据库。这个方法会同步磁盘和Unity编辑器之间的资产状态，包括添加、删除、修改文件等。如果你在Unity编辑器外部（比如在文件浏览器中或通过脚本）对项目中的文件进行了更改，使用AssetDatabase.Refresh可以让Unity编辑器识别这些更改。

`AssetDatabase.SaveAssets`方法用于将所有未保存的资产更改持久化到磁盘。这包括对预制体、场景、材质等任何在编辑器中做出的更改。如果你在脚本中修改了任何资产（比如更改了一个材质的颜色，或者添加了一个新的游戏对象到一个预制体中），并且想要确保这些更改被保存，就需要调用这个方法。

1. 脚本中修改完资源里面的属性的时候，调用一次AssetDataBase.Save() 必须要在AssetDataBase.Load()方法之间调用，不然修改就白做了
2. File接口相关的操作Directory相关的操作后必须后面跟一个AssetDataBase.Refresh调用

`AssetDatabase.MoveAsset`调用的时候,如果目标位置的路径不存在的话移动Asset的操作是不会成功的.会提示你`Could not find parent directory GUID`,所以在移动Asset之前需要先确定目标路径是否存在,你可以使用`AssetDataBase.CreateFolder`接口来创建目标路径,或者使用`Directory`相关接口来创建,注意使用`Directory`接口创建路径后需要调用`AssetDatabase.Refresh`接口来同步

# GetComponentsInChildren接口

GetComponentsInChildren方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# GetComponentInParent接口

GetComponentInParent方法中要一个参数，这个参数的作用是是否包括处于非激活状态的节点，默认是false

# DoTween

DoTween不仅可以用来做动画，也可以实现音频减弱、图片的渐显操作。

# Spine动画的使用

Spine动画有一个专门针对UI的组件叫做SkeletonGraphic，SkeletonGraphic组件是基于UICanvas绘制的，因此它的渲染层级可以被Canvas管理
另外还有专门为非UI播放动画的SkeletonAnimation组件，该组件的渲染是基于MeshRenderer的，因此不受Canvas的管理，当要在UI上展示Spine动画的时候，要选择使用SkeletonGraphic组件。

SkeletonGraphic相关接口
```
// 停止当前正在播放的动画
// heartBeatFlower.AnimationState.ClearTrack(0);    
heartBeatFlower.AnimationState.SetEmptyAnimation(0,0); 
// 播放新的动画
heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  
```
> 2024.11.8更新
> 使用SkeletonGraphic.AnimationState.ClearTrack(0)遇到坑了，目前笔者还不清楚Spine的作用原理，从表现上看，调用该接口会将Spine动画从轨道上移除，之后想要再次播放该动画的话就无法在轨道上找到这个动画，所以如果有切换播放动画的需求，只需要调用heartBeatFlower.AnimationState.SetAnimation(0, "chufa", false);  就可以了

SkeletonAnimation相关接口
```
// 停止当前正在播放的动画
heartBeatFlower.state.SetEmptyAnimation(0,0);            
// 播放新的动画
heartBeatFlower.state.SetAnimation(0, "loop", true);  
```

# Animator使用

Animator中必须设置一个从Entry进入的默认状态，这个从Entry进入默认状态的操作会在Animator所挂载的游戏物体的Active状态变为true或者Animator组件自身的enable状态变为true的时候自动执行，无法控制。如果在默认状态设置了某些动画，在其他开发同事不知情的情况下设置了这个Animator节点的Active状态，就会导致动画的自动触发，可能会给别人留坑。更好的使用Animator的方法是将默认状态设置为一个空状态。让真正的动画状态指向默认状态，如下图：

![](Unity问题杂记/image.png)

在我们需要播放动画的时机可以通过[`Animator.Play()`](https://docs.unity3d.com/cn/current/ScriptReference/Animator.Play.html)接口，并且在动画播放完之后会自动的进入默认状态，这样的好处是不用通过控制Active状态来控制动画的播放，而且在代码中我们也是通过`Animator.Play`接口控制动画的播放，比通过Active状态来控制的方法更让人知道这行代码在做什么。

接口的具体参数如下，其中第三个参数normalizedTime是一个归一化的时间，[0,1]指从动画的什么时刻开始播放
```
public void Play (string stateName, int layer= -1, float normalizedTime= float.NegativeInfinity);
public void Play (int stateNameHash, int layer= -1, float normalizedTime= float.NegativeInfinity);
```
> 2024.12.18更新
> 最近尝试了一种新的延时方法,以前在处理当某一动画播放完毕后,执行一段逻辑的时候,都是用
> 下面这两种方法都可以对当前的Animator播放动画的normalizedTime做调整
> ![](Unity问题杂记/image-1.png)
> 2024.12.28更新
> 我们更近一步,使用UniTask来实现
> 
# DOTweenAnimation组件使用

DOTweenAnimation组件挂载在节点上，调用播放的时候只播第一次，之后不播了，需要搞清楚为什么



# 协程的坑

- 协程中的等待一秒并不是真正的一秒有可能会有误差

- 提示要执行Coroutine的物体的状态是inactive的
一个GameObject,在同一帧内,先被SetActive了,然后紧接着用这个GameObject上的一个Mono执行StartCoroutine,会提示GameObject无法被执行,验证是否是这样的.

# Unity的Animation的使用的坑

Animation中有一个Legacy字段，如果不勾选这个选项，在Animation中是没有办法通过Animation.Play()接口播放该动画的。
有兴趣可以做一个实验，创建一个勾选了Legacy和一个未勾选Legacy的动画，将这两个动画都添加到一个Animation组件上。在代码中获取这个Animation组件的引用，调用`Animation.GetClipCount()`接口得到的值为2，但是如果使用`foreach(AnimationState state in Animation)`去遍历却只会遍历到勾选了Legacy的AnimationClip

# XML文件读取逻辑

定义需要从XML中读取的数据结构

```
[XmlRoot("AssetBundleConfig")]
public class AssetBundleConfig
{
    public List<BundleRule> Bundles;
    
    public List<AtlasRule> Atlas;
}


public class BundleRule
{
    public string Relative;
    public string Path;
    public string Type;
    public bool Recursion;
}

public class AtlasRule
{
    public string Path;
    public bool Recursion;
}
```
定义XML文件内容
```
<?xml version="1.0" encoding="utf-8" ?>
<AssetBundleConfig xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:noNamespaceSchemaLocation="AssetBundleConfigSchema.xsd">
    <!--带有平台名称的bundle只会出现名称所代表的平台-->
    <Bundles>
        <BundleRule name="Art_Animation">
            <Path>Assets/Art/Animation</Path>
            <Type>SubFolder</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_Audio">
            <Path>Assets/Art/Audio</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_CustomShaders">
            <Path>Assets/Art/CustomShaders</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>

        <BundleRule name="Art_Fonts">
            <Path>Assets/Art/Fonts</Path>
            <Type>None</Type>
            <Recursion>false</Recursion>
        </BundleRule>
        
        <BundleRule name="Art_Fonts8x8">
            <Relative>Assets/Art/Fonts</Relative>
            <Path>Assets/Art/Fonts/Fonts8x8</Path>
            <Type>SubFolder</Type>
            <Recursion>true</Recursion>
        </BundleRule>

    </Bundles>
    
    <Atlas>
        <AtlasRule name="Art_Atlas">
            <Path>Assets/Art/Texture/Atlas</Path>
            <Recursion>true</Recursion>
        </AtlasRule>
    </Atlas>
</AssetBundleConfig>
```

C#脚本读取该XML文件
```
public static T LoadXmlConfig<T>(string path) where T : class
{
    XmlSerializer xmlSerializer = new XmlSerializer(typeof(T));
    T result;
    using (var reader = XmlReader.Create(path))
    {
        result = (T) xmlSerializer.Deserialize(reader);
    }

    return result;
}
```
调用时只需要把`AssetBundleConfig`作为T传入该方法，就可以返回XML的内容


# 写一个假的进度条

```
using System;
using UnityEngine;
using UnityEngine.UI;

namespace UI
{
    public class LoadingUICartoon : LoadingUI
    {
        [SerializeField] private Text progressText;
        
        private float currentValue = 0f; // 当前值
        private float targetValue = 100f; // 目标值
        private float speed = 1f; // 增长速度
        private bool endLoading = false;
        
        protected override void Start()
        {
            canvas.sortingLayerID = R.SortingLayers.Default;
            progressText.text = "0%";
        }

        private void Update()
        {
            if (endLoading)
            {
                return;
            }
            
            // 使用指数衰减的方式逐渐接近目标值
            currentValue += (targetValue - currentValue) * speed * Time.deltaTime;

            // 更新Text组件的显示内容
            progressText.text = Mathf.FloorToInt(currentValue).ToString() + "%";
        }

        public void SetProgress(float progress)
        {
            endLoading = true;
            progressText.text = progress.ToString("0") + "%";
        }
        
    }
}
```

# Unity中对导入的资产进行自动化的导入格式设置

# 查询资源依赖的方案

# Unity预制体中的Image组件内容不显示

https://blog.csdn.net/WGYHAPPY/article/details/116161817

# 使用摄像机渲染一个RenderTexture 以及 我想要把一个Canvas放到世界坐标下,即不会在上层嵌套一个UICanvas

https://blog.csdn.net/qq_37179591/article/details/118148818


# Mac下如何打出乘号
https://www.zhihu.com/question/20367435

# 固定长度字符串

如果你希望 `_moveStrus[posY, posX].moveDir` 的输出是一个固定长度的字符串，可以通过格式化字符串的方式来实现。`BrickMoveDir` 是一个枚举值，默认会通过 `ToString()` 输出枚举的名称。如果你希望将其输出为固定长度，可以通过以下方式处理：

---

### **方法 1：使用字符串格式化并填充空格**
你可以用 `String.Format` 或插值字符串（C# 6.0 及以上支持）来对值进行格式化，并通过指定固定宽度填充空格。

#### 示例代码
```csharp
sb.AppendFormat("{0,-10}", _moveStrus[posY, posX].moveDir);
```

- `{0,-10}` 表示将第一个参数的值左对齐，并确保占用 10 个字符的宽度。如果值的长度不足 10，则填充空格；如果值的长度超过 10，则不会截断。

或者使用字符串插值的方式：
```csharp
sb.Append($"{_moveStrus[posY, posX].moveDir,-10}");
```

---

### **方法 2：通过 `PadRight` 或 `PadLeft` 填充**
你可以将 `moveDir` 转换为字符串后，使用 `PadRight`（左对齐）或 `PadLeft`（右对齐）来填充到固定长度。

#### 示例代码
```csharp
sb.Append(_moveStrus[posY, posX].moveDir.ToString().PadRight(10));
```

- `PadRight(10)` 会将字符串填充到 10 个字符宽度，右侧补空格。
- 如果需要右对齐，可以使用 `PadLeft(10)`。

---

### **方法 3：加上 `\t` 制表符**
如果你希望使用制表符（`\t`）来对齐列，可以在每个值后面追加一个 `\t`。不过，制表符的宽度在不同环境下可能会有所不同（通常为 4 或 8 个字符），因此不推荐用于精确对齐。

#### 示例代码
```csharp
sb.Append(_moveStrus[posY, posX].moveDir + "\t");
```

---

### **方法 4：将枚举值转换为整数并格式化**
如果你希望输出的值是固定宽度的数字（而不是枚举名称），可以将 `BrickMoveDir` 转换为整数，再进行格式化。例如，确保输出占用 4 个字符宽度：

#### 示例代码
```csharp
sb.AppendFormat("{0,4}", (int)_moveStrus[posY, posX].moveDir);
```

- `{0,4}` 表示右对齐并占用 4 个字符宽度。

---

### **完整示例**
以下是一个完整的示例，展示如何将 `_moveStrus` 的内容按固定宽度输出到 `StringBuilder` 中：

```csharp
StringBuilder sb = new StringBuilder();
for (int posY = 0; posY < 8; posY++)
{
    for (int posX = 0; posX < 8; posX++)
    {
        // 将 moveDir 输出为固定宽度的字符串（左对齐，宽度 10）
        sb.Append($"{_moveStrus[posY, posX].moveDir,-10}");
    }
    sb.AppendLine(); // 换行
}
Console.WriteLine(sb.ToString());
```

---

### **输出示例**
假设 `_moveStrus[posY, posX].moveDir` 的值为 `Up`、`Down`、`Left` 等，输出可能如下：

```
Up        Down      Left      Right     None      None      None      None      
None      None      None      None      None      None      None      None      
...
```

---

### **总结**
- 如果需要固定宽度的字符输出，推荐使用 **字符串格式化**（`String.Format` 或插值字符串）或 **PadRight/PadLeft**。
- 如果需要使用制表符（`\t`），请注意它的宽度可能因环境而异，不适合精确对齐。
- 如果需要输出数字而非字符串，可以将枚举转换为整数后格式化。


# Struct中的问题

```
[Flags]
enum BrickMoveDir
{
    None = 0,
    Up = 1 << 0,
    Left = 1 << 1,
    Right = 1 << 2,
    Down = 1 << 3,
}

struct MoveStru
{
    public BrickMoveDir moveDir;
    public Vector2 moveVector;
}

public override void Init()
{
    base.Init();
    for (int i = 0; i < 8; i++)
    {
        for (int j = 0; j < 8; j++)
        {
            _moveStrus[i, j] = new MoveStru();
        }
    }
}

public override void Release()
{
    base.Release();
}

public void PlayClearLineEffect(ClearLinePlayParam param)
{
    var clearY = param.clearLineY;
    var clearX = param.clearLineX;
    
    for (int i = 0; i < clearY.Count; i++)
    {
        for (int posY = 0; posY < 8; posY++)
        {
            if (posY < clearY[i] && i == 0)
            {
                for (int posX = 0; posX < 8; posX++)
                {
                    _moveStrus[posY, posX].moveDir |= BrickMoveDir.Up;
                    _moveStrus[posY, posX].moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
                }
            } 
                
            if (posY > clearY[i] && i == clearY.Count - 1)
            {
                for (int posX = 0; posX < 8; posX++)
                {
                    _moveStrus[posY, posX].moveDir |= BrickMoveDir.Down;
                    _moveStrus[posY, posX].moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
                }
            }
        }
    }

    for (int i = 0; i < clearX.Count; i++)
    {
        // 遍历BoardView
        for (int posX = 0; posX < 8; posX++)
        {
            if (posX < clearX[i] && i == 0)
            {
                for (int posY = 0; posY < 8; posY++)
                {
                    _moveStrus[posY, posX].moveDir |= BrickMoveDir.Left;
                    _moveStrus[posY, posX].moveVector.x = GetOffsetRatio(clearX[i], posX);
                }
            } 
                
            if (posX > clearX[i] && i == clearX.Count - 1)
            {
                for (int posY = 0; posY < 8; posY++)
                {
                    _moveStrus[posY, posX].moveDir |= BrickMoveDir.Right;
                    _moveStrus[posY, posX].moveVector.x = GetOffsetRatio(clearX[i], posX);
                }
            }
        }
    }
    
    foreach (var move in _moveStrus)
    {
        if ((move.moveDir & BrickMoveDir.Up) != 0 && (move.moveDir & BrickMoveDir.Down) != 0)
        {
            move.moveDir &= ~(BrickMoveDir.Up | BrickMoveDir.Down);
        }
        
        if ((move.moveDir & BrickMoveDir.Left) != 0 && (move.moveDir & BrickMoveDir.Right) != 0)
        {
            move.moveDir &= ~(BrickMoveDir.Left | BrickMoveDir.Right);
        }
    }
}
```
这段代码有什么问题?


这段代码看起来是用来处理一个 8x8 的二维数组 `_moveStrus` 的逻辑，其中每个元素包含一个方向标志 `moveDir` 和一个向量 `moveVector`。代码的目标是根据 `clearY` 和 `clearX` 的参数，设置数组中每个元素的移动方向和移动向量，并处理相反方向的抵消逻辑。

以下是代码中存在的一些问题和改进建议：

---

### **1. 结构体的值类型问题**
在 C# 中，`struct` 是值类型，而不是引用类型。当你通过索引访问 `_moveStrus[posY, posX]` 并修改其字段时，实际上是在操作该结构体的副本，而不是直接修改数组中的元素。因此，修改 `moveDir` 或 `moveVector` 的值不会反映到数组中。

#### **问题代码**
```csharp
_moveStrus[posY, posX].moveDir |= BrickMoveDir.Up;
_moveStrus[posY, posX].moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
```

#### **解决方案**
你需要显式地获取结构体、修改它的值，然后将其重新赋值回数组中：
```csharp
var moveStru = _moveStrus[posY, posX];
moveStru.moveDir |= BrickMoveDir.Up;
moveStru.moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
_moveStrus[posY, posX] = moveStru;
```

---

### **2. `foreach` 遍历的问题**
在 `foreach` 中，你正在尝试修改 `_moveStrus` 中的元素的 `moveDir` 值：
```csharp
foreach (var move in _moveStrus)
{
    if ((move.moveDir & BrickMoveDir.Up) != 0 && (move.moveDir & BrickMoveDir.Down) != 0)
    {
        move.moveDir &= ~(BrickMoveDir.Up | BrickMoveDir.Down);
    }
    
    if ((move.moveDir & BrickMoveDir.Left) != 0 && (move.moveDir & BrickMoveDir.Right) != 0)
    {
        move.moveDir &= ~(BrickMoveDir.Left | BrickMoveDir.Right);
    }
}
```

然而，由于 `foreach` 遍历的是数组元素的副本（值类型），对 `move` 的修改不会影响原数组中的值。这种情况下，修改 `moveDir` 是无效的。

#### **解决方案**
改用 `for` 循环，直接访问数组中的元素并修改：
```csharp
for (int i = 0; i < _moveStrus.GetLength(0); i++)
{
    for (int j = 0; j < _moveStrus.GetLength(1); j++)
    {
        var move = _moveStrus[i, j];
        if ((move.moveDir & BrickMoveDir.Up) != 0 && (move.moveDir & BrickMoveDir.Down) != 0)
        {
            move.moveDir &= ~(BrickMoveDir.Up | BrickMoveDir.Down);
        }

        if ((move.moveDir & BrickMoveDir.Left) != 0 && (move.moveDir & BrickMoveDir.Right) != 0)
        {
            move.moveDir &= ~(BrickMoveDir.Left | BrickMoveDir.Right);
        }

        _moveStrus[i, j] = move;
    }
}
```

---

### **3. `GetOffsetRatio` 方法的实现**
代码中多次调用了 `GetOffsetRatio` 方法，但其实现未提供。这个方法似乎是用来计算某种偏移比例的。如果该方法的实现有问题，可能会导致 `moveVector` 的计算错误。

#### **建议**
确保 `GetOffsetRatio` 方法的实现正确，并且能够根据 `clearY` 和 `posY`（或 `clearX` 和 `posX`）正确返回一个浮点值。

---

### **4. `PlayClearLineEffect` 中逻辑的优化**
当前的逻辑中，`clearY` 和 `clearX` 分别用于处理垂直和水平方向的清除效果。代码中有一些重复的逻辑，可以通过提取公共部分来简化。

#### **优化代码**
可以将垂直方向和水平方向的逻辑提取为单独的方法，减少重复代码。例如：
```csharp
private void ApplyVerticalEffect(List<int> clearY)
{
    for (int i = 0; i < clearY.Count; i++)
    {
        for (int posY = 0; posY < 8; posY++)
        {
            if (posY < clearY[i] && i == 0)
            {
                for (int posX = 0; posX < 8; posX++)
                {
                    var moveStru = _moveStrus[posY, posX];
                    moveStru.moveDir |= BrickMoveDir.Up;
                    moveStru.moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
                    _moveStrus[posY, posX] = moveStru;
                }
            }

            if (posY > clearY[i] && i == clearY.Count - 1)
            {
                for (int posX = 0; posX < 8; posX++)
                {
                    var moveStru = _moveStrus[posY, posX];
                    moveStru.moveDir |= BrickMoveDir.Down;
                    moveStru.moveVector.y = -1.0f * GetOffsetRatio(clearY[i], posY);
                    _moveStrus[posY, posX] = moveStru;
                }
            }
        }
    }
}

private void ApplyHorizontalEffect(List<int> clearX)
{
    for (int i = 0; i < clearX.Count; i++)
    {
        for (int posX = 0; posX < 8; posX++)
        {
            if (posX < clearX[i] && i == 0)
            {
                for (int posY = 0; posY < 8; posY++)
                {
                    var moveStru = _moveStrus[posY, posX];
                    moveStru.moveDir |= BrickMoveDir.Left;
                    moveStru.moveVector.x = GetOffsetRatio(clearX[i], posX);
                    _moveStrus[posY, posX] = moveStru;
                }
            }

            if (posX > clearX[i] && i == clearX.Count - 1)
            {
                for (int posY = 0; posY < 8; posY++)
                {
                    var moveStru = _moveStrus[posY, posX];
                    moveStru.moveDir |= BrickMoveDir.Right;
                    moveStru.moveVector.x = GetOffsetRatio(clearX[i], posX);
                    _moveStrus[posY, posX] = moveStru;
                }
            }
        }
    }
}
```

然后在 `PlayClearLineEffect` 方法中调用这些方法：
```csharp
public void PlayClearLineEffect(ClearLinePlayParam param)
{
    ApplyVerticalEffect(param.clearLineY);
    ApplyHorizontalEffect(param.clearLineX);

    for (int i = 0; i < _moveStrus.GetLength(0); i++)
    {
        for (int j = 0; j < _moveStrus.GetLength(1); j++)
        {
            var move = _moveStrus[i, j];
            if ((move.moveDir & BrickMoveDir.Up) != 0 && (move.moveDir & BrickMoveDir.Down) != 0)
            {
                move.moveDir &= ~(BrickMoveDir.Up | BrickMoveDir.Down);
            }

            if ((move.moveDir & BrickMoveDir.Left) != 0 && (move.moveDir & BrickMoveDir.Right) != 0)
            {
                move.moveDir &= ~(BrickMoveDir.Left | BrickMoveDir.Right);
            }

            _moveStrus[i, j] = move;
        }
    }
}
```

---

### **5. `Release` 方法未实现**
`Release` 方法目前是空的。如果需要释放资源或清理数据，应在此方法中实现相应的逻辑。例如，你可能希望重置 `_moveStrus` 数组中的所有值。

---

### **总结**
主要问题是：
1. 结构体是值类型，直接修改数组元素的字段无效。
2. `foreach` 只能遍历副本，无法修改原数组。
3. 代码中有重复逻辑，可以通过提取公共部分优化。
4. 确保 `GetOffsetRatio` 方法的正确性。

通过上述修改，这段代码将更加健壮和清晰。

# public class ObjectPool<T> where T : IPoolObject, new() new()的含义是什么?

在 C# 中，`where T : new()` 是一个**泛型约束**，它表示泛型类型参数 `T` 必须具有一个**无参数的公共构造函数**。这意味着使用 `new()` 约束的类型 `T` 必须满足以下条件：

1. `T` 必须是一个非抽象类（即不能是抽象类或接口）。
2. `T` 必须有一个无参数的公共构造函数（即 `public T()`）。

在你的例子中：

```csharp
public class ObjectPool<T> where T : IPoolObject, new()
```

这段代码的含义是：
- `T` 是一个泛型类型参数。
- `T` 必须实现 `IPoolObject` 接口（`where T : IPoolObject`）。
- `T` 必须具有一个无参数的公共构造函数（`where T : new()`）。

---

### **`new()` 的作用**
使用 `new()` 约束后，代码中可以直接使用 `new T()` 来创建类型 `T` 的实例。因为编译器知道 `T` 满足无参数构造函数的约束，所以可以安全地生成实例。

#### 示例代码
```csharp
public interface IPoolObject
{
    void Reset();
}

public class ObjectPool<T> where T : IPoolObject, new()
{
    private List<T> pool = new List<T>();

    public T GetObject()
    {
        if (pool.Count > 0)
        {
            T obj = pool[0];
            pool.RemoveAt(0);
            return obj;
        }
        else
        {
            // 使用 new T() 创建一个新的实例
            return new T();
        }
    }

    public void ReturnObject(T obj)
    {
        obj.Reset();
        pool.Add(obj);
    }
}

public class MyPoolObject : IPoolObject
{
    public void Reset()
    {
        Console.WriteLine("Resetting object...");
    }
}

class Program
{
    static void Main()
    {
        ObjectPool<MyPoolObject> pool = new ObjectPool<MyPoolObject>();

        // 获取对象
        MyPoolObject obj = pool.GetObject();
        Console.WriteLine("Object created");

        // 返回对象
        pool.ReturnObject(obj);
    }
}
```

#### 输出
```
Object created
Resetting object...
```

#### 解释
1. **`new T()`**：在 `GetObject()` 方法中，当池中没有可用对象时，直接通过 `new T()` 创建一个新的对象。
2. **`where T : new()`**：确保泛型类型参数 `T` 有一个无参数的构造函数，因此 `new T()` 是合法的。
3. **`where T : IPoolObject`**：确保 `T` 实现了 `IPoolObject` 接口，因此可以调用 `Reset()` 方法。

---

### **为什么需要 `new()` 约束？**
在 C# 中，泛型类型参数是未知的类型，编译器无法确定类型 `T` 是否具有构造函数。如果你尝试在没有 `new()` 约束的情况下使用 `new T()`，编译器会报错。

#### 错误示例
```csharp
public class ObjectPool<T>
{
    public T CreateObject()
    {
        // 编译错误：T 必须具有无参数构造函数
        return new T();
    }
}
```

#### 错误信息
```
'new()' constraint must be used on type parameter 'T' to create instances of the type.
```

#### 解决方法
添加 `new()` 约束：
```csharp
public class ObjectPool<T> where T : new()
{
    public T CreateObject()
    {
        return new T();
    }
}
```

---

### **`new()` 的限制**
虽然 `new()` 约束允许你使用 `new T()`，但它有以下限制：
1. **只能调用无参数的构造函数**：
   - 如果 `T` 只有带参数的构造函数（例如 `T(int value)`），则无法使用 `new()` 约束。
   - 解决方案是通过工厂模式或委托来创建对象，而不是直接使用 `new T()`。

   ```csharp
   public class ObjectPool<T> where T : IPoolObject
   {
       private Func<T> objectFactory;

       public ObjectPool(Func<T> factory)
       {
           objectFactory = factory;
       }

       public T CreateObject()
       {
           return objectFactory();
       }
   }
   ```

2. **不能用于抽象类或接口**：
   - `new()` 约束要求类型 `T` 是可以实例化的（非抽象类），因此不能直接用于抽象类或接口。

---

### **总结**
- `new()` 是一个泛型约束，表示泛型类型参数必须具有一个无参数的公共构造函数。
- 它允许在泛型类或方法中通过 `new T()` 创建实例。
- 常用于需要动态创建泛型类型实例的场景，比如对象池、工厂模式等。
- 结合其他约束（如接口约束 `where T : IPoolObject`），可以更灵活地定义泛型类的行为。


# 查询资源引用



# UnityEditor下的加载弹窗或者弹窗

# Unity中安装的NuGet包

使用Rider的时候,如果在代码中用到了一些第三方的.Net库,在提示里面会有一个选项是从NuGet中安装该第三方库.但是这个第三方库

# .Net .NetFramework .NetStandard C#版本之间的关系