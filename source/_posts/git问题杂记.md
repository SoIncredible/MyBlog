---
title: git问题杂记
tags: 
  - Git
abbrlink: a72e4bb6
categories: 硬技能
date: 2022-07-10 11:11:26
cover: https://www.notion.so/images/page-cover/met_frederic_edwin_church_1871.jpg
description: 
sticky: 2
swiper_index: 2
---

# 终端中git相关指令执行的结果中的中文乱码问题

- 在终端中使用`git status`命令时，结果显示为乱码
  解决方法是在终端中输入以下两行命令
  ```
   git config --global core.quotepath true
   git config --global core.quotepath false
  ```
  重启终端，如果仍然显示乱码，执行下面两行命令
  ```
   git config --global core.quotepath true
   git config --global core.quotepath false
  ```
  再次重启终端


# Clone GitHub仓库失败

拉取git仓库有如下报错
```
fatal: unable to access 'https://github.com/xxxxx/xxxxxx.git/': Failed to connect to github.com port 443 after 21052 ms: Couldn't connect to server
```

有可能是因为本地使用了代理工具，比如Clash修改了网络端口，解决方案如下👇👇👇
- https://blog.csdn.net/qq_27281247/article/details/135925956

# Git仓库太大拉取不下来

一些比较大的仓库,拉取的时候会有如下报错:
```
remote: Enumerating objects: 1315604, done.
remote: Counting objects: 100% (1362/1362), done.
remote: Compressing objects: 100% (850/850), done.
error: RPC failed; curl 18 transfer closed with outstanding read data remaining
error: 6333 bytes of body are still expected
fetch-pack: unexpected disconnect while reading sideband packet
fatal: early EOF
fatal: fetch-pack: invalid index-pack output
```

因为仓库非常大，一次性拉取可能会导致网络压力过大。可以尝试使用 --depth 参数进行浅克隆，只拉取最近的提交记录：
```
git clone --depth 1 <repository-url>
```
或者尝试将缓存增大为1G:
```
git config --global http.postBuffer 1048576000
```

如果还是不行,那就尝试暴力拉取,编写一个shell脚本重复执行拉取仓库直到拉取成功:
```
#!/bin/bash

# 检查是否提供了仓库目录作为参数
if [ $# -eq 0 ]; then
    echo "请提供要更新的仓库目录作为参数。"
    exit 1
fi

# 获取命令行传入的仓库目录
REPO_DIR="$1"

# 检查仓库目录是否存在
if [ ! -d "$REPO_DIR" ]; then
    echo "指定的仓库目录不存在。"
    exit 1
fi

# 进入仓库目录
cd "$REPO_DIR"

# 循环拉取更新，直到成功
while true; do
    git pull
    # 获取上一个命令的退出状态码
    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo "仓库更新成功！"
        break
    else
        echo "仓库更新失败，将在 5 秒后重试..."
        sleep 5
    fi
done
```

# Git Submodule

`git submodule update --init --recursive` 是一个在使用 Git 管理项目时非常实用的命令，下面为你详细解释它的用途和其中各参数的含义。
整体用途
这个命令主要用于初始化并更新项目中的子模块（submodule）。子模块是 Git 提供的一种机制，允许在一个 Git 仓库中包含另一个独立的 Git 仓库，且能分别对它们进行版本控制。当你克隆一个包含子模块的项目时，子模块的代码不会自动克隆下来，这时就需要使用这个命令来获取子模块的代码。
参数解释
--init
作用：--init 参数用于初始化子模块。当你克隆一个包含子模块的项目时，主项目仓库中只是记录了子模块的一些引用信息（如子模块的仓库地址、特定的提交哈希值），子模块的代码并没有实际下载到本地。--init 参数会根据主项目中记录的子模块配置信息，在本地创建子模块的仓库目录，并设置好子模块的远程仓库地址等信息。
示例：假设你克隆了一个名为 main-project 的项目，它包含一个子模块 sub-project。在克隆 main-project 后，直接查看 sub-project 目录可能为空或者只有一些配置文件。使用 git submodule update --init 命令后，Git 会根据 main-project 中记录的信息，将 sub-project 的代码从远程仓库克隆到本地的 sub-project 目录中。
--recursive
作用：--recursive 参数用于递归地初始化和更新子模块。如果子模块本身还包含其他子模块（即嵌套子模块），使用 --recursive 参数可以确保所有嵌套子模块都被正确初始化和更新。也就是说，它会逐层深入到每一个子模块中，对其中包含的子模块也执行相同的初始化和更新操作。
示例：假设 main-project 包含子模块 sub-project1，而 sub-project1 又包含子模块 sub-project2。使用 git submodule update --init --recursive 命令时，Git 不仅会初始化和更新 sub-project1，还会进一步深入到 sub-project1 中，对 sub-project2 进行初始化和更新。
常见使用场景
克隆包含子模块的项目后
当你克隆一个包含子模块的项目时，通常需要执行这个命令来获取子模块的代码。例如：
bash
git clone <main-project-repo-url>
cd main-project
git submodule update --init --recursive
拉取主项目更新后
如果主项目更新了子模块的引用信息（如子模块的提交哈希值），或者添加了新的子模块，你也可以使用这个命令来更新本地的子模块代码：
bash
git pull
git submodule update --init --recursive
通过使用 git submodule update --init --recursive 命令，你可以方便地管理项目中的子模块，确保子模块的代码与主项目记录的引用信息保持一致。

# 参考资料

[官方参考书](https://git-scm.com/book/zh/v2)
