---
title: 在Mac和Windows上配置Java开发环境
date: 2023-03-08 18:00:38
tags:
  - Java
categories: 开发日志
cover:
description: 
swiper_index:
sticky:
---

# 背景





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

我们现在只会用到两个命令`java`和`javac`，我们编写一个`hello.java`文件，注意文件名要和类名一样：

```java
public class hello{
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

# JDK JVM JRE的区别与联系
