---
title: 在Mac和Windows上配置Java开发环境
tags:
  - Java
  - IDEA
categories: 技术文档
abbrlink: f57878ea
date: 2023-03-08 18:00:38
cover:
description:
swiper_index:
sticky:
---

# 背景

本文主要记录了在Mac和Windows上配置Java开发环境，创建并运行最简单的Java程序以及如何将单个java文件导入`IntelliJ IDEA`中运行

# Mac上配置JDK

Arm架构的Mac和X86架构的Mac安装的JDK是不一样的，这是JDK的[下载地址](https://www.oracle.com/java/technologies/downloads/#java11-mac)，我们要选择MacOS下的`Arm 64 Compressed Archive`进行下载。

下载完成之后，打开终端并输入：

```shell
vim ~/.bash_profile
```

打开文件后输入以下内容：

```she
# 配置JDK11 环境变量 
# JAVA_HOME：JDK安装路径（修改成你自己的）
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11/Contents/Home
# $JAVA_HOME/bin 下面是JDK的各种命令  
export PATH=$JAVA_HOME/bin:$PATH:.

```

输入`source ~/.bash_profile`使刚才配置的文件生效，接着输入`java -version`查看JDK是否安装成功 。到这里还没有结束，如果我们重启终端再输入`java -version`是会报错的，原因是重启后的终端并没有让`.bash_profile`配置文件生效，所以我需要在终端中执行`vim ~/.zshrc`，在最后一行添加`source ~/.bash_profile`，保存后退出，然后执行`source ~/.zshrc`，这样就可以让终端每次启动的时候自动将`./bash_profile`配置文件生效。

# 在Mac上不借助任何IDE运行Java脚本

<span id = "1">我们现在只会用到两个命令`java`和`javac`，我们编写一个`hello.java`文件，注意文件名要和类名一样：</span>

```java
public class hello{
  
  // main函数的拼写都是小写的，大小写敏感
  public static void main(String[] args){
    System.out.println("HelloWorld!");
  }
}
```

然后我们在终端中依次执行：

```shell
# javac命令会将.java文件编译成class文件
javac hello.java
# java命令会运行class文件
java hello
```



# 使用IntelliJ IDEA打开并运行java文件

## 背景

我使用记事本或者`Sublime`等Text Editor编写了一个java文件，我想要在IDEA中运行这个java程序，我该怎么做？

## IntelliJ IDEA中java项目的文件结构

我先使用IDEA创建一个非常简单的Java项目，项目的文件目录结构如下：

![](在Mac和Windows上配置Java开发环境/image-20230310111924493.png)

- `.idea`目录和`iml`文件是IDEA的配置文件，可以隐藏
- `src`目录是代码源文件存放的目录
- `out`是Java程序的输出目录，存放字节码文件
- `external Libraries`是JDK的扩展类库

## 使用IDEA运行单个java程序

想要使用IDEA运行java项目，就需要这个项目能够符合IDEA项目的文件结构，有两种方法可以将单个java文件放入IDEA中运行：

**方法一**

在IDEA中选择`File`->`Open`->打开java文件所在的文件夹

![](在Mac和Windows上配置Java开发环境/截屏2023-03-1016.55.36.png)

打开文件夹后点击`File`->`Project Structure`，为项目配置`jdk`和`output`文件夹，点击Apply，返回项目就可以运行了。

![](在Mac和Windows上配置Java开发环境/截屏2023-03-1017.15.25.png)

**方法二**

直接在IDEA中新建项目，选择不勾选添加示例代码。

![](在Mac和Windows上配置Java开发环境/截屏2023-03-1017.22.02.png)

项目打开后将想要运行的java代码拖到src中去，然后就可以运行了。

# Java:main()函数的一些知识点

上文中已经展示了一个[最简单的Java程序](#1)，`main`函数的前面有两个属性：`public`和`static`，两个属性是必须要有的，否则Java程序将无法正常运行。

## 为什么要static

Main方法是Java程序的入口，JVM会查找类中的`public static void main(String[] args)`，如果找不到该方法就会抛出错误`NoSuchMethodError:main`终止程序Main方法必须严格遵循它的语法规则，方法签名必须是public static void，参数类型是String类型，在Java1.5以及以后的版本还可以使用可变的参数：`public static void main(String ... args)`。

JVM调用main方法的以后不需要创建任何包含这个main方法的实例，如果main方法不声明为静态的，JVM就必须创建main类的实例，因为构造器可以被重载，JVM没办法确定调用的是哪个main方法。静态方法和静态数据加载到内存中就可以直接调用而不需要像实例方法一样创建实例后才可以被调用，如果main方法是静态的，那么它就会被加载到JVM上下文中成为可执行的方法。

## 为什么要public

Java指定了一些可访问的修饰符，比如private、protected、public，任何方法或者变量都可以声明为public，Java可以从该类之外的地方访问。因为main方法是公共的，JVM就可以轻松的访问和执行它。

### 为什么是void

因为main返回任何值对程序都没有任何意义，所以设置成void。



# JDK JVM JRE的区别与联系
