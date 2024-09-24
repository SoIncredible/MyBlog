---
title: Mac终端优化记录
date: 2024-09-11 18:08:37
tags:
categories:
cover:
description:
swiper_index:
sticky:
---

# 下载oh-my-zsh

使用官网的下载命令下载oh-my-zsh
```
sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```
如果有如下报错提示`curl: (7) Failed to connect to raw.githubusercontent.com port 443 after 14 ms: Couldn't connect to server`，解决方法是将这个sh脚本拷贝到本地，在本地执行该脚本。

但是笔者更推荐另外一种解决方案，因为下文中安装终端主题的时候大概率也会遇到这个报错。
进入这个网址`https://www.ipaddress.com/`，在网页右上角输入`raw.githubusercontent.com`查看对应的IP地址，在终端中执行`vim ~/etc/hosts`，在最后添加类似下面的字符串：
```
185.199.111.133 raw.githubusercontent.com
```
新开一个终端重新执行下载指令就可以了

# 安装命令补全和命令高亮插件

**命令补全插件**👇👇👇
```
cd ~/.oh-my-zsh/custom/plugins
git clone https://github.com/zsh-users/zsh-autosuggestions.git
```

**高亮插件**👇👇👇
```
cd ~/.oh-my-zsh/custom/plugins
git clone https://github.com/zsh-users/zsh-syntax-highlighting.git
```
在 ~/.zshrc 中修改 plugins=(git) 为：
plugins=(git zsh-autosuggestions zsh-syntax-highlighting)

安装完之后命令高亮和命令补全插件，如果发现在终端中没有生效，则需要在`~/.zshrc`中添加以下内容
```
source ~/.oh-my-zsh/custom/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh
source ~/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh
```

# 安装主题

执行下面的命令
```
wget https://github.com/sindresorhus/terminal-snazzy/raw/main/Snazzy.terminal
```
如果上面没有使用第二种解决方案的话，这里就会下载失败。这个主题的作用是设置终端的背景颜色。找到下载到本地的`Snazzy.terminal`文件，双击它打开终端，然后在终端中将它设置为Default。


下面这个主题的作用是设置终端里显示的文本和图标，该主题的git链接:https://github.com/romkatv/powerlevel10k/tree/master
进入这个开源主题的主页按照指引安装，安装完成后会有配置引导，引导设置完成后如果对效果不满意则在终端中输入`p10k configure`可以重新设置配置。

# 安装字体

使用的是`powerlevel10k`主题推荐的字体[nerd-fonts](https://github.com/ryanoasis/nerd-fonts)

```
brew install font-hack-nerd-font
```

下载完之后在终端的字体设置中搜索一下`Hack Nerd Font Mono`字体并应用


# 参考资料
- [MAC 终端美化教程（来个全套）](https://blog.csdn.net/weixin_42326144/article/details/121957795?spm=1001.2101.3001.6650.7&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&utm_relevant_index=12)
- [Mac 终端美化指南](https://zhuanlan.zhihu.com/p/554264938)
- [更改MAC终端样式(美化、易用的提示等)](https://blog.csdn.net/m0_60980259/article/details/135977657)
- [Mac 终端配置 oh-my-zsh 和自动补全以及命令高亮](https://blog.csdn.net/wjp52/article/details/124426943)
- [安装oh-my-zsh，配置命令行高亮，命令提示，打造高效终端](https://blog.csdn.net/a143730/article/details/135573409)
- [curl: (7) Failed to connect to raw.githubusercontent.com port 443: Connection refused的几种解决方式](https://huaweicloud.csdn.net/6509554c993dd34278ee3a0f.html?dp_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ODkxMDEzLCJleHAiOjE3Mjc3ODcwNDQsImlhdCI6MTcyNzE4MjI0NCwidXNlcm5hbWUiOiJxcV80NTcyMzgyMSJ9.3rYUSIkc7_U_kaBdQ-35s0Kr6Sff-06B_M229QXJU8s&spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Eactivity-2-106862753-blog-123021848.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Eactivity-2-106862753-blog-123021848.235%5Ev43%5Econtrol&utm_relevant_index=3)