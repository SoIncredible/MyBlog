---
title: SVN——SubVersion学习记录
abbrlink: fb782408
date: 2025-05-22 21:04:39
tags: SVN
categories: 版本管理工具
cover:
description:
swiper_index:
sticky:
---

# SVN外链的使用

# SVN撤回某一次提交

你想撤销130次提交的内容（可能此后还有131、132、133…的正常提交），只撤销这次的内容，其后的变动继续存在。
操作命令

svn merge -c -130 你的工作副本目录
svn commit -m "撤销r130提交内容"
-c参数后面加上负号-130，表示“撤销 r130”的变更。
执行后记得到工作副本看下变动，再commit。
举例

如你的项目地址为http://xxx.com/svn/project/trunk，你在trunk目录下：

svn merge -c -130 .
svn commit -m "撤销第130次提交"

# SVN查看当前Revision