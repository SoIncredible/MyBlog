---
title: git问题杂记
tags: 
  - git
abbrlink: a72e4bb6
categories: 版本管理工具
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
```shell
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
```shell
git clone --depth 1 <repository-url>
```
或者尝试将缓存增大为1G:
```shell
git config --global http.postBuffer 1048576000
```

如果还是不行,那就尝试暴力拉取,编写一个shell脚本重复执行拉取仓库直到拉取成功:
```shell
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


# 使用Git管理Unity项目

## 了解Unity的目录结构

![](使用Git管理Unity项目/image-20220718093817361.png)

官方参考文档https://docs.unity3d.com/Manual/ExternalVersionControlSystemSupport.html

- Assets：存储所有游戏资源的文件夹，包括脚本、纹理、声音、<code>编辑器定制</code>等等。是项目中最重要的文件夹。
- Library：被导入资源的本地缓存，使用Git做版本控制时不需要管这个文件夹。如果Unity的项目文件太大，可以试试删除Library文件夹
- Logs：日志文件夹，由Unity自动生成管理，记录报错崩溃信息，随时可以删除清理
- Packages：包配置信息，由Unity自动生成管理，用于放置一些官方组件和第三方插件
- ProjectSettings：工程设置信息，由Unity自动生成管理
- UserSettings：编辑器的用户构建设置，记录用户的使用偏好，如Unity编辑器的布局方式等等。
- Temp：临时文件夹，Unity运行过程中生成的历史文件。

[这里](https://github.com/github/gitignore/blob/main/Unity.gitignore)是github官方提供的不同工程中gitignore中的内容，但是参考价值不大，我们自己项目工程文件的方式很多样（当然Unity工程文件夹下的基本结构是不变的，我们可能会在工程文件夹之外上传很多我们自己的文件）

2023.07.13更新

现在我希望这个命令可以更加全能一些，它能够帮助我创建git仓库、创建gitignore、创建README文件一体的命令，我将这个命令命名为

但是每一次给Unity项目创建Git仓库的时候都需要手动生成这个`.gitigonre`文件，非常地麻烦，但是我们可以在终端编写一个命令在自动生成`.ginignore`文件，在Mac和Windows上操作有所不同。

### Mac

在Mac的终端中，我们可以通过创建一个Shell脚本或简单地定义一个别名来实现自定义命令。我的Mac是Zsh环境：

1. 打开`.zshrc`

   ```bash
   vim ~/.zshrc
   ```

2. 添加如下内容：

   ```bash
   generate-unity-gitignore() {
     curl https://raw.githubusercontent.com/github/gitignore/master/Unity.gitignore -o .gitignore
   }
   ```

3. 保存并退出编辑器，重新加载配置文件以应用更改

   ```bash
   source ~/.zshrc
   ```

现在，我们在创建了一个新的Unity项目后，去到该项目的根目录下，使用`generate-unity-gitignore`就可以生成`.gitignore`文件了。

### Windows

在Windows的PowerShell环境下，我们可以通过创建一个自定义函数来实现类似的功能：

1. 打开WindowsPowerShell

2. 创建一个名为`Microsoft.PowerShell_profile.ps1`的Profile文件

   ```powershell
   if (!(Test-Path -Path $PROFILE)) {
       New-Item -Type File -Path $PROFILE -Force
   }
   ```

3. 使用文本编辑器打开`Microsoft.PowerShell_profile.ps1`文件，我们可以在PowerShell中输入`$PROFILE`找到文件的路径

4. 将下面的内容添加到其中：

   ```powershell
   function generate-unity-gitignore {
       Invoke-WebRequest -Uri "https://raw.githubusercontent.com/github/gitignore/master/Unity.gitignore" -OutFile ".gitignore"
   }
   ```

5. 保存文件并退出编辑器，重启PowerShell窗口或者执行以下命令使更改生效：

   ```powershell
   . $Profile
   ```

现在我们在PowerShell窗口中输入`generate-unity-gitignore`就可以生成`.gitignore`了

不过在近期的使用中我发现，`gitigonre`的文件内容是从github上获取的，对网络质量要求高，有可能会创建失败；而且由于我使用的编辑器是Rider，项目文件夹中会有一个.idea的文件夹并没有被列入gitignore中，当然我们可以在生成了gitignore文件之后再将这个文件夹加入到文件的末尾，但是还是太繁琐了，所以我们修改一下创建`gitignore`文件的方式，我们将Mac和Windows对应的Shell脚本和PowerShell脚本中`generate-unity-gitignore`方法的内容替换：

2023.07.13更新：经过在公司的学习和自己平时的开发使用，我们现在将这个功能扩展，因为我们使用Git管理的根文件夹可能不仅仅是一个Unity的工程的根目录，因此之前的gitignore文件存在一定的局限性，因此我在之前的文件基础上增加一些拓展性更高的方案

**Mac**

```bash
echo "# Unity .gitignore file

/[Ll]ibrary/
/[Tt]emp/
/[Oo]bj/
/[Bb]uild/
/[Bb]uilds/
/[Ll]ogs/
/[Uu]ser[Ss]ettings/
Assets/AssetStoreTools*

# Autogenerated VS/MD/Consulo solution and project files
ExportedObj/
*.csproj
*.unityproj
*.sln
*.suo
*.tmp
*.user
*.userprefs
*.pidb
*.booproj
*.svd

*.dll.mdb
*.exe.mdb
*.mdb
*.pdb

*.bak
*/symlink
*.swp
*.swf

# Visual Studio cache directory
.vs/

# JetBrains Rider cache files & settings
.idea/
*.DotSettings.user

# macOS-specific files
.DS_Store
._*
Icon?
*.bak.osx
*.gmk.osx
.cs.meta

# Thumbnails
[Systeme][Cc]ache/
[Tt]humbs.db
*.stackdump

# Cache files for various systems
library-*
.sln.docstates
.expressionblend
Tmp_*
_StyleCop.Cache
.idea/
.vsconfig
" >> .gitignore

```

**Windows**

```powershell
@'
# Unity .gitignore file

/[Ll]ibrary/
/[Tt]emp/
/[Oo]bj/
/[Bb]uild/
/[Bb]uilds/
/[Ll]ogs/
/[Uu]ser[Ss]ettings/
Assets/AssetStoreTools*

# Autogenerated VS/MD/Consulo solution and project files
ExportedObj/
*.csproj
*.unityproj
*.sln
*.suo
*.tmp
*.user
*.userprefs
*.pidb
*.booproj
*.svd

*.dll.mdb
*.exe.mdb
*.mdb
*.pdb

*.bak
*/symlink
*.swp
*.swf

# Visual Studio cache directory
.vs/

# JetBrains Rider cache files & settings
.idea/
*.DotSettings.user

# macOS-specific files
.DS_Store
._*
Icon?
*.bak.osx
*.gmk.osx
.cs.meta

# Thumbnails
[Systeme][Cc]ache/
[Tt]humbs.db
*.stackdump

# Cache files for various systems
library-*
.sln.docstates
.expressionblend
Tmp_*
_StyleCop.Cache
./idea
.vsconfig
'@ | Set-Content -Path ".gitignore" -Encoding utf8

```

如果之后还有需要忽略的文件，在这两段内容的末尾添加我们要忽略的文件名即可。

## 使用SourceTree管理Unity项目

`SourceTree`是带有可视化界面的`Git`，有了它能够大大提高我们版本管理的效率，下面记录的是我在使用`SourceTree`面对一些较为复杂的使用场景时的基本流程，为之后遇到类似流程做一个参照。

## 在Windows上使用SourceTree执行git命令有如下报错

```
The host key is not cached for this server:
 gitee.com(port22)
You have no guarantee that the server is the computer
you think it is
The server's ssh-ed25519 key fingerprint is:
	.................................
if you trust this host, enter "y" to add thie key to
```

打开Windows版的SourceTree，在菜单栏中选择`工具``->``选项``->``一般``->``SSH客户端配置``->``SSH客户端`选择`OpenSSH`就可以解决问题 。


### 使用SourceTree在错误的分支上做了修改，如何将修改从错误的分支转移到正确的分支上？

紧接着上面那个在一个项目中向两个云端仓库同步更新版本的例子使用场景，我极有可能在`Learn`分支上进行学习的时候突然要修复一个bug或者完成新的需求，但是我忘记切换分支到`master`上，于是我在`Learn`分支上完成的代码的编写，在这种情况下，我希望将我在`Learn`分支上做的修改转移到`master`分支上，需要使用贮藏`stash`的功能，基本流程如下：

1. 当前分支是`Learn`，确保需要更改分支的内容都处于未提交`commit`的状态
2. 选中需要更改分支的内容，点击贮藏`stash`
3. 切换到`master`分支上，应用刚才生成的贮藏，就将在`Learn`分支上做的修改移动到了`master`分支上
4. 提交到远程仓库

### gitignore没有生效，将本应该ignore的文件提交了该怎么办？

今天在使用`generate-unity-gitignore`方法在终端中生成gitignore文件的时候，在提交的时候发现gitignore文件好像并没有生效，提交了大量Library和Logs文件夹中的文件，导致`git add .`操作卡了好久，但是已经将不该提交的文件提交上去了，解决方案如下：

1.删除掉原来的.gitignore文件

2.创建新的.gitignore文件并且确保格式正确、要忽略的文件填写正确

3.清除已经被跟踪的文件缓存，如果要递归地删除文件夹，记得使用`-r`选项，`<directory_path>`要替换成Unity项目的根路径。

```shell
git rm -r --cached <directory_path>
```

4.提交更改，git常规三件套：`add`、`commit`、`push`

一般情况下这种问题只会出现在第一次提交的时候，而在SourceTree中在只有一次提交的情况下没有办法撤回，具体做法是，将已经提交的但是不需要追踪的文件删除掉，在SourceTree中就做了一次更改，然后提交一次

然后更新新的gitignore文件，然后撤销删除

而在SourceTree当中的操作方法是：我们首先要将.gitignore文件更新到正确的内容，然后将.gitignore文件贮藏起来，选中我们错误的那次提交记录。

Git钩子的使用

在开发过程中，我经常需要更改本地的时间去测试一些活动的功能是否正常，这导致了我很可能在没有将时间调整回来就在错误的时间下将本地的修改进行了提交，因为我们的每次提交的时间会被记录，在提交历史中会出现新一次提交记录的时间却比之前提交的时间还要往前这种诡异的记录，所以我希望在我们做了本地修改在提交之前，可以先自动地检验本地的时间是否是正确的，因此我们就需要使用到Git钩子，


# 参考资料

[官方参考书](https://git-scm.com/book/zh/v2)
