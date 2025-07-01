---
title: Unity中的动态链接库
abbrlink: 4ea0c9c3
date: 2023-09-28 18:04:28
tags: 
  - 技术文档
  - C++
  - C#
categories: 硬技能
cover: https://www.notion.so/images/page-cover/met_henry_lerolle_1885.jpg
description:
swiper_index:
sticky:
---

# Unity中使用到的jar包、dll等必须放在Plugins目录下吗
并不是的, 托管的dll放在任何地方都可以
在Mac上能编译非托管的dll类型的库嘛? c++为例, 编译之后成dylib了 如果mac上c++编译成了dll, Mac上Unity能用吗?


DLL是动态链接库, 但是有分别

刚才在Unity里面验证了, UnityEditor里面应该帮我自动设置了dll的引用关系, 需要看一下非Unity项目下, 导入一个dll是如何关联的. 
非托管的dll在Mac上不能用 
托管的dll可以用
在Mac上打非托管的dll 给Windows平台用有问题吗 我刚才在Windows Arm虚拟机上试的是不行的
> DLL并不是哪一种语言的专属, 但是由于在不同平台上, dll文件的扩展名也是不一样的, 在Windows上是.dll, 在Mac上是.dylib, 在Linux上是.so, 虽然任何语言都可以生成动态库, 但是按照笔者Unity开发的视角, 我们还是对动态链接库做一下区分: 由C#和非C#语言生成的DLL, 虽然都能生成DLL, 如果DLL的调用者是C#语言, 那么这两种DLL是有分别的: C#生成的DLL类型是托管类型的DLL, C++生成的DLL是非托管的(原生的)DLL, 前者的DLL导入到C#工程中编译器就能够自动的识别DLL中的成员类型, 后者生成的DLL导入到C#工程中则需要使用`[DLLImport]`Attribute来做一些额外的处理, 并且在C++侧也需要对于要在C#侧调用的方法签名上添加`extern "C"`标识

> 注意, 因为每个平台上的动态库的格式不一样, 因此在构建特定平台的动态库的时候, 应直接在对应平台的机器上构建, 不要用交叉编译的方式构建, 会有奇怪的错误

> 在托管代码中调用非托管代码的开销怎么样?
> 和把所有代码都写在非托管部分的开销对比怎么样?

# [DllImport("__Internal")]含义
[DllImport("xxx.dll")] 是 .NET / C# 的 P/Invoke 语法，作用是让 C# 可以调用外部的 C、C++、Objective-C 等“本地函数”。

一般写成这样：

[DllImport("myPlugin")]
public static extern int MyNativeFunction(int a, int b);
2. "__Internal" 特殊含义

当你用 [DllImport("__Internal")] 这样写时，“__Internal”是一个特殊的伪名称，而不是指某个实际文件。
其真正含义是：

让 .NET 或 Unity 在 本进程/本程序/主可执行程序自身 的导出表里寻找本地实现的符号。
也就是说，被 [DllImport("__Internal")] 标注的方法，对应的本地方法体需要在当前可执行程序内部（而不是某个外部 DLL 文件里）。
符号名搜索会在自己的 executable 导出里找。
3. 应用场景举例

场景A：iOS、Mac 等平台（特别是 Unity）

Unity/IL2CPP 在生成 iOS、Mac 原生工程时，如果你写了：
[DllImport("__Internal")]
public static extern void MyFunc();
实际上，IL2CPP 编译器会在最终的 App binary（或 Framework 二进制）里搜 名为 MyFunc 的导出函数，而不是去动态查找某个外部库（.dylib、.so、.dll）。
这样做的好处：
iOS 平台不允许像 Windows 那样动态链接第三方 DLL，所有 Native 代码都必须静态链接进 App 的主可执行文件中。
[DllImport("__Internal")] 是唯一安全支持的方式。
场景B：自己写好本地函数并链接进主程序

如果你在构建的主程序中嵌入了自己的 C 代码，可以直接给导出符号以供 .NET 查找，无需拆分外部 DLL 文件。
4. 对比调用外部DLL

[DllImport("user32.dll")] 就是在找 Windows 的系统动态库。
[DllImport("__Internal")] 是在找当前程序（App、自身进程、主 binary）中定义的本地符号——通常只有在本地代码编译成主程序时才会有。
5. Unity的常见用法

在 Unity iOS 插件开发中，Native 代码（C/Objective-C/C++/Swift）最终会被链接进主 app binary，此时 C# 访问原生函数都要用 [DllImport("__Internal")]，不能写成 "yourlib.dylib" —— 否则找不到或被苹果审核拒绝。

[DllImport("__Internal")]
public static extern void MyObjCFunc();
6. 小结

[DllImport(“__Internal”)] 让 C#（Mono/.NET/Unity）去主程序自身导出符号表里找函数，而不是去加载某个外部DLL文件。
常见于 iOS、Mac Unity 原生插件开发场景。
Windows 平台一般用 [DllImport("xxx.dll")] 加载外部库。
扩展

如果你在 Windows 下用 [DllImport("__Internal")]，绝大多数情况下用不到（除非自己玩自定义可执行文件出口，且用Mono/IL2CPP等特殊方案）。

对于C#来说, 无论是Mac还是Windows, C#导出的动态库都是.dll扩展名的, dll中都是IL语言, 不是本地的机器码

# 本地语言Native Code
本地语言，在软件开发领域通常指这样的语言或编程环境：

它编写出来的程序，经过编译后直接生成当前操作系统和硬件平台的“本地机器码”（Native Code）。
程序无需额外的“虚拟机”、“解释器”、“运行时中间层”就能直接在操作系统之上被加载和运行。
本地语言天然与操作系统、硬件架构强相关。
通俗说法：

用本地语言写的代码 -> 编译 -> 得到直接跑在操作系统上的“exe”“dll”“so”等文件。
2. 常见的本地语言举例

以下这些，是典型的本地/原生语言，用它们编译出来的程序就是Native Code：

C
C++
Objective-C（macOS/iOS下）
Rust（默认就是直接编译本地机器码）
Go语言（Go 1.5以后完全支持无需虚拟机，能直接编译原生机器码）
Fortran
Delphi/Pascal（比如 Embarcadero Delphi）
Zig
Assembly 汇编（最彻底的native code）
Swift（编译模式不同，本地或托管两种，主流iOS开发是本地的）


以实现冒泡排序功能为例记录如何在`C#`和`C++`中编写、生成和调用DLL

# CPP中的DLL

## Mac环境

1.创建头文件和CPP文件

```shell
// 创建头文件
touch Head.h

// 辅助类的方法 
touch Utils.cpp

// 排序方法
touch SortUtils.cpp

// 主方法
touch Main.cpp
```

2.编写各文件

```C++
// Head.h
#ifndef HEAD_H
#define HEAD_H
	extern "C" void Swap(int &x, int &y);
	extern "C" void PrintArr(int *arr, int size);
	extern "C" void BubbleSort(int *arr, int size);
#endif
```

```C++
// Utils.cpp
#include"Head.h"
#include<iostream>

using namespace std;

extern "C" void Swap(int &x, int &y){
  int temp = x;
  x = y;
  y = temp;
}

extern "C" void PrintArr(int *arr, int size){
  cout << "Sorted Array:" << endl;	
  for(int i = 0; i < size; i++){
    cout << arr[i] << " ";
 	}
  cout << "\n";
}
```

```C++
// SortUtils.cpp
#include"Head.h"
#include<iostream>
using namespace std;

extern "C" void BubbleSort(int *arr, int size){
  for(int i = 0; i < size; i++){
    for(int j = 0; j < size - i - 1; j++){
      	if(arr[j] > arr[j+1]){
          Swap(arr[j], arr[j+1]);
        }
    }
  }
}
```

```C++
// Main.cpp
#include<iostream>
#include<vector>
// 操作dll相关头文件
#include<dlfcn.h>
using namespace std;

typedef void (*BubbleSort)(int*,int);
typedef void (*PrintArr)(int*,int);

int main(){
  
  // 加载DLL
  void *handle = dlopen("Utils.dylib",RTLD_LAZY);
  
  // 判断DLL是否加载成功
  if(handle == nullptr){
    cout << "Load DLL Fail!" << endl;
    return 1;
  }
  
  BubbleSort bubbleSort = reinterpret_cast<BubbleSort>(dlsym(handle,"BubbleSort"));
  PrintArr printArr = reinterpret_cast<PrintArr>(dlsym(handle,"PrintArr"));
  
  // 判断DLL中的两个函数是否存在
  if(bubbleSort == nullptr){
    cout << "Load BubbleSort Func Fail!" << endl;
    return 1;
  }
  
  if(printArr == nullptr){
    cout << "Load PrintArr Func Fail!" << endl;
    return 1;
  }
  
  cout << "Input Random Numbers:(Press X and then Press Enter to End Input)" << endl;
  
  int number = 0;
  vector<int> randomNums;
  
  while(cin >> number){
    randomNums.push_back(number);
  }
  
  int size = randomNums.size();
  int arr[size];
  
  for(int i = 0; i < size; i++){
    arr[i] = randomNums[i];
  }
  
  bubbleSort(arr,size);
  printArr(arr,size);
  
  dlclose(handle);
  
  return 0;
}
```

3.生成DLL

```shell
//
g++ -c -fPIC Utils.cpp -o Utils.o

g++ -c -fPIC SortUtils.cpp -o SortUtils.o

// Mac环境中C++的dll文件类型是.dylib
g++ -shared SortUtils.o Utils.o -o Utils.dylib
```

或者

```shell
g++ -shared SortUtils.cpp Utils.cpp -o Utils.dylib
```

4.编译Main.cpp

```shell
g++ Main.cpp -o a
```

5.运行

```shell
./a
```

# CS中的DLL

## Mac环境

1.创建DLL项目

```shell
# 指定创建的项目名称和.NET版本
dotnet new classlib -o Utils -f net8.0
```
2.编写DLL脚本

```C#
// SortUtils.cs
using System;
using Utils;
namespace SortUtils{
  public class SortUtils{
    public static void BubbleSort(int[] arr){
      for(int i = 0; i < arr.Length; i++){
        for(int j = 0; j < arr.Length - i - 1; j++){
          if(arr[j] > arr[j+1]) Utils.Utils.Swap(ref arr[j], ref arr[j+1]);
        }
      }
    }
  }
}
```

```C#
// Utils.cs
using System;
namespace Utils{
  public class Utils{
    public static void Swap(ref int x, ref int y){
      int temp = x;
      x = y;
      y = temp;
    }
    
    public static void PrintArr(int[] arr){
      Console.WriteLine("Sorted Array:");
      for(int i = 0; i < arr.Length; i++){
        Console.Write($"{arr[i]} ");
      }
      Console.Write("\n");
    }
  }
}
```

3.编译生成DLL

```C#
dotnet build
```

4.创建引用DLL的项目

```C#
dotnet new console -o Project -f net7.0
```

5.在项目中引用DLL

```shell
vim Project.csproj
```

在Project标签下添加如下代码

```xml
<ItemGroup>
	<Reference Include="DLL文件名">
		<HintPath>DLL文件路径</HintPath>
	</Reference>
</ItemGroup>
```

6.编写测试代码

```C#
// Program.cs
using System.Text.RegularExpressions;
using Utils;
using SortUtils;

public class Program{
  static void Main(string[] args){
    
    Console.WriteLine("Input Random Numbers:");
    
    string? input = Console.ReadLine();
    
    if(input == null){
      Console.WriteLine("Input Nums Null!");
      return;
    }
    
    Regex regex = new Regex(@"\d+");
    
    MatchCollection matches = regex.Matches(input);
      
    List<int> randomNums = new List<int>();
    
    foreach(Match match in matches){
      if(int.TryParse(match.Value,out int number)){
        randomNums.Add(number);
      }
    }
    
    int[] arr = randomNums.ToArray();
    
    SortUtils.SortUtils.BubbleSort(arr);
    Utils.Utils.PrintArr(arr);
  }
}
```

7.验证 

```C#
dotnet run
```
# Unity中调用托管库


# Unity中调用非托管的动态库

如果想在C#侧使用C++中的一个类的话,需要将这个类的每一个public成员方法封装一个**静态方法**供C#测调用,然后C#侧做一个中间层的封装,即在C#侧将这些静态方法重新封装成类.

以一个Stack结构为例:
## C++侧

```C++
// StackLib.h
#ifndef STACKLIBRARY_H
#define STACKLIBRARY_H

#ifdef _WIN32
    #ifdef BUILDING_DLL
        #define DLL_PUBLIC __declspec(dllexport)
    #else
        #define DLL_PUBLIC __declspec(dllimport)
    #endif
#else
    #if __GNUC__ >= 4
        #define DLL_PUBLIC __attribute__((visibility("default")))
    #else
        #define DLL_PUBLIC
    #endif
#endif

#include <stack>

class Stack {
public:
    Stack();
    ~Stack();

    void Push(int value);
    int Pop();
    bool IsEmpty() const;

private:
    std::stack<int> stack_;
};

// C风格接口导出
#ifdef __cplusplus
extern "C" {
#endif

DLL_PUBLIC Stack* CreateStack();
DLL_PUBLIC void DestroyStack(Stack* instance);
DLL_PUBLIC void Push(Stack* instance, int value);
DLL_PUBLIC int Pop(Stack* instance);
DLL_PUBLIC bool IsEmpty(Stack* instance);

#ifdef __cplusplus
}
#endif

#endif // STACKLIBRARY_H
```

```C++
// StackLib.cpp
#include "StackLib.h"
#include <stdexcept>

// --- Stack的实现 ---

Stack::Stack() {}
Stack::~Stack() {}

void Stack::Push(int value) {
    stack_.push(value);
}

int Stack::Pop() {
    if (stack_.empty())
        return -1; // 或throw std::underflow_error
    int value = stack_.top();
    stack_.pop();
    return value;
}

bool Stack::IsEmpty() const {
    return stack_.empty();
}

// --- C 接口实现 ---
Stack* CreateStack() {
    return new Stack();
}

void DestroyStack(Stack* instance) {
    delete instance;
}

void Push(Stack* instance, int value) {
    if (instance) {
        instance->Push(value);
    }
}

int Pop(Stack* instance) {
    return instance ? instance->Pop() : -1;
}

bool IsEmpty(Stack* instance) {
    return instance ? instance->IsEmpty() : true;
}
```

在Mac上, 导出dylib:
```shell
g++ -std=c++11 -dynamiclib -o libStackLibrary.dylib StackLib.cpp
```

在Windows上, 导出dll:
笔者这里用的是Visual Studio 2022自带的编译器`x64 Native Tools Command Prompt for VS 2022`, 读者使用Windows自带的搜索功能应该是能搜到的, 打开是一个终端, 输入下面命令:

```bat
cl /LD /DBUILDING_DLL=1 StackLib.cpp /Fe:StackLibrary.dll
```

## C#侧

```C#
using System;
using System.Runtime.InteropServices;
using UnityEngine;
using UnityEngine.UI;

namespace CPP
{
    public class StackCaller : MonoBehaviour
    {
        public Text Log;
        
        void Start()
        {
            Log.text = string.Empty;
            Log.text += "StackCaller start\n";
            Debug.Log("StackCaller start");
            
            // 创建栈实例
            var stackWrapper = new StackCPP();

            // 入栈操作
            stackWrapper.Push(10);
            stackWrapper.Push(20);
            stackWrapper.Push(30);

            // 出栈操作并输出结果
            while (!stackWrapper.IsEmpty())
            {
                int value = stackWrapper.Pop();
                Log.text += "Popped value: " + value + "\n";
                Debug.Log("Popped value: " + value);
            }

            // 销毁栈实例
            stackWrapper.DestroyStack();
            Log.text += "Stack destroyed";
            Debug.Log("Stack destroyed");
        }
    }


    public static class StackDLL{
        // 引入C++动态链接库中的函数
        [DllImport("StackLibrary")]
        public static extern IntPtr CreateStack();

        [DllImport("StackLibrary")]
        public static extern void DestroyStack(IntPtr stackWrapper);

        [DllImport("StackLibrary")]
        public static extern void Push(IntPtr stackWrapper, int value);

        [DllImport("StackLibrary")]
        public static extern int Pop(IntPtr stackWrapper);

        [DllImport("StackLibrary")]
        [return: MarshalAs(UnmanagedType.I1)]
        public static extern bool IsEmpty(IntPtr stackWrapper);

    }
    
    public class StackCPP
    {
        private readonly IntPtr stackPointer;
        
        public StackCPP()
        {
            stackPointer = StackDLL.CreateStack();
        }

        public void Push(int value)
        {
            StackDLL.Push(stackPointer, value);
        }

        public int Pop()
        {
            return StackDLL.Pop(stackPointer);
        }

        public bool IsEmpty()
        {
            return StackDLL.IsEmpty(stackPointer);
        }

        public void DestroyStack()
        {
            StackDLL.DestroyStack(stackPointer);
        }
    }   
}
```

## ⚠️注意事项

- `unknown type name '__declspec' 和 unknown type name 'class' 错误`  https://blog.csdn.net/lc250123/article/details/81985336