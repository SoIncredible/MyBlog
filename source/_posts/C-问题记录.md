---
title: C++问题记录
abbrlink: bcb0fea1
date: 2024-01-26 21:01:12
tags:
categories:
cover: https://www.notion.so/images/page-cover/met_vincent_van_gogh_irises.jpg
description:
swiper_index: 3
sticky: 3
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

# Warning: treating 'c-header' input as 'c++-header' when in C++ mode, this behavior is deprecated

比如一个cpp文件中引用了某一个头文件,在编译的时候就没必要把这个头文件列出来.

https://stackoverflow.com/questions/23534362/warning-treating-c-header-input-as-c-header-when-in-c-mode-this-behavi
# C++宏的使用 和C#宏的使用有什么区别?

# C++模板和C#范型(generic)之间的区别

C# 泛型和 C++ 模板均是支持参数化类型的语言功能。 但是，两者之间存在很多不同。 在语法层次，C# 泛型是参数化类型的一个更简单的方法，而不具有 C++ 模板的复杂性。 此外，C# 不试图提供 C++ 模板所具有的所有功能。 在实现层次，主要区别在于 C# 泛型类型的替换在运行时执行，从而为实例化对象保留了泛型类型信息.

以下是 C# 泛型和 C++ 模板之间的主要差异：

C# 泛型的灵活性与 C++ 模板不同。 例如，虽然可以调用 C# 泛型类中的用户定义的运算符，但是无法调用算术运算符。

C# 不允许使用非类型模板参数，如 template C<int i> {}。

C# 不支持显式定制化；即特定类型模板的自定义实现。

C# 不支持部分定制化：部分类型参数的自定义实现。

C# 不允许将类型参数用作泛型类型的基类。

C# 不允许类型参数具有默认类型。

在 C# 中，泛型类型参数本身不能是泛型，但是构造类型可以用作泛型。 C++ 允许使用模板参数。

C++ 允许在模板中使用可能并非对所有类型参数有效的代码，随后针对用作类型参数的特定类型检查此代码。 C# 要求类中编写的代码可处理满足约束的任何类型。 例如，在 C++ 中可以编写一个函数，此函数对类型参数的对象使用算术运算符 + 和 -，在实例化具有不支持这些运算符的类型的模板时，此函数将产生错误。 C# 不允许此操作；唯一允许的语言构造是可以从约束中推断出来的构造。

# C++ wchar_t介绍以及和string类型的转换
https://blog.csdn.net/sxhlovehmm/article/details/40919607

# 关于CMake

还是回头来看一下从C++源文件变成可执行文件的过程.

执行CMake命令,实质上是在构建makefile的过程,即 生成构建文件.
执行CMake实在生成make, 执行make才是真正的开始将C++源代码转换成可执行文件.

## CMake中常用命令
### target_include_directories
在CMake中用于为特定的目标指定包含目录, 这些指定的目录在编译时会被添加到编译器的搜索路径中, 使得编译器可以找到目标的源文件包含的头文件. 当项目包含多个目录，并且源文件在不同目录下分布时，target_include_directories能够帮助编译器找到这些分散的头文件。这对于保持项目的组织结构清晰、解决头文件依赖问题非常有用。
使用方法
```
target_include_directories(<target>
  [BEFORE]
  <INTERFACE|PUBLIC|PRIVATE> [items1...]
  [<INTERFACE|PUBLIC|PRIVATE> [items2...] ...])
```

- <target>: 目标的名称，可以是可执行文件或库。
- BEFORE: 可选关键字，指定包含目录应该在默认目录之前被添加到编译器的搜索路径。
- <INTERFACE|PUBLIC|PRIVATE>: 指定包含目录的范围：
  - PRIVATE: 目录仅对该目标可见，不会影响依赖该目标的其他目标。
  - INTERFACE: 目录不会添加到该目标的编译选项中，但会添加到链接该目标的其他目标中。
  - PUBLIC: 目录既会添加到该目标的编译选项中，也会添加到链接该目标的其他目标中。
- `[items...]`: 要添加为包含目录的路径列表。

### add_executable
add_executable()命令用于定义一个新的可执行目标，也就是要生成的可执行文件。这个命令告诉CMake要编译一系列的源文件，然后将其链接以产生一个可执行文件

```
add_executable(<name> [WIN32] [MACOSX_BUNDLE] [EXCLUDE_FROM_ALL] source1 [source2 ...])
```
其中
- `<name>` 是要生成的可执行文件的名称。
- `[WIN32]` 在Windows上生成一个应用程序，而不是控制台应用程序。
- `[MACOSX_BUNDLE]` 在macOS上生成一个bundle。
- `[EXCLUDE_FROM_ALL]` 表示此目标不会被默认构建（例如，当你运行 "make" 或 "ninja" 时）。
- source1, source2, ... 是要编译的源文件。

### target_link_options
### pkg_check_modules
### include
### find_package
### set
### add_subdirectory
add_subdirectory() 是 CMake 中的一个命令，用于添加一个子目录到构建中。当此命令被执行时，CMake 会进入指定的子目录，并查找并处理那个子目录下的 CMakeLists.txt 文件。

```
add_subdirectory(source_dir [binary_dir] [EXCLUDE_FROM_ALL])
```

参数解释：

- source_dir：要加入构建的子目录的路径。这是必需的。
- binary_dir：为子目录生成的构建文件（如 Makefiles 或项目文件）应放置的目录。通常，如果您没有指定，CMake 将在当前二进制目录下为子目录创建一个同名目录。这不是必需的。
- EXCLUDE_FROM_ALL：当设置此选项时，这个子目录下的目标（例如库或可执行文件）不会被默认构建目标（例如 "make" 或 "ninja" 的默认目标）包括进来。
假设有以下的目录结构：
```
/my_project
├── CMakeLists.txt
└── /sub_project
    └── CMakeLists.txt
```
在 /my_project/CMakeLists.txt (根目录下面的CMakeLists.txt)中，你可以这样写：
```
add_subdirectory(sub_project)
```
这样，当CMake处理 /my_project/CMakeLists.txt 时，它也会处理 /sub_project/CMakeLists.txt。

### CMAKE_MODULE_PATH
### CMAKE_MODULE_PATH 
### CMAKE_PREFIX_PATH
### FindXXX.cmake
### <XXX>_INCLUDE_DIRS
### <XXX>_LIBRARIES
### <XXX>_EXECUTABLE
### <XXX>_ROOT_DIR
### <XXX>_FOUND


# 参考资料
[【CMake】 简单的CMakeLists命令](https://zhuanlan.zhihu.com/p/652187383)