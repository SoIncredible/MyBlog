---
title: 对C#中类型转换和拆装箱的思考
abbrlink: fad28a7c
date: 2025-04-26 11:18:44
tags: C#
categories:
cover: https://public.ysjf.com/mediastorm/material/material/%E8%87%AA%E7%84%B6%E9%A3%8E%E5%85%89_%E9%98%BF%E7%8E%9B%E7%9B%B4%E7%B1%B3_4_%E5%85%A8%E6%99%AF.jpg
description:
swiper_index:
sticky:
---

# 类型转换的开销来自哪里?

类型转换在时间和空间上都会造成开销, 因为类型转换C#编译器会生成额外的类型转换处理代码, 导致代码文件的体积变大. 既然有额外的IL代码生成则就需要有额外的时间去执行. 如果类型转换中涉及到装拆箱操作, 则还会对运行时内存产生影响.

- CLR会生成额外IL代码执行类型转换是否合法的判断逻辑, 这部分开销是不可避免的, 即便开发者笃定类型转换必定合法. 不过这部分开销的影响并不大. 如果类型转换判定为不合法, 则需要额外的开销来处理异常, 我们通常会有两种类型转换的方式, 两种转换方式不合法的处理的开销不同:
  - 使用类型强转, 即`var a = (someType)b`形式, 这种方式在转换不合法的时候会抛出异常, 开销较大.
  - 使用`as`操作符,即`var a = b as someType`, 这种方式在转换不合法的时候会将a字段设置为`null`,开销小. 更推荐使用这种方式.
  不过如果你真的笃定类型转换不会出错, 那么以上这两种类型转换的方式其实影响不大.
- 拆箱装箱造成的开销, 请记住这句话:**装箱拆箱一定是类型转换造成的, 但是类型转换不一定会导致装箱拆箱** 装拆箱详解请继续往下看.

# 值类型与引用类型

值类型创建在栈上, 堆类型创建在堆上 这句话是不完全对的, 更准确的描述是: 引用类型一定创建在堆上, 值类型既可以创建在堆上, 也可以创建在栈上, 值类型创建在栈还是堆上取决于它所在的容器: 如果值类型是引用类型的一个成员字段, 则该值类型创建在堆上, 如果该值类型是一个局部变量(临时变量)或者是一个是另外一个值类型的字段成员的话, 则创建在堆上.

# 装箱拆箱的开销来自哪里

**装箱过程发生了什么**
1. 在托管堆中分配内存, 分配的内存量是值类型各字段所需的内存量, 还要加上托管堆所有对象都有的两个额外成员(类型对象指针和同步块索引)所需的内存量
2. 值类型的字段复制到新分配的堆内存
3. 返回对象地址. 现在该地址是对象引用; 值类型成了引用类型

```c#
object obj = 10;
```

**拆箱过程发生了什么**
请反复阅读并理解这句话: **拆箱不是直接将装箱过程倒过来, 拆箱过程本身不会复制任何类型, 但是拆箱过后往往跟随着一次字段的复制.**
```
object obj = 10; //装箱

int a = (int)b // 拆箱 + 字段的复制
```
`(int)b`部分完成了拆箱操作, `int a =`部分完成了字段复制操作, 我们在写代码的时候很自然而然地会这么写. 

另外，值类型装箱后是不能改变它的值的，装箱后的值具有恒定性（Immutable）的特点，如果想给引用类型的变量赋予新的值，那就需要再堆上另开辟新的内存空间，一旦一个值类型被装箱，它的值就没有办法改变了. 看下面的代码:
```
struct ValueType
{
    public int Value;
}

public void SomeFunc()
{
    var value = new ValueType
    {
        Value = 100
    };

    object vv = value;
    
    ((ValueType)vv).Value = 200; // 这里会报错Cannot modify struct member when accessed struct is not classified as a variable

    var vvv = ((ValueType)vv); // 不会报错
    vvv.Value = 200;
}
```
可以看到, 如果只是进行拆箱操作, 我们无法对拆箱后的字段做任何修改, 并报错提示拆箱后的值成员并没有分类为变量, 我们必须将拆箱后的数据复制给一个值类型变量, 在这个变量上进行修改, 不过这个变量是拆箱数据的拷贝, 拆箱的数据没有任何关系. 所以上面才说**拆箱过后往往跟随着一次字段的复制**

# 反复地拆装箱

反复地拆装箱会产生额外的IL代码, 增加代码体积. 更大的问题是, 额外的装箱步骤会在托管堆中分配额外的对象, 将来必须对其进行垃圾回收. 拆装箱的滥用会严重影响程序的性能和内存消耗.
如果我们想修改装箱后的值类型的内容, 虽然笔者想不出实际开发中会有这种场景, 我们就必须先将其拆箱, 接着复制给一个值类型变量, 修改值类型变量的内容, 将值类型变量装箱, 最后把指向原来装箱值类型在堆上地址的引用指向的地址改为指向新的值类型变量装箱后所在堆上的地址. 那么原来那一个被装箱的值类型在堆上占据的内容空间就废弃了, 在未来需要进行垃圾回收. 这也就是为什么装箱拆箱会引起GC。

# 小节
在实际写代码的过程中, 拆装箱问题并不常见, 因为大部分开发者想都不想只会把class传来传去, 而且我们肯定不会闲得声明一个object类型的字段, 却用一个值类型的数据给它赋值. 所以类型转换中主要的性能开销**在于CLR会生成额外IL代码执行类型转换是否合法的判断逻辑**(程序员一般情况下还是可以保证类型转换是合法的). 

# 其他要注意的点

**类型检查中要注意的点**
使用`is`操作符和`.GetType()`接口都可以做类型检查, 但是两者的开销是不同的, 按照GPT的说法, `is`操作符的性能开销要小于`.GetType()`的开销:
- 使用`is`操作符, C#编译器生成直接的类型检查指令(isinst IL指令), 无需触发完整的类型元数据加载. `is`操作符会利用类型继承关系进行快速判断, 无需获取完整的Type对象, 值类型优化: 如果转换后的类型是值类型, is会避免装箱, 前提是obj是值类型且类型兼容
- 使用`.GetType()`时, 会触发完整类型元数据加载, 需要访问对象的Type对象, 涉及元数据查询
精确类型比较: GetTypE()返回的是对象的实际运行时类型, 与typeof(MyType)比较时,只有当obj的运行时类型完全等于MyType时才返回true, `.GetType()`是虚方法调用, 需经过虚方法表查找.

当然, `.GetType`也有其用武之处, 因为在有些场景下确实是需要精确查找的, 继承关系A->B->C, A是基类, 如果传入的实例是C, 使用`is B`返回的结果是true, 而使用`.GetType() == typeof(B)` 返回的是false. 实际开发中大部分场景下`is`操作符就足够完成需求了.

**typeof、GetType()、is 和 as 这四种类型判断操作的开销对比**
| 操作      | 开销来源                         | 适用场景                 | 示例代码                              |
| --------- | -------------------------------- | ------------------------ | ------------------------------------- |
| typeof(T) | ⚡️ 编译时静态解析（零运行时开销） | 编译时已知类型           | if (type == typeof(MyClass))          |
| is        | ⚡️ 单次类型检查（无转换）         | 安全类型检查             | if (obj is MyClass)                   |
| as        | ⚡️ 单次类型检查 + 返回转换结果    | 安全类型转换             | var x = obj as MyClass;               |
| GetType() | ⚠️ 访问对象类型句柄 + 元数据查找  | 需获取对象实际运行时类型 | if (obj.GetType() == typeof(MyClass)) |



**值类型、引用类型与闭包**

因为值类型的生命周期会随着其作用域的结束而释放掉, 但是引用类型不会, 引用类型释放掉的只是指向堆上内存位置的指针而已. 本来笔者计划使用闭包的方式把两种类型带出其各自的作用域, 不过实操下来, 两种类型里面的数据都是正常读取的. 据GPT说值类型的闭包是将数据复制了一份传入到了闭包中, 因此在原值类型数据生命周期外数据能正确读取, 而引用类型则是在其生命周期结束之后, 真正的数据还都存放在堆上呢根本就没释放掉呢, 因此可以正确读取, 不过笔者个人猜测, 引用类型本身(或者叫创建在线程栈上指向堆中数据的指针)在生命周期结束后其实也被释放掉了, 在闭包中也是传入了这个引用类型本身的一份拷贝而已. 但是笔者在这里就不深究了.

# 更优雅的类型转换

按照GPT的说法, 泛型是一种能够规避类型转换、且保证类型安全的双赢方法, 泛型的类型转换并不是在运行时做的, 而是在编译时编译器静默地进行了类型转换, 因此泛型在运行时是没有额外开销的. 

下面看两组代码的对比:
```
// 非泛型接口
public interface IUILogic
{
    void OnShow(object data); // 数据用 object 传递
    void OnClose();
}

// 非泛型基类
public abstract class BaseEUI : MonoBehaviour, IUILogic
{
    public abstract void OnShow(object data);
    public abstract void OnClose();
}

// 具体 UI 类（需手动转换数据）
public class PlayPage : BaseEUI
{
    public override void OnShow(object data)
    {
        var showData = (PlayPageShowData)data; // 运行时转换
        // 实际逻辑...
    }

    public override void OnClose() { }
}

// UI 管理器
public class UIMgr
{
    public static UIMgr Instance { get; } = new UIMgr();
    private Dictionary<Type, BaseEUI> _uiInstances = new Dictionary<Type, BaseEUI>();

    public void ShowUI(Type uiType, object data)
    {
        if (_uiInstances.TryGetValue(uiType, out var ui))
        {
            ui.OnShow(data); // 非泛型调用
        }
        else
        {
            var obj = Resources.Load("");
            var go = Object.Instantiate(obj) as GameObject;
            ui = go.AddComponent(uiType) as BaseEUI;
            ui.OnShow(data);
            _uiInstances.Add(uiType, ui);
        }
    }
}

// 调用示例
var param = new PlayPageShowData();
UIMgr.Instance.ShowUI(typeof(PlayPage), param); // 需显式传递 Type
```

```
// 泛型接口（类型安全）
public interface IUILogic<TUIShowData> where TUIShowData : struct
{
    void OnShow(TUIShowData data); // 明确数据类型
    void OnClose();
}

// 泛型基类
public abstract class BaseEUI<TUIShowData> : MonoBehaviour, IUILogic<TUIShowData> 
    where TUIShowData : struct
{
    public abstract void OnShow(TUIShowData data);
    public abstract void OnClose();
}

// 具体 UI 类（无需手动转换数据）
public class PlayPage : BaseEUI<PlayPageShowData>
{
    public override void OnShow(PlayPageShowData data) // 直接使用具体类型
    {
        // 直接访问 data 的字段，无需类型转换
    }

    public override void OnClose() { }
}

// UI 管理器（泛型版）
public class UIMgr
{
    public static UIMgr Instance { get; } = new UIMgr();
    private Dictionary<Type, object> _uiInstances = new Dictionary<Type, object>();

    // 泛型方法：类型安全且无装箱
    public void ShowUI<TUI, TUIShowData>(TUIShowData data)
        where TUI : BaseEUI<TUIShowData>, new()
        where TUIShowData : struct
    {
        if (_uiInstances.TryGetValue(typeof(TUI), out var ui))
        {
            ((IUILogic<TUIShowData>)ui).OnShow(data); // 需一次接口转换
        }
        else
        {
            var obj = Resources.Load("");
            var go = Object.Instantiate(obj) as GameObject;
            var uiLogic = go.AddComponent<TUI>();
            uiLogic.OnShow(data);
            _uiInstances.Add(typeof(TUI), uiLogic);
        }
    }
}

// 调用示例
var param = new PlayPageShowData();
UIMgr.Instance.ShowUI<PlayPage, PlayPageShowData>(param); // 编译时类型检查
```

对比上面两组代码, 非泛型方式打开一个UI要经过4次类型转换, 而且还有装箱和拆箱的风险; 而泛型版本打开一个UI只需要进行2次类型转换 

**非泛型和泛型方案对比**
| 特性         | 非泛型方案                        | 泛型方案 (IUILogic<T>)           |
| ------------ | --------------------------------- | -------------------------------- |
| 类型安全     | ❌ 运行时可能 InvalidCastException | ✔️ 编译时检查                     |
| 数据传递效率 | ⚠️ 值类型会装箱（struct → object） | ✔️ 无装箱（直接传递 struct）      |
| 代码复杂度   | ✔️ 更简单                          | ❌ 需要泛型约束和类型参数         |
| 性能开销     | ⚠️ 装箱/拆箱 + 类型转换            | ⚡️ 无额外开销（除可能的 as 转换） |
| 扩展性       | ❌ 新增 UI 需手动维护数据类型      | ✔️ 自动适配不同 TUIShowData       |



**注意** 泛型确实是一种十分优雅的类型转换方式, 但是妄图使用泛型替代所有的类型转换是不可能的. 泛型在一定程度上破坏了突破了面向对象的继承结构的限制, 但是你想要在面向对象的编程语言中编写非面向对象的代码是十分困难的, 看上面UIMgr代码的例子你就知道了, UIMgr作为所有UI的管理者, 它管理者一类UI, 为了实现管理一组对象的效果, 这一组对象在UIMgr的视角下必须是同一种类的, 也就是它们要具有同一父类, 因此即便泛型打破了继承关系, 但是在管理一组对象这种领域还是无法替代普通类型转换, 所以在由上层管理到底层执行之间有一个不可避免的类型转换, 不过这一次类型转换是必定成功的, 这是由上层管理和底层具体执行视角不同导致的.

# 总结

因此, 滥用object作为接口的参数并不是一种优雅的方式, 类型转换在实际开发中不可避免, 但我们还是可以通过泛型等方式尽可能地减少类型转换的开销.