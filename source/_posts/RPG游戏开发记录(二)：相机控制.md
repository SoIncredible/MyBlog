---
title: RPG游戏开发记录(二)：相机控制
tags:
  - RPG
categories: RPG游戏开发
cover: https://www.notion.so/images/page-cover/met_vincent_van_gogh_ginoux.jpg
abbrlink: ea538f2a
date: 2024-09-15 00:38:54
description:
swiper_index:
sticky:
---

# 基本思想

相机的运动可以被拆分成相机跟随、水平转向、俯仰(竖直)转向三个分运动。
因此，在笔者设计的相机跟随的方案中，相机组件会被设计成一个具有三层嵌套关系的结构——最外层负责跟随角色移动、中间层负责相机水平方向上的转向、最内层负责相机竖直方向的转向。

巫师3中的相机是锁头的，如果Player的头和摄像机之间有遮挡就会位移 

# 用到的接口

- [Transform.InverseTransformDirection(Vector3 direction);](https://docs.unity3d.com/ScriptReference/Transform.InverseTransformDirection.html)
  传入一个世界空间坐标下表示的三维向量，得到这个transform对象空间坐标下该向量的表示
- [Transform.TransformDirection(Vector3 direction);](https://docs.unity3d.com/ScriptReference/Transform.TransformDirection.html)
  传入一个在该transform坐标空间下表示的三维向量，得到这个向量在世界空间坐标下的表示
- [Transform.RotateAround(Vector3 point, Vector3 axis, float angle);](https://docs.unity3d.com/ScriptReference/Transform.RotateAround.html)
  使一个物体绕着穿过某一点的某一个方向的轴旋转angle角度，在左手坐标系中，顺时针为正向旋转。
- [Vector3.SmoothDamp(Vector3 current, Vector3 target, ref Vector3 currentVelocity, float smoothTime, float maxSpeed = Mathf.Infinity, float deltaTime = Time.deltaTime);](https://docs.unity3d.com/ScriptReference/Vector3.SmoothDamp.html)
  Gradually changes a vector towards a desired goal over time.

我还需要知道相机距离角色的距离，这个距离应该是三维空间下的距离，比如说如果摄像机的视角很高的话，这时候镜头就会离控制角色的头顶很近，我们需要有一套逻辑去动态控制这个距离，比如说角色靠墙很近的时候，镜头要尽可能地靠近角色，这个功能能不能直接通过给相机上装一个碰撞体实现？

另外还需要注意的是，相机的旋转是以我们控制的角色为中心的，在视角中我们的角色一般偏左，镜头旋转也是围绕着左侧为轴进行的

# 外层跟随逻辑

我们要让相机的外层节点始终跟随Player，我们站在摄像机的视角看，无论我们怎么操作角色和相机，摄像机和Player之间的相对方向是不变的(由外层节点原点指向Player方向的矢量是不变的，但是距离可能会变)，因此，我们需要给定一个摄像机和Player位置关系的单位方向矢量**v**，和一个摄像机和Player时间的距离`distance`。注意，这个方向矢量是对象空间坐标下表示的，我们要通过坐标系变换，将该矢量变换到世界坐标系下，然后再用世界坐标下Player的位置减去方向矢量乘`distance`的结果，就是摄像机所处的位置。

# 中层水平旋转逻辑

在水平和俯仰的旋转实现中，一定要注意旋转的中心不是摄像机的中心点，而是Player的中心点，对于水平旋转的描述就是中层节点绕过Player的中心点的平行于世界空间y轴正方向的轴进行旋转。

# 内层俯仰逻辑

同中层水平旋转逻辑一样，旋转的中心点位于Player的中心点，对俯仰旋转的描述是内层节点绕过Player的中心点的平行于世界空间x轴正方向的轴进行旋转。

除此之外，我们还需要给俯仰旋转添加旋转角度的限制，如果不设限镜头会翻转且视角很奇怪。这里会遇到的一个坑就是，在Inspector窗口中看到的rotation是负值，但是在代码中该值实际是360 + 负值，因此我们设置俯仰角度的范围实际上是[0, max]$\cup$[360 + min, 360]。

# 代码实现

```
using UnityEngine;

public class CameraHandler : MonoBehaviour
{
    public Transform targetTransform;
    private Transform CameraFollowTransform => transform;
    public Transform cameraTransform;
    public Transform cameraHorizontalTransform;
    
    [SerializeField] private float cameraDistance;
    
    public float horizontalSpeed = 300f;
    public float verticalSpeed = 300f;
    public float minimumVerticalAngle = -45;
    public float maximumAngle = 20;

    private Vector3 _targetDirInCameraCoordinates;

    private void Awake()
    {
        CalculateTargetDirectionInCameraCoordinates();
    }

    private void Update()
    {
        FollowTarget();
        HandleCameraHorizontalRotation(Input.GetAxis("Mouse X"));
        HandleCameraVerticalRotation(Input.GetAxis("Mouse Y"));
    }
    
    // TODO Eddie 实现在Editor中移动相机可以实时获取并保存相机和Player之间的向量关系
    // TODO Eddie 移除该方法
    private void CalculateTargetDirectionInCameraCoordinates()
    {
        var dir = targetTransform.position - transform.position;
        _targetDirInCameraCoordinates = transform.InverseTransformDirection(dir).normalized;
    }

    private void FollowTarget()
    {
        var worldDir = CameraFollowTransform.TransformDirection(_targetDirInCameraCoordinates).normalized;
        var targetPosition = targetTransform.position - worldDir * cameraDistance;
        var velocity = Vector3.zero;
        CameraFollowTransform.position = Vector3.SmoothDamp(CameraFollowTransform.position, targetPosition, ref velocity, 0f);
    }

    private void HandleCameraHorizontalRotation(float mouseXInput)
    {
        var horizontalAngle = mouseXInput * horizontalSpeed * Time.deltaTime;
        cameraHorizontalTransform.RotateAround(targetTransform.position, Vector3.up, horizontalAngle);
    }

    private void HandleCameraVerticalRotation(float mouseYInput)
    {
        var currentAngle = cameraTransform.localEulerAngles.x;
        switch (mouseYInput)
        {
            case > 0 when currentAngle <= 360 + minimumVerticalAngle && currentAngle > 180:
            case < 0 when currentAngle >= maximumAngle && currentAngle < 180:
                return;
            default:
                cameraTransform.RotateAround(targetTransform.position, cameraTransform.right, -mouseYInput * verticalSpeed * Time.deltaTime);
                break;
        }
    }
}

```

# 参考资料

