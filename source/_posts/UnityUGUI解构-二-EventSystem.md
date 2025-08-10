---
title: UnityUGUI解构(二)-EventSystem
abbrlink: 28d70de0
date: 2025-08-10 08:42:26
tags:
categories:
cover:
description:
swiper_index:
sticky:
---


# GraphicRaycaster
为什么在创建Canvas节点的时候, 都要GrapihcRaycaster会自动创建
GrapihcRaycaster组件不是必须的, 删除掉之后这个Canvas就不能响应点击了.

要看明白下面这段逻辑是在干什么
```C# 
public virtual bool Raycast(Vector2 sp, Camera eventCamera)
{
    if (!isActiveAndEnabled)
        return false;

    var t = transform;
    var components = ListPool<Component>.Get();

    bool ignoreParentGroups = false;
    bool continueTraversal = true;

    while (t != null)
    {
        t.GetComponents(components);
        for (var i = 0; i < components.Count; i++)
        {
            var canvas = components[i] as Canvas;
            if (canvas != null && canvas.overrideSorting)
                continueTraversal = false;

            var filter = components[i] as ICanvasRaycastFilter;

            if (filter == null)
                continue;

            var raycastValid = true;

            var group = components[i] as CanvasGroup;
            if (group != null)
            {
                if (!group.enabled)
                    continue;

                if (ignoreParentGroups == false && group.ignoreParentGroups)
                {
                    ignoreParentGroups = true;
                    raycastValid = filter.IsRaycastLocationValid(sp, eventCamera);
                }
                else if (!ignoreParentGroups)
                    raycastValid = filter.IsRaycastLocationValid(sp, eventCamera);
            }
            else
            {
                raycastValid = filter.IsRaycastLocationValid(sp, eventCamera);
            }

            if (!raycastValid)
            {
                ListPool<Component>.Release(components);
                return false;
            }
        }
        t = continueTraversal ? t.parent : null;
    }
    ListPool<Component>.Release(components);
    return true;
}
```


# EventSystem的触发机理
依赖Raycast给的一些数据, 用这些数据找到Raycast命中的gameobject, 找到这些gameObject上的Handler组件, 触发这些Handler.


不管市面上的UnityUI的方案(比如FGUI)多炫酷, 最终到Unity中都是要走UGUI的底层

# 