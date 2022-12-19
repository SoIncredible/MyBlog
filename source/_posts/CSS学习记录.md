---
title: CSS学习记录
tags: CSS
top_img: >-
  https://i0.wp.com/www.floxblog.com/wp-content/uploads/2020/08/What-is-CSS-1.jpg
categories: 前端
cover: >-
  https://i0.wp.com/www.floxblog.com/wp-content/uploads/2020/08/What-is-CSS-1.jpg
description: CSS学习笔记(持续更新)
abbrlink: 127bc3c9
date: 2022-12-19 18:13:50
updated:
---





# StartHere

Css全称Cascading Style Sheet(层叠样式表),它是一种样式表语言,用来描述HTML或者XML文档的呈现,CSS描述了在屏幕,纸质,音频等其他媒体上的元素应该如何被渲染的问题.

## CSS是如何工作的?

当浏览器展示一个文件的时候,它必须坚固文件的内容和文件的样式信息,下面我们会了解到它处理文件的标准的流程,需要知道的事,下面的步骤是浏览加载网页的简化版本,而且不同的浏览器在处理文件的时候会有不同的方式,但是下面的步骤基本都会出现.

1. 浏览器载入HTML文件(比如从网络上获取).

2. 将HTML文件转换成一个DOM(Document Object Model),DOM是文件在计算机内存中的表现形式,之后会更加详细的解释**DOM(挖坑),记得回来填上**

   

   {% tip warning faa-horizontal animated %}这里挖坑了,记得填上.{% endtip %}

3. 接下来,浏览器会拉取该HTML相关的大部分资源,比如嵌入到页面的图片,视频和CSS样式.JavaScript则会稍后进行处理,简单起见,我们不会过多讨论如何加载JavaScript.

4. 浏览器拉取到CSS之后便会进行解析,根据选择器(Selector)的不同类型(比如element,class,id等等)把他们分到不同的“**桶**”中.浏览器基于它找到的不同的选择器(Selector),将不同的规则(基于选择器的规则,如元素选择器,类选择器,id选择器等等)应用在对应的DOM节点中,并添加节点依赖的样式(这个中间步骤称为渲染树).

5. 上述的规则应用于渲染树之后,渲染树会依照因该出现的结构进行布局

6. 网页展示在屏幕上(这一步被成为着色).

结合流程图理解更加形象:

![](https://developer.mozilla.org/en-US/docs/Learn/CSS/First_steps/How_CSS_works/rendering.svg)

## 关于DOM

一个DOM又一个树形结构,标记语言中的每一个元素、属性以及每一段文字都对应着结构树中的一个节点(node/DOM或者DOM node).节点由节点本身和其他DOM节点的关系定义,有些节点有父节点,有些节点有兄弟节点(同级节点).

对于DOM的理解会很大程度上帮助我们设计、调试和维护我们的CSS,因为DOM是CSS样式和文件内容的结合.当我们使用浏览器F12调试的时候我们可以操作DOM来查看使用了哪些规则.

## 一个真实的DOM案例

不同于很长且枯燥的案例,这里我们通过一个HTML片段来了解HTML是怎么转换成DOM的

```html
<p>
  Let's use:
  <span>Cascading</span>
  <span>Style</span>
  <span>Sheets</span>
</p>

```

在这个DOM中,`<p>`元素对应了父节点,他的子节点是一个text节点和三个对应了`<span>`元素的节点,SPAN节点同时也是它们中的Text节点的父节点

```
P
├─ "Let's use:"
├─ SPAN
|  └─ "Cascading"
├─ SPAN
|  └─ "Style"
└─ SPAN
    └─ "Sheets"
```

上图就是浏览器如何解析这个HTML片段,它生成上图的DOM树形结构并将它按照如下输出到浏览器:

Let's use: Cascading Style Sheets

## 应用CSS到DOM

接下来让我们看看添加一些CSS到文件里加以渲染,同样的HTML代码:

```html
<p>
  Let's use:
  <span>Cascading</span>
  <span>Style</span>
  <span>Sheets</span>
</p>
```

添加CSS代码:

```css
span {
  border: 1px solid black;
  background-color: lime;
}
```

浏览器会解析HTML并创造一个DOM,然后解析CSS.可以看到唯一的选择器就是`span`元素选择器,浏览器处理规则会非常快,把同样的规则直接使用在三个`<span>`标签上,然后渲染出图像到屏幕.

现在的显示如下:

<p>
  Let's use:
  <span style="border: 1px solid black;background-color: lime;">Cascading</span>
  <span style="border: 1px solid black;background-color: lime;">Style</span>
  <span style="border: 1px solid black;background-color: lime;">Sheets</span>
</p>

## 当浏览器遇到无法解析的CSS代码会发生什么?

浏览器什么都不会做,继续解析下一个CSS样式.

如果一个浏览器在解析你所书写的CSS规则中遇到了无法理解的属性或者值,它会忽略这些并继续解析下面的CSS声明,在你书写了错误的CSS代码(或者错误拼写),又或者当浏览器遇到对于它来说很新的还没有支持的CSS代码的时候,上述情况同样会发生.

相似的,当浏览器遇到无法解析的选择器的时候,他会直接忽略掉整个选择器的规则,然后解析下一个CSS选择器.

这样做的好处很多,你所编写的CSS优化过程中浏览器遇到无法解析的规则也不会报错,当你为一个元素制定多个CSS样式的时候,浏览器会加载样式表中最后的CSS代码进行渲染(样式表的优先级看这👀),也正因为如此,我们可以给同一个元素制定多个CSS样式来解决有些浏览器不兼容新特性的问题(比如指定两个width).

这一特点在你想使用一个很新的CSS特性但并不是所有的浏览器都支持的时候非常有用,比如一些老的浏览器不接受`calcu()`作为一个值.我们可能使用它结合像素为一个元素设置了动态宽度,老式的浏览器由于无法解析而会选择忽略掉这一行,新式浏览器则会把这一行解析成像素值,并且覆盖第一行制定的宽度.

```css
.box {
  width: 500px;
  width: calc(100% - 50px);
}
```

## CSS在html中有三种引用方式:

1. 通过外部文件，在head中使用link因为css外部文件

   ```html
   <!DOCTYPE html>
   <html lang="en">
   <head>
       <meta charset="UTF-8">
       <meta http-equiv="X-UA-Compatible" content="IE=edge">
       <meta name="viewport" content="width=device-width, initial-scale=1.0">
       <title>Document</title>
       <link rel="stylesheet" href="css/style.css" >
   </head>
   <body>
       <p>I'm learning css!</p>
   </body>
   </html>
   ```

   

2. 第二种是在head组件中写，添加<code>style</code>标签，缺点是会使得html看起来很臃肿.

   ```html
     <head>
     <meta charset="UTF-8">
     <meta http-equiv="X-UA-Compatible" content="IE=edge">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <title>Document</title>
     <style>
     	p{
     		color:burlywood
     	}
     </style>
     </head>
   <body>
       <p>I'm learning css!</p>
   </body>
   </html>
   ```

3. 使用内联方式编写,此方法最不推荐.

   ```html
   <!DOCTYPE html>
   <html lang="en">
   <head>
       <meta charset="UTF-8">
       <meta http-equiv="X-UA-Compatible" content="IE=edge">
       <meta name="viewport" content="width=device-width, initial-scale=1.0">
       <title>Document</title>
       <link rel="stylesheet" href="css/style.css" >
   </head>
   <body>
       <p style="color:burlywood">I'm learning css!</p>
   </body>
   </html>
   ```
   
   

## CSS的优先级



## 验证css是否生效？

使用以下网站

![](CSS学习记录/image-20221219183501480.png)

可以检验自己的css是否生效







css selectors



p{

color: a

}

p{

color: b

}

页面上的颜色为b

优先级问题

重写

​	span是什么？

# Colors



透明度 rgba的概念

alpha channel

hsl是什么

颜色可以有哪几种表示方式

什么情况下颜色可以缩写

# Units&Size
