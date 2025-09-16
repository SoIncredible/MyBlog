---
title: C#问题杂记
tags:
  - C#
categories:
  -  硬技能
cover: https://www.notion.so/images/page-cover/met_jules_tavernier_1878.jpg
abbrlink: '34114324'
date: 2023-04-04 15:09:52
description:
swiper_index:
sticky: 98
---

# 使用C#创建一个文件

遇到 `System.IO.DirectoryNotFoundException` 错误表明尝试访问的目录路径中的某部分不存在。这个问题通常发生在尝试创建文件或目录、读取文件、或者执行其他需要访问文件系统的操作时。针对这个问题，有几个可能的解决方案：

## 确认路径的正确性
首先，确认你尝试访问的路径是正确的。检查路径中的每一部分，确保没有拼写错误或者路径错误。路径应该完全匹配到你想要访问的位置。

## 确认目录结构存在
在尝试创建文件之前，确保文件所在的目录结构已经存在。如果目录结构中的任何部分不存在，尝试创建文件时就会引发 `DirectoryNotFoundException`。

## 创建缺失的目录
如果你的应用程序需要在特定的目录中创建文件，但那些目录可能不存在，你应该首先检查这些目录是否存在，如果不存在，则创建它们。可以使用以下代码段来实现这一点：

```csharp
string directoryPath = @"/Users/mac/Desktop/TetrisBlock/TetrisBlock-Unity/Assets/FontProcessLog/RedundantFontInfo";
string filePath = Path.Combine(directoryPath, "SFUIDisplay-Bold.txt");

// 确保目录存在
if (!Directory.Exists(directoryPath))
{
    Directory.CreateDirectory(directoryPath);
}

// 现在可以安全地创建文件了
File.WriteAllText(filePath, "Your content here");
```

这段代码首先检查目标目录是否存在，如果不存在，则使用 `Directory.CreateDirectory` 方法创建它。这样可以确保在尝试写入文件之前，文件的目标目录已经存在，从而避免 `DirectoryNotFoundException`。

## 使用绝对路径而非相对路径
确保你使用的是绝对路径而非相对路径，因为相对路径可能会因为当前工作目录的不同而导致路径解析错误。在你的例子中，你已经使用了绝对路径，这是一个好的做法。

## 检查权限问题
确保你的应用程序或者你的用户账户有权限访问指定的路径。在某些情况下，权限限制可能会导致无法访问路径。


# 重写、重载、多态的区别

重写 `override` 重载 `overload` 多态`polymorphism`

重写`override`指的是重写了一个方法来实现不同的功能，一般是用于子类在继承父类的时候后，重写父类中的方法，重写的规则：

1. 重写方法的参数列表必须完全与被重写方法的参数列表相同，否则就不能称之为重写而是重载
2. 重写方法的访问修饰符一定要大于被重写方法的访问修饰符public > protected > default > private
3. 重写的方法的返回值必须和被重写方法的返回值一致
4. 重写的方法所抛出的异常必须和被重写方法所抛出的异常一致，或者是其子类
5. 被重写的方法不能是private，否则在其子类中只是新定义了一个方法，并没有对其进行重写
6. 静态方法不能被重写为非静态方法
7. 静态方法属于类，不能被重写，因此也不能多态

重载`overload`一般用于在一个类内实现若干重载的方法，这些方法的名称相同而参数形式不同

重载的规则：

1. 在使用重载时只能通过相同的方法名、不同的参数形式实现。可以是不同的参数个数，不同的参数顺序（参数类型必须不一样）
2. 不能通过访问权限、返回类型、抛出的异常进行重载
3. 方法的异常类型和数目不会对重载造成影响

多态`polymorphism`可以是静态的或动态的，在静态多态性中，函数的响应是在编译的时候发生的。在动态多态性中，函数的响应是在运行时发生的。

静态多态：在编译阶段，函数和对象的连接机制被称为早起绑定，也被称为静态绑定。C#提供了两种技术来实现静态多态性：函数重载和[运算符重载](#2)

动态多态的三个条件：

1. 继承
2. 重写（重写父类继承的方法）
3. 父类引用指向子类的对象（这个在[IEnumerator和IEnumerable的理解与辨析](http://soincredible777.com.cn/posts/133a9667/)这篇博客末尾提到了，当时还不太明确这是实现多态的一个条件），语句在堆内存中开辟了子类的对象，并把栈内存中的父类的引用指向了这个子类的对象

## 函数重载实例

我们可以在同一个范围内对相同的函数名有多个定义，函数的定义必须彼此不同，可以是参数列表中的参数类型不同，也可以是参数个数不同。**不能重载只有返回类型不同的函数声明**

```C#
namespace LearnOverload
{
    public class Overload
    {
        public static int Add(int a, int b, int c)
        {
            return a + b + c;
        }

        public static int Add(int a, int b)
        {
            return a + b;
        }
      
      	public static float Add(float a, float b)
        {
            return a + b;
        }

        static void Main(string[] args)
        {
            int a = 1;
            int b = 1;
            int c = 1;
            Console.WriteLine(Overload.Add(a, b));
            Console.WriteLine(Overload.Add(a, b, c));
        }
    }
}
```

## 动态多态：

C#允许我们使用关键字`abstract`创建抽象类，用于提供接口部分类的实现。当一个派生类继承自该抽象类时，实现即完成。抽象类包含抽象方法，抽象方法可以被派生类实现，派生类具有更专业的功能，我们在使用抽象类的时候要注意以下几点：

- 我们不能创建一个抽象类的实例
- 我们不能在一个抽象类外部声明一个抽象方法
- 通过在类定义前面放置关键字`sealed`，可以将类声明为密封类。当一个类被声明为`sealed`时，它不能被继承。抽象类不能被声明为`sealed`

```C#
namespace PolymorhismApplication
{
    public abstract class Shape
    {
        public abstract int Area();
    }

    class Rectangle : Shape
    {
        private int _length;
        private int _width;

        public Rectangle(int a, int b)
        {
            _length = a;
            _width = b;
        }

        public int Length
        {
            set { _length = value; }
            get { return _length; }
        }

        public int Width
        {
            set { _width = value; }
            get { return _width; }
        }


        public override int Area()
        {
            return _length * _width;
        }
    }

    class RectangleTest
    {
        static void Main(string[] args)
        {
            Rectangle r = new Rectangle(2, 3);
            Console.WriteLine(r.Area());
            Console.ReadKey();
        }
    }
}
```

下面使用了虚方法来实现多态

```C#
namespace PolymorhismApplication
{
    public class Shape
    {
        public virtual int? Area()
        {
            Console.WriteLine("执行了绘制图形的基类");
            return null;
        }
    }

    class Rectangle : Shape
    {
        private int _length;
        private int _width;

        public Rectangle(int a, int b)
        {
            _length = a;
            _width = b;
        }

        public int Length
        {
            set { _length = value; }
            get { return _length; }
        }

        public int Width
        {
            set { _width = value; }
            get { return _width; }
        }


        public override int? Area()
        {
            Console.WriteLine("执行绘制矩形的函数");
            return _length * _width;
        }
    }


    class Circle : Shape
    {
        private int _radius;


        public int Radius
        {
            set { _radius = value; }
            get { return _radius; }
        }


        public Circle(int radius)
        {
            _radius = radius;
        }

        public override int? Area()
        {
            Console.WriteLine("执行Circle类的绘制函数");
            return null;
            return base.Area();
        }
    }

    class Test
    {
        static void Main(string[] args)
        {
            var shapes = new List<Shape>
            {
                new Rectangle(2, 3),
                new Circle(2)
            };

            foreach (var p in shapes)
            {
                p.Area();
            }

            Console.ReadKey();
        }
    }
}
```

联想：在写枚举器那一篇博客的时候，我们自己写枚举器的目的是为了枚举自己自定义的数据类型，而C#中是有List这个数据类型的，我们可以把我们自定义的数据类型放在List里，不也能实现枚举的效果么，那我们自己写枚举器的意义何在呢？

```C#
using System.Collections;

namespace learnIEnumerator
{
    public class Person
    {
        public string Name { set; get; }

        public Person(string name)
        {
            Name = name;
        }

        public void ShowName()
        {
            Console.WriteLine(Name);
        }
    }
    
    class Program
    {
        static void Main(string[] args)
        {
            var ll = new List<Person>
                {
                    new Person("李磊"),
                    new Person("王刚"),
                    new Person("彤彤"),
                    new Person("丹丹"),
                }
                ;
            foreach (Person p in ll)
            {
                Console.WriteLine(p.Name);
            }
        }
    }
}
```

## <span id = "2">运算符重载</span>

我们可以重定义或者重载C#中内置的运算符。因此，我们也可以使用自定义类型的运算符。重载运算符具有特殊名称的函数，是通过关键字`operator`后跟运算符的符号来定义的。于其他函数一样，重载运算符返回类型和参数列表：

```C#
namespace OverloadOperator
{
    public class Box
    {
        private double length;
        private double breadth;
        private double height;

        public double getVolume()
        {
            return length * breadth * height;
        }

        public void setBreadth(double bre)
        {
            breadth = bre;
        }

        public void setLength(double len)
        {
            length = len;
        }

        public void setHeight(double hei)
        {
            height = hei;
        }


        public static Box operator +(Box b, Box c)
        {
            Box box = new Box();
            box.length = b.length + c.length;
            box.breadth = b.breadth + c.breadth;
            box.height = b.height + c.height;
            return box;
        }
    }

    class Tester
    {
        static void Main(string[] args)
        {
            Box Box1 = new Box();
            Box Box2 = new Box();
            Box Box3 = new Box();
            double volume = 0.0;


            Box1.setLength(6.0);
            Box1.setBreadth(7.0);
            Box1.setHeight(5.0);

            Box2.setLength(12.0);
            Box2.setBreadth(13.0);
            Box2.setHeight(10.0);

            volume = Box1.getVolume();
            Console.WriteLine("Box1的体积：{0}", volume);

            volume = Box2.getVolume();
            Console.WriteLine("Box2的体积：{0}", volume);

            Box3 = Box1 + Box2;

            volume = Box3.getVolume();
            Console.WriteLine("Box3的体积：{0}", volume);
            Console.ReadKey();
        }
    }
}
```

上面的代码实现的是我们自己对自定义类Box的加法运算符，它把两个Box对象的属性相加，并返回相加后的Box对象。

# C#中out和ref之间的区别

首先两者都是按地址传递的，使用后都将改变原来参数的数值。

其次，ref可以把参数的数值传递进函数，但是out是要把参数清空，我们无法把一个数值从out传递进去，out进去后，参数的数值为空，我们必须初始化一次。

# C#中的params关键字

params是C#关键字，可变长参数，是在声明方法时参数类型或者参数个数不确定的时候使用的。

关于params参数数组，需要掌握以下几点：

1. 参数数组必须是一维数组
2. 不允许将params修饰符与ref和out修饰符组合起来使用
3. 与参数数组对应的实参可以是同一类型的数组名，也可以是任意多个与该数组元素属于同一类型的变量
4. 如果实参是数组按引用传递，若实参是变量或表达式则按值传递
5. 形式为：方法修饰符 返回类型 方法名(params 类型[] 变量名)
6. params参数必须是参数列表的最后一个参数

```C#
class Program
{
    static void Main()
    {
        UserParams(1, 2, 3);

        int[] myarray = new int[3] { 10, 11, 12 };
        UserParams(myarray);

        UserParams2(1, 'a', "dasd");
    }

    public static void UserParams(params int[] list)
    {
        for (int i = 0; i < list.Length; i++)
        {
            Console.WriteLine(list[i]);
        }

        Console.WriteLine();
    }

    public static void UserParams2(params object[] list)
    {
        for (int i = 0; i < list.Length; i++)
        {
            Console.WriteLine(list[i]);
        }

        Console.WriteLine();
    }
}
```

# 结构体和类的区别

- 关于结构体，C#的结构不同于传统的C或者C++中的结构，它的特点如下：
  - 结构可以带有方法、字段、索引、属性、运算符方法和事件
  - 结构可以自定义构造函数，但是不能定义析构函数。要注意，我们不能为结构定义无参的构造函数，无参的构造函数默认是自动定义的，而且不能被改变
  - 与类不同，结构不能继承其他的结构或类
  - 结构不能作为其他结构或类的基础结构（不能被继承）
  - 结构可以实现一个或多个接口
  - 结构成员不能制定为`abstract`、`virtual`l或者`protected`
  - 当我们使用New操作服创建一个结构对象的时候，会调用适当的构造函数来创建结构。与类不同，结构可以不适用New操作符即可以被实例化
  - 如果不使用New操作符，有在所有的字段都被初始化之后，字符才被赋值，对象才被引用。

- 类和结构的区别
  - 类是引用类型，结构是值类型
  - 结构不支持继承
  - 结构不能声明默认的构造函数

# C#的类修饰符

- `public`：访问不受限制的，所有的本程序集以及其他的程序集里面的类都能够访问
- `internal`：本程序集内的类可以访问，这是类默认的修饰符，在一个程序集内，public和internal的权限是一样的
- `partial`：部分类，可以将一个类分成几部分写在不同的文件中，最终编译时将合成一个文件，并且各个部分不能分散在不同的程序集中
- `abstract`：修饰类的时候表示该类为抽象类，不能够创建该类的实例。修饰方法的时候表示该方法需要子类来实现，如果子类没有实现该方法那么子类同样是抽象类；并且含有抽象方法的类一定是抽象类。
- `sealed`：修饰类表示该类不能够被继承
- `static`：修饰类时表示该类是静态类，不能够实例化该类的对象，那么这个类也就不能够含有对象成员，即该类所有成员为静态。

- `new`：只能用于嵌套的类，表示对继承父类同名类型的隐藏

**C#类修饰符的总结**

- 抽象类就是不能使用new方法进行实例化的类，即没有具体实例对象的类。抽象类有点类似“模板”的作用，目的是根据其格式来创建和修改新的类，对象不能由抽象类直接创建，只可以通过抽象类派生出新的子类，再由其子类来创建对象。当一个类被声明为抽象类时，要在这个类前面加上修饰符abstract
- 在抽象类中的成员方法可以包括**一般方法**和**抽象方法**，抽象方法就是以abstract修饰的方法，这种方法只声明返回的数据类型、方法名称和所需的参数，没有方法体，也就是说冲向方法只需要声明而不需要实现。当一个方法为抽象方法时，意味着这个方法必须被子类的方法所重写，否则其子类的该方法仍然是abstract的，而这个子类也必须是抽象的，即声明为abstract，想要调用**抽象类中的一般方法只能通过定义一个子类并实例化它之后才能调用**。
- 抽象类中不一定包含抽象方法，但是包含抽象方法的类一定要被声明为抽象类。抽象类本身不具备实际的功能，只能用于派生其子类。抽象类中可以包含构造方法，但是构造方法不能被声明为抽象。
- 调用抽象类中的方法（抽象方法和非抽象方法），如果方法是static的，直接使用`抽象类.方法`就可以了，如果是非static的则必须一个继承的非抽象类，然后用这个非抽象类的实例来调用方法。
- 抽象类可以实现接口，接口中的方法，在抽象类中可以不实现，当有子类继承抽象类时，并且子类不是抽象类时，子类需要将抽象类中的抽象方法和接口中的方法都实现。
- 抽象类不能用final来修饰，即一个类不能既是最终类又是抽象类。
- abstract不能与private、static、final、native并列修饰同一个方法。

**TIPS：抽象方法和虚方法都可以供派生类重写，它们的区别如下**

- 虚方法必须有实现部分，并为派生类提供了覆盖方法的选项；抽象方法没有提供实现部分抽象方法是一种强制派生类覆盖的方法，否则派生类将不能被实例化。
- 抽象方法只能在抽象类中声明，抽象方法必须在派生类中重写，这一点跟接口类似；虚方法不是也不必要重写。如果类包含抽象方法，那么该类也是抽象的，也必须声明为抽象的。
- 抽象方法不能声明方法实体，而虚方法可以；包含抽象方法的类不能够被实例化，而包含虚方法的类可以被实例化。

protected、private、protected internal只能用于嵌套的类

为什么要有嵌套类？

嵌套类主要用于当该类仅仅被所在类使用，不需要外部进行显式地构造，并且需要对所在类的成员进行大量访问操作的情况

嵌套类型的访问修饰符为：public、internal、protected、private和protected internal

# 随机数
随机数生成的原理可以分为两类：伪随机数生成（Pseudo-Random Number Generation, PRNG）和真随机数生成（True Random Number Generation, TRNG）。大多数计算机系统使用的是伪随机数生成器，因为它们效率高且易于实现。以下是随机数生成的基本原理和方法。

1. 伪随机数生成（PRNG）
伪随机数生成器是一种算法，它通过数学公式或预定义的规则生成一个看似随机的数列。伪随机数生成器并不是真正的随机，它是确定性的，即只要输入相同的初始值（种子），就会生成相同的随机数序列。

1.1 基本原理
伪随机数生成器通常基于以下公式：

$$ X_{n + 1}  = (a ⋅ X_{n} + c) \space mod \space m$$

其中:
- $X_{n}$ 当前随机数(种子)
- $X_{n+1}$ 下一个随机数
- a 乘数
- c 增量
- m 模数（Modulus）。
- 初始值$X_{0}$ 种子

这个公式被称为线性同余生成器（Linear Congruential Generator, LCG），是最常见的伪随机数生成算法之一。

# int?

`int?`表示可空类型，它是一种特殊的值类型，它的值可以为null，在变量设初值的时候，给变量赋值为null，而不是0

`int??`用于判断并赋值，先判断当前变量是否为null，如果是就可以赋一个新值，否则跳过。注：这是一个右值，不是一个statement。

```c#
public int？ a=null；
public int b()
{
return this.a ?? 0;
}
```

# public class ObjectPool<T> where T : IPoolObject, new() new()的含义是什么?

在 C# 中，`where T : new()` 是一个**泛型约束**，它表示泛型类型参数 `T` 必须具有一个**无参数的公共构造函数**。这意味着使用 `new()` 约束的类型 `T` 必须满足以下条件：

1. `T` 必须是一个非抽象类（即不能是抽象类或接口）。
2. `T` 必须有一个无参数的公共构造函数（即 `public T()`）。

在你的例子中：

```csharp
public class ObjectPool<T> where T : IPoolObject, new()
```

这段代码的含义是：
- `T` 是一个泛型类型参数。
- `T` 必须实现 `IPoolObject` 接口（`where T : IPoolObject`）。
- `T` 必须具有一个无参数的公共构造函数（`where T : new()`）。

---

### **`new()` 的作用**
使用 `new()` 约束后，代码中可以直接使用 `new T()` 来创建类型 `T` 的实例。因为编译器知道 `T` 满足无参数构造函数的约束，所以可以安全地生成实例。

#### 示例代码
```csharp
public interface IPoolObject
{
    void Reset();
}

public class ObjectPool<T> where T : IPoolObject, new()
{
    private List<T> pool = new List<T>();

    public T GetObject()
    {
        if (pool.Count > 0)
        {
            T obj = pool[0];
            pool.RemoveAt(0);
            return obj;
        }
        else
        {
            // 使用 new T() 创建一个新的实例
            return new T();
        }
    }

    public void ReturnObject(T obj)
    {
        obj.Reset();
        pool.Add(obj);
    }
}

public class MyPoolObject : IPoolObject
{
    public void Reset()
    {
        Console.WriteLine("Resetting object...");
    }
}

class Program
{
    static void Main()
    {
        ObjectPool<MyPoolObject> pool = new ObjectPool<MyPoolObject>();

        // 获取对象
        MyPoolObject obj = pool.GetObject();
        Console.WriteLine("Object created");

        // 返回对象
        pool.ReturnObject(obj);
    }
}
```

#### 输出
```
Object created
Resetting object...
```

#### 解释
1. **`new T()`**：在 `GetObject()` 方法中，当池中没有可用对象时，直接通过 `new T()` 创建一个新的对象。
2. **`where T : new()`**：确保泛型类型参数 `T` 有一个无参数的构造函数，因此 `new T()` 是合法的。
3. **`where T : IPoolObject`**：确保 `T` 实现了 `IPoolObject` 接口，因此可以调用 `Reset()` 方法。

---

### **为什么需要 `new()` 约束？**
在 C# 中，泛型类型参数是未知的类型，编译器无法确定类型 `T` 是否具有构造函数。如果你尝试在没有 `new()` 约束的情况下使用 `new T()`，编译器会报错。

#### 错误示例
```csharp
public class ObjectPool<T>
{
    public T CreateObject()
    {
        // 编译错误：T 必须具有无参数构造函数
        return new T();
    }
}
```

#### 错误信息
```
'new()' constraint must be used on type parameter 'T' to create instances of the type.
```

#### 解决方法
添加 `new()` 约束：
```csharp
public class ObjectPool<T> where T : new()
{
    public T CreateObject()
    {
        return new T();
    }
}
```

---

### **`new()` 的限制**
虽然 `new()` 约束允许你使用 `new T()`，但它有以下限制：
1. **只能调用无参数的构造函数**：
   - 如果 `T` 只有带参数的构造函数（例如 `T(int value)`），则无法使用 `new()` 约束。
   - 解决方案是通过工厂模式或委托来创建对象，而不是直接使用 `new T()`。

   ```csharp
   public class ObjectPool<T> where T : IPoolObject
   {
       private Func<T> objectFactory;

       public ObjectPool(Func<T> factory)
       {
           objectFactory = factory;
       }

       public T CreateObject()
       {
           return objectFactory();
       }
   }
   ```

2. **不能用于抽象类或接口**：
   - `new()` 约束要求类型 `T` 是可以实例化的（非抽象类），因此不能直接用于抽象类或接口。

---

### **总结**
- `new()` 是一个泛型约束，表示泛型类型参数必须具有一个无参数的公共构造函数。
- 它允许在泛型类或方法中通过 `new T()` 创建实例。
- 常用于需要动态创建泛型类型实例的场景，比如对象池、工厂模式等。
- 结合其他约束（如接口约束 `where T : IPoolObject`），可以更灵活地定义泛型类的行为。

# 固定长度字符串

如果你希望 `_moveStrus[posY, posX].moveDir` 的输出是一个固定长度的字符串，可以通过格式化字符串的方式来实现。`BrickMoveDir` 是一个枚举值，默认会通过 `ToString()` 输出枚举的名称。如果你希望将其输出为固定长度，可以通过以下方式处理：

---

### **方法 1：使用字符串格式化并填充空格**
你可以用 `String.Format` 或插值字符串（C# 6.0 及以上支持）来对值进行格式化，并通过指定固定宽度填充空格。

#### 示例代码
```csharp
sb.AppendFormat("{0,-10}", _moveStrus[posY, posX].moveDir);
```

- `{0,-10}` 表示将第一个参数的值左对齐，并确保占用 10 个字符的宽度。如果值的长度不足 10，则填充空格；如果值的长度超过 10，则不会截断。

或者使用字符串插值的方式：
```csharp
sb.Append($"{_moveStrus[posY, posX].moveDir,-10}");
```

---

### **方法 2：通过 `PadRight` 或 `PadLeft` 填充**
你可以将 `moveDir` 转换为字符串后，使用 `PadRight`（左对齐）或 `PadLeft`（右对齐）来填充到固定长度。

#### 示例代码
```csharp
sb.Append(_moveStrus[posY, posX].moveDir.ToString().PadRight(10));
```

- `PadRight(10)` 会将字符串填充到 10 个字符宽度，右侧补空格。
- 如果需要右对齐，可以使用 `PadLeft(10)`。

---

### **方法 3：加上 `\t` 制表符**
如果你希望使用制表符（`\t`）来对齐列，可以在每个值后面追加一个 `\t`。不过，制表符的宽度在不同环境下可能会有所不同（通常为 4 或 8 个字符），因此不推荐用于精确对齐。

#### 示例代码
```csharp
sb.Append(_moveStrus[posY, posX].moveDir + "\t");
```

---

### **方法 4：将枚举值转换为整数并格式化**
如果你希望输出的值是固定宽度的数字（而不是枚举名称），可以将 `BrickMoveDir` 转换为整数，再进行格式化。例如，确保输出占用 4 个字符宽度：

#### 示例代码
```csharp
sb.AppendFormat("{0,4}", (int)_moveStrus[posY, posX].moveDir);
```

- `{0,4}` 表示右对齐并占用 4 个字符宽度。

---

### **完整示例**
以下是一个完整的示例，展示如何将 `_moveStrus` 的内容按固定宽度输出到 `StringBuilder` 中：

```csharp
StringBuilder sb = new StringBuilder();
for (int posY = 0; posY < 8; posY++)
{
    for (int posX = 0; posX < 8; posX++)
    {
        // 将 moveDir 输出为固定宽度的字符串（左对齐，宽度 10）
        sb.Append($"{_moveStrus[posY, posX].moveDir,-10}");
    }
    sb.AppendLine(); // 换行
}
Console.WriteLine(sb.ToString());
```

---

### **输出示例**
假设 `_moveStrus[posY, posX].moveDir` 的值为 `Up`、`Down`、`Left` 等，输出可能如下：

```
Up        Down      Left      Right     None      None      None      None      
None      None      None      None      None      None      None      None      
...
```

---

### **总结**
- 如果需要固定宽度的字符输出，推荐使用 **字符串格式化**（`String.Format` 或插值字符串）或 **PadRight/PadLeft**。
- 如果需要使用制表符（`\t`），请注意它的宽度可能因环境而异，不适合精确对齐。
- 如果需要输出数字而非字符串，可以将枚举转换为整数后格式化。


# 浮点数精度问题

https://blog.csdn.net/weixin_42186870/article/details/106086671

```C#
int expPlus = Mathf.FloorToInt((DoushenMgr.Instance.GetTasksExpPlus() - 1.0f) * 100f);
```


# C#中的值类型

foreach与值类型天然不相容, 如果要遍历一个值类型的集合, 那么你只能是去访问这些值, 而不能去改变这些值. 因为 这个结构就会导致 data是从集合中深拷贝出来的, 跟集合里的数据已经没有关系了.
```C#
foreach(var data in dataCollection){

}
```


```C#
public struct Info{
    public string name;
    public int age;
}
```
如果使用一个Collection比如List或者Dictionary遍历由Info构成的集合, 并且尝试修改其中的值, 是不支持的, foreach嘛 如果这是个class就可以
这是值类型和引用类型的本质区别 


# C# CodeGenerator

https://devblogs.microsoft.com/dotnet/introducing-c-source-generators/


# C#Stopwatch
