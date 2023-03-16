---
title: Java基础语法
tags:
  - Java
categories: 学习笔记
cover: 'http://soincredible777.com.cn:90/23.png'
abbrlink: ea538f2a
date: 2023-03-14 19:56:33
description:
swiper_index:
sticky:
---

一个Java程序可以认为是一系列对象的集合，而这些对象通过彼此调用的方法来协同工作，下面介绍类、对象、方法、实例变量的概念

- **对象**：对象是类的一个实例，有状态和行为。例如，一条狗是一个对象，它的状态有：颜色、名字、品种；行为有：摇尾巴、叫、吃等。
- **类**：类是一个模板，它描述一类对象的行为和状态。
- **方法**：方法就是行为，一个类可以有很多方法。逻辑运算、数据修改以及所有动作都是在方法中完成的。
- **实例变量**：每个对象都有独特的实例变量，对象的状态由这些实例变量的值决定。

# 第一个Java程序

```java
public class HelloWorld {
    /* 第一个Java程序
     * 它将输出字符串 Hello World
     */
    public static void main(String[] args) {
        System.out.println("Hello World"); // 输出 Hello World
    }
}
```

![](https://www.runoob.com/wp-content/uploads/2013/12/662E827A-FA32-4464-B0BD-40087F429E98.jpg)

如何不借助任何IDE运行java程序在[这篇博客](http://soincredible777.com.cn/posts/f57878ea/)中已经介绍的十分详细了

# 基本语法

编写Java程序时，应该注意以下几点：

- 大小写敏感：Java是大小写敏感的，这就意味着标识符Hello和hello是不同的。
- 类名：对于所有类来说，类名的首字母应该大些。如果类名由若干单词组成，那么每个单词的首字母应该大写，例如：MyFirstJavaClass
- 方法名：所有的方法名都应该以小写字母开头。如果方法名含有若干单词，则后面的每个单词首字母大写，驼峰原则（Camel-Case）。
- 源文件名：源文件名必须和类名相同。当保存文件的时候，你应该使用类名作为文件名保存（切记Java是大小写敏感的），文件名的后缀为.`java`，如果文件名和类名不相同则会导致编译错误
- 主方法入口：所有Java程序由`public static void main(String[] args)`方法开始执行。

# Java标识符

Java所有的组成部分都需要名字。类名、变量名以及方法名都被称为标识符。

关于Java标识符，有以下几点需要注意：

- 所有的标识符都应该是以字母（A-Z或者a-z）、美元符号（$）或者下划线（_）开始。
- 首字符之后可以是字母、美元符号、下划线或数字的任何字符组合。
- 关键字不能用作标识符。
- 标识符是大小写敏感的。
- 合法标识符举例：age、¥salary、_value、__1_value
- 非法标识符举例：123abc、- salary

# Java修饰符

像其他语言一样，Java可以使用修饰符来修饰类中的方法和属性，主要有两类修饰符：

- 访问控制修饰符：`default`、`public`、`protected`、`private`
- 非访问控制修饰符：`final`、`abstract`、`static`、`synchronized`

# Java变量

Java中主要有如下几种类型的变量

- 局部变量
- 类变量（静态变量）
- 成员变量（非静态变量）

# Java数组

数组是存储在堆上的对象，可以保存多个同类型的变量。在后面的章节中，我们将学会如何声明、构造以及初始化一个数组。

# Java枚举

Java5.0引入了枚举，枚举限制变量只能是预先设定好的值。使用枚举可以减少代码中的bug。

例如，我们为果汁店设计一个程序，它将限制果汁为小杯、中杯、大杯。这就意味着它不允许顾客点除了这三种尺寸外的果汁。

## 实例

```java
class FreshJuice {
   enum FreshJuiceSize{ SMALL, MEDIUM , LARGE }
   FreshJuiceSize size;
}
 
public class FreshJuiceTest {
   public static void main(String[] args){
      FreshJuice juice = new FreshJuice();
      juice.size = FreshJuice.FreshJuiceSize.MEDIUM  ;
   }
}
```

**注意：**枚举可以单独声明或者声明在类里面。方法、变量、构造函数也可以在枚举中定义。






# Java注释

类似于C/C++、Java也支持单行以及多行注释，注释中的字符将被Java编译器忽略。

```java
public class HelloWorld {
   /* 这是第一个Java程序
    * 它将输出 Hello World
    * 这是一个多行注释的示例
    */
    public static void main(String[] args){
       // 这是单行注释的示例
       /* 这个也是单行注释的示例 */
       System.out.println("Hello World"); 
    }
}
```

# Java空行

空白行或者有注释的行，Java编译器都会忽略掉。

# 继承

在Java中，一个类可以由其他派生类派生。如果你要创建一个类，而且已经存在一个类具有你所需要的属性或方法，那么你可以将新建的类继承该类。

利用继承的方法，可以重用已存在类的方法和属性，而不用重写这些代码。被继承的类称为超类（super class），派生类称为子类（sub class）

# 接口

在Java中，接口可以理解为对象间通信的协议。接口在继承中扮演着很重要的角色。

接口只定义派生要用到的方法，但是方法的具体实现完全取决于派生类。

# Java源程序与编译型程序的区别

![](https://www.runoob.com/wp-content/uploads/2013/12/ZSSDMld.png)
