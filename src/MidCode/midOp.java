package MidCode;

public enum midOp {
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
    STRCON, // 常量字符串
    PRINTEXP, //写变量
    PRINTSTR, //写字符串
    
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
