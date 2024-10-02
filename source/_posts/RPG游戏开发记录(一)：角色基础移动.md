---
title: RPG游戏开发记录(一)：角色基础移动
abbrlink: 1987e04c
date: 2024-09-14 00:38:54
tags:
categories:
cover: https://www.notion.so/images/page-cover/met_bruegel_1565.jpg
description:
swiper_index:
sticky:
---

首先我们明确一下我们要实现的是类似GTA和巫师3这样的第三人称视角的控制角色移动方式，本节只实现最简单的前后左右移动和角色转向的逻辑。

# 基本思想

游戏中，我们按下W键，角色会向前移动；按下S键，角色会先转身面对镜头，然后移动；按下A键，角色先向左转，然后移动；按下D键，角色先向右转，然后移动。我们发现，无论按下哪个键，角色都是朝着他们自己的前方移动的。但是我们在按下`WSAD`键时是希望角色向前、后、左、右移动的。这里存在一个参考系的区别，如果我们把自己代入到我们控制的角色视角上，我们确实一直都是在向前走；而在摄像机的视角中，我们控制的角色也确实按照我们的意愿朝某一方向在移动。因此我们想要向某个方向移动是一个**摄像机视角下**的概念。摄像机面向的方向是前进的方向，摄像机面向方向的左方是向左移动的方向，右边和后面分别是摄像机面向方向的右边和后边。我们要向前移动，就是要让我们控制的角色的朝着摄像机朝向的方向移动；要向左移动，就是要让角色的朝向和摄像机左方的方向移动，角色的朝向向我们移动方向重合的过程中就会造成的角色转向。

所以说，实现本节角色移动效果的关键是处理好角色的朝向和摄像机朝向的关系。

在开始写代码之前之前我们需要了解四个接口：

- [Quaternion.Slerp](https://docs.unity3d.com/ScriptReference/Quaternion.Slerp.html)
  传入**我们控制角色当前在世界空间下朝向的Quaternion**、**我们希望角色移动方向的Quaternion**和一个表示转向速度的值，返回当前帧控制角色在世界空间下新的朝向Quaternion。

- [Quaternion.LookRotation](https://docs.unity3d.com/ScriptReference/Quaternion.LookRotation.html)
  传入一个世界空间坐标下的方向向量，返回该方向向量对应的Quaternion。

- [Rigidbody.MoveRotation](https://docs.unity3d.com/ScriptReference/Rigidbody.MoveRotation.html)
  传入这一帧角色朝向的世界坐标下的Quaternion，更新角色的朝向。
  
- [Rigidbody.MovePosition](https://docs.unity3d.com/ScriptReference/Rigidbody.MovePosition.html)
  传入这一帧角色的世界坐标下的Position，更新角色的位置。

以及transform.forward和Vector3.forward的区别：

[transform.Translate](https://docs.unity3d.com/ScriptReference/Transform.Translate.html)
(vector3.forward * Time.deltaTime);等价于 transform.Translate(transform.forward * Time.deltaTime, Space.World);前者是在对象空间坐标下实现物体移动的方式，后者是在世界空间坐标下实现物体移动的方式，因此transform.forward是一个世界空间坐标系下的概念。

# 代码实现思路

我们在代码中声明了一个用来表示当前帧移动方向的二维向量`_moveDirection`，通过判断当前帧有哪些按键被按下决定`_moveDirection`的值。
注意`_moveDirection`是作用在是摄像机对象空间下的，我们需要把这个向量转换到世界空间坐标系下，才能和角色的`transform.forward`向量进行运算。

我们把`_moveDirection`与`speed * Time.deltaTime`相乘，就能得到当前帧中角色的位移，将角色当前的位置加上角色在该帧中的位移就得到了这一帧最终角色的位置，将最终的位置传入`Rigidbody.MovePosition`方法，完成角色的移动效果。

同时，将上面两个值和`turnSpeed * Time.deltaTime`传入`Quaternion.Slerp`方法中，就能够得到当前帧中角色最终朝向的方向，然后最终朝向传给`Rigidbody.MoveRotation`方法，完成角色的转向效果。

# 完整代码

```
using UnityEngine;

namespace PlayerMovement
{
    public class PlayerMovement : MonoBehaviour
    {
        private Camera _camera;
        private Rigidbody _rigidBody;

        [SerializeField] private float turnSpeed = 20f;
        [SerializeField] private float speed = 20f;

        private Vector2 _moveDirection = Vector2.zero;
        private void Awake()
        {
            _camera = GameObject.Find("Camera").GetComponent<Camera>();

            _rigidBody = GetComponent<Rigidbody>();
        }

        private void Update()
        {
            UpdateInput();
         
            UpdateMovement();
            UpdateRotation();
        }

        private void UpdateInput()
        {
            _moveDirection = Vector2.zero;
            if (Input.GetKey(KeyCode.W))
            {
                _moveDirection += Vector2.up;
            }
            
            if (Input.GetKey(KeyCode.A))
            {
                _moveDirection += Vector2.left;
            }
            
            if (Input.GetKey(KeyCode.S))
            {
                _moveDirection += Vector2.down;
            }
            
            if (Input.GetKey(KeyCode.D))
            {
                _moveDirection += Vector2.right;
            }
            
            _moveDirection.Normalize();
        }
        
        private void UpdateMovement()
        {
           var movement = CameraDirection(new Vector3(_moveDirection.x, 0, _moveDirection.y)) * speed * Time.deltaTime;
           _rigidBody.MovePosition(transform.position + movement);
        }

        private void UpdateRotation()
        {
            if(_moveDirection.magnitude <= 0.01f) return;
            var rotation = Quaternion.Slerp(_rigidBody.rotation,
                Quaternion.LookRotation (CameraDirection(new Vector3(_moveDirection.x, 0, _moveDirection.y))), turnSpeed * Time.deltaTime);
            
            _rigidBody.MoveRotation(rotation);
        }

        private Vector3 CameraDirection(Vector3 dir)
        {
            var cameraForward = _camera.transform.forward;
            var cameraRight = _camera.transform.right;
            
            cameraForward.y = 0;
            cameraRight.y = 0;
            
            return cameraForward * dir.z + cameraRight * dir.x;
        }
    }
}
```

# 参考资料

- [Unity3d vector3.forward和transform.forward的区别！](https://blog.csdn.net/kaluluosi111/article/details/17206655?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2-17206655-blog-78846299.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-2-17206655-blog-78846299.235%5Ev43%5Econtrol&utm_relevant_index=5)
