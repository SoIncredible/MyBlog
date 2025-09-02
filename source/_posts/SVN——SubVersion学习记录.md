---
title: SVN——SubVersion学习记录
abbrlink: fb782408
date: 2025-05-22 21:04:39
tags: SVN
categories: 版本管理工具
cover:
description:
swiper_index:
sticky:
---

CornerStone真的垃圾, 用Rider中的SVN插件进行版本控制.

SVN中的两种冲突
## 1. 文件冲突（file conflict）是什么？

- **文件冲突**指的是：  
    多人对**同一个文件的同一个部分**进行了不同修改。
- 例如：你和同事同时编辑 `a.txt` 的第5行内容，提交更新时就会发生文件冲突。

### 常见表现

- SVN 会把有冲突的部分插入一些 `<<<<<<<`、`=======`、`>>>>>>>` 标记，等你手动合并/选择。
- 你需要打开文件合并或直接重新编辑。

---

## 2. 树冲突（tree conflict）是什么？

- **树冲突**是 SVN 1.6 及以后的概念。
- 所谓“树”其实指**目录结构/文件结构（不是树状控件，也不是数据结构的树，而是整个项目的文件、文件夹组成的目录树）**。
- **树冲突出现在：**  
    针对目录结构操作（如文件/文件夹的新增、重命名、删除、移动）时，不同人对同一部分目录树做了**结构性变化**，并发生了冲突。

### 典型例子

1. **你删除了某个文件/文件夹，同事修改了这个文件**
    - 你删了`A/B/C.cs`，同事还在同一版本上编辑`C.cs`，你们都提交，然后更新。你就会遇到`tree conflict`。
2. **一方移动/重命名目录，另一方在原地新建或修改文件**
3. **父目录被删除、子文件被修改或反之**
4. **任何目录或者文件在结构上发生变化，而非内容修改**


# SVN撤回某一次提交

你想撤销130次提交的内容（可能此后还有131、132、133…的正常提交），只撤销这次的内容，其后的变动继续存在。
操作命令

svn merge -c -130 你的工作副本目录
svn commit -m "撤销r130提交内容"
-c参数后面加上负号-130，表示“撤销 r130”的变更。
执行后记得到工作副本看下变动，再commit。
举例

如你的项目地址为http://xxx.com/svn/project/trunk，你在trunk目录下：

svn merge -c -130 .
svn commit -m "撤销第130次提交"

# SVN查看当前Revision
```shell
svn info
```


# 写一个可以查询当天提交记录的shell脚本
```shell
#!/bin/bash

  
# 用法: ./svn_tag_summary.sh /path/to/your/svn/working_copy



SVN_PATH="$1"

AUTHOR="liyinuo@tuyoogame.com"

TODAY=$(date +"%Y-%m-%d")

  

if [[ ! -d "$SVN_PATH/.svn" ]]; then

echo "路径 $SVN_PATH 不是svn仓库"

exit 1

fi

  

cd "$SVN_PATH" || exit 1

  

# 获取今天该作者的提交记录

svn log -r {${TODAY}T00:00:00}:{${TODAY}T23:59:59} --search "$AUTHOR" --verbose --xml > __svn_today.xml

  

if ! grep -q "<logentry" __svn_today.xml; then

echo "没有今天作者 $AUTHOR 的提交记录"

rm -f __svn_today.xml

exit 0

fi

  

# 取所有 commit message

msgs=$(xmllint --xpath '//logentry[author="'$AUTHOR'"]/msg/text()' __svn_today.xml 2>/dev/null | grep -v '^$')

  

# 各分类字符串

feat_msgs=""

fix_msgs=""

refactor_msgs=""

docs_msgs=""

test_msgs=""

chore_msgs=""

other_msgs=""

  

while IFS= read -r line; do

if [[ "$line" =~ ^\[feat\][[:space:]]*[:：]? ]]; then

feat_msgs="${feat_msgs}${line}"$'\n'

elif [[ "$line" =~ ^\[fix\][[:space:]]*[:：]? ]]; then

fix_msgs="${fix_msgs}${line}"$'\n'

elif [[ "$line" =~ ^\[refactor\][[:space:]]*[:：]? ]]; then

refactor_msgs="${refactor_msgs}${line}"$'\n'

elif [[ "$line" =~ ^\[docs\][[:space:]]*[:：]? ]]; then

docs_msgs="${docs_msgs}${line}"$'\n'

elif [[ "$line" =~ ^\[test\][[:space:]]*[:：]? ]]; then

test_msgs="${test_msgs}${line}"$'\n'

elif [[ "$line" =~ ^\[chore\][[:space:]]*[:：]? ]]; then

chore_msgs="${chore_msgs}${line}"$'\n'

else

other_msgs="${other_msgs}${line}"$'\n'

fi

done <<< "$msgs"

echo "作者: $AUTHOR 今天的提交分类如下："

echo

  

if [ -n "$feat_msgs" ]; then

echo "---------------- [feat] ----------------"

echo "$feat_msgs"

fi

  

if [ -n "$fix_msgs" ]; then

echo "---------------- [fix] ----------------"

echo "$fix_msgs"

fi

  

if [ -n "$refactor_msgs" ]; then

echo "---------------- [refactor] ----------------"

echo "$refactor_msgs"

fi

  

if [ -n "$docs_msgs" ]; then

echo "---------------- [docs] ----------------"

echo "$docs_msgs"

fi

  

if [ -n "$test_msgs" ]; then

echo "---------------- [test] ----------------"

echo "$test_msgs"

fi

  

if [ -n "$chore_msgs" ]; then

echo "---------------- [chore] ----------------"

echo "$chore_msgs"

fi

  

if [ -n "$other_msgs" ]; then

echo "---------------- [other] ----------------"

echo "$other_msgs"

fi

  

rm -f __svn_today.xml
```

### 1. SVN 删除一个文件后如何用命令行提交这个修改

删除文件后，请按以下步骤操作：

#### （1）删除文件

```
svn delete 文件名
# 比如
svn delete test.txt
```

或者直接

```
svn del test.txt
```

#### （2）提交删除操作

```
svn commit -m "删除了test.txt文件"
```

---

### 2. SVN 使用命令行 Add to working copy

如果您想将某个文件或目录添加到您的工作副本（working copy），即包含到版本控制，命令如下：

#### （1）添加文件或目录

```
svn add 文件名或目录名
# 比如
svn add newfile.txt
```

如果是新文件夹，可以递归添加内部全部新文件：

```
svn add newfolder --force
```

#### （2）提交添加操作

```
svn commit -m "添加了新文件（或文件夹）"
```

---

### 总结

- **删除后提交：**
    
    ```
    svn delete 文件
    svn commit -m "说明"
    ```
    
- **新增后提交：**
    
    ```
    svn add 文件
    svn commit -m "说明"
    ```