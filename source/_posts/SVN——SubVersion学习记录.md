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

# SVN外链的使用

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