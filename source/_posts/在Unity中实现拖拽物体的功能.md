---
title: 在Unity中实现拖拽物体的功能
abbrlink: aa328bbc
date: 2025-08-10 16:33:19
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

```C#
using UnityEngine;
using UnityEngine.EventSystems;

namespace MB
{
    public class DraggableObj : MonoBehaviour, 
        IDragHandler, IBeginDragHandler, IEndDragHandler
    
    {
        enum DragState
        {
            None,
            Idle,
            Dragging,
        }    
        
        private DragState dragState = DragState.None;
        
        public void OnDrag(PointerEventData eventData)
        {
            Debug.Log("拖拽中...");
            RectTransformUtility.ScreenPointToLocalPointInRectangle(transform.parent.transform as RectTransform, eventData.position, Camera.main, out Vector2 point);
            transform.localPosition = point;
        }

        public void OnBeginDrag(PointerEventData eventData)
        {
            Debug.Log("开始拖拽");
            transform.localScale = Vector3.one * 1.5f;
        }

        public void OnEndDrag(PointerEventData eventData)
        {
            Debug.Log("拖拽结束...");
            transform.localScale = Vector3.one;
        }
    }
}
```

可以将上面的逻辑进一步抽象一下, 把每个接口内的行为抽离出来, 作为一个action, 在合适的时机传进去.