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

# 在M系列芯片上构建wesnoth

按照安装指引,安装所有的依赖

Arm架构的Boost库不能直接用,会报错.

终端中执行的命令:
```
export LIBRARY_PATH=/opt/homebrew/lib:$LIBRARY_PATH 

cmake -DICU_ROOT=/opt/homebrew/opt/icu4c -DICU_INCLUDE_DIR=/opt/homebrew/opt/icu4c/include -DICU_LIBRARY=/opt/homebrew/opt/icu4c/lib . -DCMAKE_BUILD_TYPE=Release  -DCMAKE_CXX_FLAGS="${SDL2_IMAGE_CFLAGS}" -DCMAKE_PREFIX_PATH="/Users/eddielee/Documents/boost;$CMAKE_PREFIX_PATH"\
  -DCMAKE_EXE_LINKER_FLAGS="${SDL2_IMAGE_LIBS}" \
  -DSDL2_IMAGE_LIBRARY="${SDL2_IMAGE_LIBRARIES}" -DBOOST_ROOT=/Users/eddielee/Documents/boost
```



https://stackoverflow.com/questions/75536743/how-to-fix-undefined-symbols-for-arm64-when-using-boostfilesystem-on-m1-macboo

```
ld: symbol(s) not found for architecture arm64
c++: error: linker command failed with exit code 1 (use -v to see invocation)
make[2]: *** [wesnoth] Error 1
make[1]: *** [src/CMakeFiles/wesnoth.dir/all] Error 2
make: *** [all] Error 2
```