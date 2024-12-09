---
title: OpenGL学习记录——入门篇
abbrlink: a627ba0f
date: 2024-12-10 1:00:57
tags: OpenGL
categories: 图形学
cover: https://www.notion.so/images/page-cover/woodcuts_sekka_1.jpg
description:
swiper_index:
sticky:
---

笔者在网上能找到的C++与OpenGL的教程几乎都是基于Windows+VisualStudio展开的，也因为其直观和简易的环境配置流程，让初学者更专注于学习知识本身。这对作为Mac用户的笔者就很不友好了，需要额外了解CMake的工作原理、要选什么样的编译器等等诸如此类的，都是笔者要去搞清楚的点。

# C++基础

## 头文件和CPP文件的区别

在(游戏引擎架构)[https://book.douban.com/subject/25815142/]这本书中，介绍了头文件和CPP文件的区别：头文件通常用于在多个翻译单元之间共享信息，例如类型的声明以及函数原型。C++编译器并不了解头文件，实际情况是，C++预处理器预先把每个#include语句替换成相应的头文件内容，然后再把翻译单元送交源一起。这是头文件和源文件之间一个细微但是非常重要的区别。从程序员的角度看，头文件是独立的文件，但多亏了有预处理器把头文件展开，编译器接收到的才都是翻译单元。翻译单元的定义是：因为编译器每次只编译一个C++源文件至机器码，所以在技术上，源文件被称为翻译单元。那么像Mac上用的clang就是编译器的前端，负责预编译和编译的环节，

## include后""和<>的区别

比如在OpenGL入门篇的[变换](https://learnopengl-cn.github.io/01%20Getting%20started/07%20Transformations/#_20)一节中，我们使用到了一个名为GLM的库，

## C++代码的执行过程

clang是编译器前端，通过clang，
预编译、编译、汇编、链接，每一步都在干什么呢？
cmake install或者ninja install又是在干什么呢？

# Hello Window

入门篇的[你好,窗口](https://learnopengl-cn.github.io/01%20Getting%20started/03%20Hello%20Window/)章节在笔者配置OpenGL开发环境那一篇博客中已经部分介绍了，不过两篇文章的侧重点不同，本篇文章的侧重点是对每一行教程中的代码和重要概念进行解读。另外，如果我们对某些函数的描述不清楚，我们可以直接去看GLFW的源码，或者查看文档

## GLFW和GLAD

想要使用OpenGL进行渲染，我们需要两个库：GLFW与GLAD。GLFW是一个专门针对OpenGL的C语言库，


## 接口使用

### glfwInit()

```
glfwInit(); // 初始化glfw
```

### glViewport (GLint x, GLint y, GLsizei width, GLsizei height)

### GLFWAPI GLFWwindow* glfwCreateWindow(int width, int height, const char* title, GLFWmonitor* monitor, GLFWwindow* share)

这两个接口笔者想要放在一起介绍,因为这两个接口引入了两个相关的概念:窗口(Window)与视口(Viewport).接下来笔者会用一些实操来直观地带各位区分二者的区别.前提是你需要完成Hello Triangle章节的学习.

首先,我们注释掉代码中glViewport接口的调用,你的代码中可能有两处(如果你是按照中文教程学习的话),一处在`main`方法内部,另一处在调用`glfwSetFramebufferSizeCallback`接口时传入的回调方法里.然后,我们将在脚本中声明的三角形顶点数组内容修改成下面的值:
```
float vertices[] = {
        1.0f, 1.0f, 0.0f,  // 右上角
        1.0f, -1.0f, 0.0f, // 右下角
        -1.0f, 1.0f, 0.0f, // 左上角
    };
```
最后,我们可以选择将`glCreateWindow`中窗口的宽度和高度改的大一些,比如改成`1920 * 1080`,这是笔者显示器的分辨率.重新构建我们的程序你会发现,我们的程序生成了一个铺满我们整个屏幕的窗口,而且窗口中的这个三角形铺满了我们整个窗口的右上部分屏幕!由此我们可以得出第一个结论:我们定义的三角形的顶点位置和窗口的实际大小之间存在一层映射关系.而当我们拖拽窗口的时候,这个三角形的形状会随着窗口形状的变化而变化.
如果我们想要让三角形的形状固定,不会随着窗口形状的变化而变化,该怎么做呢?那我们就要使用`glViewPort`接口了.`glViewport`接口的的前两个参数代表的是`视口`左下角距离相对于`窗口`左下角的坐标.而第三个和第四个参数代表的是视口的大小.现在你可能还不清楚视口和窗口的区别,没关系,跟着笔者一起操作,取消注释代码中的`glViewport`接口,并设置其接口的参数值如下:
```
glViewport(600, 400, 200, 200);
```
重新构建程序,你会发现,我们的三角形变得好小!这个三角形变成了变长为200*200的等腰直角三角形!和`glViewport`接口的第三和第四个参数的值是一样的,而且当我们调整窗口大小的时候,注意:我们观察的是鼠标左键抬起后三角形的形状和位置的变化,你会发现这个三角形相对于窗口的左下角的位置没有发生变化!到这里,视口和窗口的区别与联系应该明了了:我们渲染的三角形并不是渲染在窗口上的,而是渲染在视口上的,默认情况下如果我们不调用`glViewport`设置视口的位置尺寸,视口的大小和窗口的大小相等,而如果调用了`glViewport`显式指定的视口的大小,那么视口与窗口的区别便会显现.

### 其他

```
void glfwWindowHint	(int hint,
                    int value 
                    )		
```
This function sets hints for the next call to glfwCreateWindow. The hints, once set, retain their values until changed by a call to this function or glfwDefaultWindowHints, or until the library is terminated.

Window hints need to be set before the creation of the window and context you wish to have the specified attributes. They function as additional arguments to glfwCreateWindow.

```

GLFWwindow * glfwCreateWindow(int width,
                            int height,
                            const char * title,
                            GLFWmonitor * monitor,
                            GLFWwindow * share 
                            )	
```
This function creates a window and its associated OpenGL or OpenGL ES context. Most of the options controlling how the window and its context should be created are specified with window hints.

```
void glfwMakeContextCurrent	(GLFWwindow * window)
```

This function makes the OpenGL or OpenGL ES context of the specified window current on the calling thread. It can also detach the current context from the calling thread without making a new one current by passing in NULL.

我们使用glfwCreateWindow()

# 重要概念

- VAO: Vertex Array Object, 顶点数组对象
- VBO: Vertex Buffer Object, 顶点缓冲对象
- EBO: Element Buffer Object, 元素缓冲对象
- IBO: Index Buffer Object, 索引缓冲对象