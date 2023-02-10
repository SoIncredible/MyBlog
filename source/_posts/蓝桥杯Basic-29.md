---
title: Basic-29 高精度加法
tags: 蓝桥杯
abbrlink: fc881a87
categories: 算法
date: 2022-06-27 00:00:00
---

**资源限制**

时间限制：1.0s   内存限制：512.0MB


**问题描述**

输入两个整数*a*和*b*，输出这两个整数的和。*a*和*b*都不超过100位。

<!-- more -->
**算法描述**

由于*a*和*b*都比较大，所以不能直接使用语言中的标准数据类型来存储。对于这种问题，一般使用数组来处理。  
 定义一个数组*A*，*A*[0]用于存储*a*的个位，*A*[1]用于存储*a*的十位，依此类推。同样可以用一个数组*B*来存储*b*。  
 计算*c* = *a* + *b*的时候，首先将*A*[0]与*B*[0]相加，如果有进位产生，则把进位（即和的十位数）存入*r*，把和的个位数存入*C*[0]，即*C*[0]等于(*A*[0]+*B*[0])%10。然后计算*A*[1]与*B*[1]相加，这时还应将低位进上来的值*r*也加起来，即*C*[1]应该是*A*[1]、*B*[1]和*r*三个数的和．如果又有进位产生，则仍可将新的进位存入到*r*中，和的个位存到*C*[1]中。依此类推，即可求出*C*的所有位。最后将*C*输出即可。

**输入格式**

输入包括两行，第一行为一个非负整数*a*，第二行为一个非负整数*b*。两个整数都不超过100位，两数的最高位都不是0。



**输出格式**

输出一行，表示*a* + *b*的值。



**样例输入**

20100122201001221234567890  
2010012220100122



**样例输出**

20100122203011233454668012


```cpp

#include <iostream>
#include <vector>
using namespace std;
int main() {

    vector<int> A;
    vector<int> B;
    vector<int> C;
    string a,b;
    int add = 0;  //表示进位
    
    //几个注意的小细节
    //1.string类型可以用数组表示其中的某个字符，但类型是char类，比如 s[1]
    //2.我定义的A B C是int类型的，但是输入的a b是string类型的，要经过类型的转换
    //3.结合第一条，我们要用到的是atoi函数，而不是stoi，但是vector的push_back函数里好像不能用atoi，所以我选择用了数字对应ASCII码 - 48，也能得到正确的数字
    
    
    
    //我在对A B进行相加运算时的想法
    //我想让A B一样的长度，如果A更长一些，那么A比B多出来的长度就补0
    //A B等长的好处体现在循环处，现在假设如果B已经到头了，而A还没有
    //那按照等长的写法，你可以不用改变你的代码，相当于时A[i] + 0
    
    cin >> a;
    cin >> b;

    int i = a.size() - 1;
    int j = b.size() - 1;

    while(i >= 0){
        A.push_back(a[i] - 48);
        i--;
    }
    while(j >= 0){
        B.push_back(b[j] - 48);
        j--;
    }

    int cha;
    i = a.size();
    j = b.size();
    if(i > j){
        cha = i -j;
        while(cha > 0){
            B.push_back(0);
            cha --;
        }

    }else{
        cha = j - i;
        while (cha > 0){
            A.push_back(0);
            cha --;
        }
    }
    i = 0;
    //计算两数之和
    while(i < A.size()){

        int res = add;
        int temp = A[i] + B[i];
        res += temp;
        if(res / 10 != 0){
            add = res/10;
            res = res%10;
            if(i == A.size() -1){
                if(add == 0) break;
                else{
                    C.push_back(res); //这里需要注意，当是最后一位并且存在进位的时候，要先把当前位的计算结果压入C中，再压入一个进位的值
                    C.push_back(add);
                    break;
                }
            }
        }else add = 0;
        C.push_back(res);
        i++;
    }


    i = C.size() - 1;
    while(i >= 0){
        cout << C[i];
        i--;
    }

    return 0;
}
```

