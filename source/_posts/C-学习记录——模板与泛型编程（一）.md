---
title: C++学习记录——模板与泛型编程（一）
tags: C++学习记录
abbrlink: '10851814'
date: 2022-09-21 16:05:44
categories:
---

假定我们希望编写一个函数来比较两个值，并指出第一个值是小于、等于还是大于第二个值，在实际中，我们可能想要定义多个函数，没个函数比较给一种给定类型的值，我们的初次尝试可能是定义多个重载函数：

```c++
int compare(const string &v1, const string &v2){
  if(v1 < v2) return -1;
  if(v1 > v2) return 1;
  return 0;
}

int compare(const double &v1, const double &v2){
  if(v1 < v2) return -1;
  if(v1 > v2) return 1;
  return 0;
}
```

这两个函数几乎是相同的，唯一的差异是参数的类型，函数体则完全一样。

如果对每种希望比较的类型都不得不重复定义完全一样的函数体，是非常繁琐且容易出错的。更麻烦的是，在编写程序的时候，我们就要确定可能要compare的所有类型，如果希望能在用户提供的类型上使用此函数，这种策略就失效了。

# 函数模板

我们可以定义一个通用的**函数模板**(<code>function template</code>)，而不是为每个类型都定义一个新函数，一个函数模板就是一个公式，可以用来生成针对特定类型的函数版本。compare的模板版本可能就像下面这样：

```c++
template <typename T>
int compare(const T &v1, const T &v2){
  if(v1 < v2) return -1;
  if(v1 > v2) return 1;
  return 0;
}
```

模板定义以关键字<code>template</code>开始，后面跟一个**模板参数列表**<code>template parameter list</code>，这是一个逗号分隔的一个或多个模板参数的列表，用<code><</code>和<code>></code>包围起来。

模板参数列表的作用很像函数参数列表，函数参数列表定义了若干特定类型的局部变量，但并未指出如何初始化它们，在运行时，调用者提供实参来初始化形参。

类似的，模板参数表示在类或者函数定义中用到的类或值，当使用模板时，我们指定模板实参(<code>template argument</code>)，将其绑定到模板参数上。

后面书上的内容过于抽象，等看完类的相关部分再回来完成这一部分博客吧😓

