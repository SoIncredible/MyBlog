---
title: Mac终端优化记录
abbrlink: eeec06d0
date: 2024-09-11 18:08:37
tags: 环境配置
categories: 硬技能
cover: https://www.notion.so/images/page-cover/woodcuts_sekka_3.jpg
description:
swiper_index:
sticky:
---

# 下载oh-my-zsh

在终端中输入下面的命令安装oh-my-zsh
```
sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```
如果有如下报错提示`curl: (7) Failed to connect to raw.githubusercontent.com port 443 after 14 ms: Couldn't connect to server`，有两种解决方法:

- 第一种解决方法是在本地创建一个.sh脚本，将下载链接指向的脚本中的内容拷贝到本地创建的脚本中，并执行该脚本。
- 笔者更推荐第二种解决方案，因为接下来安装终端主题的时候大概率也会遇到这个报错。
进入这个网址`https://www.ipaddress.com/`，在网页右上角输入`raw.githubusercontent.com`查看并复制对应的IP地址
![](Mac终端优化记录/image.png)
在终端中执行`vim ~/etc/hosts`，在最后添加类似下面一行：

```
*替换为上面获得的IP地址* raw.githubusercontent.com
```
重新进入终端执行安装命令就可以在本地安装好`oh-my-zsh`了

# 安装命令补全和命令高亮插件

**命令补全插件**
终端中执行下面命令👇👇👇
```
cd ~/.oh-my-zsh/custom/plugins
git clone https://github.com/zsh-users/zsh-autosuggestions.git
```

**高亮插件**
终端中执行下面命令👇👇👇
```
cd ~/.oh-my-zsh/custom/plugins
git clone https://github.com/zsh-users/zsh-syntax-highlighting.git
```

执行`vim ~/.zshrc`，在打开的文件中找到 `plugins=(git)` 一行，替换成这一行:
`plugins=(git zsh-autosuggestions zsh-syntax-highlighting)`，如果没有找到则直接添加到文件中就好了。

保存并退出`~/.zshrc`，执行`source ~/.zshrc`，完成安装。

> 安装完之后，如果发现这两个插件在终端中没有生效，则需要在`~/.zshrc`中添加以下内容
> ```
> source ~/.oh-my-zsh/custom/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh
> source ~/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh
> ```
> 保存并退出`~/.zshrc`，执行`source ~/.zshrc`，重开终端，问题解决。

# 安装字体

字体使用的是`powerlevel10k`主题推荐的字体[nerd-fonts](https://github.com/ryanoasis/nerd-fonts)

终端中执行下面命令👇👇👇
```
brew install font-hack-nerd-font
```

下载完之后在终端的字体设置中搜索一下`Hack Nerd Font Mono`字体并应用。

# 安装主题

我们安装两个主题，一个主题是终端的主题，另一个主题是`oh-my-zsh`的主题。

## 安装终端主题

执行下面的命令
```
wget https://github.com/sindresorhus/terminal-snazzy/raw/main/Snazzy.terminal
```
如果上面没有使用第二种解决方案的话，这里就会下载失败。这个主题的作用是设置终端的背景颜色。找到下载到本地的`Snazzy.terminal`文件，双击它打开终端，然后在终端的设置中将`Snazzy`设置为Default主题。

## 安装oh-my-zsh主题

笔者选择的主题是[powerlevel10k](https://github.com/romkatv/powerlevel10k/tree/master)

终端中执行下面命令👇👇👇
```
git clone --depth=1 https://gitee.com/romkatv/powerlevel10k.git ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k
```

执行`vim ~/.zshrc`,找到`ZSH_THEME="anytheme"`那一行替换为`ZSH_THEME="powerlevel10k/powerlevel10k"`

保存并退出`~/.zshrc`，执行`source ~/.zshrc`。执行完毕之后会弹出该主题的配置引导，按照自己的喜好配置即可，如果对效果不满意则在终端中输入`p10k configure`可以重新进行配置。

![](Mac终端优化记录/image-1.png)

## 问题记录

- [[oh-my-zsh] theme 'powerlevel10k' not found](https://github.com/romkatv/powerlevel10k/issues/1942)
  不知道怎么搞的这个主题被安装在了笔者电脑的根目录下，正确的目录应该是`${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k`，只有在这个目录下主题才能被正确识别，另外注意`~/.zshrc`中添加一行`source ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k/powerlevel10k.zsh-theme`代码，注释掉之前source该主题的命令。
- [[WARNING]: Console output during zsh initialization detected](https://github.com/romkatv/powerlevel10k/issues/1023)
  直接在引导配置的最后一项中选择`Quite`，而不是选择推荐的`Verbose`。
  ![](Mac终端优化记录/image-2.png)

# 参考资料
- [MAC 终端美化教程（来个全套）](https://blog.csdn.net/weixin_42326144/article/details/121957795?spm=1001.2101.3001.6650.7&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&utm_relevant_index=12)
- [Mac 终端美化指南](https://zhuanlan.zhihu.com/p/554264938)
- [更改MAC终端样式(美化、易用的提示等)](https://blog.csdn.net/m0_60980259/article/details/135977657)
- [Mac 终端配置 oh-my-zsh 和自动补全以及命令高亮](https://blog.csdn.net/wjp52/article/details/124426943)
- [安装oh-my-zsh，配置命令行高亮，命令提示，打造高效终端](https://blog.csdn.net/a143730/article/details/135573409)
- [curl: (7) Failed to connect to raw.githubusercontent.com port 443: Connection refused的几种解决方式](https://huaweicloud.csdn.net/6509554c993dd34278ee3a0f.html?dp_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ODkxMDEzLCJleHAiOjE3Mjc3ODcwNDQsImlhdCI6MTcyNzE4MjI0NCwidXNlcm5hbWUiOiJxcV80NTcyMzgyMSJ9.3rYUSIkc7_U_kaBdQ-35s0Kr6Sff-06B_M229QXJU8s&spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Eactivity-2-106862753-blog-123021848.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Eactivity-2-106862753-blog-123021848.235%5Ev43%5Econtrol&utm_relevant_index=3)