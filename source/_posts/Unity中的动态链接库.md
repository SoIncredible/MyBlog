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

# 啥是动态链接库

如果你在搜索引擎上直接搜索动态链接库的定义, 搜索结果大部分都会与[Microsoft以及Windows平台有关](https://learn.microsoft.com/zh-cn/troubleshoot/windows-client/setup-upgrade-and-drivers/dynamic-link-library): 动态链接库（Dynamic Link Library，DLL）是微软公司在Windows操作系统中实现共享函数库的一种机制，旨在解决静态库代码冗余问题。其核心原理是程序编译时仅标记所需库文件，运行时由系统加载器动态载入内存，实现多程序共享同一份库代码，减少资源占用。在非Windows平台上也存在和动态链接库一样共享代码的机制, 只是不叫动态链接库罢了. 笔者在这里想讨论是DLL这种代码共享的思想机制, 不局限在Windowsw平台上, 因此下文中任何平台的代码共享库笔者全部称之为DLL或动态库.

动态链接库分为两种: 由**原生语言(Native Languagege)*编写并编译的**原生DLL**和由**托管类语言(Managed Language)**编写并编译的**托管DLL**.
原生DLL是传统意义上的DLL, 这类DLL在不同的平台有各自动态链接库的文件格式. 在Windows上是`.dll`, 在Mac上是`.dylib`, 在Linux上是`.so`. 原生语言编写出来的程序，经过编译后直接生成当前操作系统和硬件平台的“本地机器码”（Native Code）。程序无需额外的“虚拟机”、“解释器”、“运行时中间层”就能直接在操作系统之上被加载和运行。原生语言天然与操作系统、硬件架构强相关。以下语言都是原生语言: C、C++、Objective-C（macOS/iOS下）、Rust（默认就是直接编译本地机器码）、Go语言（Go 1.5以后完全支持无需虚拟机，能直接编译原生机器码）、Fortran、Delphi/Pascal（比如 Embarcadero Delphi）、Zig、Assembly 汇编（最彻底的native code）、Swift（编译模式不同，本地或托管两种，主流iOS开发是本地的）.

托管DLL与原生DLL不同, C#和Java都是托管语言, 因为它们的编译器并不是直接将它们编译成机器码, 而是有一个中间态, 比如C#的中间态就是IL, 然后运行时由CLR解析执行IL代码, Java的JVM应该也是类似的机制. 因此, 使用C#等托管语言导出的dll并不是传统意义上的DLL.

# 在Unity中使用DLL

我们先来说托管代码, 笔者作为Unity开发者, 就以C#为例来讲, C#语言编译导出的dll, 本质上是一组程序集编译导出的IL代码集合, 根据上文的描述, C#的编译其实不受平台的限制, 这点很好理解, 因此各个平台的C#动态库的文件扩展名都是`.dll`. 因此, **托管类型的.dll格式的动态库是可以在非Windows平台上被正确识别和执行的**, 因此随着.Net支持跨平台, .dll这种文件格式也就出现在了各个平台上, 因此当你在某一个非Windows平台上看到了一个.dll文件被使用并正确执行, 很有可能这是一个C#dll. 当然这是有前提的: 这**类dll必须在.Net环境下才能够正确的执行**. UnityEditor显然是.Net(Mono)环境, 因此C#类型的dll导入进Unity之后就能正确识别并执行. 我们的业务代码可以直接访问C#dll中的成员.

至于非托管的动态库, 想要在Unity开发中使用就十分麻烦了(不只是Unity, 所有要跨平台的开发调用非托管动态库都十分麻烦), 因为非托管的动态库与平台强绑定, 如果要做跨平台开发, 就需要针对每一个平台构建专门的动态库, 光这一点就十分麻烦, **应当在各自目标平台上构建动态库, 而不是在一个平台上使用交叉编译的方式构建其他平台的动态库.** 除此之外, 还需要开发者使用`[DLLImport]`属性在C#层手动做一下桥接.

下面是笔者尝试在Unity中构建一个支持在Arm版本的Windows和Arm版本的Mac平台上调用原生DLL的尝试. 最后笔者会总结整个过程中容易出错的点.

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


> 💡 在托管代码中调用非托管代码的开销怎么样?
> 和把所有代码都写在非托管部分的开销对比怎么样?
> 你的这个问题非常有代表性！
**总结一句话：**在你给的场景下，

原生纯C#代码效率更高、且更易维护。
具体原因

1. C# 纯托管代码

Unity 内部的 C# 代码是编译为 IL（中间语言）字节码，然后由 JIT（或 IL2CPP 时为 AOT，本地机器码）执行。
.NET/Mono 对容器如 System.Collections.Generic.Stack<T> 优化很好，内存布局高效，垃圾回收管理。
函数调用、数据操作全在同一运行时上下文内，不涉及任何跨语言、跨运行时的开销。
2. 跨语言 P/Invoke（你的C++ DLL 方案）

每次调用 DLL 的函数（即 C++ 实现的栈）都需要一次 P/Invoke（平台调用）过程：
C# -> 托管到非托管转换（参数marshal，栈检查，引用计数，线程切换等）。
指针/数据类型需要转码，尤其遇到结构体、数组、复杂类型时消耗更大。
栈的数据存储在 C++ 的堆空间：
如果你的栈只是管理 int（基础类型）还好，一旦涉及 string/object/struct，这些类型的 marshal 成本和 bug 风险会更高。
Python/.NET/Java 等现代 JIT 容器同样如此，调用 C/C++ 动态库属于“昂贵操作”，普通业务逻辑不推荐用。
3. 性能实际对比（总结）

若仅存储/操作 int，且每次入栈/出栈调用都要走 P/Invoke，C# 纯代码比你这样DLL调用快出一个（甚至数个）量级。
通常 P/Invoke 一次开销可能相当于数十到百次的 C# 方法调用！
只有当你批量性、长计算、并且纯本地重计算数据时（比如大块音视频处理/加密/AI大循环），C++ DLL 才有优势。
4. 实际维护与跨平台

C++ DLL 要单独为 Windows/Mac/Linux/ARM 编译多套，代码维护麻烦，不如 C# 直接可移植性好。
一些平台如 iOS/主机（Switch/PS/Xbox）原生 DLL 支持还有限制。
结论

比较项	纯C# Stack	P/Invoke+C++ Stack
性能	更高（无调用/传参开销）	更低（每次P/Invoke有显著性能损耗）
灵活性	高	低，维护多端库困难
跨平台性	高	需要多平台分别编译
安全性	托管，易debug/gc管理	内存泄漏、资源安全需谨慎
合适场景	常规业务、绝大部分算法	高性能、本地专有硬件/遗留代码
什么时候才选 C++ DLL？

涉及必须使用 C/C++（如已有遗留算法、调用独立专利库、或 Unity 无法实现的特性/硬件IO/第三方SDK）。
性能瓶颈点全部在DLL方法内部，且调用次数（P/Invoke）不是瓶颈。如果像音频解码、图片解码那样一调用能处理成百上千数据，可以摊薄调用损耗。
跟其他C++工程重用，或要接入平台专属硬件API（如iOS/Android的框架库）。
补充演示

你可以自己微型测试一下：

var sw = Stopwatch.StartNew();
for(int i=0; i<10000; ++i) stack.Push(i); // C#内建Stack或List
sw.Stop();
Debug.Log("C# stack 用时：" + sw.ElapsedMilliseconds);

sw.Restart();
for(int i=0; i<10000; ++i) StackDLL.Push(dllStack, i); // 你的DLL P/Invoke
sw.Stop();
Debug.Log("DLL stack 用时：" + sw.ElapsedMilliseconds);
通常C# Stack会胜出很多倍。

总结

本例里，纯C#栈的效率明显高于调用C++ DLL。
DLL适用于必须利用C/C++优势/平台/性能的极特殊场景。
如需测量实际数据、代码微调或探讨更复杂场景，欢迎追问！



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

# [DllImport("__Internal")]
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

## ⚠️注意事项

- `unknown type name '__declspec' 和 unknown type name 'class' 错误`  https://blog.csdn.net/lc250123/article/details/81985336