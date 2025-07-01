---
title: CPP与CS编译生成DLL
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

> C#和C++虽然都能生成DLL, 如果DLL的调用者是C#语言, 那么这两种DLL是有分别的: C#生成的DLL类型是托管类型的DLL, C++生成的DLL是非托管的(原生的)DLL, 前者的DLL导入到C#工程中编译器就能够自动的识别DLL中的成员类型, 后者生成的DLL导入到C#工程中则需要使用`[DLLImport]`Attribute来做一些额外的处理, 并且在C++侧也需要对于要在C#侧调用的方法签名上添加`extern "C"`标识

> 在托管代码中调用非托管代码的开销怎么样?
> 和把所有代码都写在非托管部分的开销对比怎么样?

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

7.验证 //TODO Eddie 刚才在Unity里面验证了, UnityEditor里面应该帮我自动设置了dll的引用关系, 需要看一下非Unity项目下, 导入一个dll是如何关联的. 
非托管的dll在Mac上不能用 
托管的dll可以用
在Mac上打非托管的dll 给Windows平台用有问题吗 我刚才在Windows Arm虚拟机上试的是不行的

```C#
dotnet run
```

# Unity中C#和C++协同

如果想在C#侧使用C++中的一个类的话,需要将这个类的每一个public成员方法封装一个**静态方法**供C#测调用,然后C#侧做一个中间层的封装,即在C#侧将这些静态方法重新封装成类.

以一个Stack结构为例:
## C++侧

```C++
// StackLib.h
#ifndef STACKLIBRARY_H
#define STACKLIBRARY_H

#if defined _WIN32 || defined __CYGWIN__
#ifdef BUILDING_DLL
#ifdef __GNUC__
#define DLL_PUBLIC __attribute__((dllexport))
#else
#define DLL_PUBLIC __declspec(dllexport) // Note: actually gcc seems to also supports this syntax.
#endif
#else
#ifdef __GNUC__
#define DLL_PUBLIC __attribute__((dllimport))
#else
#define DLL_PUBLIC __declspec(dllimport) // Note: actually gcc seems to also supports this syntax.
#endif
#endif
#define DLL_LOCAL
#else
#if __GNUC__ >= 4
#define DLL_PUBLIC __attribute__((visibility("default")))
#define DLL_LOCAL __attribute__((visibility("hidden")))
#else
#define DLL_PUBLIC
#define DLL_LOCAL
#endif
#endif

#include <stack>

// 定义一个结构体来包装栈类
typedef struct StackWrapper
{
    std::stack<int> stack;
} StackWrapper;

class Stack{
    private:
        

    public:
        StackWrapper *stack;

        // 创建栈实例
        Stack *CreateStack();

        Stack();

        // 销毁栈实例
        void DestroyStack(Stack *stackWrapper);

        // 入栈操作
        void Push(Stack *stackWrapper, int value);

        // 出栈操作
        int Pop(Stack *stackWrapper);

        // 判断栈是否为空
        bool IsEmpty(Stack *stackWrapper);
};

#endif // STACKLIBRARY_H
```

```C++
// StackLib.cpp
#include "StackLib.h"

Stack::Stack(){
    stack = new StackWrapper();
}

// 创建栈实例
Stack * Stack::CreateStack()
{
    return new Stack();
}

// 销毁栈实例
void Stack::DestroyStack(Stack* stackWrapper) {
    if (stackWrapper) {
        delete stackWrapper;
    }
}

// 入栈操作
void Stack::Push(Stack *stackWrapper, int value)
{
    if (stackWrapper) {
        stackWrapper->stack->stack.push(value);
    }
}

// 出栈操作
int Stack::Pop(Stack *stackWrapper)
{
    if (stackWrapper && !stackWrapper->stack->stack.empty())
    {
        int value = stackWrapper->stack->stack.top();
        stackWrapper->stack->stack.pop();
        return value;
    }
    return -1; // 表示栈为空
}

// 判断栈是否为空
bool Stack::IsEmpty(Stack *stackWrapper)
{
    if (stackWrapper) {
        return stackWrapper->stack->stack.empty();
    }
    return true;
}
```

```C++
// StackUtils.cpp
#include "StackLib.h"

extern "C" {

    DLL_PUBLIC Stack *CreateStack();
    DLL_PUBLIC void DestroyStack(Stack *stackWrapper);
    DLL_PUBLIC void Push(Stack *stackWrapper, int value);
    DLL_PUBLIC int Pop(Stack *StackWrapper);
    DLL_PUBLIC bool IsEmpty(Stack *StackWrapper);

    Stack* CreateStack(){
        return new Stack();
    }

    void DestroyStack(Stack* stackWrapper){
        stackWrapper->DestroyStack(stackWrapper);
    }

    void Push(Stack* StackWrapper, int value){
        StackWrapper->Push(StackWrapper, value);
    }

    int Pop(Stack* StackWrapper){
        return StackWrapper->Pop(StackWrapper);
    }

    bool IsEmpty(Stack* StackWrapper){
        return StackWrapper->IsEmpty(StackWrapper);
    }
}
```

在Mac上,导出dylib:

```shell
g++ -shared -o StackLibrary.dylib StackLib.cpp StackUtils.cpp   
```

## C#侧

```C#
using System;
using System.Runtime.InteropServices;
using UnityEngine;

namespace CPP
{
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
        private IntPtr _stackPointer;
        
        public void StackCpp()
        {
            _stackPointer = StackDLL.CreateStack();
        }

        public void Push(int value)
        {
            StackDLL.Push(_stackPointer, value);
        }

        public int Pop()
        {
            return StackDLL.Pop(_stackPointer);
        }

        public bool IsEmpty()
        {
            return StackDLL.IsEmpty(_stackPointer);
        }

        public void DestroyStack()
        {
            StackDLL.DestroyStack(_stackPointer);
        }
    }
    
    public class StackCaller : MonoBehaviour
    {
      
        void Start()
        {
         
            Debug.Log("StackCaller start");
            
           
            // 创建栈实例
            StackCPP stackWrapper = new StackCPP();

            // 入栈操作
            stackWrapper.Push(10);
            stackWrapper.Push(20);
            stackWrapper.Push(30);

            // 出栈操作并输出结果
            while (!stackWrapper.IsEmpty())
            {
                int value = stackWrapper.Pop();
                Debug.Log("Popped value: " + value);
            }

            // 销毁栈实例
            stackWrapper.DestroyStack();
        }
    }
}
```

# Unity中C#和C++协同 - Windows DLL编译指南

> Windows中修改了一些头文件的内容，待会看一下具体修改了什么， 然后再尝试一下用修改过的代码在Mac上编译dll，build出来能不能在WindowsArm虚拟机上执行

在Windows上编译C++代码为DLL与Mac上编译dylib有些不同。以下是针对Windows平台的修改和编译指南：

## Windows DLL编译修改

### 1. 修改头文件 (StackLib.h)

确保DLL导出宏正确定义：

```cpp
// StackLib.h
#ifndef STACKLIBRARY_H
#define STACKLIBRARY_H

#ifdef _WIN32
    #ifdef BUILDING_DLL
        #define DLL_PUBLIC __declspec(dllexport)
    #else
        #define DLL_PUBLIC __declspec(dllimport)
    #endif
    #define DLL_LOCAL
#else
    #if __GNUC__ >= 4
        #define DLL_PUBLIC __attribute__((visibility("default")))
        #define DLL_LOCAL __attribute__((visibility("hidden")))
    #else
        #define DLL_PUBLIC
        #define DLL_LOCAL
    #endif
#endif

#include <stack>

// ... 其余代码保持不变 ...
```

### 2. 修改StackUtils.cpp

确保extern "C"部分正确导出函数：

```cpp
// StackUtils.cpp
#include "StackLib.h"

extern "C" {
    DLL_PUBLIC Stack* CreateStack() {
        return new Stack();
    }

    DLL_PUBLIC void DestroyStack(Stack* stackWrapper) {
        if (stackWrapper) {
            delete stackWrapper;
        }
    }

    DLL_PUBLIC void Push(Stack* stackWrapper, int value) {
        if (stackWrapper) {
            stackWrapper->stack->stack.push(value);
        }
    }

    DLL_PUBLIC int Pop(Stack* stackWrapper) {
        if (stackWrapper && !stackWrapper->stack->stack.empty()) {
            int value = stackWrapper->stack->stack.top();
            stackWrapper->stack->stack.pop();
            return value;
        }
        return -1; // 表示栈为空
    }

    DLL_PUBLIC bool IsEmpty(Stack* stackWrapper) {
        if (stackWrapper) {
            return stackWrapper->stack->stack.empty();
        }
        return true;
    }
}
```

## Windows DLL编译方法

有几种方法可以在Windows上编译DLL：

### 方法1: 使用Visual Studio命令行工具

1. 打开"x64 Native Tools Command Prompt for VS 20XX"（根据你的Visual Studio版本）
2. 导航到源代码目录
3. 执行以下命令：

```bat
cl /LD StackLib.cpp StackUtils.cpp /FeStackLibrary.dll /I.
```

### 方法2: 使用MinGW (GCC for Windows)

如果你安装了MinGW，可以使用：

```bat
g++ -shared -o StackLibrary.dll StackLib.cpp StackUtils.cpp -DBUILDING_DLL
```

### 方法3: 使用Visual Studio IDE

1. 创建新的"Dynamic-Link Library (DLL)"项目
2. 添加你的源代码文件
3. 确保项目属性中：
   - 配置类型设置为"Dynamic Library (.dll)"
   - 在预处理器定义中添加`BUILDING_DLL`

## C#侧调用DLL的注意事项

在Windows上，C#调用DLL需要使用`DllImport`特性：

```csharp
using System;
using System.Runtime.InteropServices;

public class NativeStack
{
    [DllImport("StackLibrary.dll", CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr CreateStack();
    
    [DllImport("StackLibrary.dll", CallingConvention = CallingConvention.Cdecl)]
    public static extern void DestroyStack(IntPtr stack);
    
    [DllImport("StackLibrary.dll", CallingConvention = CallingConvention.Cdecl)]
    public static extern void Push(IntPtr stack, int value);
    
    [DllImport("StackLibrary.dll", CallingConvention = CallingConvention.Cdecl)]
    public static extern int Pop(IntPtr stack);
    
    [DllImport("StackLibrary.dll", CallingConvention = CallingConvention.Cdecl)]
    public static extern bool IsEmpty(IntPtr stack);
}
```

## 建议改进

1. **简化设计**：你的设计中有双重封装（StackWrapper和Stack），可以简化为直接使用Stack类
2. **错误处理**：添加更完善的错误处理机制
3. **内存管理**：确保所有分配的内存都被正确释放
4. **跨平台兼容**：考虑使用CMake来统一管理不同平台的构建过程

希望这些信息能帮助你在Windows上成功编译和使用DLL！

# 一些Unity中无法内置的dll的处理
https://blog.csdn.net/lanchunhui/article/details/53239441

https://zh.wikipedia.org/zh-hans/%E6%AD%A3%E6%80%81%E5%88%86%E5%B8%83

https://blog.csdn.net/qq_17347313/article/details/106995687
C#中正态分布的第三方库

# 正态分布

## 标准正态分布

# 正偏态分布

# 累积分布函数

## ⚠️注意事项

- `unknown type name '__declspec' 和 unknown type name 'class' 错误`  https://blog.csdn.net/lc250123/article/details/81985336