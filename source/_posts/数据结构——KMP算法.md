---
title: 数据结构——KMP算法
tags: 
  - 算法
  - 数据结构
abbrlink: 5e0804b8
date: 2022-08-23 21:39:03
categories: 学习笔记
cover: "http://soincredible777.com.cn:90/17.png"
description: 
---

# 串的模式匹配算法——KMP

先了解三个概念：

- 前缀：除了最后一个字符以外，字符串的所有头部子串
- 后缀：除了第一个字符外，字符串所有的尾部子串
- 部分匹配值指的是字符串的前缀和后缀的最长相等前后缀的长度

现给一主串S，一模式串T，他们的值分别是S<code>"ababcabcacbab"</code>，T<code>"abcac"</code>

根据上面给出的概念，我们可以算出子串T中每一个字符的<code>部分匹配值</code>如下表：

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-baqh{text-align:center;vertical-align:top}
.tg .tg-c3ow{border-color:inherit;text-align:center;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-baqh">编号</th>
    <th class="tg-c3ow">1</th>
    <th class="tg-c3ow">2</th>
    <th class="tg-c3ow">3</th>
    <th class="tg-c3ow">4</th>
    <th class="tg-baqh">5</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-baqh">S</td>
    <td class="tg-c3ow">a</td>
    <td class="tg-c3ow">b</td>
    <td class="tg-c3ow">c</td>
    <td class="tg-c3ow">a</td>
    <td class="tg-baqh">c</td>
  </tr>
  <tr>
    <td class="tg-baqh">PM</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-c3ow">1</td>
    <td class="tg-baqh">0</td>
  </tr>
</tbody>
</table>

现在模拟KMP算法的实现过程：

```
主串 a b a b c a b c a c b a b
子串 a b c
```

第一趟匹配过程：发现c与a不匹配，前面的2个字符<code>ab</code>是匹配的，最后一个匹配字符a对应的部分匹配值为0，因此按照下面的公式算出子串需要向后移动的位数：

<center><code>移动位数 = 已匹配的字符数 - 最后一位匹配字符对应的部分匹配值</code></center>

2 - 0 = 0，因此将子串向后移动2位，如下进行第二趟匹配：

```
主串 a b a b c a b c a c b a b
子串     a b c a c 
```

第二趟匹配过程：发现c与b不匹配，前面的四个字符<code>abca</code>是匹配的，最后一个匹配字符c对应的部分匹配值为0，因此按照公式，4 - 1 = 3，如下进行第三趟匹配：

```
主串 a b a b c a b c a c b a b
子串           a b c a c       
```

整个匹配过程中，主串始终没有回退，故KMP可以在O(m+n)的数量级上完成串的模式匹配操作，大大提高了模式匹配的效率。

某趟发生失配时，如果对应部分的匹配值为0，那么表示已匹配相等序列中没有相等的前缀，此时移动的位数最大，直接将子串的首字符后移到主串i位置进行下一次比较，如果已匹配相等序列中存在最大相等前后缀，那么将子串向右滑动到和该相等前后缀对齐，然后从主串i位置进行下一趟比较，比如：

```
主串 a b b a b b a c a c b a b
子串 a b b a c
子串       a b b a c 
最大相等前后缀为 a 	长度为1 已匹配字符数为4 则模式串向后移动 4 - 1 = 3 位
```

# KMP算法的原理是什么？

如何理解公式<code>移动位数 = 已匹配的字符数 - 已匹配的最后一位字符对应的部分匹配值</code>？

还是以上面的例子来说明，当c与b不匹配时，已匹配的<code>abca</code>中的前缀a和后缀a为最长公共元素，已知前缀a与b、c均不同，与后缀a相同，故无需比较，直接将子串移动「已匹配的字符数 - 对应部分的匹配值」，用子串前缀后面的元素与主串匹配失败的元素开始比较即可。

再用大白话说明一下：已经有四个字符匹配好了，这四个字符中，第一个字符和最后一个字符一样，说明在主串中对应的位置上的第一个字符和最后一个字符也是一样的，那就把子串的首字符移动到主串对应的最后一个字符的位置；同样地，如果说四个字符匹配好了，四个字符中前两个字符和后两个字符是一样的，说明在主串中对应的位置上前两个字符和后两个字符也是一样的，那就把子串中的首字符移动到对应主串中后两个字符的起始位置，有点像首尾相连。

```
主串 a b a b c a b c a c b a b
子串     a b c a c 
```

那么上面的计算方法写成代码：<code>Move = (j - 1) - PM[j - 1]</code>。

其中j为当前匹配失败字符的位置，对上面的这段代码还是有改进空间的，使用部分匹配值时，每当匹配失败，就要去找它前面一个元素的部分匹配值，这样使用起来有些不方便，所以将PM表右移一位，这样哪个元素匹配失败，直接看它自己部分的匹配值即可，将上例字符串中<code>abcac</code>的PM表右移一位，就得到了next数组：

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-baqh{text-align:center;vertical-align:top}
.tg .tg-c3ow{border-color:inherit;text-align:center;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-baqh">编号</th>
    <th class="tg-c3ow">1</th>
    <th class="tg-c3ow">2</th>
    <th class="tg-c3ow">3</th>
    <th class="tg-c3ow">4</th>
    <th class="tg-baqh">5</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-baqh">S</td>
    <td class="tg-c3ow">a</td>
    <td class="tg-c3ow">b</td>
    <td class="tg-c3ow">c</td>
    <td class="tg-c3ow">a</td>
    <td class="tg-baqh">c</td>
  </tr>
  <tr>
    <td class="tg-baqh">PM</td>
    <td class="tg-c3ow">-1</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-c3ow">0</td>
    <td class="tg-baqh">1</td>
  </tr>
</tbody>
</table>

我们注意到：

- 第一个元素右移以后空缺的用-1来填充，因为若是第一个元素匹配失败，那么需要将子串向右移动一位，而不需要计算子串移动的位数
- 最后一个元素在右移的过程中溢出，因为原来的子串中，最后一个元素的部分匹配值是其下一个元素使用的，显然没有下一个元素了，所以可以舍去掉

这样，上面Move就可以写成<code>Move = (j-1) - next[j]

相当于将子串的比较指针j回退到<code>j = j - Move = next[j] + 1 </code>，总之，next[j]的含义就是：在子串第j个字符与主串发生失配时，则跳到子串的next[j]位置重新与主串当前位置进行比较。
