# Hi～ 😚

我在这里要说点啥呢，我在`.gitignore`文件中忽略了`node_modules`这个文件夹，所以我在其他设备上去clone这个仓库的时候获取不到我在这个文件夹内做的更改。

既然你都`igonre`了，那为啥要改这个文件夹的内容啊？那把这个文件夹从.ignore中去掉不OK么？

应该是ok的，但是这个module是存放各种依赖的仓库，在新的设备上使用`npm install`命令就能够很快的将所有我们需要的依赖下载下来，如果我非要把这个依赖文件夹上传到GitHub上来，就GitHub这一言难尽的龟速....所还是算了，由于依赖导致Bug需要我们自己去改这些依赖中的代码，因此我会在这个README文件中记录所有需要修改的依赖。

- ## 文章目录无法跳转

  打开浏览器的开发者模式，toc-link并没有生成，所以没有办法实现跳转，解决办法：

  安装插件：

  ```shell
  npm install markdown-it-named-headings --save
  ```

  然后进入项目的根目录，修改根目录下 `node_modules\hexo-renderer-markdown-it\lib\renderer.js` 文件，在 `renderer.js` 中添加一行以使用此插件：

  ```js
  parser.use(require('markdown-it-named-headings'))
  ```

  ![](README/1.png)

