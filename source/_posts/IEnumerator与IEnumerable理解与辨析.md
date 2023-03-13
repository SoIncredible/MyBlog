---
title: IEnumerator与IEnumerable理解与辨析
tags:
  - C#
categories: 学习笔记
abbrlink: 133a9667
date: 2023-03-13 15:49:55
cover: "http://soincredible777.com.cn:90/13.png"
description:
swiper_index:
sticky:
---

# 背景

编写的几乎所有程序都需要循环访问集合，因此需要编写代码来检查集合中的每一项。

迭代器可以读取集合中的数据，但是不能从底层修改集合

语法糖：计算机中添加某种语法，这种语法对语言功能没有影响，但是更方便程序员使用，语法糖让代码更加简洁，有更高的可读性

由迭代器引出了更多的基础知识：什么是接口？https://www.cnblogs.com/binyao/p/4891306.html 很好的一篇博客

# C#的类修饰符

- `public`：访问不受限制的，所有的本程序集以及其他的程序集里面的类都能够访问
- `internal`：本程序集内的类可以访问，这是类默认的修饰符
- `partial`：部分类，可以将一个类分成几部分写在不同的文件中，最终编译时将合成一个文件，并且各个部分不能分散在不同的程序集中
- `abstract`：修饰类的时候表示该类为抽象类，不能够创建该类的实例。修饰方法的时候表示该方法需要子类来实现，如果子类没有实现该方法那么子类同样是抽象类；并且含有抽象方法的类一定是抽象类。
- `sealed`：修饰类表示该类不能够被继承
- `static`：修饰类时表示该类是静态类，不能够实例化该类的对象，那么这个类也就不能够含有对象成员，即该类所有成员为静态。

- `new`：只能用于嵌套的类，表示对继承父类同名类型的隐藏

**C#类修饰符的总结**

- 抽象类就是不能使用new方法进行实例化的类，即没有具体实例对象的类。抽象类有点类似“模板”的作用，目的是根据其格式来创建和修改新的类，对象不能由抽象类直接创建，只可以通过抽象类派生出新的子类，再由其子类来创建对象。当一个类被声明为抽象类时，要在这个类前面加上修饰符abstract
- 在抽象类中的成员方法可以包括一般方法和抽象方法，抽象方法就是以abstract修饰的方法，这种方法只声明返回的数据类型、方法名称和所需的参数，没有方法体，也就是说冲向方法只需要声明而不需要实现。当一个方法为抽象方法时，意味着这个方法必须被子类的方法所重写，否则其子类的该方法仍然是abstract的，而这个子类也必须是抽象的，即声明为abstract。
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

C#中类的构造函数、析构函数等

https://www.runoob.com/csharp/csharp-class.html

接口与类（抽象类）的区别

**接口是对动作的抽象表示这个对象能做什么，类是对根源的抽象表示这个对象是什么。**

# 接口（Interface）

接口定义了所有类继承接口时应该遵循的语法合同，接口定义了语法合同“是什么”的部分，派生类定义了语法合同“怎么做”部分。接口定义了属性、方法和事件，这些都是接口的成员。接口只包含了成员的声明。成员的定义是派生类的责任。接口提供了派生类应该遵循的标准结构。接口使得实现接口的类或结构在形式上保持一致。抽象类在某种程度上与接口类似，但是它们大多只是用在当只有少数方法由基类声明由派生类实现时。接口本身并不实现任何功能，它只是和声明实现该接口的对象订立一个必须实现哪些行为的契约。







# IEnumerable和IEnumerator

IEnumerable和IEnumerator本质是两个接口，二者的源码如下：

```C#
// Decompiled with JetBrains decompiler
// Type: System.Collections.IEnumerator
// Assembly: netstandard, Version=2.1.0.0, Culture=neutral, PublicKeyToken=cc7b13ffcd2ddd51
// MVID: 5A41D6B7-1898-42EC-A409-FD0B1C3E3DCF
// Assembly location: /Applications/2021.3.14f1/Unity.app/Contents/NetStandard/ref/2.1.0/netstandard.dll

namespace System.Collections
{
  /// <summary><para>Supports a simple iteration over a non-generic collection.</para></summary>
  public interface IEnumerator
  {
    /// <summary><para>Gets the current element in the collection.</para></summary>
    object Current { get; }

    /// <summary><para>Advances the enumerator to the next element of the collection.</para></summary>
    bool MoveNext();

    /// <summary><para>Sets the enumerator to its initial position, which is before the first element in the collection.</para></summary>
    void Reset();
  }
}

```

```C#
// Licensed to the .NET Foundation under one or more agreements.
// The .NET Foundation licenses this file to you under the MIT license.

namespace System.Collections.Generic
{
    // Implement this interface if you need to support foreach semantics.
    public interface IEnumerable<out T> : IEnumerable
    {
        // Returns an IEnumerator for this enumerable Object.  The enumerator provides
        // a simple way to access all the contents of a collection.
#if MONO
        [System.Diagnostics.CodeAnalysis.DynamicDependency(nameof(Array.InternalArray__IEnumerable_GetEnumerator) + "``1 ", typeof(Array))]
#endif
        new IEnumerator<T> GetEnumerator();
    }
}

```

二者的区别与联系：

一个集合可查询（使用foreach，where，any等），必须以某种方式返回IEnumerator object，也就是必须实现IEnumerable接口

IEnumerator object具体实现了iterator（通过MoveNext(),Reset(),Current）。

从这两个接口的选词上，可以看出IEnumerable是一个声明式的接口，声明实现该接口的class是“可枚举的”

IEnumerable和IEnumerator通过IEnumerable的GetEnumerator()方法建立了连接。



看一个例子：

```C#
using System.Collections;

namespace LearnIEnumerator
{
    public class Person
    {
        public string Name { set; get; }

        public Person(string name)
        {
            Name = name;
        }
    }

    public class PeopleEnum : IEnumerator
    {
        public Person[] _people;
        int position = -1;

        public PeopleEnum(Person[] list)
        {
            _people = list;
        }


        public bool MoveNext()
        {
            position++;
            return (position < _people.Length);
        }

        public void Reset()
        {
            position = -1;
        }

        public object Current
        {
            get
            {
                try
                {
                    return _people[position];
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    throw new InvalidOperationException();
                }
            }
        }
    }


    public class People : IEnumerable
    {
        private Person[] people;

        public People(Person[] pArray)
        {
            people = new Person[pArray.Length];
            for (int i = 0; i < pArray.Length; i++)
            {
                people[i] = pArray[i];
            }
        }

        public IEnumerator GetEnumerator()
        {
            return new PeopleEnum(people);
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            Person[] people = new Person[4]
            {
                new Person("李磊"),
                new Person("王刚"),
                new Person("彤彤"),
                new Person("丹丹"),
            };

            People listPeople = new People(people);
            foreach (var p in listPeople)
            {
                Console.WriteLine(((Person)p).Name);
            }

            Console.ReadLine();
        }
    }
}
```

Coroutine的本质就是迭代器，原理是什么？



