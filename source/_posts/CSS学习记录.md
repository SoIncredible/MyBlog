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

## CSS在html中三种引用方式:

1. 外部样式表(external style sheet)，在`head`中使用link引入css外部文件

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

2. 内部样式表，添加<code>style</code>标签，缺点是会使得html看起来很臃肿.

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

3. 内连样式,此方法最不推荐.

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


## 小练习1

在VSCode中安装`LiveServer`插件,这个插件可以在你对CSS做完更改后立刻将你所做的更改渲染到页面上.安装完成后在`html`文件中使用`右键`->`open with liver server`

### 修改元素的默认属性

我们可以修改浏览器默认的样式,只需要选定想要修改的元素,加一条CSS规则即可.

拿无序列表`<ul>`举例,它自带项目符号,你不喜欢它就可以这样移除它们:

```css
li {
  list-style-type: none;
}
```

```html
<h1>I am a level one heading</h1>

<p>This is a paragraph of text. In the text is a <span>span element</span> 
and also a <a href="http://example.com">link</a>.</p>

<p>This is the second paragraph. It contains an <em>emphasized</em> element.</p>

<ul>
    <li>Item one</li>
    <li>Item two</li>
    <li>Item <em>three</em></li>
</ul>
```

我们不仅可以移除项目符号,我们甚至可以改变它们的形状:

```css
li {
  list-style-type: square;
}
```

### 使用类名

我们已经能够修改HTML元素的默认属性了,接下来我们尝试给HTML元素添加类(class),用来实现更加定制化的效果.

举个例子,我们更改一下上面的html代码:

```html
<ul>
    <li>Item one</li>
    <li class="special">Item two</li>
    <li>Item <em>three</em></li>
</ul>
```

在CSS文档中,我们添加这个`special`类

```css
.special {
  color: orange;
  font-weight: bold;
}
```

保存后,我们通过`live server`能够直接看到变化,通过添加类,我们实现了对HTML相同元素的不同效果的显示.

应该注意的是,这个`special`类可不仅仅局限于列表,它可以应用到各种元素上,举个例子,我们可以让段落里的`<span>`也具有同样的效果.

```html
<p>This is a paragraph of text. In the text is a <span class="special">span element</span> 
and also a <a href="http://example.com">link</a>.</p>
```



还有一种写法,HTML元素选择器跟类一起出现:

```css
li.special {
  color: orange;
  font-weight: bold; 
}
```

意思就是说,“选中每个`special`类的`li`元素”,如果是这样的话,那它对`<span>`还有其他的元素都不起作用了,想要再次对`<span>`起作用就需要这样写:

```css
li.special,
span.special {
  color: orange;
  font-weight: bold;
}
```

这样写可太麻烦了,作为一个懒狗我还是想把一个类的属性应用在多个元素上,所以大部分时候我们还是不要管元素了,光看类就完事儿了.

### 根据元素在文档中的位置确定样式

有时候,我们希望某些内容根据它在文档中的位置而有所不同,有很多选择器可以为我们所用,在这里我们只介绍两种.在我们的文档中有两个`<em>`元素,一个在段落内,另一个在列表项内.仅选择嵌套在`<li>`元素内的`<em>`我们可以使用一个称为**包含选择符**的选择器,它只是单纯地在两个选择器之间加上了一个空格.

```css
li em {
  color: rebeccapurple;
}
```

该选择器将选择`<li>`内部的任何`<em>`元素(`<li>`的后代),因此在上面给到的html代码中,实现的效果应该是第三个列表项内的`<em>`是紫色,但是在段落内的那个没有发生变化.

再举一个我们可能想要实现的效果:设置在HTML文档中直接出现在标题后面并且与标题有相同层级的段落样式,在两个选择器之间添加一个`+`号(称为**相邻选择符**)

```css
h1 + p {
  font-size: 200%;
}
```

### 根据状态确定样式

在本次小练习中我们最后要看的一种修改样式的方法是根据标签的状态确定样式.一个直观的例子就是当我们修改连接的样式时,我们需要定位(针对)`<a>`标签,取决于是否是未访问的、访问过的、被鼠标悬停的、被键盘定位的,亦或是正在被点击当中的状态,这个标签有着不同的状态,我们可以使用CSS去定位或者说针对这些不同的状态进行修饰,下面的代码使得没有被访问的链接的颜色变成粉色、访问过的链接变成绿色.

```css
a:link {
  color: pink;
}

a:visited {
  color: green;
}
```

我们也可以改变被鼠标悬停时候的样式,如移除下划线,下面的代码就实现了这个功能:

```css
a:hover {
  text-decoration: none;
}
```

### 同时使用选择器和选择符

```css
/* selects any <span> that is inside a <p>, which is inside an <article>  */
article p span { ... }

/* selects any <p> that comes directly after a <ul>, which comes directly after an <h1>  */
h1 + ul + p { ... }

```

向我们自己的CSS中添加如下代码:

```css
/* 这段的意思是选取body标签中所有和h1标签同级的p标签的special类 */
body h1 + p .special {
  color: yellow;
  background-color: black;
  padding: 5px;
}
```

可能上面的代码看起来有点复杂了,不要灰心,通过深入的学习我们会找到窍门的.

## CSS的优先级

{% tip warning faa-horizontal animated %}这里挖坑了,记得填上.{% endtip %}

优先级是分配给指定的CSS声明的一个权重,它由匹配的选择器中的每一种选择类型的数值所决定

而当优先级与多个CSS声明中任意一个声明的优先级相等的时候,CSS中最后的那个声明将会被应用到元素上.

当同一个元素有多个声明的时候,优先级才会有意义.因为每个直接作用于元素的CSS规则总是会接管/覆盖(takeover)该元素从祖先元素继承而来的规则.

### 选择器类型

下面给出的列表,选择器类型的优先级是递增的(出现了`伪元素`、`伪类`这些新概念不要怕,后面我们会使用到):

1. 类型选择器(比如`<h1>`)和伪元素(比如,`::before`)
2. 类选择器(比如,`.example`),属性选择器(比如,`[type="radio"]`)和伪类(比如,`:hover`)
3. ID选择器(比如,`#example`)

通配选择符(universal selector)(`*`)、关系选择符(combinators)([`+`](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Adjacent_sibling_combinator), [`>`](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Child_combinator), [`~`](https://developer.mozilla.org/zh-CN/docs/Web/CSS/General_sibling_combinator), [" "](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Descendant_combinator), [`||`](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Column_combinator))、否定伪类(negation pseudo-class)(`:not()`)对优先级没有影响(但是在`:not()`内部声明的选择器会影响优先级)

给元素添加的内联样式(例如,`style="font-weight:bold"`)总会覆盖外部样式表的任何样式,因此可以看作是具有最高的优先级

### `!important`例外规则

当在一个样式声明中使用一个`!important`规则时,此声明将覆盖任何其他声明.虽然,从技术上讲,`!important`与优先级无关,但是它与最终的结果直接相关,使用`!important`是一个坏习惯,应该尽量避免,因为这破坏了样式表中固有的级联规则,使得调试找bug变得更加困难了.当两条相呼冲突的带有`!important`规则的声明被应用到相同的元素上时,拥有更大优先级的声明会被采用.

{% tip info %}[此处](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Specificity)有关于`!important`的使用心得,未来某天我会将它整理到我的博客中{% endtip %}

## 验证css是否生效？

使用以下网站

![](CSS学习记录/image-20221219183501480.png)

可以检验自己的css是否生效

# CSS Selectors

CSS选择器是CSS规则的一部分,它是元素和其他部分组合起来告诉浏览器哪个html元素应当是被选为应用规则中的CSS属性值的方式,选择器所选择的元素,叫做选择器的对象.

我们已经遇到过几种不同的选择器,选择器可以以不同的方式选择元素,比如`h1`元素,或者是根据class(类)选择例如`.special`.



## 选择器列表

如果我们有多个使用相同样式的CSS选择器,那么这些单独的选择器可以被混编为一个“选择器列表”,这样,规则就可以应用到所有的单个选择器上了,比如`h1`和`.special`类有相同的CSS,那么我可以把它们写成两个分开的规则.

```css
h1 {
  color: blue;
}

.special {
  color: blue;
}
```

我也可以将它们组合起来,在它们之间加上一个逗号,变成**选择器列表**.

```css
h1, .special {
  color: blue;
}
```

空格可以在逗号的前面或者后面,我们还可以让每个选择器都另起一行,这样会更好读一些.

```css
h1,
.special {
  color: blue;
}
```

在下面的示例中,我们尝试把两个相同声明的选择器组合起来,展示的效果在组合起来以后应该还是一样的.



{% tip warning faa-horizontal animated %}这里要插入示例{% endtip %}

当我们使用**选择器列表**时,如果任何一个选择器无效(存在语法错误),那么整条规则都会被忽略.

在下面的示例中,无效的class选择器会被忽略,但是`h1`选择器仍然会被样式化

```css
h1 {
  color: blue;
}

..special {
  color: blue;
}
```

但是在被组合起来以后,整个规则都会失效,无论是`h1`还是这个class都不会被样式化

```css
h1, ..special {
  color: blue;
}
```



## 选择器的种类

有几组不同的选择器,知道了需要哪种选择器,我们能够正确的使用它们,

### 类型、类和ID选择器(最常用)

```css
/* 类型选择器 */
h1 { }

/* 类选择器 */
.box { }

/* ID选择器 */
#unique { }

```

{% tip info %}下面这些选择器都还没有碰到过{% endtip %}

### 标签属性选择器

这组选择器根据一个元素上某个标签的属性的存在以选择不同的方式:

```css
a[title] { }
```

或者根据一个有特定值的标签属性是否存在来选择:

```css
a[href="https://example.com"] { }
```

### 伪类与伪元素

这组选择器包含了伪类,用来样式化一个元素的特定状态.例如`:hover`伪类会在鼠标指针悬浮到一个元素上的时候选择这个元素

```css
a:hover { }
```

它还可以包含了伪元素,选择一个元素的某个部分而不是元素自己.例如,`::first-line`是会选择的一个元素(下面的情况中是`<p>`)中的第一行,类似`<span>`包含在了第一个被格式化的行外面,然后选择这个`<span>`

```css
p::first-line { }
```

### 运算符

最后一组选择器可以将其他选择器组合起来,更复杂的选择元素.下面的示例用运算符`>`选择了`<article>`元素的初代子元素.

```css
article > p { }
```



# Colors



透明度 rgba的概念

alpha channel

hsl是什么

颜色可以有哪几种表示方式

什么情况下颜色可以缩写

# Units&Size
