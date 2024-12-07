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


# 重要概念

⭕️ 遗留问题，三角形的生成一开始不是在屏幕的中心 为什么？
⭕️ CMake如何把我需要的脚本关联起来的？
- VAO: Vertex Array Object, 顶点数组对象
- VBO: Vertex Buffer Object, 顶点缓冲对象
- EBO: Element Buffer Object, 元素缓冲对象
- IBO: Index Buffer Object, 索引缓冲对象

# 实践

shader.h
```
#ifndef SHADER_H
#define SHADER_H

#include <glad/glad.h>

#include <string>
#include <fstream>
#include <sstream>
#include <iostream>

class Shader
{
public:
    // 程序ID
    unsigned int ID;

    // 构造器读取并构建着色器
    Shader(const char *vertexPath, const char *fragmentPath)
    {
        // 1. 从文件路径中获取顶点/片段着色器
        std::string vertexCode;
        std::string fragmentCode;
        std::ifstream vShaderFile;
        std::ifstream fShaderFile;
        // 保证ifstream对象可以抛出异常：
        vShaderFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
        vShaderFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
        try
        {
            // 打开文件
            vShaderFile.open(vertexPath);
            fShaderFile.open(fragmentPath);
            std::stringstream vShaderStream, fShaderStream;
            // 读取文件的缓冲内容到数据流中
            vShaderStream << vShaderFile.rdbuf();
            fShaderStream << fShaderFile.rdbuf();
            // 关闭文件处理器
            vShaderFile.close();
            fShaderFile.close();
            // 转换数据流到string
            vertexCode = vShaderStream.str();
            fragmentCode = fShaderStream.str();

            log(vertexCode);
            log(fragmentCode);
        }
        catch (std::ifstream::failure e)
        {
            std::cout << "ERROR::SHADER::FILE_NOT_SUCCESSFULLY_READ" << std::endl;
        }
        const char *vShaderCode = vertexCode.c_str();
        const char *fShaderCode = fragmentCode.c_str();

        // 2. 编译着色器
        unsigned int vertex, fragment;
        int success;
        char infoLog[512];

        // 顶点着色器
        vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, 1, &vShaderCode, NULL);
        glCompileShader(vertex);
        // 打印编译错误（如果有的话）
        glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
        if (!success)
        {
            glGetShaderInfoLog(vertex, 512, NULL, infoLog);
            std::cout << "ERROR::SHADER::VERTEX::COMPILATION_FAILED\n"
                      << infoLog << std::endl;
        }

        // 片元着色器 如果有的话
        fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, 1, &fShaderCode, NULL);
        glCompileShader(fragment);
        // 打印编译错误（如果有的话）
        glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
        if (!success)
        {
            glGetShaderInfoLog(fragment, 512, NULL, infoLog);
            std::cout << "ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n"
                      << infoLog << std::endl;
        }

        // 着色器程序
        ID = glCreateProgram();
        glAttachShader(ID, vertex);
        glAttachShader(ID, fragment);
        glLinkProgram(ID);

        // 打印链接错误(如果有的话)
        glGetProgramiv(ID, GL_LINK_STATUS, &success);
        if (!success)
        {
            glGetProgramInfoLog(ID, 512, NULL, infoLog);
            std::cout << "ERROR::SHADER::PROGRAM::LINKING_FAILED\n"
                      << infoLog << std::endl;
        }

        // 删除着色器，他们已经链接到我们的程序中了，已经不再需要了
        glDeleteShader(vertex);
        glDeleteShader(fragment);

        log("shader program init success !!");
    }
    // 使用/激活程序
    void use()
    {
        glUseProgram(ID);
    }
    // uniform工具函数
    void setBool(const std::string &name, bool value) const
    {
        glUniform1i(glGetUniformLocation(ID, name.c_str()), (int)value);
    }
    void setInt(const std::string &name, int value) const
    {
        glUniform1i(glGetUniformLocation(ID, name.c_str()), value);
    }
    void setFloat(const std::string &name, float value) const
    {
        glUniform1f(glGetUniformLocation(ID, name.c_str()), value);
    }

private:
    void log(std::string logInfo)
    {
        std::cout << logInfo << std::endl;
    }
};

#endif
```

shader.vs
```
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;

out vec3 ourColor;

void main(){
    gl_Position = vec4(aPos, 1.0);
    ourColor = aColor;
}
```

shader.fs
```
#version 330 core
out vec4 FragColor;

in vec3 ourColor;

void main(){
    FragColor = vec4(ourColor, 1.0);
}
```

```
#include <glad/glad.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include "shader.h"

void framebuffer_size_callback(GLFWwindow *window, int width, int height);
void processInput(GLFWwindow *window);

int main()
{
    glfwInit();
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

    GLFWwindow *window = glfwCreateWindow(800, 600, "Shaders", NULL, NULL);

    if (window == NULL)
    {
        std::cout << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return -1;
    }

    glfwMakeContextCurrent(window);

    if (!gladLoadGLLoader((GLADloadproc)glfwGetProcAddress))
    {
        std::cout << "Failed to initialize GLFW window" << std::endl;
        glfwTerminate();
        return -1;
    }

    glfwMakeContextCurrent(window);

    if (!gladLoadGLLoader((GLADloadproc)glfwGetProcAddress))
    {
        std::cout << "Failed to initialize GLAD" << std::endl;
        return -1;
    }

    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);
    glViewport(0, 0, 800, 600);

    // 创建要绘制的顶点
    float vertices[] = {
        // 位置              // 颜色
        -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f};

    Shader ourShader("../src/shaders/shader.vs", "../src/shaders/shader.fs");

    // 创建VBO和VAO
    unsigned int VBO;
    unsigned int VAO;
    glGenBuffers(1, &VBO);
    glGenVertexArrays(1, &VAO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBindVertexArray(VAO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    // 位置属性
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void *)0);
    glEnableVertexAttribArray(0);
    // 颜色属性
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void *)(3 * sizeof(float)));
    glEnableVertexAttribArray(1);

    // 进循环
    while (!glfwWindowShouldClose(window))
    {
        processInput(window);
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        ourShader.use();
        // 绘制三角形
        glBindVertexArray(VAO);
        glDrawArrays(GL_TRIANGLES, 0, 3);

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    glDeleteVertexArrays(1, &VAO);
    glDeleteBuffers(1, &VBO);

    glfwTerminate();
    return 0;
}

void framebuffer_size_callback(GLFWwindow *window, int width, int height)
{
    glViewport(0, 0, width, height);
}

void processInput(GLFWwindow *window)
{
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
    {
        glfwSetWindowShouldClose(window, true);
    }
}
```