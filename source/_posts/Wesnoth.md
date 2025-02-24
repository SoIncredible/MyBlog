---
title: Wesnoth
cover: 'https://www.notion.so/images/page-cover/woodcuts_16.jpg'
abbrlink: d1d5504
date: 2025-01-27 01:22:14
tags:
categories:
description:
swiper_index:
sticky:
---

[仓库地址](https://github.com/wesnoth/wesnoth)

# build wesnoth

笔者在按照[INSTALL](https://github.com/wesnoth/wesnoth/blob/master/INSTALL.md)部分在M1系列芯片的Mac上使用homebrew安装好了wesnoth依赖的所有lib, 但是通过CMake build的时候却还是提示有这样那样的lib未找到、这样那样的头文件没有找到. 笔者并不清楚中间是什么环节出了问题, 在此先进行记录, 日后若有思路再回来补充.

首先, 笔者的假设是, 官方编写的CMake构建脚本是没有问题的, 问题出在了笔者的环境配置上, 在安装完wesnoth需要的库之后,需要在终端执行以下命令:
```
export LIBRARY_PATH=/opt/homebrew/Cellar/boost/1.87.0/lib:/opt/homebrew/lib:$LIBRARY_PATH
export CPATH=/opt/homebrew/include/harfbuzz:/opt/homebrew/Cellar/boost/1.87.0/include/boost:$CPATH     
```                

出现如下报错的原因: 方法有定义 没实现.
```
Undefined symbols for architecture arm64:
  "Hello()", referenced from:
      _main in test_charconv.cpp.o
ld: symbol(s) not found for architecture arm64
c++: error: linker command failed with exit code 1 (use -v to see invocation)
make[2]: *** [test_charconv] Error 1
make[1]: *** [CMakeFiles/test_charconv.dir/all] Error 2
make: *** [all] Error 2
```

https://stackoverflow.com/questions/75536743/how-to-fix-undefined-symbols-for-arm64-when-using-boostfilesystem-on-m1-macboo
