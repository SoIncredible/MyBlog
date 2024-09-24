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


# 下载on-my-zsh

使用官网的下载命令`sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"`无法下载,提示`curl: (7) Failed to connect to raw.githubusercontent.com port 443 after 14 ms: Couldn't connect to server`,解决方法是将这个sh脚本拷贝到本地，在本地执行该脚本。

另外一种解决方案是进入这个网址`https://www.ipaddress.com/`，在网页右上角输入`raw.githubusercontent.com`查看对应的IP地址，在终端中执行`vim ~/etc/hosts`，在最后添加类似下面的字符串：
```
185.199.111.133 raw.githubusercontent.com
```
新开一个终端重新执行下载指令就可以了

安装omz的命令高亮和命令补全插件，发现在终端中没有生效，则需要在`~/.zshrc`中添加以下内容
```
source ~/.oh-my-zsh/custom/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh
source ~/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh
```

# 参考资料
- https://blog.csdn.net/weixin_42326144/article/details/121957795?spm=1001.2101.3001.6650.7&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-7-121957795-blog-135977657.235%5Ev43%5Econtrol&utm_relevant_index=12
- https://zhuanlan.zhihu.com/p/554264938
- https://blog.csdn.net/m0_60980259/article/details/135977657
- https://blog.csdn.net/wjp52/article/details/124426943