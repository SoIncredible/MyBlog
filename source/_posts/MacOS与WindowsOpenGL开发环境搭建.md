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
在参考的[OpenGL教程](https://learnopengl-cn.github.io/01%20Getting%20started/02%20Creating%20a%20window/)中使用的开发平台是Windows系统上的VisualStudio，但是Mac系统上的VisualStudio不能进行C++开发，为了在Mac和Windows上保持相同的开发体验，我决定在MacOS和Windows上统一使用`VSCode` + `CMake` + `Ninja` + `Clang`的开发方案。

# 前置知识

## OpenGL是什么？

OpenGL十分类似面向对象编程思想中的接口和虚方法，它只是定义了一个框架，而没有任何具体的实现。所有的实现需要显卡生产厂商自己实现。可以说OpenGL是一套规范。

## GLFW和GLAD

GLFW是一个专门针对OpenGL的C语言库，它提供了一些渲染物体所需的最低限度的接口。它允许用户创建OpenGL上下文、定义窗口参数以及处理用户输入。

因为OpenGL只是一个标准/规范，具体的实现是由驱动开发商针对特定显卡实现的。由于OpenGL驱动版本众多，它大多数函数的位置都无法在编译时确定下来，需要在运行时查询。所以任务就落在了开发者身上，开发者需要在运行时获取函数地址并将其保存在一个函数指针中供以后使用。
GLAD是一个开源的库，它能解决我们上面提到的那个繁琐的问题。GLAD的配置与大多数的开源库有些许的不同，GLAD使用了一个在线服务。在这里我们能够告诉GLAD需要定义的OpenGL版本，并且根据这个版本加载所有相关的OpenGL函数。

## C++工具链

将C++从源代码变成可执行程序需要经过四个基本步骤：预编译、编译、汇编、链接。每一步都需要有相应的工具支持，将在C++编译成可执行程序过程中每个环节使用到的工具放在一起，就称这些工具组成了一套C++工具链。目前主流的C++工具链有GNU、LLVM和MSVC等工具链。

## [Clang和LLVM的介绍](https://llvm.org/)

## [Ninja-build](https://ninja-build.org/)介绍

# 准备工作

## 安装[VSCode](https://code.visualstudio.com/)

## 安装[CMake](https://cmake.org/)

Mac上也可以使用Homebrew进行安装

```
brew install cmake
```

## 安装Ninja

1. Mac上使用Homebrew安装Ninja

```
brew install ninja
```

2. Windows上可以在github上的[ninja主页](https://github.com/ninja-build/ninja/releases)进行下载，将下载完的文件的根目录添加到环境变量中

在终端中运行`ninja --version`如果正确显示版本号就说明ninja安装成功了。

## 安装clang

1. Mac上默认使用的C++编译器就是clang 不需要额外下载
2. Windows去到[LLVM](https://github.com/llvm/llvm-project/releases)的Github主页上下载 下载LLVM-win64版本。将下载好的目录下的bin目录设置为环境变量。在终端中运行`clang --verison`如果正确显示版本号说明clang安装成功。

## 编译GLFW源码

去到[GLFW官网](https://www.glfw.org/download.html)下载GFLW源码，解压下载好的源码文件，在终端中进入源码的根目录，创建名为`build`的路径

```
mkdir build
```

要使我们编写的程序正确地调用GLFW库中的方法，我们可以使用动态链接或者静态链接GLFW库的方式，优先选择静态链接的方式，因为动态链接的方式需要额外指定一下参数，而且编译成功后运行可执行文件时还有找不到动态链接库的问题，具体细节会在下文会具体阐述。

使用静态链接方式要生成静态链接库，Windows下对应的文件是.lib，Mac下对应的文件是.a

CMake可以指定使用何种类型的构建系统，Windows下默认的构建系统是VisualStudio工程，这并不是我们想要的，因此在调用cmake指令的时候要手动指定要构建的系统。另外Windows下默认使用的编译器是VisualStudio编译器，我们也需要指定为clang编译器，所以cmake的完整指令如下：
  
```
cmake .. -G Ninja -D CMAKE_C_COMPILER=clang -D CMAKE_CXX_COMPILER=clang++
```

接着使用`ninja`指令，如果编译成功的话，我们就能在build目录下的src目录下找到对应平台的静态链接库文件了。

使用动态链接的方式要生成动态链接库，Windows下对应的文件是.dll，Mac下对应的文件是.dylib，动态链接库的编译命令和静态链接库是一样的，但是需要指定一下`DBUILD_SHARED_LIBS`参数，注意指定一下要构建的系统，另外还要注意Windows下指定使用的编译器

```
cmake .. -G Ninja -DBUILD_SHARED_LIBS=ON -D CMAKE_C_COMPILER=clang -D CMAKE_CXX_COMPILER=clang++
```
接着使用`ninja`指令

对于MacOS系统，还使用`make install`命令可以把动态链接库安装到本机，可能需要管理员权限。动态库将会被安装到`usr/local/lib`目录，头文件安装到`usr/local/include`。

执行完上面的操作之后，就可以在`build`目录的`src`目录下看到对应的平台的动态链接库文件了。

## 下载GLAD

使用GLAD的[在线服务](https://glad.dav1d.de/)下载所需要的文件。API栏目下的gl的版本一般选择最新的就可以，Profile栏目选择Core，其他的不用配置，点击Generate之后下载生成的压缩包到本地。解压压缩包，文件结构应该如下：
```
.
├── include
│   ├── KHR
│   │   └── khrplatform.h
│   └── glad
│       └── glad.h
└── src
    └── glad.c

```

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

问题描述：在设置完自动补全之后 点击头文件能打开 但是

编译GLFW


# OpenGL环境搭建



# 测试搭建环境

## 编写HelloWindow程序
参考OpenGL教程的[GettingStart\HelloWindow](https://learnopengl-cn.github.io/01%20Getting%20started/03%20Hello%20Window/)章节
```


```


## 编写CMakeList

```
# 指定需要的CMake的最小版本
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


# 参考文章
[OpenGL中文教程](https://learnopengl-cn.github.io)

[OpenGL英文教程](https://learnopengl.com/)

[VSCode+CMake搭建OpenGL开发环境](https://huosk.github.io/2019/12/12/OpenGLDevWithVSCode-CMake/)

[How to build GLFW on Mac OSX 10.13 for use in xcode](https://fdhenard.github.io/build_glfw_on_osx.html)
