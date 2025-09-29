---
title: UnityUGUI解构(一)——RectTransform
abbrlink: 67d7d086
date: 2025-07-15 21:56:01
tags:
categories:
cover:
description:
swiper_index:
sticky:
---
RectTransform继承自Transform, 并没有包含在UGUI体系里面, 但是RectTransform的字段绝大多数情况下只在UGUI体系中才有意义.  因此我们可以将RectTransform看作是Unity UGUI的一部分, 并且将其作为UGUI系列中首个分析的对象.

# 锚点(Anchor)与中心点(Pivot)

## 锚点Anchor
这个Anchor并不是直接参与位置信息运算的数据, Unity会先根据这个信息计算出一个anchorReferencePosition, 再用这个值进行其他的运算.
在Unity中 锚点是可以至多被分成四个角的, 并不是一个点. 而在计算一个RectTransform中的一些属性时需要把锚点作为一个"点" 参与运算, 作为四个角不重合的情况, 需要有一个Anchor Reference Position来作为锚点, 
Anchor Reference Position的计算公式如下:
$$ AnchorReferencePosition_x = (1 - pivot_x) \times x_0 + pivot_x \times x_1 $$
$$ AnchorReferencePosition_y = (1 - pivot_y) \times y_0 + pivot_y \times y_1 $$
其中 (x0, y0)代表的是AnchorMin在该RectTransform的父节点中的位置, (x1, y1)代表的是AnchorMax在该RectTransform的父节点中的位置, (pivotx, pivoty)指的是中心点的归一化位置坐标

有一个概念叫AnchoredPosition, 这个值的含义是Pivot点相对于`Anchor Reference Position`的距离, 当锚点的四个角没有重合在一点时, 我们可能会以为Anchor Reference Position

AnchoredPosition和Local Position的区别是什么?
在Transform(非RectTransform)体系中, 子物体的localPosition只能通过调整其和父物体的相对位置来实现变化, 在RectTransform体系下, 子物体的localPosition除了受相对位置的影响, 还与父物体的width、子物体本身的pivot有关

# RectTransform中的字段

> 💡本文我们只讨论RectTransform中有而Transform中没有的字段

- rect Rect 注意RectTransform中的rect成员不支持直接修改注意以上字段都是不可以通过RectTransform操作其值的., 因为没有set属性, 外部没有对它的访问权限
	- xMin, 代表的是rect围成矩形区域的左下角的横坐标值, 坐标系原点是这个rect围成的矩形区域的中心点, 
	- yMin, 代表的是rect围成矩形区域的左下角的纵坐标值, 坐标系原点是这个rect围成的矩形区域的中心点, 
	- mHeight, 这个rect围成矩形的高
	- mWidth, 这个rect围成矩形的宽
- anchorMin Vector2 以当前RectTransform的父节点的rect的左下角为坐标原点, 锚点左下角的归一化位置坐标
- anchorMax Vector2 以当前RectTransform的父节点的rect的左下角为坐标原点, 锚点右上角的归一化位置坐标
- sizeDelta Vector2 当锚点四角重合时, sizeDelta.x=rect.width=(offsetMax-offsetMin).x, sizeDelta.y=rect.height=(offsetMax-offsetMin).y 当不重合时 sizeDelta.x=(offsetMax-offsetMin).x, sizeDelta.y=(offsetMax-offsetMin).y
- offsetMin Vector2 指锚点左下角与rect的左下角的偏移值 以当前RectTransform的父节点的rect的左下角为坐标原点
- offsetMax Vector2  指锚点右上角与rect的右上角的偏移值 以当前RectTransform的父节点的rect的左下角为坐标原点
- anchoredPosition Vector2
- anchoredPosition3D Vector3 包含anchoredPosition的xy分量, 增加了z分量 一般用不到
localPosition的含义是当前RectTransform的pivot相对于该RectTransform的父节点rect的中心点的位置, 由于可以调整父节点的rect, 所以可以间接影响子物体的localPosition属性, 而我也可以让rect的绝对位置不变 通过改变pivot的位置来影响localPosition的值, 这在Transform(非RectTransform)体系下是不太可能做到的, 

锚点(Anchor)描述的是当前UI节点和其父节点的位置关系, 锚点虽然叫点, 但是会存在锚点的四个角没有重合的情况. 只不过锚点的四角重合时anchor reference position和该点重合了而已 unity会计算出一个anchor reference point, 和pivot做运算得到anchored position
中心点(Pivot)描述的是当前UI节点的轴心位置 与父节点无关



# IDragHandler

在此猜测 , IDragHandler的生效逻辑是什么


每一帧 Unity都会从摄像机出发生成一个射线 由你的pointer投射到场景中的gameObject上, 每一帧你都会拿到这些信息 拿到这些物体, 查看这些物体上挂载的组件有没有实现IDragHandler接口, 如果有, 出发这个IDragHnader的fafa


在Unity源码中 搜 localIdentifierInFile


BuildSerialization.cpp
```c++
static void ConvertSceneObjectsToInstanceIDBuildRemap(const core::string& path, const WriteDataArray& sceneObjects, InstanceIDBuildRemap& output)
{
    int pathIndex = GetPersistentManager().GetSerializedFileIndexFromPath(path);

    output.reserve(output.size() + sceneObjects.size());
    for (WriteDataArray::const_iterator i = sceneObjects.begin(); i != sceneObjects.end(); i++)
    {
        Assert(i->localIdentifierInFile != 0);
        output.push_unsorted(i->instanceID, SerializedObjectIdentifier(pathIndex, i->localIdentifierInFile));
    }
    output.sort();
}
```

ResourceManager.cpp中的void BuiltinResourceManager::InitializeResources()方法






下面这段代码, 推测应该是Unity给资产生成FileId的逻辑
明天验证一下 对于大部分的FBX中的mesh资源, 他们应该都是叫同样的名字, 又因为他们都是mesh, 所以传入的参数一样, 所以在meta文件中, 你可以看到, 即便是引用了不同的fbx的mesh, 变的只有guid, fileid都是一样了 
明天验证一下, 两个mesh名不一样的fbx, 应该fileId就会不一样, 而且改了mesh的名字, fileId的名字也就会跟着变


# 说说RectTransform中的更新逻辑

```c++
void RectTransform::InitializeClass()
{
    RegisterAllowNameConversion(TypeOf<RectTransform>()->GetName(), "m_Position", "m_AnchoredPosition");

    REGISTER_MESSAGE(kTransformChanged, OnTransformChanged, int);

    InitializeRectTransformAnimationBindingInterface();
}

void RectTransform::OnTransformChanged(int mask)
{
    // If reparenting then simply dirty the rectangle.
    if (mask & Transform::kParentingChanged)
    {
        UpdatePosAndRectRecursive();
        return;
    }

    // Only update if transform position changed.
    if (!(mask & Transform::kPositionChanged))
        return;

    // Don't process rect-updates otherwise we'll get infinite recursion.
    // Don't process parent-changes as these will come from the prefab-merging code when it is dealing with sibling positioning which causes positioning issues.
    if (mask & (Transform::kDontUpdateRect | Transform::kSiblingOrderChanged))
        return;

    Vector3f targetLocalPosition = GetLocalPosition();
    Vector2f rectLocalPosition = CalculateLocalPosition2();
    Vector2f anchoredPosition = Vector2f(m_AnchoredPosition.x + targetLocalPosition.x - rectLocalPosition.x, m_AnchoredPosition.y + targetLocalPosition.y - rectLocalPosition.y);

    if (mask & Transform::kReceivedDueToCameraTRSChanged)
        SetAnchoredPositionWithoutNotification(anchoredPosition);
    else
        SetAnchoredPosition(anchoredPosition);
}

void RectTransform::UpdatePosAndRectRecursive(bool sendTransformChange, const Rectf* forceRect)
{
    UInt32 changeMask = 0;

    if (forceRect)
    {
        if (m_CachedRect != *forceRect)
        {
            changeMask |= kLocalRectChanged;
            m_CachedRect = *forceRect;
        }
    }
    else
    {
        changeMask = UpdatePosAndRect(sendTransformChange);
    }

    if (changeMask & kLocalPositionChange)
    {
        if (sendTransformChange)
            SendTransformChanged(Transform::kPositionChanged | Transform::kDontUpdateRect);
    }

    // Recursively update the cached rect if the local rect has changed
    if (changeMask & kLocalRectChanged)
    {
        int childCount = GetChildrenCount();
        for (int i = 0; i < childCount; i++)
        {
            Transform& child = GetChild(i);
            RectTransform* childRect = dynamic_pptr_cast<RectTransform*>(&child);
            if (childRect != NULL)
            {
                childRect->UpdatePosAndRectRecursive(sendTransformChange);
            }
        }


        if (sendTransformChange)
        {
            // LocalPosition or local Rect has changed, all geometry must be recomputed.
            // We send the message kOnRectTransformDimensionsChange  to children first, and then to their parents and so on.
            SendMessage(kOnRectTransformDimensionsChange);
        }
    }
}
```