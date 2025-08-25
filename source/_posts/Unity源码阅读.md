---
title: Unity源码阅读
abbrlink: 4d404b8c
date: 2025-08-10 10:11:30
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


```c++
void PlayerLoop()
{
    ReentrancyChecker checker(&s_InsidePlayerLoop);
    if (!checker.IsOK())
    {
        ErrorString("An abnormal situation has occurred: the PlayerLoop internal function has been called recursively. "
            "Please contact Customer Support with a sample project so that we can reproduce the problem and troubleshoot it.");
        return;
    }

#if FRAME_DEBUGGER_REMOTE_PLAYER
    if (FrameDebugger::IsLocalEnabled())
    {
        FrameDebugger::PlayerLoop();
        return;
    }
#endif

    ::SetWorldPlayingThisFrame(IsWorldPlaying());   // The IsWorldPlaying state can potentially be
                                                    // modified inside the loop, so to ensure consistency
                                                    // we need to store it for the duration of the frame.


    //          ---                                               ----
    // NOTE: Do NOT add any logic or branches below, any new code MUST use callbacks
    //          ---                                               ----


    // The time should be updated as early as possible to allow the rest of the
    // loop logic to be on the same time for this loop. It is also a must before
    // calling SynchronizeState (used in cluster mode).
    CALL_UPDATE_MODULAR(Initialization, PlayerUpdateTime);
    CALL_UPDATE_MODULAR(Initialization, AsyncUploadTimeSlicedUpdate);
    CALL_UPDATE_MODULAR(Initialization, SynchronizeInputs);
    CALL_UPDATE_MODULAR(Initialization, SynchronizeState);
    CALL_UPDATE_MODULAR(Initialization, XREarlyUpdate);             // Potentially invalidates our GfxDevice!

    CALL_UPDATE_MODULAR(EarlyUpdate, PollPlayerConnection);
    CALL_UPDATE_MODULAR(EarlyUpdate, ProfilerStartFrame);
    CALL_UPDATE_MODULAR(EarlyUpdate, GpuTimestamp);
    CALL_UPDATE_MODULAR(EarlyUpdate, UnityConnectClientUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, CloudWebServicesUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, NScreenUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, UnityWebRequestUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, ExecuteMainThreadJobs);        // Update background tasks
    CALL_UPDATE_MODULAR(EarlyUpdate, ProcessMouseInWindow);
    CALL_UPDATE_MODULAR(EarlyUpdate, ClearIntermediateRenderers);   // In the editor, clear intermediate renderers before loop.
                                                                    // So that in paused state or when resizing windows,
                                                                    // we can still draw the previous ones.
    CALL_UPDATE_MODULAR(EarlyUpdate, ClearLines);
    CALL_UPDATE_MODULAR(EarlyUpdate, PresentBeforeUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, ResetFrameStatsAfterPresent);  // Reset frame stats after present (case 496221)
    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateAllUnityWebStreams);

    ////@TODO: CLeanup code where input is processed after scene loading
    /// All input should be processed prior to scene loading

    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateTextureStreamingManager);
    CALL_UPDATE_MODULAR(EarlyUpdate, UpdatePreloading);
    CALL_UPDATE_MODULAR(EarlyUpdate, RendererNotifyInvisible);
    CALL_UPDATE_MODULAR(EarlyUpdate, PlayerCleanupCachedData);

    // Make sure the screen manager uses the correct Game View size, which is only known on the managed side.
    // We query it in the game loop rather than setting it from the Game View. This prevents multiple Game Views overwriting the value,
    // or none setting it because no Game View has its tab in front.
    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateMainGameViewRect);

    // UpdateCanvasRectTransform modifies the RectTransform of the Canvas.
    // It is done before all Update functions so that UI elements can react to the change in the same frame.
    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateCanvasRectTransform);

    ////@TODO: CLeanup code where input is processed after scene loading
    /// All input should be processed prior to scene loading

    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateInputManager);
    CALL_UPDATE_MODULAR(EarlyUpdate, ProcessRemoteInput);
    CALL_UPDATE_MODULAR(EarlyUpdate, XRUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, TangoUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, ScriptRunDelayedStartupFrame);
    CALL_UPDATE_MODULAR(EarlyUpdate, UpdateKinect);
    CALL_UPDATE_MODULAR(EarlyUpdate, DeliverIosPlatformEvents);
    CALL_UPDATE_MODULAR(EarlyUpdate, DispatchEventQueueEvents);
    CALL_UPDATE_MODULAR(EarlyUpdate, DirectorSampleTime);
    CALL_UPDATE_MODULAR(EarlyUpdate, PhysicsResetInterpolatedTransformPosition); // Initializes fixed time step loop
    CALL_UPDATE_MODULAR(EarlyUpdate, NewInputBeginFrame);
    CALL_UPDATE_MODULAR(EarlyUpdate, SpriteAtlasManagerUpdate);
    CALL_UPDATE_MODULAR(EarlyUpdate, PerformanceAnalyticsUpdate);

    /* This is what fixed time stepping is doing
    float time = GetProfilerTime ();
    while (fixedTime < time)
    {
        fixedTime += fixedDeltaTime;
        UpdateFixedBehaviours ();
        UdateDynamicsManager ();
    }
    Which means:
      - fixed timestep is always larger than dynamic timestep
      - fixed delta time is always the same
    */

    // Fixed framerate loop (fixed behaviours, dynamics, delayed calling)
    while (GetTimeManager().StepFixedTime())
    {
        CALL_UPDATE_MODULAR(FixedUpdate, ClearLines);                       // Placed here so we ensure it is also called
                                                                            // in edit-mode (fix for case 379024: pressing
                                                                            // stop did not properly clear fixedStepLines)
        CALL_UPDATE_MODULAR(FixedUpdate, NewInputEndFixedUpdate);
        CALL_UPDATE_MODULAR(FixedUpdate, DirectorFixedSampleTime);
        CALL_UPDATE_MODULAR(FixedUpdate, AudioFixedUpdate);
        CALL_UPDATE_MODULAR(FixedUpdate, ScriptRunBehaviourFixedUpdate);    // Script.FixedUpdate
        CALL_UPDATE_MODULAR(FixedUpdate, DirectorFixedUpdate);
        CALL_UPDATE_MODULAR(FixedUpdate, LegacyFixedAnimationUpdate);       // Animation (Root motion)
        CALL_UPDATE_MODULAR(FixedUpdate, XRFixedUpdate);
        CALL_UPDATE_MODULAR(FixedUpdate, PhysicsFixedUpdate);               // 3D Physics
        CALL_UPDATE_MODULAR(FixedUpdate, Physics2DFixedUpdate);             // 2D Physics
        CALL_UPDATE_MODULAR(FixedUpdate, DirectorFixedUpdatePostPhysics);   // Animation IK and write bones
        CALL_UPDATE_MODULAR(FixedUpdate, ScriptRunDelayedFixedFrameRate);   // Script Coroutines
        CALL_UPDATE_MODULAR(FixedUpdate, ScriptRunDelayedTasks);            // Tasks on Synchronization Context
        CALL_UPDATE_MODULAR(FixedUpdate, NewInputBeginFixedUpdate);
    }

    // Dynamics, animation, behaviours

    CALL_UPDATE_MODULAR(PreUpdate, PhysicsUpdate);
    CALL_UPDATE_MODULAR(PreUpdate, Physics2DUpdate);
    CALL_UPDATE_MODULAR(PreUpdate, CheckTexFieldInput);
    CALL_UPDATE_MODULAR(PreUpdate, IMGUISendQueuedEvents);
    CALL_UPDATE_MODULAR(PreUpdate, NewInputUpdate);
    CALL_UPDATE_MODULAR(PreUpdate, SendMouseEvents);
    CALL_UPDATE_MODULAR(PreUpdate, AIUpdate);
    CALL_UPDATE_MODULAR(PreUpdate, WindUpdate);
    CALL_UPDATE_MODULAR(PreUpdate, UpdateVideo);

    CALL_UPDATE_MODULAR(Update, ScriptRunBehaviourUpdate);
    CALL_UPDATE_MODULAR(Update, DirectorUpdate);
    CALL_UPDATE_MODULAR(Update, ScriptRunDelayedDynamicFrameRate);

    CALL_UPDATE_MODULAR(PreLateUpdate, AIUpdatePostScript);
    CALL_UPDATE_MODULAR(PreLateUpdate, DirectorUpdateAnimationBegin);       // Dynamic Step Animation Update
    CALL_UPDATE_MODULAR(PreLateUpdate, LegacyAnimationUpdate);
    CALL_UPDATE_MODULAR(PreLateUpdate, DirectorUpdateAnimationEnd);
    CALL_UPDATE_MODULAR(PreLateUpdate, DirectorDeferredEvaluate);
    CALL_UPDATE_MODULAR(PreLateUpdate, UpdateNetworkManager);
    CALL_UPDATE_MODULAR(PreLateUpdate, UpdateMasterServerInterface);
    CALL_UPDATE_MODULAR(PreLateUpdate, UNetUpdate);
    CALL_UPDATE_MODULAR(PreLateUpdate, EndGraphicsJobsLate);                // Latest possible time to end graphics jobs of the previous frame. Must run before any graphics callbacks.
    CALL_UPDATE_MODULAR(PreLateUpdate, ParticleSystemBeginUpdateAll);
    CALL_UPDATE_MODULAR(PreLateUpdate, ScriptRunBehaviourLateUpdate);

    CALL_UPDATE_MODULAR(PostLateUpdate, PlayerSendFrameStarted);            // For Editor, we call begin frame just before starting
                                                                            // rendering RepaintController. For Runtime we start here.
    CALL_UPDATE_MODULAR(PostLateUpdate, DirectorLateUpdate);
    CALL_UPDATE_MODULAR(PostLateUpdate, ScriptRunDelayedDynamicFrameRate);
    CALL_UPDATE_MODULAR(PostLateUpdate, PhysicsSkinnedClothBeginUpdate);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateCanvasRectTransform);         // UI update here after late update
    CALL_UPDATE_MODULAR(PostLateUpdate, PlayerUpdateCanvases);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateAudio);
    CALL_UPDATE_MODULAR(PostLateUpdate, ParticlesLegacyUpdateAllParticleSystems);
    CALL_UPDATE_MODULAR(PostLateUpdate, ParticleSystemEndUpdateAll);        // We need to sync particle systems here to
                                                                            // make sure they update their renderers properly
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateSubstance);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateCustomRenderTextures);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateAllRenderers);
    CALL_UPDATE_MODULAR(PostLateUpdate, EnlightenRuntimeUpdate);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateAllSkinnedMeshes);
    CALL_UPDATE_MODULAR(PostLateUpdate, ProcessWebSendMessages);
    CALL_UPDATE_MODULAR(PostLateUpdate, SortingGroupsUpdate);

    // removed because nothing seems to actually register at this manager
    //GetUpdateManager ().Update ();

    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateVideoTextures);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateVideo);
    CALL_UPDATE_MODULAR(PostLateUpdate, DirectorRenderImage);
    CALL_UPDATE_MODULAR(PostLateUpdate, ExecuteOneJobInMainThread);         // Execute one job from the main thread
    CALL_UPDATE_MODULAR(PostLateUpdate, PlayerEmitCanvasGeometry);
    CALL_UPDATE_MODULAR(PostLateUpdate, PhysicsSkinnedClothFinishUpdate);
    CALL_UPDATE_MODULAR(PostLateUpdate, FinishFrameRendering);
    CALL_UPDATE_MODULAR(PostLateUpdate, BatchModeUpdate);
    CALL_UPDATE_MODULAR(PostLateUpdate, PlayerSendFrameComplete);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateCaptureScreenshot);
    CALL_UPDATE_MODULAR(PostLateUpdate, PresentAfterDraw);
    CALL_UPDATE_MODULAR(PostLateUpdate, ClearImmediateRenderers);
    CALL_UPDATE_MODULAR(PostLateUpdate, PlayerSendFramePostPresent);
    CALL_UPDATE_MODULAR(PostLateUpdate, UpdateResolution);
    CALL_UPDATE_MODULAR(PostLateUpdate, InputEndFrame);                     // Clear the input string and the key-down events at the end
                                                                            // of the Loop. This makes sure all input string is cleared.

    // We have two UI systems. IM GUI & the new component based UI system.
    // IM GUI doesn't consume the move events. They are consumed by the player loop.
    // But there might be no scripts popping the vents. So clear them at the end of the frame.
    CALL_UPDATE_MODULAR(PostLateUpdate, GUIClearEvents);

    CALL_UPDATE_MODULAR(PostLateUpdate, ShaderHandleErrors);                // Handle any shaders with errors discovered
    CALL_UPDATE_MODULAR(PostLateUpdate, ResetInputAxis);                    // We entered Text Field input this frame, Game mode input
                                                                            // is disabled. Reset axes, so they don't stick.
    CALL_UPDATE_MODULAR(PostLateUpdate, ThreadedLoadingDebug);
    CALL_UPDATE_MODULAR(PostLateUpdate, ProfilerSynchronizeStats);
    CALL_UPDATE_MODULAR(PostLateUpdate, MemoryFrameMaintenance);
    CALL_UPDATE_MODULAR(PostLateUpdate, ExecuteGameCenterCallbacks);
    CALL_UPDATE_MODULAR(PostLateUpdate, ProfilerEndFrame);
}

```

```c++
#define REGISTER_PLAYERLOOP_CALL(TYPE_, NAME_, BODY_, ...)                       \
    struct TYPE_##NAME_##Registrator                                             \
    {                                                                            \
        static void Forward (__VA_ARGS__)                                        \
        {                                                                        \
            PROFILE_CALLBACK_AUTO(TYPE_##NAME_##Registrator, #TYPE_ "." #NAME_); \
            BODY_;                                                               \
        }                                                                        \
    };                                                                           \
    gPlayerLoopCallbacks.TYPE_.NAME_ = TYPE_##NAME_##Registrator::Forward;
```