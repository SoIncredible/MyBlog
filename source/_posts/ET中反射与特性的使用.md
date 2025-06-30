---
title: ET中反射与特性的使用
tags:
  - ET
  - C#
categories: ET
abbrlink: cd96d12
date: 2023-03-17 15:48:00
cover: https://www.notion.so/images/page-cover/met_the_unicorn_in_captivity.jpg
description:
swiper_index: 
sticky: 
---

# ET中用到的其他Attribute

[BsonIgnore]
[UnityEngine.HideInInspector]
[MemoryPackIgnore]

> 2025.3.20更新

笔者最近在看ET框架的Demo,里面有一个名为`CodeLoader`的脚本,里面用到了反射的东西

反射可以直接与程序集交互,越过常规的类构造函数,直接由程序集中实例化出对象来.
通过反射执行方法/实例化对象的方式也有缺点,在Editor中无法直观定位到调用

Assembly程序集提供给了一个`GetTypes()`接口,允许你访问该程序集中包含的所有Type
Type中存储了FullName、以及这个Type所处的程序集

在`CodeTypes`脚本的Awake方法中
```
public void Awake(Assembly[] assemblies)
{—
    Dictionary<string, Type> addTypes = AssemblyHelper.GetAssemblyTypes(assemblies);
    foreach ((string fullName, Type type) in addTypes)
    {
        this.allTypes[fullName] = type;
        
        if (type.IsAbstract)
        {
            continue;
        }
        
        // 记录所有的有BaseAttribute标记的的类型
        object[] objects = type.GetCustomAttributes(typeof(BaseAttribute), true);

        foreach (object o in objects)
        {
            this.types.Add(o.GetType(), type);
        }
    }
}
```

CodeLoader中的Start方法如下:
```
public void Start()
{
    if (!Define.IsEditor)
    {
        byte[] modelAssBytes = this.dlls["Unity.Model.dll"].bytes;
        byte[] modelPdbBytes = this.dlls["Unity.Model.pdb"].bytes;
        byte[] modelViewAssBytes = this.dlls["Unity.ModelView.dll"].bytes;
        byte[] modelViewPdbBytes = this.dlls["Unity.ModelView.pdb"].bytes;
        // 如果需要测试，可替换成下面注释的代码直接加载Assets/Bundles/Code/Unity.Model.dll.bytes，但真正打包时必须使用上面的代码
        //modelAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.dll.bytes"));
        //modelPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.pdb.bytes"));
        //modelViewAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.dll.bytes"));
        //modelViewPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.pdb.bytes"));

        if (Define.EnableIL2CPP)
        {
            foreach (var kv in this.aotDlls)
            {
                TextAsset textAsset = kv.Value;
                RuntimeApi.LoadMetadataForAOTAssembly(textAsset.bytes, HomologousImageMode.SuperSet);
            }
        }
        this.modelAssembly = Assembly.Load(modelAssBytes, modelPdbBytes);
        this.modelViewAssembly = Assembly.Load(modelViewAssBytes, modelViewPdbBytes);
    }
    else
    {
        if (this.enableDll)
        {
            byte[] modelAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.dll.bytes"));
            byte[] modelPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.Model.pdb.bytes"));
            byte[] modelViewAssBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.dll.bytes"));
            byte[] modelViewPdbBytes = File.ReadAllBytes(Path.Combine(Define.CodeDir, "Unity.ModelView.pdb.bytes"));
            this.modelAssembly = Assembly.Load(modelAssBytes, modelPdbBytes);
            this.modelViewAssembly = Assembly.Load(modelViewAssBytes, modelViewPdbBytes);
        }
        else
        {
            Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();
            foreach (Assembly ass in assemblies)
            {
                string name = ass.GetName().Name;
                if (name == "Unity.Model")
                {
                    this.modelAssembly = ass;
                }
                else if (name == "Unity.ModelView")
                {
                    this.modelViewAssembly = ass;
                }

                if (this.modelAssembly != null && this.modelViewAssembly != null)
                {
                    break;
                }
            }
        }
    }
    
    (Assembly hotfixAssembly, Assembly hotfixViewAssembly) = this.LoadHotfix();

    World.Instance.AddSingleton<CodeTypes, Assembly[]>(new[]
    {
        typeof (World).Assembly, typeof (Init).Assembly, this.modelAssembly, this.modelViewAssembly, hotfixAssembly,
        hotfixViewAssembly
    });

    IStaticMethod start = new StaticMethod(this.modelAssembly, "ET.Entry", "Start");
    start.Run();
}
```

如果你是Editor模式下的话,你应该会走到`this.enableDll`为true的分支
通过执行CodeLoader中的Start方法,被遍历到的程序集有`World`类所在的程序集`Unity.Core`、`Init`类所在的程序集`Unity.Loader`、modelAssemBly`Unity.Model`,modelViewAssembly`Unity.ModelView`,hotfixAssembly`Unity.Hotfix`,hotfixViewAssembly`Unity.HotfixView`

代入一下就是,通过AddSingleton方法,创建了CodeTypes实例,并将上面提到的程序集作为参数传入CodeType的Awake方法中.

然后在modelAssembly`Unity.Model`中,找到`ET.Entry`类中的Start方法,并执行该方法,然后在该方法中,执行了`CodeTypes.Instance.CreateCode`方法,该方法会在上面收集到的程序集中,找到所有被标记了CodeAttribute属性的类,并将这些类实例化出来,这些类分别是
- EntitySystemSingleton
- MessageDispatcher MessagePatcher中的Awake方法中实例化了所有被标记为MessageHandlerAttribute属性的类
- EventSystem
- HttpDispatcher
- LSEntitySystemSingleton
- AIDispatcherComponent
- ConsoleDispatcher
- MessageSessionDispatcher
- NumericWatcherComponent
- UIEventComponent
也就是说,上面这十个类,通过执行ET的Entry方法之后就已经被创建出来了. 

在`MessageSessionDispatcher`的Awake方法中如下：
```
public void Awake()
{
    HashSet<Type> types = CodeTypes.Instance.GetTypes(typeof (MessageSessionHandlerAttribute));

    foreach (Type type in types)
    {
        IMessageSessionHandler iMessageSessionHandler = Activator.CreateInstance(type) as IMessageSessionHandler;
        if (iMessageSessionHandler == null)
        {
            Log.Error($"message handle {type.Name} 需要继承 IMHandler");
            continue;
        }

        object[] attrs = type.GetCustomAttributes(typeof(MessageSessionHandlerAttribute), true);
        
        foreach (object attr in attrs)
        {
            MessageSessionHandlerAttribute messageSessionHandlerAttribute = attr as MessageSessionHandlerAttribute;
            
            Type messageType = iMessageSessionHandler.GetMessageType();
            
            ushort opcode = OpcodeType.Instance.GetOpcode(messageType);
            if (opcode == 0)
            {
                Log.Error($"消息opcode为0: {messageType.Name}");
                continue;
            }

            MessageSessionDispatcherInfo messageSessionDispatcherInfo = new (messageSessionHandlerAttribute.SceneType, iMessageSessionHandler);
            this.RegisterHandler(opcode, messageSessionDispatcherInfo);
        }
    }
}
```

这段代码中的`GetCustomAttributes(typeof(MessageSessionHandlerAttribute), true);`接口

# AIHandlerAttribute
# CodeAttribute
# ConfigAttribute
# ConsoleHandlerAttribute
# EnableClassAttribute
# EntitySystemAttribute
# EntitySystemOfAttribute
# EventAttribute
# HttpHandlerAttribute
# InvokeAttribute
# LSEntitySystemAttribute
# LSEntitySystemOfAttribute
# MessageAttribute
# MessageHandlerAttribute
## MessageLocationHandlerAttribute
# MessageSessionHandlerAttritube
# NumericWAtcherAttribute
# ResponseTypeAttribute
# UIEventAttribute



---

# 背景

这篇博客本来想聊一聊反射的内容，但是反射的前置知识点优点太多了，所以真正要聊到反射可能要到下一篇博客了。

# 特性

特性（Attribute）是用于在运行时传递程序中各种元素（比如类、方法、结构、枚举、组件等等）的行为信息的声明性标签。我们可以使用特性向程序添加声明性信息。一个声明性标签是通过放置在它所应用的元素前面的方括号`[]`来描述的。

特性用于添加元数据，如编译器指令和注释

# 反射

反射指程序可以访问、检测和修改它本身状态和行为的一种能力。程序集包含模块，而模块包含类型，类型又包含成员。反射提供了封装程序集、模块和类型的对象。我们可以使用反射动态地创建类型的实例，将类型绑定到现有对象上，或者从现有对象中获取类型。然后可以调用类型的方法或者访问其字段和属性。

## 优缺点

优点：

1. 反射提高了程序的灵活性和扩展性
2. 降低耦合性，提高自适应能力
3. 它允许程序创建和控制任何类的对象，无需提前硬编码目标类

缺点：

1. 性能问题：反射基本上是一种解释操作，用于字段和方法接入时要远慢于直接代码，因此反射机制主要应用在对灵活性和拓展性要求很高的系统框架上，普通程序不建议使用。
2. 使用反射会模糊程序内部的逻辑，程序员希望在源码中看到程序的逻辑，反射却绕过了源代码的技术，因而会带来维护的问题，反射代码比相应的直接代码更为复杂。
#  反射为什么慢?
因为信息都是字符串 是要检索字符串的 而且字符串的检索是大小不敏感的. 

## 反射的用途：

反射有以下用途：

1. 它允许在运行时查看特性（attribute）信息
2. 它允许审查集合中的各种类型，以及实例化这些类型
3. 它允许延迟绑定的方法和属性
4. 它允许在运行时创建新的类型，然后使用这些类型执行一些任务

