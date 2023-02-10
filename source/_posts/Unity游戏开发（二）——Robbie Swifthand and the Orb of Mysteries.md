---
title: Unity游戏开发（二）——Robbie Swifthand and the Orb of Mysteries
date: 2022-06-27 00:00:00
tags: Robbie
abbrlink: asdfasdf
categories: Unity游戏开发
---

前段时间把Robbie的所有视频都看完了，也跟着做了，那么最后我想回过头来，从全局的角度，看一下这个游戏是怎么做出来的。



<!--more-->

人物角色：

1.状态
    1.1跑动
        
        Michael的视频中，喜欢用Rigidbody2D组件的属性来控制角色的移动（当然还有别的方法）
        
        Rigidbody2D rb;
        
        float xVelocity = Input.GetAxis("Horizontal") * speed; //velocity译为速度
        
        rb.velocity = new Vector2(xVelocity,0);


​        
​        
​        更改面部朝向
​        //注意一个小细节，判断条件中不要带等号，否则人物从移动到静止后的画面会很诡异，可能有一个突然的转向
​        if(xVelocity < 0) transform.localScale = new Vector3(-1,1,1);
​        else if(xVelocity > 0) transform.localScale = new Vector(1,1,1);
​        
​    1.2下蹲
​    
        按下S键可以下蹲，什么时候起立，这里我们用射线检测来进行判断。
    
    1.3跳跃
        
        跳跃有前提，需要用到射线检测，我会在第四点中提及，这里只需要知道我们需要射线检测作为判断条件就可以了
        
    1.4悬挂
    
        这里同样需要用到射线检测，但也同样只需要知道，它只是作为一个判断条件

2.动画
    与状态绑定


3.音效
    与状态绑定

4.机制
    4.1射线检测
    
    
    4.2收集物品机制
    
    4.3死亡机制

5.摄像机跟踪
    用到了Cine Machine


6.GameManager



## 场景、关卡设置

1.TileMap


    创建好下面几个TileMap之后首先记得调整它们的SortingLayer
    为了方便地图的绘制我用了2DExtra这个插件：
    ![](assets/16517484297440.jpg)
    使用方法：在Project窗口中新建-2D-Tiles-RuleTile，在Inspector窗口中设置你的绘制规则，最后Project窗口中创建的这个预制体拖到Tile Palette中。就可以其他Tile一样自由绘制了，Robbie系列中只用到了Random、Single两种方式。
    1.1Background 
    
        Background没啥好说的，直接画满就可以了
    
    1.2BackgroundDetrails
    
        添加灯光效果后，主要就靠这一层TileMap展现效果，里面有一些材质贴图的知识，在文章最后我会浅浅的写一下
    
    1.3Platforms
        Platforms是与角色发生碰撞检测的TileMap，要为它添加TileMap Collider2D，除此之外，我们还要为它添加Composite Collider 2D，让Platforms上的Tile变成一个整体，这样角色就不会在Tile间卡住。
        另外，角色跳跃的过程中碰到了墙壁可能也会卡在墙里面，我们只需要给Platforms添加一个材质就可以了（好像没有）？？？
    1.4Shadows


​    
2.机关设置



3.宝珠设置



4.石门（通关）设置    



5.光效设置



​        

