---
title: MacOS与WindowsOpenGL开发环境搭建
date: 2024-04-02 19:10:32
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

本篇博客记录以[JoeyDeVries](https://github.com/JoeyDeVries)的[OpenGL教程](https://learnopengl.com/Introduction)为指导在`MacOS`和`Windows`系统上搭建OpenGL的过程和疑惑。

# 前置知识

## OpenGL是什么？

OpenGL十分类似面向对象编程思想中的接口和虚方法，它只是定义了一个框架，而没有任何具体的实现。所有的实现需要显卡生产厂商自己实现。可以说OpenGL是一套规范。

## GLFW和GLAD

GLFW是一个专门针对OpenGL的C语言库，它提供了一些渲染物体所需的最低限度的接口。它允许用户创建OpenGL上下文、定义窗口参数以及处理用户输入。

因为OpenGL只是一个标准/规范，具体的实现是由驱动开发商针对特定显卡实现的。由于OpenGL驱动版本众多，它大多数函数的位置都无法在编译时确定下来，需要在运行时查询。所以任务就落在了开发者身上，开发者需要在运行时获取函数地址并将其保存在一个函数指针中供以后使用。
GLAD是一个开源的库，它能解决我们上面提到的那个繁琐的问题。GLAD的配置与大多数的开源库有些许的不同，GLAD使用了一个在线服务。在这里我们能够告诉GLAD需要定义的OpenGL版本，并且根据这个版本加载所有相关的OpenGL函数。

## Clang是什么 LLVM是什么？


# 准备工作

在参考的[OpenGL教程](https://learnopengl-cn.github.io/01%20Getting%20started/02%20Creating%20a%20window/)中使用的开发平台是VisualStudio，但是Mac系统上的VisualStudio不能进行C++开发，为了在Mac和Windows上保持相同的开发体验，双平台统一使用`VSCode` + `CMake` + `Clang`的开发方案。

## 安装[VSCode](https://code.visualstudio.com/)

## 安装[CMake](https://cmake.org/)

## 编译GLFW源码

去到[GLFW官网](https://www.glfw.org/download.html)下载GFLW源码，解压下载好的源码文件，在终端中进入源码的根目录，创建名为`build`的路径

```
mkdir build
```

要使我们编写的程序正确地调用GLFW库中的方法，我们可以使用动态链接或者静态链接GLFW库的方式，选择任意一种方式就可以，在这里我把两种方式都尝试了一下。

使用静态链接方式要生成静态链接库，Windows下对应的文件是.lib，Mac下对应的文件是.a

CMake可以指定将源码编译成何种类型的构建系统，Windows下默认构建的是VisualStudio工程，这并不是我们想要的，因此在调用cmake指令的时候要手动指定要构建的系统
  
```
cmake .. -G "Unix Makefiles"
```
MacOS下默认构建的系统就是类Unix的Makefile，因此可以不指定。

接着使用`make`指令

使用动态链接的方式要生成动态链接库，Windows下对应的文件是.dll，Mac下对应的文件是.dylib，动态链接库的编译命令和静态链接库是一样的，但是需要指定一下`DBUILD_SHARED_LIBS`参数，还要注意Windows下指定要构建的系统

```
cmake .. -G "Unix Makefiles" -DBUILD_SHARED_LIBS=ON
```
接着使用`make`指令

对于MacOS系统，还使用`make install`命令可以把动态链接库安装到本机，可能需要管理员权限。动态库将会被安装到`usr/local/lib`目录，头文件安装到`usr/local/include`。

执行完上面的操作之后，就可以在`build`目录的`src`目录下看到对应的链接库文件了

## 下载GLAD

尝试一下在Windows上编译成dylib文件可不可以运行

## VSCodeCoding体验优化

### 更改设置

使用'Command + ,' 命令可以打开设置页面,在设置页面的右上角选择使用Json模式打开, 将下面的文本粘贴进去

```
"files.autoSave": "afterDelay", // 设置自动保存
"files.autoGuessEncoding": true, // 
"workbench.list.smoothScrolling": true, // 动画相关
"editor.cursorSmoothCaretAnimation": "on", // 动画相关
"editor.smoothScrolling": true, 
"editor.cursorBlinking": "smooth",
"editor.mouseWheelZoom": true,
"editor.formatOnPaste": true,
"editor.formatOnType": true,
"editor.formatOnSave": true,
"editor.wordWrap": "on",
"editor.guides.bracketPairs": true,
"editor.suggest.snippetsPreventQuickSuggestions": false,
"editor.acceptSuggestionOnEnter": "smart",
"editor.suggestSelection": "recentlyUsed",
"window.dialogStyle": "custom",
"debug.showBreakpointsInOverviewRuler": true,
"editor.fontFamily": "JetBrains Mono",
"git.ignoreMissingGitWarning": true,
"explorer.confirmDelete": false,
"cmake.configureOnOpen": false,
```

#### 插件

MarkDown插件
Markdown Preview Enhanced
Markdown All in One

代码截图工具 CodeSnap

图标主题

代码格式化工具 Prettier - Code formatter

主题推荐 [OneDarkPro](https://marketplace.visualstudio.com/items?itemName=zhuangtongfa.Material-theme)
#### 字体
字体推荐 [JetBrains Mono](https://www.jetbrains.com/lp/mono/)
#### VSCode自动补全


#### VSCode报红问题


## Windows准备工作

### 安装[Visual Studio](https://visualstudio.microsoft.com/zh-hans/)

### GLAD准备工作

去GLAD在线网站

### GLFW准备工作


问题描述：在设置完自动补全之后 点击头文件能打开 但是

编译GLFW


# OpenGL环境搭建

MacOS编写CMakeLists

cmake .. 是干什么？

make 是干什么？


# 测试搭建环境

参考OpenGL教程编写代码

编写可以在Mac和Windows上通用的CMakeList

```
cmake_minimum_required(VERSION 3.10)
project(HelloWindow VERSION 0.1.0)

# 设置源代码的目录
set(SOURCE_DIR "${PROJECT_SOURCE_DIR}/src")

# 设置头文件目录
set(INCLUDE_DIR "${PROJECT_SOURCE_DIR}/../../include")

# 设置lib目录
set(LIBRARY_DIR "${PROJECT_SOURCE_DIR}/../../lib")

# 添加头文件目录
include_directories(${INCLUDE_DIR})

# 找到所有的源代码
file(GLOB SOURCES "${SOURCE_DIR}/main.cpp" "${SOURCE_DIR}/glad.c")

add_executable(${PROJECT_NAME} ${SOURCES})
target_link_libraries(${PROJECT_NAME} "${LIBRARY_DIR}/libglfw.3.dylib")

```

创建build目录



# 参考文章
[OpenGL中文教程](https://learnopengl-cn.github.io)

[OpenGL英文教程](https://learnopengl.com/)

[VSCode+CMake搭建OpenGL开发环境](https://huosk.github.io/2019/12/12/OpenGLDevWithVSCode-CMake/)

[How to build GLFW on Mac OSX 10.13 for use in xcode](https://fdhenard.github.io/build_glfw_on_osx.html)
