---
title: UnitySourceCode——bindings
abbrlink: 958d72fd
date: 2025-08-27 10:30:47
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

Unity中有一种`.bindings `格式的文件, 这类文件经过某种处理会转换为C#文件

```.bindings
C++RAW

#include "UnityPrefix.h"
#include "Configuration/UnityConfigure.h"
#include "Runtime/Mono/MonoBehaviour.h"
#include "Runtime/UI/Canvas.h"
#include "Runtime/UI/UIStructs.h"
#include "Runtime/UI/BatchGenerator.h"

#if ENABLE_PROFILER
#include "Modules/Profiler/Public/ProfilerImpl.h"
#endif

CSRAW
using System;
using UnityEngine;
using Object = UnityEngine.Object;

namespace UnityEngine
{
    ENUM RenderMode
        ScreenSpaceOverlay = 0,
        ScreenSpaceCamera = 1,
        WorldSpace = 2
    END

    [Flags]
    ENUM AdditionalCanvasShaderChannels
        None = 0,
        TexCoord1 = 1 << 0,
        TexCoord2 = 1 << 1,
        TexCoord3 = 1 << 2,
        Normal = 1 << 3,
        Tangent = 1 << 4,
    END

    // CanvasRenderer is the C++ rendering backend for the UI system.
    [RequireComponent(typeof(RectTransform))]
    [NativeClass("UI::Canvas")]
    CLASS Canvas : Behaviour

        AUTO_PROP RenderMode renderMode GetRenderMode SetRenderMode
        AUTO_PROP bool isRootCanvas GetIsRootCanvas
        AUTO_PTR_PROP Camera worldCamera GetCamera SetCamera
        AUTO_PROP Rect pixelRect GetPixelRect
        AUTO_PROP float scaleFactor GetScaleFactor SetScaleFactor
        AUTO_PROP float referencePixelsPerUnit GetReferencePixelsPerUnit SetReferencePixelsPerUnit
        AUTO_PROP bool overridePixelPerfect GetOverridePixelPerfect SetOverridePixelPerfect
        AUTO_PROP bool pixelPerfect GetPixelPerfect SetPixelPerfect
        AUTO_PROP float planeDistance GetPlaneDistance SetPlaneDistance

        AUTO_PROP int renderOrder GetRenderOrder

        AUTO_PROP bool overrideSorting GetOverrideSorting SetOverrideSorting
        AUTO_PROP int sortingOrder GetSortingOrder SetSortingOrder
        AUTO_PROP int targetDisplay GetTargetDisplay SetTargetDisplay

        OBSOLETE warning Setting normalizedSize via a int is not supported. Please use normalizedSortingGridSize
        AUTO_PROP int sortingGridNormalizedSize GetSortingBucketNormalizedSize SetSortingBucketNormalizedSize

        AUTO_PROP float normalizedSortingGridSize GetSortingBucketNormalizedSize SetSortingBucketNormalizedSize

        AUTO_PROP int sortingLayerID GetSortingLayerID SetSortingLayerID
        AUTO_PROP int cachedSortingLayerValue GetCachedSortingLayerValue


        AUTO_PROP AdditionalCanvasShaderChannels additionalShaderChannels GetAdditionalShaderChannels SetAdditionalShaderChannels

        CUSTOM_PROP string sortingLayerName
        {
            return scripting_string_new(self->GetSortingLayerName());
        }
        {
            self->SetSortingLayerName(value);
        }

        CUSTOM_PROP Canvas rootCanvas
        {
            UI::Canvas* root = self->GetRootCanvas();
            return Scripting::ScriptingWrapperFor(root == NULL ? self : root);
        }

        CUSTOM static Material GetDefaultCanvasMaterial()
        {
            return Scripting::ScriptingWrapperFor(UI::GetDefaultUIMaterial());
        }

        CUSTOM static Material GetETC1SupportedCanvasMaterial()
        {
            return Scripting::ScriptingWrapperFor(UI::GetETC1SupportedCanvasMaterial());
        }

        OBSOLETE warning Shared default material now used for text and general UI elements, call Canvas.GetDefaultCanvasMaterial()
        CUSTOM static Material GetDefaultCanvasTextMaterial()
        {
            return Scripting::ScriptingWrapperFor(UI::GetDefaultUIMaterial());
        }

        CSRAW
        public delegate void WillRenderCanvases();
        public static event WillRenderCanvases willRenderCanvases;
        [RequiredByNativeCode]
        private static void SendWillRenderCanvases() { if (willRenderCanvases != null) willRenderCanvases(); }
        public static void ForceUpdateCanvases() { SendWillRenderCanvases(); }
    END

    CLASS UISystemProfilerApi
        C++RAW
        enum SampleType { Layout, Render };
        ENUM SampleType
            Layout,
            Render,
        END
        CUSTOM static public void BeginSample(SampleType type)
        {
#if ENABLE_PROFILER
            if (UnityProfilerPerThread::ms_InstanceTLS != NULL)
            {
                ProfilerInformation& profilerInfo = *profiler_get_info_for_name(type == Layout ? "Layout" : "Render", type == Layout ? kProfilerUISystemLayout : kProfilerUISystemRender);
                PROFILER_BEGIN(profilerInfo, NULL);
            }
#endif // ENABLE_PROFILER
        }

        CUSTOM static public void EndSample(SampleType type)
        {
#if ENABLE_PROFILER
            if (UnityProfilerPerThread::ms_InstanceTLS != NULL)
            {
                ProfilerInformation& profilerInfo = *profiler_get_info_for_name(type == Layout ? "Layout" : "Render", type == Layout ? kProfilerUISystemLayout : kProfilerUISystemRender);
                PROFILER_END(profilerInfo);
            }
#endif // ENABLE_PROFILER
        }

        CUSTOM static public void AddMarker(string name, Object obj)
        {
#if ENABLE_PROFILER && UNITY_EDITOR
            InstanceID instanceID = InstanceID_None;
            std::string nameStr(name);
            if (!obj.IsNull() && Thread::CurrentThreadIsMainThread())
            {
                instanceID =  obj->GetInstanceID();
                nameStr += " ";
                nameStr += obj->GetName();
            }
            UI::GetCanvasManager().AddMarker(nameStr.c_str(), instanceID);
#endif // ENABLE_PROFILER
        }

    END

    CSRAW
}

```