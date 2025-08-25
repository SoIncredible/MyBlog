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
```cpp
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



# C++ volatile 关键字

在 C++ 中，`volatile` 是一个类型修饰符（关键字），用于告诉编译器：**被修饰的变量可能会被程序之外的因素（如硬件、中断、多线程等）修改**，因此编译器不应对该变量的读写操作进行优化（如缓存到寄存器、重排指令等），必须每次都直接从内存中读取或写入。


### 核心作用：阻止编译器优化
编译器在编译时会对代码进行优化，例如：
- 将频繁访问的变量缓存到寄存器（减少内存读写开销）
- 重排指令执行顺序（提高执行效率）
- 省略重复的读写操作（如连续多次读取同一个变量时，可能只读取一次）

但对于 `volatile` 修饰的变量，这些优化可能导致错误，因为变量的值可能在编译器不知情的情况下被修改。`volatile` 强制编译器每次都直接与内存交互，确保操作的真实性。


### 典型使用场景
1. **硬件寄存器访问**  
   硬件设备的寄存器（如传感器数据、定时器计数）的值可能会被硬件直接修改，必须用 `volatile` 修饰，避免编译器缓存旧值。
   ```cpp
   // 假设 0x1234 是硬件寄存器地址
   volatile int* sensor_data = (volatile int*)0x1234;

   // 每次都从内存（寄存器）读取最新值
   int current_value = *sensor_data;
   ```

2. **中断服务程序（ISR）与主程序共享变量**  
   中断程序可能随时修改变量，主程序必须每次都读取内存中的最新值。
   ```cpp
   volatile bool flag = false;  // 被中断程序和主程序共享

   // 中断服务程序（ISR）
   void interrupt_handler() {
       flag = true;  // 修改标志
   }

   // 主程序
   int main() {
       while (!flag) {  // 每次都从内存读取 flag 的最新值
           // 等待中断
       }
       return 0;
   }
   ```

3. **多线程共享变量（有限场景）**  
   在没有同步机制（如互斥锁）的情况下，`volatile` 可确保线程读取到变量的最新值（但**不能替代线程同步**，因为它不保证原子性）。


### 注意事项
1. **`volatile` 不保证原子性**  
   它仅确保变量的读写不被优化，但复杂操作（如 `i++`）仍可能被拆分为多个指令，在多线程环境中存在竞态条件。需配合原子操作（如 `std::atomic`）或锁机制使用。

2. **`volatile` 与 `const` 可同时使用**  
   表示变量“可以被外部修改，但程序内部不能修改”：
   ```cpp
   volatile const int* config = /* 硬件配置寄存器地址 */;
   // *config = 10;  // 错误：const 不允许修改
   // 但硬件可以修改 *config 的值
   ```

3. **函数参数和返回值也可被修饰**  
   表示参数/返回值可能被外部修改：
   ```cpp
   volatile int read_data(volatile int* ptr) {
       return *ptr;  // 读取 volatile 变量
   }
   ```

4. **不要过度使用**  
   `volatile` 会禁用编译器优化，可能降低程序性能，仅在确实需要时使用。


### 总结
`volatile` 的核心意义是**告诉编译器：“这个变量的变化不受程序控制，请勿优化其读写操作”**，主要用于硬件交互、中断处理等场景，确保程序能获取变量的最新值。但它不能解决线程同步问题，需与其他机制配合使用。

# C++ inline 关键字

在 C++ 中，`inline` 关键字用于向编译器建议将函数**内联展开**（inline expansion），即把函数调用处直接替换为函数体代码，而不是通过传统的函数调用机制（如压栈、跳转、返回等）执行。

### 主要作用：
1. **减少函数调用开销**  
   传统函数调用需要执行一系列操作（如保存寄存器、参数入栈、程序跳转等），这些操作会产生额外开销。对于简短的函数（如只包含几行代码），调用开销可能甚至超过函数本身的执行时间。  
   `inline` 建议编译器在调用处直接插入函数体，消除这些额外开销，提高程序运行效率。

2. **不影响函数的封装性**  
   与 `#define` 宏定义相比，`inline` 函数既保留了函数的类型检查、作用域规则等封装特性，又能实现类似宏的"代码替换"效果，是更安全的选择。


### 使用注意事项：
1. **只是编译器的"建议"**  
   `inline` 是给编译器的**提示**，而非强制命令。编译器可能会忽略该建议（例如，对于递归函数、包含循环/switch的复杂函数，编译器通常不会将其 inline）。

2. **通常用于简短函数**  
   如果函数体过长，inline 会导致代码膨胀（重复插入函数体），反而增加内存占用和指令缓存压力，降低性能。

3. **定义需放在头文件中**  
   与普通函数不同，`inline` 函数的定义通常需要放在头文件里（而非仅声明），因为编译器在编译调用处代码时，需要知道函数体内容才能进行 inline 展开。

4. **避免副作用**  
   与宏不同，`inline` 函数的参数只计算一次，不会产生类似宏的副作用。例如：
   ```cpp
   // 宏可能产生副作用
   #define ADD(a, b) (a + b)
   int x = 1;
   int y = ADD(x++, x++);  // 结果不确定（x可能被递增2次或1次）

   // inline函数无此问题
   inline int add(int a, int b) { return a + b; }
   int y = add(x++, x++);  // 明确：x先递增两次，再计算和
   ```


### 典型用法示例：
```cpp
// 头文件中定义inline函数
inline int max(int a, int b) {
    return (a > b) ? a : b;
}

// 调用处会被编译器建议展开为：(a > b) ? a : b
int result = max(3, 5);
```


### 总结：
`inline` 的核心价值是**在不牺牲代码可读性和安全性的前提下，优化简短函数的调用效率**。它是 C++ 对宏定义的一种改进，合理使用能提升程序性能，但过度使用（如对复杂函数加 inline）可能适得其反。

# C++ 友元

在 C++ 中，**友元（friend）** 是一种打破类封装性的机制，允许特定的外部函数或类访问当前类的私有（private）和保护（protected）成员。


### 友元的定义方式：
1. **友元函数**：在类内部声明一个外部函数为友元，使其能访问类的私有成员。
   ```cpp
   class MyClass {
   private:
       int value;
   public:
       MyClass(int v) : value(v) {}
       // 声明外部函数为友元
       friend void printValue(MyClass obj);
   };

   // 友元函数可以直接访问MyClass的私有成员
   void printValue(MyClass obj) {
       cout << obj.value << endl;  // 合法：访问私有成员value
   }
   ```

2. **友元类**：一个类可以声明另一个类为友元，友元类的所有成员函数都能访问当前类的私有成员。
   ```cpp
   class A {
   private:
       int secret;
       // 声明B为友元类
       friend class B;
   };

   class B {
   public:
       void accessA(A& a) {
           a.secret = 100;  // 合法：B是A的友元，可访问其私有成员
       }
   };
   ```


### 为什么需要友元？
C++ 的类封装性要求私有成员只能被类内部的成员函数访问，但在某些场景下，这种严格的封装会带来不便，友元机制正是为了解决这些特殊需求：

1. **方便操作类的私有数据**  
   例如在实现运算符重载（如 `<<` 输出运算符）时，需要访问类的私有成员，但运算符重载函数通常是全局函数而非成员函数，此时可将其声明为友元。
   ```cpp
   class Point {
   private:
       int x, y;
       // 声明输出运算符重载为友元
       friend ostream& operator<<(ostream& os, const Point& p);
   };

   ostream& operator<<(ostream& os, const Point& p) {
       os << "(" << p.x << "," << p.y << ")";  // 访问私有成员x、y
       return os;
   }
   ```

2. **实现类之间的协作**  
   当两个类关系紧密（如容器类与迭代器类），迭代器需要访问容器的内部数据结构时，可将迭代器类声明为容器类的友元，简化协作逻辑。

3. **避免过度暴露接口**  
   如果不使用友元，为了让外部访问私有成员，可能需要增加大量的 `getter/setter` 方法，反而破坏封装的简洁性。友元可以在不暴露接口的前提下，有选择地开放访问权限。


### 注意事项：
1. **友元关系是单向的**：若 A 是 B 的友元，B 不一定是 A 的友元。
2. **友元关系不可传递**：若 A 是 B 的友元，B 是 C 的友元，A 不一定是 C 的友元。
3. **破坏封装性**：友元本质上是对封装的突破，过度使用会降低代码的安全性和可维护性，应谨慎使用。

友元的核心价值是**在保证封装性的前提下，为特殊场景提供灵活的访问机制**，是对严格封装的一种补充，而非替代。

# C++ 继承多个类    

在 C++ 中，**支持多继承**，即一个派生类可以同时继承多个基类。这种机制增强了代码的灵活性，但也带来了一些复杂问题，需要特别注意。


### 一、多继承的基本语法
派生类声明时，在基类列表中用逗号分隔多个基类：
```cpp
class Base1 { /* ... */ };
class Base2 { /* ... */ };

// 派生类同时继承 Base1 和 Base2
class Derived : public Base1, public Base2 {
    // 派生类成员
};
```


### 二、多继承需要注意的问题
1. **菱形继承（钻石问题）导致的二义性**  
   最典型的问题是“菱形继承”：两个派生类（B、C）继承自同一个基类（A），而另一个派生类（D）同时继承 B 和 C。此时，D 会间接包含两份 A 的成员，导致访问 A 的成员时出现二义性。

   ```cpp
   class A { public: int x; };
   class B : public A { };  // B 继承 A
   class C : public A { };  // C 继承 A
   class D : public B, public C { };  // D 继承 B 和 C

   int main() {
       D d;
       // d.x = 10;  // 错误：二义性（B::x 和 C::x 冲突）
       return 0;
   }
   ```

   **解决方法**：使用**虚继承（virtual inheritance）**，让 B 和 C 虚继承自 A，确保 A 在 D 中只存在一份实例：
   ```cpp
   class A { public: int x; };
   class B : virtual public A { };  // 虚继承
   class C : virtual public A { };  // 虚继承
   class D : public B, public C { };

   int main() {
       D d;
       d.x = 10;  // 合法：A 只存在一份，无歧义
       return 0;
   }
   ```


2. **成员名冲突**  
   若多个基类有同名成员（变量或函数），派生类访问时必须通过**作用域限定符**明确指定来自哪个基类，否则会产生二义性：
   ```cpp
   class Base1 {
   public:
       void func() { cout << "Base1::func" << endl; }
   };
   class Base2 {
   public:
       void func() { cout << "Base2::func" << endl; }
   };
   class Derived : public Base1, public Base2 { };

   int main() {
       Derived d;
       // d.func();  // 错误：二义性（Base1::func 和 Base2::func 冲突）
       d.Base1::func();  // 正确：指定访问 Base1 的 func
       d.Base2::func();  // 正确：指定访问 Base2 的 func
       return 0;
   }
   ```


3. **构造函数与析构函数的调用顺序**  
   - 构造函数：先调用所有基类的构造函数（按继承列表中的声明顺序），再调用派生类的构造函数。  
   - 析构函数：与构造函数顺序相反（先派生类，再基类，按继承列表的逆序）。  

   ```cpp
   class Base1 { public: Base1() { cout << "Base1 构造" << endl; } };
   class Base2 { public: Base2() { cout << "Base2 构造" << endl; } };
   class Derived : public Base1, public Base2 {
   public:
       Derived() { cout << "Derived 构造" << endl; }
   };
   // 输出顺序：Base1 构造 → Base2 构造 → Derived 构造
   ```


4. **代码复杂度与维护性**  
   多继承会增加类层次的复杂性，过度使用可能导致代码难以理解和维护。在很多场景下，可通过“组合”或“接口继承+实现分离”替代多继承（如 Java 的单继承+接口）。


### 三、多继承中的类型转换
多继承中的类型转换与单继承类似，但需注意基类之间的独立性。


1. **向上转型（派生类 → 基类）**  
   派生类对象可以隐式转换为任意基类的指针或引用（安全转换）：
   ```cpp
   class Base1 { };
   class Base2 { };
   class Derived : public Base1, public Base2 { };

   int main() {
       Derived d;
       Base1* b1 = &d;  // 隐式转换：Derived* → Base1*
       Base2* b2 = &d;  // 隐式转换：Derived* → Base2*
       return 0;
   }
   ```


2. **向下转型（基类 → 派生类）**  
   基类指针/引用转换为派生类指针/引用时，必须显式转换，且需确保安全性：  
   - 若基类有虚函数，推荐使用 `dynamic_cast`（运行时检查，转换失败返回 `nullptr` 或抛出异常）。  
   - 若基类无虚函数，可使用 `static_cast`（编译时转换，无运行时检查，风险较高）。

   ```cpp
   class Base1 { public: virtual void f() {} };  // 有虚函数
   class Base2 { public: virtual void g() {} };
   class Derived : public Base1, public Base2 { };

   int main() {
       Base1* b1 = new Derived();  // 向上转型
       
       // 向下转型：Base1* → Derived*
       Derived* d = dynamic_cast<Derived*>(b1);
       if (d) {  // 转换成功
           cout << "转换成功" << endl;
       }
       
       delete b1;
       return 0;
   }
   ```


3. **交叉转型（基类 → 另一个基类）**  
   两个基类（如 Base1 和 Base2）之间无继承关系，不能直接转换。需先转换为派生类，再转换为另一个基类：
   ```cpp
   class Base1 { public: virtual void f() {} };
   class Base2 { public: virtual void g() {} };
   class Derived : public Base1, public Base2 { };

   int main() {
       Base1* b1 = new Derived();
       
       // 先转为 Derived*，再转为 Base2*
       Derived* d = dynamic_cast<Derived*>(b1);
       if (d) {
           Base2* b2 = d;  // 隐式转换：Derived* → Base2*
       }
       
       delete b1;
       return 0;
   }
   ```


### 总结
- C++ 支持多继承，允许一个类继承多个基类。  
- 需重点注意菱形继承的二义性（通过虚继承解决）、成员名冲突（通过作用域限定符解决）。  
- 类型转换中，向上转型隐式安全，向下转型需用 `dynamic_cast`（带虚函数时），交叉转型需通过派生类中转。  

多继承是一把“双刃剑”，合理使用可提升灵活性，但过度使用会增加复杂度，实践中需谨慎设计类层次。

在 C++ 中，**虚继承（virtual inheritance）** 是一种特殊的继承方式，用于解决多继承中可能出现的**菱形继承（钻石问题）** 导致的成员冗余和二义性问题。


### 什么是菱形继承问题？
当类的继承关系形成“菱形”结构时，会出现基类成员被多次继承的问题：
- 类 A 是顶层基类，包含成员 `x`。
- 类 B 和类 C 分别继承自 A，因此都包含 A 的成员 `x`。
- 类 D 同时继承 B 和 C，此时 D 中会间接包含**两份 A 的成员 `x`**（一份来自 B，一份来自 C）。

这种情况下，访问 D 中的 `x` 会产生二义性（编译器无法确定访问的是 B 继承的 `x` 还是 C 继承的 `x`）。


### 虚继承的作用
虚继承通过让中间基类（如 B 和 C）**虚继承**顶层基类（如 A），确保顶层基类在最终派生类（如 D）中**只存在一份实例**，从而消除冗余和二义性。


### 虚继承的语法
在继承时使用 `virtual` 关键字声明：
```cpp
// 顶层基类
class A {
public:
    int x;
    A() : x(0) {}
};

// 中间基类 B 虚继承 A
class B : virtual public A { };

// 中间基类 C 虚继承 A
class C : virtual public A { };

// 最终派生类 D 继承 B 和 C
class D : public B, public C { };
```

此时，D 中只会包含**一份 A 的成员 `x`**，访问 `d.x` 时不再有二义性：
```cpp
int main() {
    D d;
    d.x = 10;  // 合法：A 只存在一份，无歧义
    return 0;
}
```


### 虚继承的实现原理
编译器通过**虚基类表（virtual base table）** 和**虚基类指针（virtual base pointer）** 实现虚继承：
- 虚继承的中间类（B、C）会生成一个虚基类表，存储自身与顶层虚基类（A）的偏移量。
- 中间类的对象中会增加一个虚基类指针（指向虚基类表），最终派生类（D）通过该指针找到唯一的顶层基类实例。

这种机制确保无论中间类被继承多少次，顶层虚基类在最终派生类中只存在一份。


### 注意事项
1. **构造函数的调用顺序**  
   虚基类的构造函数由**最终派生类**直接调用，而非中间基类。即使中间基类在构造函数中显式初始化虚基类，最终派生类也会覆盖这种初始化：
   ```cpp
   class A {
   public:
       A(int val) : x(val) {}
       int x;
   };

   class B : virtual public A {
   public:
       B() : A(10) {}  // 中间类对虚基类的初始化会被覆盖
   };

   class D : public B {
   public:
       D() : A(20) {}  // 最终派生类直接初始化虚基类
   };

   int main() {
       D d;
       cout << d.x;  // 输出 20（而非 10）
       return 0;
   }
   ```

2. **只在必要时使用**  
   虚继承会增加内存开销（额外的虚基类指针）和运行时开销（通过指针访问成员），仅在需要解决菱形继承问题时使用。

3. **避免复杂的虚继承层次**  
   多层虚继承可能导致代码逻辑复杂，降低可读性和维护性，设计类层次时应尽量简化。


### 总结
虚继承是 C++ 为解决多继承中菱形继承问题而设计的机制，通过让中间基类虚继承顶层基类，确保顶层基类在最终派生类中只存在一份实例，从而消除成员冗余和访问二义性。其核心价值是在支持多继承的同时，弥补了菱形继承带来的缺陷。

# C++中const的用法
是的，`const` 在 C++ 中是一个非常丰富和多用途的关键字，除了你提到的三种用法，还有其他几种重要用法。以下是 `const` 关键字的全面总结：

---

### 1. Const 变量（命名常量）

这是最基础的用法，用于声明一个其值不可被修改的变量。

```cpp
const int MAX_BUFFER_SIZE = 1024; // 值不可修改
const float PI = 3.14159f;

// 尝试修改会导致编译错误
// MAX_BUFFER_SIZE = 2048; // ❌ 错误！
```

**与 `#define` 的区别**：`const` 变量有类型信息和作用域，更安全，便于调试。

---

### 2. Const 引用

声明一个对常量的引用，不能通过这个引用来修改所引用的对象。

```cpp
int originalValue = 42;

const int& constRef = originalValue; // 常量引用
// constRef = 100; // ❌ 错误！不能通过const引用修改值

int& nonConstRef = originalValue;    // 非常量引用
nonConstRef = 100;                   // ✅ 可以修改
```

**主要用途**：
*   作为函数参数，避免不必要的拷贝同时保证不修改原对象（比传值更高效）。
*   ```cpp
    void ProcessData(const std::string& data) { 
        // 可以读取data，但不能修改它 
        size_t len = data.length(); // ✅
        // data.clear(); // ❌
    }
    ```

---

### 3. Const 指针 vs 指向 Const 的指针

这是一个容易混淆但很重要的区别：

```cpp
int value = 10;
int anotherValue = 20;

// 1. 指向常量的指针（Pointer to const）
// - 指针本身可以改变指向，但不能通过它修改所指的值
const int* ptrToConst = &value;
// *ptrToConst = 15; // ❌ 错误：不能通过ptrToConst修改value
ptrToConst = &anotherValue; // ✅ 正确：可以改变指针指向

// 2. 常量指针（Const pointer）
// - 指针本身不能改变指向，但可以通过它修改所指的值
int* const constPtr = &value;
*constPtr = 15; // ✅ 正确：可以通过constPtr修改value
// constPtr = &anotherValue; // ❌ 错误：不能改变指针指向

// 3. 指向常量的常量指针（Const pointer to const）
// - 既不能改变指向，也不能通过它修改值
const int* const constPtrToConst = &value;
// *constPtrToConst = 15; // ❌
// constPtrToConst = &anotherValue; // ❌
```

**记忆口诀**：`const` 在 `*` 左边表示指向常量，在 `*` 右边表示指针本身是常量。

---

### 4. Const 成员变量

在类中声明不可修改的成员变量。**必须在构造函数的初始化列表中初始化**，不能在函数体内赋值。

```cpp
class Circle {
private:
    const double m_PI; // const成员变量
    double m_Radius;
    
public:
    Circle(double radius) 
        : m_PI(3.14159) // 必须在初始化列表中初始化
        , m_Radius(radius) 
    {
        // m_PI = 3.14; // ❌ 错误！不能在构造函数体内赋值
    }
    
    double GetArea() const {
        return m_PI * m_Radius * m_Radius;
    }
};
```

---

### 5. Constexpr (C++11 引入)

`constexpr` 比 `const` 更严格，表示**编译期常量**，值必须在编译时就能确定。

```cpp
constexpr int ArraySize = 100; // 编译期常量
int myArray[ArraySize]; // ✅ 可以用作数组大小

const int size = SomeFunction(); // 运行时才能确定
// int anotherArray[size]; // ❌ 错误：size不是编译期常量

// constexpr函数：如果参数是编译期常量，则结果也是编译期常量
constexpr int Square(int x) {
    return x * x;
}

constexpr int squaredValue = Square(5); // 编译时计算
int array[Square(5)]; // ✅ 正确
```

---

### 6. Mutable 成员变量

这是一个与 `const` 相关的特殊关键字。被声明为 `mutable` 的成员变量，**即使在 `const` 成员函数中也可以被修改**。

```cpp
class DataLogger {
private:
    mutable int m_AccessCount; // 可变成员变量
    std::string m_Data;
    
public:
    const std::string& GetData() const {
        m_AccessCount++; // ✅ 即使这是const函数，也可以修改mutable成员
        return m_Data;
    }
    
    int GetAccessCount() const { return m_AccessCount; }
};
```

**典型用途**：用于缓存、引用计数、调试统计等需要在逻辑const操作中更新的辅助数据。

---

### 7. Const 与函数返回值

让函数返回常量值或常量引用，可以防止返回值被意外修改。

```cpp
class BigData {
    std::vector<int> m_HugeData;
public:
    // 返回const引用，防止调用者修改内部数据
    const std::vector<int>& GetData() const { 
        return m_HugeData; 
    }
    
    // 不好的设计：返回非const引用，暴露了内部实现
    std::vector<int>& GetData() { 
        return m_HugeData; 
    }
};

BigData data;
// 好的：只能读取，不能修改
const std::vector<int>& safeRef = data.GetData();
// safeRef.clear(); // ❌ 编译错误

// 不好的：可以修改内部数据（破坏了封装性）
std::vector<int>& unsafeRef = data.GetData();
unsafeRef.clear(); // ✅ 编译通过，但可能破坏对象状态
```

---

### 总结对比表

| 用法                | 语法示例                        | 含义                                       |
| ------------------- | ------------------------------- | ------------------------------------------ |
| **Const变量**       | `const int size = 10;`          | 值不可变的命名常量                         |
| **Const引用**       | `const std::string& str;`       | 不能通过引用修改目标对象                   |
| **指向Const的指针** | `const int* ptr;`               | 可通过指针改指向，不能改值                 |
| **Const指针**       | `int* const ptr;`               | 不可改指向，可通过指针改值                 |
| **Const成员函数**   | `void func() const;`            | 不修改对象状态的成员函数                   |
| **Const成员变量**   | `const int m_Value;`            | 必须在构造函数初始化列表中初始化的常量成员 |
| **Constexpr**       | `constexpr int size = 10;`      | 编译期常量，值必须在编译时确定             |
| **Mutable**         | `mutable int m_Counter;`        | 即使在const函数中也可修改的成员变量        |
| **Const返回值**     | `const Type& GetValue() const;` | 返回不可修改的值或引用，保护内部数据       |

C++ 中 `const` 的正确使用是编写健壮、安全和易于维护代码的关键技能，它可以在编译期捕获许多潜在的错误。

# 关于define的用法

在C++中，`#define` 是预处理指令，用于创建宏定义，主要作用是在编译前对代码进行文本替换。它的使用方式灵活，常见用法如下：

### 1. 定义常量
最基本的用法是定义常量，提高代码可读性和维护性：
```cpp
#define PI 3.1415926  // 定义圆周率常量
#define MAX_SIZE 100  // 定义数组最大长度

// 使用示例
double area = PI * r * r;
int arr[MAX_SIZE];
```

### 2. 定义带参数的宏
可以像函数一样定义带参数的宏，实现简单的代码替换：
```cpp
// 计算两数之和
#define ADD(a, b) (a + b)

// 计算平方
#define SQUARE(x) (x) * (x)

// 使用示例
int sum = ADD(3, 5);  // 替换为 (3 + 5)
int square = SQUARE(4);  // 替换为 (4) * (4)
```

**注意**：带参数的宏需要注意括号的使用，避免运算优先级问题：
```cpp
// 错误示例（缺少括号）
#define MUL(a, b) a * b
int result = MUL(2 + 3, 4);  // 会被替换为 2 + 3 * 4，结果为14（非预期的20）

// 正确示例
#define MUL(a, b) ((a) * (b))
int result = MUL(2 + 3, 4);  // 替换为 ((2 + 3) * 4)，结果为20
```

### 3. 定义代码片段
可以定义一段代码块，用于简化重复操作：
```cpp
// 交换两个变量的值
#define SWAP(a, b) do { \
    int temp = a; \
    a = b; \
    b = temp; \
} while(0)

// 使用示例
int x = 5, y = 10;
SWAP(x, y);  // 替换为上述代码块
```
这里的 `do-while(0)` 用于确保宏在任何场景下都能正确执行（如在 `if` 语句后无需加括号）。

### 4. 条件编译
结合其他预处理指令（如 `#ifdef`、`#ifndef`、`#else`、`#endif`）实现条件编译：
```cpp
#define DEBUG  // 定义DEBUG宏

#ifdef DEBUG
    // 调试模式下执行的代码
    #define LOG(message) cout << "Debug: " << message << endl
#else
    // 发布模式下不执行任何操作
    #define LOG(message)
#endif

// 使用示例
LOG("程序开始执行");  // 调试模式下会输出，发布模式下无操作
```

### 5. 取消宏定义
使用 `#undef` 可以取消已定义的宏：
```cpp
#define MAX 100
cout << MAX << endl;  // 输出100

#undef MAX  // 取消MAX的定义
// cout << MAX << endl;  // 错误：MAX未定义
```

### 注意事项
1. **宏与函数的区别**：宏是编译前的文本替换，没有类型检查，而函数是运行时的代码调用，有类型检查。
2. **避免副作用**：带参数的宏可能产生副作用，例如 `ADD(a++, b++)` 会导致变量被多次递增。
3. **命名规范**：通常宏名使用全大写字母，以区分普通变量和函数。
4. **C++中的替代方案**：在很多场景下，`const` 定义的常量或 `inline` 函数可以替代宏，提供更好的类型安全性。

合理使用 `#define` 可以简化代码，但过度使用可能导致代码可读性和可维护性下降，需谨慎使用。

# 参考资料
[【CMake】 简单的CMakeLists命令](https://zhuanlan.zhihu.com/p/652187383)