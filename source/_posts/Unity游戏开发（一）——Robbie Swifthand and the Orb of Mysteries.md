---
title: Unity游戏开发（一）——Robbie Swifthand and the Orb of Mysteries
date: 2022-06-27 00:00:00
tags: Robbie
abbrlink: b87d8d3c
categories: Unity游戏开发
---





1.导入素材
    import custom（定做的，定制的）packages



<!--more-->

​    

2.创建Tile Map（瓦片地图）    
    Window - 2D - TilePalette - New TilePalette - 选择要保存的位置 - 创建好后将要绘制的瓦片Tile拖进这个Palette中
    奇怪的报错：*Handles.Repaint called outside an editor OnGUI* 不清楚是编译器版本的问题还是素材本身的问题（其实素材本身没有问题，归根结底还是因为换了编译器的版本才导致出了问题）。
    

    使用2DExtra插件绘制tile map
3.给TileMap设置层级
    我们创建了好几个TileMap，需要给他们一个排序，用到了TileMap中的Sorting Layers（PS1：在Unity列表中越在下面的图层它在画面中越显示在上方）（PS2：Inspector窗口中最上面还有一个Layer，我们这里说的是Tilemap中Tilemap render里的Sorting Layer，但好像也没什么区别，自己想想吧），我们还要用到Order in Layer，数值越高越显示在上面
    
    
    
4.绘制Tilemap
    首先要知道Player的高度，然后根据Player的高度去绘制地图。
    右键新建，在2D中有一个Tiles，选择其中的Rule Tile然后在其中可以添加绘制规则
    可以设置随机生成，也可以设置按照位置生成
    
    来点高级的，使用2DExtra插件（2D Tilemap Extras），可以高效地绘制TileMap


5.给Platform的TileMap添加TileMap Collider，然后选择Use Composite 将Platform的Collider变成一个整体，然后需要给它添加一个Composite Collider 2D，但是添加这个组件会同时给你添加Rigidbody组件，我需要在Rigidbody组件中将BodyType改成static，确保Platform不会受重力影响掉下去。 在Inspector窗口第一行也有一个static的选项，也可以勾上。
6.给Player添加物理组件
    首先添加rigidbody组件，更改collision detection 为 continuous，将interpolate改为 interpolate，目的是为了优化手感。
    其次添加BoxCollider 2D，调整Collider的大小。
    注意要锁定Rigidbody中的Constraints中的Z轴。
    我们希望人物跳到墙上的时候可以滑下来，不是卡在墙上，所以我们在Player中的BoxCollider中的Material添加一个材质，其摩擦力为0，Bounciness为0.
    要为Hierarchy中的每一个物体添加一个Layer，上文说的Inspector第一行的标签，我们用这个标签来判断比如人物是否站在地面上等等。这个Layer没有图层先后的关系，只是用来区分不同的物体。
    
    
    
6.让角色动起来
    开始写代码了，这一小节实现基本的移动、转向、跳跃、下蹲
```
    private Rigidbody2D rb;
    private BoxCollider2D coll; //这里一定要写boxcollider2d，因为有好多种collider，但是只有boxcollider2d可以获得offset和size的属性
    
    [Header("移动参数")]  //这一行会显示在Inspector中，可以为以后自己写插件提供一个思路
    public float speed = 8f;
    //下蹲的时候会用speed除以下面的crouchSpeedDivisor来实现下蹲移动速度慢的效果
    public float crouchSpeedDivisor = 3f;
    float xVelocity; 
    
    [Header("跳跃参数")]
    
    //一共设置了三种跳跃方式
    //1.一般跳
    //2.蓄力跳 最多蓄力0.1s（jumpHoldDuration）
    //3.下蹲跳
    public float jumpForce = 6.3f;
    public float jumpHoldForce = 1.9f;
    public float jumpHoldDuration = 0.1f;
    public float crouchJumpBoost = 4.0f;
    float jumpTime; //用来计算跳跃的时间
    
    [Header("状态检测")]
    public bool isCrouch;   //判断是否下蹲
    public bool isOnGround; //判断是否在地面上
    public bool isJump; //判断是否跳跃
    public bool isHeadBlocked; //判断头顶是否有遮挡
    
      
    
        
   
    [Header("下蹲操作")]
    Vector2 colliderStandSize;
    Vector2 colliderStandOffset;
    Vector2 colliderCrouchSize;
    Vector2 colliderCrouchOffset
    
    [Header("环境监测")]
    public LayerMask groundLayer;
    
    [Header("按键设置")]
    bool jumpPressed;
    bool jumpHeld;
    bool crouchHeld;
    
    
    //在这里浅浅总结一下：
    //Crouch和StandUP函数都属于接收敏感但是执行不敏感的函数，因此要放到Update函
    //数中去执行，因为通常Update函数一秒执行的次数比Fixed Update函数要多，也就是
    //说Update函数会更灵敏一些
    //对于移动、奔跑这类执行敏感接收不敏感的函数，可以放到Update也可以放到
    //FixedUpdate中使用（个人感觉）
    
    void Update(){
        
        if(Input.GetButtonDown("Jump")){
            jumpPressed = true;
        }
        
        jumpHeld = Input.GetButton("Jump");
        crouchHeld = Input.GetButton("Crouch");
        
    }
    
    private void FixedUpdate(){
    
        PhysicsCheck();
        GroundMovement();
        
        MidAirMovement();
        
        jumpPressed = false;
        
    
    }
    
      /*****基本的移动*****/
     
    void GroundMovement(){
        xVelocity = Input.GetAxis("Horizontal");
        rb.velocity = new Vector2(xVelocity * speed, rb.velocity.y);
        //以上是最基本的移动代码
    }
    //面部朝向
    void FilpDirection(){
    
        //一开始的时候等式右边用的是Vector2，但是后面我们给场景做了景深的处理
        //如果用vector2的话z轴会被更新成0，我们希望它是1，所以要用vector3
        //但是为什么Scale参数会有z轴啊，这不是纸片人？暂且认为z轴是调节它的厚度吧
        if(xVelocity < 0) transform.localScale = new Vector3(-1,1，1);
        else if(xVelocity > 0) transform.localScale = new Vector3(1,1，1);
    }

    
    /******跳跃函数*****/
    void MidAirMovement(){
        
        
        //首先确保头顶没有遮挡，如果有遮挡跳跃想都不要想
        if(!isHeadBlocked){
            if(jumpPressed && isOnGround && !isJump){
                //这里的判断条件需要想的全一点
                //实现起来是很简单的 
                if(isCrouch){
                    //如果是下蹲的状态，有下蹲跳
                    StandUp();
                    //新方法
                    rb.AddForce(new Vector2(0f,jumpForce),ForceMode2D.Impluse);
                }else if(isJump){
                //这里就可以体现必须要用FixedUpdate了
                //跳跃确定的次数，或者说特定的帧数
                    if(jumpHeld){
                        rb.addForce(new Vector2(0f, jumpForce), ForceMode2D.Impulse);
                
                    }
                    if(jumpTime < Time.time){
                        //超过了0.1s，跳跃结束
                        isJump = false;
                    
                    }
                
                
                }
                
        }  
        
        
    }
    


     /*****下蹲和起立*****/
    //下蹲操作
    //思路：
    //我们只考虑对碰撞体的高度进行处理，动画另说
    //下蹲时碰撞体的高度减少一半，起立时恢复原来的高度就可以了
    //接收碰撞体的Offset和Size信息
    
    //下蹲函数
    //Unity本身没有下蹲的按键，我们去到Edit-ProjectSettings中
    //在Jump上右键Duplicate Array Element，然后改名为Crouch
    void Crouch(){
    
        isCrouch = true;
        coll.size = colliderCrouchSize;
        coll.offset = colliderCrouchOffset;
    
    }
    //起立函数
    void StandUP(){
        
        isCrouch = false;
        //还原collider的offset和size
        coll.size = colliderStandSize;
        coll.offset = colliderStandOffset;
        
    }    
```



7.更改重力
    
    Edit - ProjectSettings - Physics 2D 更改Gravit y -9.81为-50；

8.射线检测

    重构函数



9.跳跃、悬挂代码（目前解决了跳跃不灵敏的问题，但是还需要改进）




10.实现摄像机跟踪和透视效果
    首先我们把场景外的背景颜色改一下：选择Camera Background更改颜色
    然后选择Projection，这里面有两个选项：1.Perspective 2.Orthographic，区别是1是真实摄像机视角，有景深的感觉，2是一个长方体盒子，没有景深的感觉
    然后我们给Grid中不同的TileMap更改一下Z轴。
    添加Cinemachine，这是一个很方便的摄像机组件，只需要把需要跟随的物体托给它就可以了
    注意我们需要给Cinemachine添加CinemachineConfider组件来限制摄像机的边界，并且创建一个空物体给他加一个polygon collider 2D的组件来自由地画出边界，注意要选择isTrigger。
    
11.灯光效果


    法线贴图，只有有了贴图才会有灯光照亮的效果


​    
​    
12.给角色添加动画

    blend tree    


​    
​    
13.给角色添加音效


    用代码生成


​    
​    
​    
​    
​    因为用到了很多预制体，但是预制体上的代码文件有的缺失了，所以运行起来以后会有感叹号并且提示：The referenced script (Unknown) on this Behaviour is missing!解决方法就是顺藤摸瓜找到代码文件缺失的预制体。

14.死亡机制    
    
    