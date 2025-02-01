---
title: C++问题记录
abbrlink: bcb0fea1
date: 2025-01-26 21:01:12
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 并不是所有的对象都是new出来的

在使用了一年多C#之后,回头看C++时,有很多疑惑.首先是,在C#中,所有的对象都是需要new的.

看下面代码:
```
#include <iostream>
#include <string>

// 定义一个简单的类
class Person
{
private:
    std::string name;
    int age;

public:
    Person()
    {
        std ::cout << "执行无参构造函数" << std::endl;
    }

    // 构造函数，用于初始化对象
    Person(const std::string &n, int a) : name(n), age(a)
    {
        std::cout << "执行有参构造函数" << std::endl;
    }

    // 成员函数，用于显示对象信息
    void displayInfo()
    {
        std::cout << "Name: " << name << ", Age: " << age << std::endl;
    }

    // 析构函数，在对象销毁时自动调用
    ~Person()
    {
        std::cout << "Destroying person: " << name << std::endl;
    }
};

int main()
{
    // 在栈上创建 Person 类的对象
    Person person("Alice", 23);

    // 调用对象的成员函数
    person.displayInfo();

    // 在栈上创建 Person 类对象
    Person *p = new Person("Hank", 20);
    p->displayInfo();
    delete p;

    return 0;
}
```

看一下在栈上创建的对象的特点:
- 在栈上创建的对象不需要new,也不需要delete,其生命周期跟随其所在方法域的生命周期一致.
- 在栈上创建的对象不需要delete
- 在栈上创建的对象调用其字段和方法成员时,使用操作符`.`,在堆上创建的对象使用操作符`->`访问其成员.

[这篇文章](https://blog.csdn.net/qq_30835655/article/details/68938861)介绍了如何在C++中实现只能在堆上或者只能在栈上创建的对象
