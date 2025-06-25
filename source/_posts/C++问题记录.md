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
```C++
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

在C++中可以较为自由地控制某一个对象是创建在堆上还是栈上，而在C#中，值类型只能创建在栈上，堆类型只能创建在堆上，在不考虑装拆箱类型强转的情况下。

# C++宏的使用

在 C++ 中，除了系统相关的宏，其他宏的开关控制方式主要有编译器默认定义、构建系统设置、头文件包含等，以下是具体介绍：
编译器默认定义
一些编译器会根据自身特性和编译选项，默认定义某些宏。例如，GCC 编译器会定义__GNUC__宏，用于标识使用的是 GCC 编译器，其值表示 GCC 的版本号。
对于 C++ 标准库相关的宏，如__cplusplus，它用于标识当前编译环境的 C++ 标准版本。在 C++11 环境下，__cplusplus的值通常被定义为201103L，在 C++20 环境下则为202002L等，编译器会根据所支持的 C++ 标准自动定义该宏。
构建系统设置
Makefile：可以在 Makefile 中通过CFLAGS（针对 C 语言）或CXXFLAGS（针对 C++ 语言）变量来定义宏。比如要定义一个名为MY_MACRO的宏，可以写成CFLAGS += -DMY_MACRO或CXXFLAGS += -DMY_MACRO。如果要为宏指定值，例如MY_MACRO的值为10，则可以写成CFLAGS += -DMY_MACRO=10或CXXFLAGS += -DMY_MACRO=10。
CMake：使用add_definitions命令来定义宏，如add_definitions(-DMY_MACRO)或add_definitions(-DMY_MACRO=10)。也可以通过set命令结合CMAKE_C_FLAGS或CMAKE_CXX_FLAGS变量来设置，例如set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DMY_MACRO=10")。
Ninja：与 Makefile 类似，在生成 Ninja 构建文件时，可以通过相关的命令行参数或配置选项来添加宏定义。具体方式取决于生成 Ninja 文件的工具或脚本，例如使用 CMake 生成 Ninja 文件时，可以通过 CMake 的配置来间接影响 Ninja 构建时的宏定义。
头文件包含
许多库的头文件会根据自身的需求和条件定义一些宏。当包含这些头文件时，相应的宏就会被定义。例如，在包含<stdio.h>头文件时，可能会引入一些与标准输入输出相关的宏定义。
一些跨平台的库会在头文件中根据不同的平台来定义特定的宏。以 OpenGL 库为例，在 Windows 平台下包含<windows.h>头文件后，可能会定义一些与 Windows 图形系统相关的宏，然后在 OpenGL 的头文件中会根据这些宏来进一步定义与 OpenGL 在 Windows 平台上相关的特定宏。
命令行参数
在使用编译器命令行进行编译时，可以通过命令行参数来定义宏。例如，使用 GCC 编译时，可以通过-D选项来定义宏，如gcc -D MY_MACRO main.c会在编译main.c文件时定义MY_MACRO宏。如果要为宏赋值，可使用-D MY_MACRO=10的形式。
在使用 CMake 等构建系统时，也可以在命令行中通过-D选项来传递自定义的宏定义。例如cmake -D MY_MACRO=10..，这会在运行 CMake 配置项目时定义MY_MACRO宏，后续的编译过程中就可以使用该宏。

简单的使用可以看一下[这个仓库](https://github.com/SoIncredible/programming-practice)下的`Code/CPP/assertAndMacro`

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


# 关于C++中的*和&符号

&符号多用于

在C++中有符合类型的概念,引用和指针就是符合类型中的两种,*和&号是这两种符合类型中经常使用到的符号.

引用为对象起了另外一个名字,引用必须初始化.

C++中函数参数的传递方式有:
- 按值传递
- 按指针传递
- 按引用传递

函数传参的过程,发生了什么?
如何理解函数参数中的&符号,比如下面这个例子
```
void func(int a, int &b){
    a++;
    b++;
}

int main(){

    int a = 1;
    int b = 2;
    func(a, b);
}

```

在调用函数时,我们需要向函数中传入一系列的参数,如果我们不给参数添加&时,你可以理解为,调用函数一方,也就是原始参数持有一方所持有的函数,和真正参与函数执行的参数,是完全没关系的. &的作用就是将这两个参数关联起来,或者说,让这两个参数就是一个参数.因此,函数内对参数的操作会在函数执行完毕,回到调用函数侧时,对参数做的修改会得以保留.




那如果我想要把外层和函数内层的字段关联起来,该怎么做呢?通过指针啊,指针之间的传值是复制,但是只要指针指向的内存地址一样,就可以使用不同的指针对同一片内存地址操作啊.

引用&符号的作用更多的是用在函数传参中,避免不必要的参数复制造成额外开销.
迷糊了,直接用指针就可以
比如一个站

# 模版template的使用

在 C++ 里，模板函数和模板类的定义和声明通常需要放在同一个头文件中。这是因为模板在编译时才会被实例化，编译器需要看到模板的完整定义才能生成具体的代码。在你的代码里，Stack 类的定义放在了 stack.h 头文件中，而成员函数的实现放在了 stack.cpp 文件中，这就导致编译器在编译 main.cpp 时，无法找到 Stack<custom> 类成员函数的具体实现，从而在链接阶段出现未定义符号的错误。

# 如何在C++中实现C#中的反射 HybridCLR可能会用到

# 参考资料
[【CMake】 简单的CMakeLists命令](https://zhuanlan.zhihu.com/p/652187383)