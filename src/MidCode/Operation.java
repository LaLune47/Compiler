package MidCode;
/*
输出结构参考：
const int a = 1
var int e = 1
var int glo_1
    <LABEL 1 start>
int test_1( )
RET  1
    <LABEL 1 end>
    <LABEL 2 start>
void test_2( )
para int var_6
RET null
    <LABEL 2 end>
    <LABEL 3 start>
void test_3( )
RET null
    <LABEL 3 end>
    <LABEL 4 start>
MAIN
    <LABEL 5 start>
push e
call test_2
call test_1
retvalue t&1
    <LABEL 5 end>
scan glo_1
    <LABEL 6 start>
    <LABEL 6 end>
print "19231076\n"
RET  0
    <LABEL 4 end>
-----------------EXIT--------------


    int a = 1;
    int b = 2;
    int c = a+ b;
    
    var int a = 1
    var int b = 2
    t&1 = a + b
    var int c = t&1
    RET  0
    RET null
 */

public enum Operation {
    PLUSOP, //+
    MINUOP, //-
    MULTOP, //*
    DIVOP,  // /
    MODOP,  // %
    LSSOP,  //<
    LEQOP,  //<=
    GREOP,  //>
    GEQOP,  //>=
    EQLOP,  //==
    NEQOP,  //!=
    ASSIGNOP,  //=
    
    PUSH,  //函数调用,传参
    CALL,  //函数调用语句
    RET,   //函数返回语句
    RETVALUE, //有返回值函数返回的结果
    
    SCAN,  //读
    PRINT, //写
    
    CONST, //常量
    ARRAY, //数组
    VAR,   //变量
    FUNC,  //函数定义
    PARA, //函数参数
    
    MAIN,   //主函数进入入口
    EXIT,  //退出 main最后
    LABEL, //标号,区分不同作用域和标号
    
    GOTO,  //无条件跳转
    Jump,  //跳转标记
    BZ,    //不满足条件跳转
    BNZ,   //满足条件跳转
    
    GETARRAY,  //取数组的值  t = a[]
    PUTARRAY,  //给数组赋值  a[] = t
    
    DEFAULT
}
