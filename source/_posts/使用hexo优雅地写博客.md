---
title: 使用hexo优雅的地写博客
date: 2022-06-27 16:58:54
tags: 
  - Hexo博客
abbrlink: e47b5157
categories: 技术文档
cover: "http://soincredible777.com.cn:90/7.png"
description: 博客写作的一系列标准规范
swiper_index: 2
sticky: 1
---



# 博客编写规范

博客头部信息解读



# 多设备同步更新博客



我现在想要在两台设备上面同时编辑博客，之前想的方案是，利用已经在云服务器上创建的git仓库，在每次写完博客之前先进使用pull同步最近的博客文件，但是已经创建的这个仓库是一个裸仓库，整个文件结构和一般仓库不太一样，为了省事，我想了另外一种方法：在GitHub上面创建一个仓库专门用于保存我的文章（仅仅是push<code>source</code>文件夹里的东西），每次写完博客就push上去，换设备写博客的时候再pull下来，都将整个hexo文件夹push上去，在新的设备上面先pull一下把在另外一个设备上更新的博客同步过来，在新设备上写完博客以后再push上去，最终要发布到云服务器还是要用<code>hexo d</code>命令。

由于我对于git的使用不太熟悉，下面具体操作一次，留作记录。

现在假设我在A电脑上新建了一篇文章，A电脑中有最完整的博客文件，那我应该把A电脑的<code>source</code>文件夹push上去。

进入到<code>source</code>文件夹中。

```shell
#如果source文件夹还没有初始化git
git init

#和远程仓库进行连接
git remote add origin my_git_repo_url

#查看链接
git remote -v

#如果没有问题进行push
git push origin <local>:<remote>
#注意 git pull origin <remote>:<local> 先后顺序

```

在B电脑上，我需要先将GitHub上的仓库pull回来保持最新，

```shell
git pull origin <remote>:<local>

#要与本地分支进行合并
git merge origin/main
#之后的操作就和在A电脑上的一样了

```

# 外挂标签的使用





# 在文章中实现页内跳转

<a id = "table">跳转到这里</a>

使用span标签，跳转











[跳转到Table1](#table)













































































要跳转的地方使用<code>[跳转](#这里要一样)<code>

CCF第二十六次CSP认证

