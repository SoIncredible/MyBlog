---
title: UnityEditorIMGUI开发
categories: UnityEditor
abbrlink: 7f55a4b3
date: 2025-09-09 09:05:37
tags:
cover:
description:
swiper_index:
sticky:
---


# 何为IMGUI

 IMGUI即Immediate Mode GUI 随着Unity的版本更新, Unity官方逐渐抛弃IMGUI, 转用UITookit
# 一些疑问
# 1. Editor GUI（OnGUI）模式

Unity 的 Editor 扩展（EditorWindow、Editor、CustomInspector 等）使用的是**Immediate Mode GUI**，即 IMGUI。

- 在 IMGUI（OnGUI）里，每次要显示控件都必须重新绘制一遍，所以 _textArea = GUILayout.TextArea(_textArea); 这种写法是**必要的**，也是推荐的。
- 只要你需要文本输入框能保存、显示、修改文本，就必须把每帧 OnGUI 返回的最新字符串赋值回来，否者控件无法响应用户输入。
- OnGUI 并不是像 Update 那样只做逻辑运算，它是“每帧重画+同步最新值”的机制。

**这个流程虽然“每帧赋值”，但实际上是：**
- 用户输入 → 控件更新，控件的内容通过 TextArea 的返回值传递出来。  
- 你用一个变量保存起来，下次再传进去，循环往复。

实际上，这样的“赋值”并不是真正“每帧都做了无用功”，它不涉及复杂逻辑，只是保证数据同步，很轻量。

---

# 2. UGUI（UnityEngine.UI）模式

UGUI 使用的是**Retained Mode GUI**，即“保持式 GUI”。
- UGUI（比如 InputField）是 GameObject，上面挂着脚本组件（InputField、Text 等），值都是挂在对象内部，“实时绑定、事件驱动”，只有当用户输入发生变化时才会更新内部值。
- 你**不需要每帧赋值 InputField.text**，通常只在初始化或特定逻辑中赋值，或者通过事件（onValueChanged）处理输入变化。

例如：

```csharp
public InputField input;
private void Start()
{
    input.onValueChanged.AddListener(OnInputChanged);
}

void OnInputChanged(string newText)
{
    // 这里只在用户输入改变时触发一次
}
```
每帧 Update 时 **并不需要 assign**。

---

## 3. 为什么 Editor 要这样写？

因为 IMGUI 的机制只能你自己手动接管“可视控件内容”和“你的变量”，
每帧同步其实只是“状态同步”，并不是做大量冗余逻辑。所以**IMGUI风格就该每帧赋值；UGUI则用事件驱动。**

---

## 4. 有没有“更优雅”的写法？

- 在 Editor .OnGUI，没有更优雅的方案，这就是官方推荐用法（**赋值其实就1条指令，性能压力很小**）。
- 如果你只想提高代码“观感”，可以包装一层函数，但底层逻辑还是要你管数据同步。
- UGUI、EditorGUILayout、Runtime GUI，没有更优雅方案，因为机制不同。

---

## 结论

- **Editor IMGUI 就要每帧赋值，这是机制要求。**
- **UGUI/InputField等只在需要时赋值，不必每帧。**
- **赋值本身只是同步文本状态，不是无用功，也不影响性能。**

---

### 延伸阅读

官方说明：  
[IMGUI Vs Retained Mode](https://docs.unity3d.com/Manual/UI-system-compare.html)  
[Editor scripting: Immediate/Retained GUI](https://docs.unity3d.com/ScriptReference/GUILayout.TextArea.html)

---
**简明一句话：IMGUI 每帧赋值是必要的，就是它的机制设计；UGUI只在需要转移数据时才赋值，不是每帧。**

# GUI都有哪些控件

- Button 在用户点击按钮**（按下并抬起）**时，GUILayout.Button 返回 true
- RepeatButton 在用户按住按钮时，GUILayout.RepeatButton 每帧都返回 true。
- Label
- TextArea
- Toggle
- Box
# GUIStyleState

# GUIContent

看一下源码就可以发现, 当我们调用`GUI.Label("Hello")`这样的接口时, GUI内部都是将传入的字符串专成一个GUIContent. 这是一种便捷的写法

GUIContent决定渲染什么
GUIStyle决定如何渲染

# GUIStyle

Style

有一些代表状态的字段 这些状态是给固定的控件使用的.
- active Button
- onHover 

GUIStyle像是一个上下文, 或者状态机里面的Blackboard, 所有的UI控件, 能够呈现的样式, GUIStyle中的字段全部包含, 一个UI控件能表现的效果所需要的字段, 只是GUIStyle的子集.

比如GUI.Label


# 布局对齐有两种范畴

第一是一个组件内 比如Box组件内部的Text和Icon 使用GUIStyle的alignment来对齐, 第二是整个Box组件在窗口中的对齐方式, 使用GUILayout.FlexibleSpace() + GUILayout.BeginHorizontal()来实现布局

# GUI、EditorGUI、GUILayout、EditorGUILayout、EditorGUIUtility、GUIUtility

GUI附带的还有GUIGroup、GUIStyle等 EditorGUI好像没有?
在UnityEditor开发中下面这些类型都是可以用的
https://docs.unity3d.com/ScriptReference/GUI.html
https://docs.unity3d.com/ScriptReference/GUIContent.html
https://docs.unity3d.com/ScriptReference/GUISkin.html
https://docs.unity3d.com/ScriptReference/GUIStyle.html
https://docs.unity3d.com/ScriptReference/EditorGUI.html
https://docs.unity3d.com/ScriptReference/EditorStyles.html

# EditorToolBar
