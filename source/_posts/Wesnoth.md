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

笔者在build项目的时候遇到了很多问题, 建议先对CMake有一定的基础之后再继续进行下去.

# 在M系列芯片上构建wesnoth

按照安装指引,安装所有的依赖

Arm架构的Boost库不能直接用,会报错.

终端中执行的命令:
```
export LIBRARY_PATH=/opt/homebrew/lib:$LIBRARY_PATH 

cmake -DICU_ROOT=/opt/homebrew/opt/icu4c -DICU_INCLUDE_DIR=/opt/homebrew/opt/icu4c/include -DICU_LIBRARY=/opt/homebrew/opt/icu4c/lib . -DCMAKE_BUILD_TYPE=Release  -DCMAKE_CXX_FLAGS="${SDL2_IMAGE_CFLAGS}" -DCMAKE_EXE_LINKER_FLAGS="${SDL2_IMAGE_LIBS}" -DSDL2_IMAGE_LIBRARY="${SDL2_IMAGE_LIBRARIES}"
```



https://stackoverflow.com/questions/75536743/how-to-fix-undefined-symbols-for-arm64-when-using-boostfilesystem-on-m1-macboo

```
ld: symbol(s) not found for architecture arm64
c++: error: linker command failed with exit code 1 (use -v to see invocation)
make[2]: *** [wesnoth] Error 1
make[1]: *** [src/CMakeFiles/wesnoth.dir/all] Error 2
make: *** [all] Error 2
```

按照官方的构建文档,安装相关的库:
- Boost libraries >= 1.66.0 Most headers plus the following binary libs:
  - Filesystem
  - Locale
  - Iostreams
  - Random
  - Regex
  - Program Options
  - System
  - Coroutine
  - Graph
  - Charconv (This requires boost 1.85 or higher and is optional but reccomended especially for clang builds)
- SDL2 libraries:
  - SDL2 >= 2.0.18 (macOS: 2.0.22 due to needing https://github.com/libsdl-org/SDL/commit/3bebdaccb7bff8c40438856081d404a7ce3def30)
  - SDL2_image >= 2.0.2 (with PNG, JPEG, and WEBP support)
  - SDL2_mixer >= 2.0.0 (with Ogg Vorbis support)
- Fontconfig >= 2.4.1
- Cairo >= 1.10.0
- Pango >= 1.44.0 (with Cairo backend)
- Vorbisfile aka libvorbis
- libbz2
- libz
- libssl
- libcrypto (from OpenSSL)
- libcurl4 (OpenSSL version)


# 在Arm芯片Mac上构建wesnoth提示找不到sdl2_image的问题


```
g++ -std=c++17 -o test src/main.cpp -I /opt/homebrew/include `sdl2-config --cflags --libs` -lSDL2_image  
```

遇到很奇怪的问题,执行make命令之后会提示找不到SDL2_image库,笔者已经使用homebrew安装到本机了,
于是笔者写了一个测试项目,决定搞清楚到底是什么原因导致C++找不到SDL库的.
```
// #include "/opt/homebrew/include/SDL2/SDL.h"
// #include "/opt/homebrew/include/SDL2/SDL_image.h"
#include "SDL2/SDL.h"
#include "SDL2/SDL_image.h"
#include <iostream>

const int SCREEN_WIDTH = 640;
const int SCREEN_HEIGHT = 480;

// 初始化 SDL 和 SDL_image
bool init(SDL_Window*& window, SDL_Renderer*& renderer) {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        std::cerr << "SDL could not initialize! SDL_Error: " << SDL_GetError() << std::endl;
        return false;
    }

    window = SDL_CreateWindow("SDL2_image Example", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED,
                              SCREEN_WIDTH, SCREEN_HEIGHT, SDL_WINDOW_SHOWN);
    if (window == nullptr) {
        std::cerr << "Window could not be created! SDL_Error: " << SDL_GetError() << std::endl;
        return false;
    }

    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (renderer == nullptr) {
        std::cerr << "Renderer could not be created! SDL_Error: " << SDL_GetError() << std::endl;
        return false;
    }

    int imgFlags = IMG_INIT_PNG;
    if (!(IMG_Init(imgFlags) & imgFlags)) {
        std::cerr << "SDL_image could not initialize! SDL_image Error: " << IMG_GetError() << std::endl;
        return false;
    }

    return true;
}

// 加载纹理
SDL_Texture* loadTexture(const std::string& path, SDL_Renderer* renderer) {
    SDL_Texture* newTexture = nullptr;
    SDL_Surface* loadedSurface = IMG_Load(path.c_str());
    if (loadedSurface == nullptr) {
        std::cerr << "Unable to load image " << path << "! SDL_image Error: " << IMG_GetError() << std::endl;
    } else {
        newTexture = SDL_CreateTextureFromSurface(renderer, loadedSurface);
        if (newTexture == nullptr) {
            std::cerr << "Unable to create texture from " << path << "! SDL Error: " << SDL_GetError() << std::endl;
        }
        SDL_FreeSurface(loadedSurface);
    }
    return newTexture;
}

// 清理资源并退出
void close(SDL_Window* window, SDL_Renderer* renderer) {
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    IMG_Quit();
    SDL_Quit();
}

int main(int argc, char* args[]) {
    SDL_Window* window = nullptr;
    SDL_Renderer* renderer = nullptr;

    if (!init(window, renderer)) {
        return -1;
    }

    // 假设当前目录下有一个名为 example.png 的图像文件
    SDL_Texture* texture = loadTexture("example.png", renderer);
    if (texture == nullptr) {
        close(window, renderer);
        return -1;
    }

    // 主循环
    SDL_Event e;
    bool quit = false;
    while (!quit) {
        while (SDL_PollEvent(&e) != 0) {
            if (e.type == SDL_QUIT) {
                quit = true;
            }
        }

        // 清屏
        SDL_SetRenderDrawColor(renderer, 0xFF, 0xFF, 0xFF, 0xFF);
        SDL_RenderClear(renderer);

        // 渲染纹理
        SDL_RenderCopy(renderer, texture, nullptr, nullptr);

        // 更新屏幕
        SDL_RenderPresent(renderer);
    }

    // 释放纹理
    SDL_DestroyTexture(texture);
    // 清理并退出
    close(window, renderer);

    return 0;
}
```

```
cmake_minimum_required(VERSION 3.10)

# 设置项目名称
project(SDLImageExample)

# 查找 SDL2 和 SDL2_image 库
find_package(SDL2 REQUIRED)
find_package(SDL2_image REQUIRED)

# 打印调试信息，确认查找结果
message("SDL2_INCLUDE_DIRS: ${SDL2_INCLUDE_DIRS}")
message("SDL2_IMAGE_INCLUDE_DIRS: ${SDL2_IMAGE_INCLUDE_DIRS}")
message("SDL2_LIBRARIES: ${SDL2_LIBRARIES}")
message("SDL2_IMAGE_LIBRARIES: ${SDL2_IMAGE_LIBRARIES}")

set(SDL2_INCLUDE_DIRS /opt/homebrew/include)
set(SDL2_IMAGE_INCLUDE_DIRS /opt/homebrew/include)
set(SDL2_LIBRARIES /opt/homebrew/lib/libSDL2.dylib)
set(SDL2_IMAGE_LIBRARIES /opt/homebrew/lib/libSDL2_image.dylib)

message("SDL2_INCLUDE_DIRS: ${SDL2_INCLUDE_DIRS}")
message("SDL2_IMAGE_INCLUDE_DIRS: ${SDL2_IMAGE_INCLUDE_DIRS}")
message("SDL2_LIBRARIES: ${SDL2_LIBRARIES}")
message("SDL2_IMAGE_LIBRARIES: ${SDL2_IMAGE_LIBRARIES}")

# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 11)

# 手动指定 SDL2 和 SDL2_image 的库文件路径
set(SDL2_LIBRARY /opt/homebrew/lib/libSDL2.dylib)
set(SDL2_IMAGE_LIBRARY /opt/homebrew/lib/libSDL2_image.dylib)

# 手动指定 SDL2 和 SDL2_image 的包含路径
set(SDL2_INCLUDE_DIR /opt/homebrew/include/SDL2)
set(SDL2_IMAGE_INCLUDE_DIR /opt/homebrew/include/SDL2)

# 打印调试信息
# message("SDL2_INCLUDE_DIR: ${SDL2_INCLUDE_DIR}")
# message("SDL2_IMAGE_INCLUDE_DIR: ${SDL2_IMAGE_INCLUDE_DIR}")

# 添加可执行文件
add_executable(SDLImageExample src/main.cpp)

# 包含 SDL2 和 SDL2_image 的头文件目录
target_include_directories(SDLImageExample PRIVATE ${SDL2_INCLUDE_DIR} ${SDL2_IMAGE_INCLUDE_DIR})

# 链接 SDL2 和 SDL2_image 库
target_link_libraries(SDLImageExample PRIVATE ${SDL2_LIBRARY} ${SDL2_IMAGE_LIBRARY})
```
在这段代码中,笔者在CMake中手动指定了SDL库的安装位置,执行make之后还是会提示找不到sdl库,那推测C++在要链接SDL的时候根本就没有用CMake中指定的`${SDL2_INCLUDE_DIRS}`、`${SDL2_IMAGE_INCLUDE_DIRS}`、`${SDL2_LIBRARIES}`、`${SDL2_IMAGE_LIBRARIES}`这几个字段,只有在cpp中指定SDL的绝对路径,才能够通过编译.


