---
title: test
abbrlink: d87f7e0c
date: 2022-12-14 01:39:31
tags:
updated:
top_img:
categories:
swiper_index: 3
description:  这里是描述
---

# 1.添加加载动画

博客里有一些图片比较大,进人到博客后还没有完全加载出来,这样很不好看,所以我想做一个加载动画.

参考原帖在[这里](https://anzhiy.cn/posts/52d8.html),注意Butterfly主题的版本是**4.5.1*.*

![](test/image-20221214202908896.png)

- 修改<code>themes/butterfly/layout/includes/loading/fullpage-loading.pug</code>

  ```css
  #loading-box(onclick='document.getElementById("loading-box").classList.add("loaded")')
    .loading-bg
      div.loading-img
      .loading-image-dot
  
  script.
    const preloader = {
      endLoading: () => {
        document.body.style.overflow = 'auto';
        document.getElementById('loading-box').classList.add("loaded")
      },
      initLoading: () => {
        document.body.style.overflow = '';
        document.getElementById('loading-box').classList.remove("loaded")
  
      }
    }
    window.addEventListener('load',()=> { preloader.endLoading() })
  
    if (!{theme.pjax && theme.pjax.enable}) {
      document.addEventListener('pjax:send', () => { preloader.initLoading() })
      document.addEventListener('pjax:complete', () => { preloader.endLoading() })
    }
  ```

- 修改<code>themes/butterfly/layout/includes/loading/index.pug</code>

  ```css
  if theme.preloader.source === 1
    include ./fullpage-loading.pug
  else if theme.preloader.source === 2
    include ./pace.pug
  else
    include ./fullpage-loading.pug
    include ./pace.pug
  ```

- 新建<code>source/css/progress_bar.css</code>, 也可以不做这一步下面配置文件`pace_css_url`这一项就要留空, 这一步是修改 pace 加载的胶囊样式用的.

  ```css
  .pace {
    -webkit-pointer-events: none;
    pointer-events: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    user-select: none;
    z-index: 2000;
    position: fixed;
    margin: auto;
    top: 10px;
    left: 0;
    right: 0;
    height: 8px;
    border-radius: 8px;
    width: 4rem;
    background: #eaecf2;
    border: 1px #e3e8f7;
    overflow: hidden;
  }
  
  .pace-inactive .pace-progress {
    opacity: 0;
    transition: 0.3s ease-in;
  }
  
  .pace .pace-progress {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    -ms-box-sizing: border-box;
    -o-box-sizing: border-box;
    box-sizing: border-box;
    -webkit-transform: translate3d(0, 0, 0);
    -moz-transform: translate3d(0, 0, 0);
    -ms-transform: translate3d(0, 0, 0);
    -o-transform: translate3d(0, 0, 0);
    transform: translate3d(0, 0, 0);
    max-width: 200px;
    position: absolute;
    z-index: 2000;
    display: block;
    top: 0;
    right: 100%;
    height: 100%;
    width: 100%;
    background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
    animation: gradient 1.5s ease infinite;
    background-size: 200%;
  }
  
  .pace.pace-inactive {
    opacity: 0;
    transition: 0.3s;
    top: -8px;
  }
  @keyframes gradient {
    0% {
      background-position: 0% 50%;
    }
    50% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0% 50%;
    }
  }
  ```

  - 修改<code>themes/butterfly/source/css/_layout/loading.styl</code>, 其中的颜色可以替换成你喜欢的色值。

    ```css
    if hexo-config('preloader')
      .loading-bg
        display: flex;
        width: 100%;
        height: 100%;
        position: fixed;
        background: var(/* 这里替换成你的色值 */);
        z-index: 1001;
        opacity: 1;
        transition: .3s;
    
      #loading-box
        .loading-img
          width: 100px;
          height: 100px;
          border-radius: 50%;
          margin: auto;
          border: 4px solid #f0f0f2;
          animation-duration: .3s;
          animation-name: loadingAction;
          animation-iteration-count: infinite;
          animation-direction: alternate;
        .loading-image-dot
          width: 30px;
          height: 30px;
          background: #6bdf8f;
          position: absolute;
          border-radius: 50%;
          border: 6px solid #fff;
          top: 50%;
          left: 50%;
          transform: translate(18px, 24px);
        &.loaded
          .loading-bg
            opacity: 0;
            z-index: -1000;
    
      @keyframes loadingAction
        0% {
            opacity: 1;
        }
    
        100% {
            opacity: .4;
        }
    
    ```

- 在<code>[BlogRoot]/source/css/</code>下创建一个custom.css的文件,在该文件中添加如下代码:

  ```css
  .loading-img {
    background: url(https://npm.elemecdn.com/anzhiyu-blog@2.1.1/img/avatar.webp) no-repeat center center;
    background-size: cover;
  }
  ```

- 修改<code>_config.butterfly.yml</code>中<code>preloader</code>选项, 改完以后<code>source: 1</code>为满屏加载无pace胶囊,<code>source: 2</code>为pace胶囊无满屏动画, <code>source: 3</code>是两者都启用。

  ```yml
  # Loading Animation (加载动画)
  preloader:
    enable: true
    # source
    # 1. fullpage-loading
    # 2. pace (progress bar)
    # else all
    source: 3
    # pace theme (see https://codebyzach.github.io/pace/)
    pace_css_url: /css/progress_bar.css
  ```

- 记得要在<code>_config.butterfly.yml</code>中添加<code>inject</code>

  ```yml
  inject:
    head:
      # 自定义css
      - <link rel="stylesheet" href="/css/custom.css" media="defer" onload="this.media='all'">
  ```

  # 2.向博客中添加图片,并且能够在Typora和网页中都能正常显示

  将<code>_config.yml</code>中<code>post_asset_folder:</code设置为true,在新建文章时hexo会自动生成一个和文章同名的文件夹存放文章中的图片.

  在

  调整Typora中的设置



