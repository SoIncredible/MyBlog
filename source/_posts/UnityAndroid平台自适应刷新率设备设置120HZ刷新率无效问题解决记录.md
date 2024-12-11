---
title: UnityAndroid平台自适应刷新率设备设置120HZ刷新率无效问题解决记录
abbrlink: 7292a3de
date: 2024-12-10 00:53:54
tags:
categories:
cover: https://www.notion.so/images/page-cover/woodcuts_8.jpg
description:
swiper_index:
sticky:
---


国产厂商的对安卓系统的特殊调教
⭕️ 安卓层的尝试 遇到了getHolder获取不到surfaceView的问题 原因是什么unity3d.player.I cant cast to surfaceView
⭕️ Unity层的尝试 使用SysInfo.Resolutions获取当前设备所有支持的分辨率+刷新率,从中挑选出那个最高分辨率的,然后调用setResolution接口设置上
⭕️ 还有一种策略没尝试:如果一个设备支持120HZ刷新率,但是支持多种分辨率下的120HZ刷新率,我最高的分辨率设置上去不生效,那如果设置最低的呢?另外要取消勾选FramePacing,还要把V Sync关掉

# 解除帧率限制(Android)

对一些安卓高刷屏来说，进游戏时屏幕刷新率会被设置成60，这应该和Android系统的策略有关，因此通过`Screen.currentResolution.refreshRateRatio`接口拿到的屏幕刷新率是不准确的，
如果要开启高刷，则需要关闭 ProjectSettings -> Player -> Resolution and Presentation 下的 [Optimized Frame Pacing](https://docs.unity3d.com/ScriptReference/PlayerSettings.Android-optimizedFramePacing.html)，并且在脚本中设置 `Application.targetFrameRate = 120;`

以上的解决方案过于粗糙了，更完善的解决方案需要参考Android的[官方文档](https://developer.android.com/media/optimize/performance/frame-rate?hl=zh-cn)去到Android层实现帧率的设置，一篇[实践的帖子](https://blog.csdn.net/a310989583/article/details/135771394?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-135771394-blog-118787844.235%5Ev43%5Epc_blog_bottom_relevance_base8&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-4-135771394-blog-118787844.235%5Ev43%5Epc_blog_bottom_relevance_base8&utm_relevant_index=9)

- https://source.android.com/docs/core/graphics/multiple-refresh-rate?hl=zh-cn
  
> 2024.12.10更新
> 尝试了上面的方法,最终失败了

 
# 问题

有些Android手机设置帧率120HZ但是实际上达不到的问题https://answer.uwa4d.com/question/64e7779266b5c657c78be192