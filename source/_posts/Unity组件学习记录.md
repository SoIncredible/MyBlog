---
title: Unity组件学习记录
tags:
  - Unity
categories:
  - 学习笔记
abbrlink: d5c2d910
date: 2023-04-23 10:44:19
cover: 'http://soincredible777.com.cn:90/32.png'
description:
swiper_index:
sticky:
---

# 背景

在经过两周多与迭代器纠缠不清的拉扯之后，终于理解了迭代器的设计思想，但是把迭代器这种东西是要花费太多功夫了，下面这段时间我想把精力多放在学习Unity组件使用上。

# UI组件

## Scroll View

ScrollView是实现无限滑动列表的基础，我们先来看看它的结构、以及每个节点上挂在的组件的作用：

```
- ScrollView (GameObject)
  ├─ RectTransform
  └─ Scroll Rect (Script)
  ├─ Viewport (GameObject)
  │  ├─ RectTransform
  │  └─ Mask + Image (Script+Component)
  │  └─ Content (GameObject)
  │    └─ RectTransform (Inside the content, you will have your items or elements)
  ├─ Scrollbar Horizontal (GameObject) (optional)
  │  ├─ RectTransform
  │  └─ Scrollbar (Script)
  ├─ Scrollbar Vertical (GameObject) (optional)
     ├─ RectTransform
     └─ Scrollbar (Script)
```

这里面有两个组件需要我们搞清楚：`Scroll Rect`和`Rect Transform`

### Scroll Rect组件

在Unity中，ScrollRect组件用于实现滚动窗口，在这个窗口中，我们可以移动其内容来查看被遮挡的部分。经常搭配UI元素（比如列表、长文本等）使用

下面是Scroll Rect组件的一些主要属性与功能：

1. Content：一个RectTransform，表示滚动区域中的内容。它包含所有需要滚动的子物体。通常还会添加一个LayoutGroup组件（比如水平排列布局组、垂直排列布局组等），以自动定位和调整子物体的位置和大小
2. Horizontal和Vertical：分别控制水平滚动和垂直滚动是否启用
3. Movement Type：
   - Unrestricted：容器可以在任何方向上无限制地滚动。
   - Elastic：容器具有弹性边界。当用户停止拖动之后，内容会缓冲返回原始边界内。
   - Clamped：不允许将内容拖到容器边界以外
4. Elasticity：管理了Elastic类型移动时的回弹效果。值越高，回弹速度越快，反之越慢。
5. Inertia：如果启动惯性，用户在拖动内容并释放时，内容会按照惯性继续滚动一段时间。
6. Deceleration Rate：表示成比例的惯性减速（仅在惯性启用的时候又消失）。值越低，减速的效果越强。
7. Scroll Sensitivity：滚动响应的灵敏度。值越大，对拖动的响应越敏感。
8. Viewport：可视区域的RectTransform。可以将大小调整为要显示内容的部分区域。如果不设置，会退回外层容器作为Viewport
9. Horizontal ScrollBar和Vertical ScrollBar：缩略图栏，可以将Scrollbar组件连接到Scroll Rect组，与滚动内容同步变化。
10. Visibility：定义ScrollBar是否一直可见、仅在拖动时可见、或者用户悬停时可见。另外还有不支持此功能的选项AutoHideAndExpandViewport，它不仅可以自动隐藏滚动条，并且在隐藏时会扩大可视界面以填充原本滚动条占据的空间。

### RectTransform

在 Unity 中，RectTransform（矩形变换）组件是 UI 元素的核心部分。它用于定义 2D 界面元素在屏幕上的位置、大小和方向。RectTransform 专为 Unity 的 UI 系统（UGUI，Unity Graphic User Interface）设计，继承自 Transform 组件。与标准的 Transform 组件相比，RectTransform 提供了针对二维空间的额外功能。

以下是 RectTransform 的一些关键特性和属性：

1. **锚定（Anchors）**：锚点用于确定 UI 元素在其父容器中的相对位置。有两种类型的锚点：最小锚点和最大锚点。最小锚点表示左下角的位置，最大锚点表示右上角的位置。通过调整这两个锚点，您可以将 UI 元素固定到父容器的一个角落、边缘或中心。
2. **轴对齐（Pivot）**：轴心点是旋转和缩放操作的参考点。默认情况下，轴心点位于矩形的中心（0.5, 0.5）。您可以移动轴心点以更改 UI 元素的旋转和缩放方式。
3. **尺寸（Width and Height）**：这两个属性定义了 UI 元素的宽度和高度。你可以通过直接设置数值或使用 RectTransform 工具在场景视图中进行调整。
4. **位置（Position）**：还可以设置 RectTransform 的位置属性，以确定 UI 元素在屏幕上的具体位置。位置属性包括 X、Y 和 Z 坐标。
5. **缩放（Scale）**：与 Transform 组件类似，RectTransform 也有一个表示局部缩放的属性，允许您改变 UI 元素的大小。

要使用 RectTransform，需要先确保已经导入了 Unity 的 UI 系统。然后，在场景中创建 UI 元素（例如按钮、文本框、图像等），它们将自动带有 RectTransform 组件。通过调整 RectTransform 的各种属性，您可以灵活地布局和设计 UI 界面。

总之，RectTransform 是 Unity UI 系统的核心组件，用于在场景中定位、旋转、缩放和调整大小。借助 RectTransform，开发者可以轻松设计出响应式和美观的用户界面。

除了上面这两个组件之外，还有一个组件我们需要了解：`Grid Layout Group`

### GridLayoutGroup

Grid Layout Group组件是一种UI布局组件，它可以让我们轻松地创建规则的二维网格布局。Grid Layout Group组件可以在其中放置子元素，并根据指定的行列书自动整理排列这些子物体

使用时，将Grid Layout Group组件附加到某个UI对象，比如Panel或者GameObject。它的主要属性有：

- Padding：它定义了网格内子元素与容器边缘之间的间距，它有四个方向的值：左、右、上、下。通过这些值，可以控制整个网格相对于其夫对象容器的内边距，通过设置这些值，可以控制整个网格相对于其父对象容器的内边距，这样一来我们就可以根据设定完整布局在容器内的留白区域了。
- Cell Size：单元格尺寸决定了子物体的大小
- Spacing：间距用于控制每个单元格之间的横向和纵向距离。
- Start Corner：起始角落，定义从哪个角落开始排列子物体（左上、右上、左下、右下）
- StartAxis：起始轴，确定网格的填充顺序是先填充行还是先填充列。
- Child Alignment：子物体对齐方式，设置子元素在网格内的对齐方式。
- Constraint：约束，可选择无约束、固定列数或者固定行数，在此情况下会自动调整它们的数量或者高度/宽度以满足限制条件。

### 如何隐藏掉ScrollBar

我们可以通过调整ScrollBar Visibility属性来将ScrollBar隐藏掉，但是给出的选项中是没有将ScrollBar一直隐藏掉的功能，我们需要通过其他的方式实现：

将ScrollBar的透明度设置为0，选中包含ScrollBar组件的GameObject，在Inspector窗口找到Image组件。展开`Color`属性并将Alpha设置为0。这将使ScrollBar变为透明，看起来像是消失了一样，但是仍然具有交互的功能。不过要注意的是我们要确保将所有跟ScrollBar相关的图片、颜色、子物体都设置成透明。

到此还没有结束，因为虽然ScrollBar现在看上去已经透明了，但是ScrollBar占据的空间还在那，所以效果上就像是少了什么东西一样，我们需要修改ScrollRect组件中的ScrollBar的Spacing的值。个人测试将值修改为-20可以达到理想的展示效果。

## 如何自己实现一个Scroll View？

为什么会有这个环节？

因为我们要尽可能地了解底层，然后避免对于Unity通用组件的依赖。

## 如何实现无限滑动列表？

一个优秀的无限滑动列表不是一天两天就能写出来的，只有对Unity UI组件的底层实现有全面的了解之后才能写出一个高性能的无限滑动列表，本篇博客主要是Unity UI组件的学习，在这里详细讨论无限滑动列表的实现不太合适，当我对Unity的UI组件有一个较为全面的了解之后我会尝试自己去写一个无限滑动列表

首先滑动列表的原理就是用固定的一组Prefab来表示更大的一组东西，我们称我们要展示的这些东西叫做`Item`，我们用一个`ItemList`的List来存放它们，

我们来看一下具体的场景：

![](Unity组件学习记录/image-20230425093946102.png)

当头部的这个Item完全出了我的可视区域，或者说headIndex后面的第二个Item移动到可视区域的顶部边界的时候 ，上面的Item已经看不到了，我们就可以将它转移到我们Item List的末尾去

放图2

当头部的这个Item恰好在可视区域的顶部边界的时候，头部Item上面没有Item了，就需要将ItemList尾部的Item插入到头部，

放图3

 所以说，当头部的Item处在可视区域顶部边界之间的时候，是不会发生Item位置的变化的，只有到达了顶部或者底部的边界的时候，才会发生变化。

![](Unity组件学习记录/image-20230424165422902.png)

接下来我们先对于整个过程有一个感性的认知：

![](Unity组件学习记录/image-20230424174050893.png)

在我们向上滑动，底部出现新的Item的时候，会出现这种情况：

最顶部的一个Item会逐渐移出我们的可视区域的顶部，当顶部的Item被完全移出可视区域之后，它就没有必要存在了，我们应该将它移动到我们List的末尾，让它在下面进行显示。

在我们向下滑动，在可视区域的顶部要出现新的Item的时候，由于现在最顶部展示的已经是List头部的Item了，再往前是没有Item供我们展示的，但是这个时候List尾部的Item已经在我们可视区域的下方了，我们可以把它移动到我们ItemList的顶部，让它作为新的Item显示出来，因此我们的实现思路其实很简单，就是我们要判断出上面我们所说的两种情况，然后分别在这两种情况内去做处理就可以了，可视问题是我们该如何去判断Item的位置和可视区域的位置呢？

这里我们需要了解一个属性的含义了：`anchoredPosition`，这个属性表示当前UI元素相对于其锚点的位置（二维向量）。它基于父级UI元素的矩形边界以及自身的锚点设定来计算相对坐标。注意锚点是不会随着

# 代码在这

```C#
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json.Converters;
using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.UI;

public class MyScrollView : MonoBehaviour
{

    public RectTransform content;

    public ScrollRect scrollRect;

    public GridLayoutGroup gridLayoutGroup;

    public GameObject prefab;
    public int bufferCount;

    public int totalCount;

    [SerializeField]private int headIndex;
    [SerializeField]private int tailIndex;

    public List<GameObject> Items;

    public Vector2 firstItemAnchoredPos;
    // Start is called before the first frame update
    void Start()
    {
        
        SetContentLength();
        SetGridLayoutGroupDefault();
        // 给初始化一下
        SetFirstItemAnchoredPosition();
        
        
        // 实例化Item
        // 第一次初始化的时候我们时不需要关心它的位置的，因为它会自动帮助我们调整好
        Items = new List<GameObject>(totalCount);
        headIndex = 0;
        tailIndex = bufferCount - 1;
        for (int i = 0; i < bufferCount; i++)
        {
            GameObject tempGameobject = Instantiate(prefab, content);
            Items.Add(tempGameobject);
            SetShow(tempGameobject, i+1);
        }
        
        scrollRect.onValueChanged.AddListener(OnScroll);
        
    }

    public void SetFirstItemAnchoredPosition()
    {
        firstItemAnchoredPos = new Vector2(
            gridLayoutGroup.padding.left + gridLayoutGroup.cellSize.x / 2,
            - gridLayoutGroup.padding.top - gridLayoutGroup.cellSize.y / 2);
    }

    public void SetGridLayoutGroupDefault()
    {  
        // 给GridLayoutGroup进行初始化
        gridLayoutGroup.startCorner = GridLayoutGroup.Corner.UpperLeft;
        gridLayoutGroup.startAxis = GridLayoutGroup.Axis.Vertical;
        gridLayoutGroup.childAlignment = TextAnchor.UpperLeft;
        gridLayoutGroup.constraint = GridLayoutGroup.Constraint.Flexible;
        gridLayoutGroup.constraintCount = 1;
    }


    public void OnScroll(Vector2 v)
    {
        // 向上滑动
        while (content.anchoredPosition.y >= gridLayoutGroup.padding.top + (headIndex + 1) * (gridLayoutGroup.spacing.y + gridLayoutGroup.cellSize.y) && tailIndex != totalCount - 1)
        {
            GameObject tempGameObj = Items[0];
            Items.Remove(tempGameObj);
            
           
            headIndex++;
            tailIndex++;
            SetPos(tempGameObj,tailIndex);
            SetShow(tempGameObj,tailIndex+1);
            Items.Add(tempGameObj);
            
            
        }
        // 向下滑动
        while (content.anchoredPosition.y <= gridLayoutGroup.padding.top + headIndex * (gridLayoutGroup.spacing.y + gridLayoutGroup.cellSize.y) && headIndex != 0)
        {
            GameObject temGamObj = Items[bufferCount - 1];
            Items.Remove(temGamObj);
            headIndex--;
            tailIndex--;
            SetPos(temGamObj,headIndex);
            SetShow(temGamObj, headIndex + 1);
            Items.Insert(0,temGamObj);
        }
    }
    public void SetPos(GameObject obj ,int index)
    {
        obj.GetComponent<RectTransform>().anchoredPosition = new Vector2(
            firstItemAnchoredPos.x,
            firstItemAnchoredPos.y - index * (gridLayoutGroup.spacing.y + gridLayoutGroup.cellSize.y));
    }

    public void SetShow(GameObject obj, int index)
    {
        obj.name = index.ToString();
        obj.GetComponentInChildren<Text>().text = index.ToString();
    }

    public void SetContentLength()
    {
        content.sizeDelta = new Vector2(
            gridLayoutGroup.cellSize.x,gridLayoutGroup.padding.top + gridLayoutGroup.padding.bottom + gridLayoutGroup.cellSize.y * totalCount + gridLayoutGroup.spacing.y * (totalCount -1));
    }
}

```

其实是有很多实现滑动列表的方案，哪种方案是最完美的？

- 不依托其他的组件？
- 通用性？
- ...

问了宇哥，宇哥说要依情况而定，按照需求设计最合适的滑动列表，不愧是宇哥👍

唉，本来今天刚一来还因为自己成功实现了滑动列表而沾沾自喜，结果和侯大师对了一下之后发现自己写的滑动列表实在是一言难尽，所以原计划今天要看DOTWEEN插件的，但是看这个情况应该是要把DOTWEEN插件延后到下周五一的时候看了😢

如果我们想要实现一个所谓的完美的无限滑动列表，那么我们最好的选择就是不要依赖任何Unity提供的现成的组件，因为有了Unity组件的加持，我们可以不去关心某项功能是怎么实现的，现在有了对卓越的追求时候，很多我们依赖的组件是需要我们搞清楚我们需要这个组件里面的哪些属性的，我们只需要自己把这些属性写出来，省去那些不去要的属性，那么我们项目的性能就能得到一个很大的飞跃。

下一步的工作就是了解：

1.要实现一个滑动列表所依赖的最最基础的属性有哪些？

2.需要实现哪些方法才能实现滑动列表

3.可以去GitHub上看一看无限滑动列表的案例



## 关于RectTransform和Transform

Unity中所有在场景中的物体肯定都会有这两个组件的其中一个，因为场景中的物体都需要一些数据表明自己的位置，而物体的位置信息就由这两个组件表明：

1. `Transform Component`：三维空间中任何游戏物体都会拥有一个Transform组件。Transform组件负责记录物体的位置`position`、旋转`rotation`和缩放`scale`。这些属性是基于父物体的坐标系来描述的。Transform表示的坐标主要用于3D场景物体
2. `RectTranform Component`：RectTransform组件用于UI元素比如`Text`、`Image`、`Button`等，它**继承自**Transform组件，在二维环境下（在`Canvas`内）描述UI元素的位置和尺寸。RectTransform包含了对齐/锚点设置`Anchors`、尺寸`Width and Height`以及`anchoredPosition`等属性。创建2D UI的时候，RectTransform提供了更多适应屏幕变化和布局的便利。

总的来说，表示场景中物体坐标的两个核心组件分别是`Transform`（一般对象，主要针对3D）和`RectTransform`（UI对象，针对UI布局）。其他与坐标相关的功能多数时候会涉及操作这两个组件。

在Unity中，对于UI元素来讲，使用rectTransform而非Transform组件是至关重要的。使用Transform组件处理UI元素可能导致以下问题：

1. 布局缩放和适应性：RectTransform组件针对二维UI设计，其锚点、pivot和布局设定可以更好地适应不同屏幕尺寸或者方向。而使用Transform组件设计UI布局的时候无法保证良好的自适应性。
2. UI层级丢失：所有UI元素都需要位于Canvas内，并且由Canvas渲染。将UI元素转换为普通物体试图使用Transform组件时，可能会导致UI层级结构的丢失，从而造成渲染问题。
3. 位置、尺寸与交互问题：Rect Transform独有众多属性（比如锚点、宽度、高度等），很难在Transform组件上实现。使用Transform组件处理UI布局将无法确保正确的位置、尺寸与屏幕交互。

但是在Unity中，我们可以将2D的UI元素放置于3D空间中，并且产生立体的效果，我们可以通过World SpaceCanvas来完成，这个原理也比较简单，使用World Space Canvas的时候，UI元素会被视作一个在三维空间中存在的平面。这意味着它们可以与其他3D物体共享相同的坐标空间，并且有明确的深度关系。因此，尽管UI元素本身仍然是2D的，但是通过将它们放在World Space里的Canvas上，那些元素在三维空间中便拥有了虚拟的XYZ坐标。

使用World Space Canvas，UI元素会变成场景的一部分，而不再是位于屏幕覆盖模式ScreenSpace - Overlay或者相机模式Screen Space - Camera Mode下由专用的Canvas独立渲染的UI层。这使得它们具有了更多与3D场景交互、变换和运动的可能性，从而实现丰富的视觉效果或游戏玩法。但是UI元素的2D属性并没有改变。



## Unity中的坐标参考系

在Unity Editor的Hierarchy窗口中，我们经常看到一个节点下会挂载很多的子节点。比如说我们现在有一个父节点`Parent Cube`，有一个子节点`Child Sphere`，如果我们现在在Scene窗口中移动`Parent Cube`物体，Child Sphere也会跟着移动；而如果我们只是移动Child Sphere，Parent Cube物体并不会跟着移动。

我们可以看一下在Inspector窗口中，Child Sphere物体Transform组件的position，在父节点移动的时候，position属性没有发生任何改变。而在移动

上面移动的场景是比较好理解的。

![](Unity组件学习记录/image-20230507170000872.png)

在Unity的Scene窗口中，有一个`Toggle Tool Handle Position`和一个`Toggle Tool Handle Rotation`，这是Unity的Scene窗口中的两个选项，用于切换Transform工具的操控模式。它们主要影响移动、旋转和缩放游戏对象所使用的参考坐标系。

- Toggle Tool Handle Position（快捷键T）：这个按钮用于在局部Local和全局Global坐标系之间的切换。当我们选择这个选项时，并且处于移动或者缩放模式，我们可以看到在Scene视图中的Transform工具会在这两种坐标系之间切换。
  - 局部坐标系：Transform工具将基于游戏对象自身的坐标系进行操作。这意味着当我们在场景中移动、旋转或者缩放游戏对象的时候，Unity将根据游戏对象的本地方向执行操作。
  - 全局坐标系：Transform工具将基于世界坐标系进行操作。这意味着当我们在场景中移动、旋转或缩放游戏对象的时候，Unity将根据全局方向执行操作。
- Toggle Tool Handle Rotation（快捷键R）：这个按钮用于在角度旋转（Rotation）和方向旋转（Orientation）之间切换。当我们选择这个选项的时候并且处于旋转模式，我们可以看到在Scene视图中的Transform工具会在这两种旋转方式之间切换。
  - 角度旋转：Transform工具将基于游戏对象的角度进行操作。这意味着当我们在场景中旋转游戏对象的时候，Unity将根据游戏对象的角度进行操作。
  - 方向旋转：Transform工具将基于游戏对象的方向进行操作。这意味着当我们在场景中旋转游戏对象的时候，Unity将根据独享的方向执行操作

这些选项对于灵活地调整与游戏相关地位置、旋转和缩放非常有用。通过不同的选项，可以方便地以游戏对象地局部坐标系或者世界坐标系为基准来调整它们在场景中地变换。需要注意地是，这些选项仅会影响Scene窗口中的操作方式，并不会改变游戏对象在运行时的表现。

# 

**2023.7.26更新**

Slider的学习，最近在项目开发中我们需要了进度条实现方法不统一的问题，于是我们想要统一所有进度条的实现，我们选择基于Unity的Slider组件来实现，借此机会好好学习一下Slider组件



