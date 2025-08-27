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