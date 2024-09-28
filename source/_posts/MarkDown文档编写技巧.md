---
title: MarkDown文档编写技巧
date: 2022-06-27 16:58:54
tags: 
  - Hexo博客
abbrlink: e47b5157
categories: 技术文档
cover: https://www.notion.so/images/page-cover/met_henri_tl_1892.jpg
description: 
swiper_index: 2
sticky: 1
---

# 博客编写规范

# 在MarkDown中使用数学公式


# 实现文章内部跳转

要跳转的目的地使用`span`标签进行包裹，跳转的出发地使用`[]()`进行包裹，方括号中填入的是跳转的文字内容，圆括号中填入的是span标签中id的值。

```html
[点击此处跳转跳转](#jump)

<span id = jump>跳转到这里<span>
```

