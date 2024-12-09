---
title: OpenGL学习记录——入门篇
abbrlink: a627ba0f
date: 2024-04-05 15:32:57
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


## 方法介绍

```
glfwInit(); // 初始化glfw
```

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

```
glViewport(1, 2, 3, 4);
```

```
glfwCreateWindow();
```
前两个参数代表要创建的窗口的宽和高,第三个参数代表窗口的名称,第四个和第五个参数我们先忽略.
如果我们不使用glViewport接口,也不给glfwSetFramebufferSizeCallback传入回调的话,运行我们的程序,当我们拖拽窗口的时候,我们生成的图形就会随着窗口的比例缩放,有时这并不是我们想要的.因此,我们要使用glViewPort接口的话
glViewport的前两个参数代表的是视口左下角距离相对于窗口左下角的坐标.而第三个和第四个参数代表的是视口的大小,那么如果我不想让我渲染的图形收到窗口非等比例缩放的影响的话,我需要添加回调

好了,现在看起来我们的窗口在拖拽过程中只要不松手,我们渲染的内容就不会更新.

glCreateWindow与glViewport是两个决定窗口大小相关的方法
```

```

我们使用glfwCreateWindow()

# 重要概念

⭕️ 遗留问题，三角形的生成一开始不是在屏幕的中心 为什么？
⭕️ CMake如何把我需要的脚本关联起来的？
- VAO: Vertex Array Object, 顶点数组对象
- VBO: Vertex Buffer Object, 顶点缓冲对象
- EBO: Element Buffer Object, 元素缓冲对象
- IBO: Index Buffer Object, 索引缓冲对象

# 实践