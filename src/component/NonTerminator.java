package component;

public enum NonTerminator {
    CompUnit, // 编译单元 {Decl} {FuncDef} MainFuncDef
    
    //CompUnit
    Decl,  // (不输出)声明 ConstDecl | VarDecl
    FuncDef,  // 函数定义  FuncType Ident '(' [FuncFParams] ')' Block
    MainFuncDef, // 主函数定义  'int' 'main' '(' ')' Block
    
    //Decl
    ConstDecl, // 常量声明   'const' BType ConstDef { ',' ConstDef } ';'
    VarDecl,   // 变量声明   BType VarDef { ',' VarDef } ';'
    //下一层
    BType,  // (不输出)基本类型 'int'
    ConstDef,  // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    VarDef,  // 变量定义  Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    //下一层
    ConstInitVal, // 常量初值   ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    InitVal,      // 变量初值   Exp | '{' [ InitVal { ',' InitVal } ] '}
    
    //FuncDef
    FuncType,  // 函数类型   'void' | 'int'
    FuncFParams,  // 函数形参表   FuncFParam { ',' FuncFParam }
    // 下一层
    FuncFParam,  // 函数形参   BType Ident ['[' ']' { '[' ConstExp ']' }]
    
    
    // block部分
    Block,     // 语句块  '{' { BlockItem } '}'
    BlockItem, // （不输出）语句块项   Decl | Stmt
    Stmt,  /* 语句  多种情况,主要分为
                LVal左值表达式类，exp或者getint()
                [EXP] ';'
                Block嵌套
                if、while等关键词
            */
    
    
    // exp部分
    // 基本表达式
    PrimaryExp,   // 基本表达式  → '(' Exp ')' | LVal | Number
    Exp,        // 表达式  → AddExp
    LVal,       // 左值表达式  → Ident {'[' Exp ']'}
    Number,     // 数值 → IntConst
    
    // 基于基本表达式，基本表达式
    UnaryExp,   // 一元表达式  → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    UnaryOp,    // 单目运算符 '+' | '−' | '!' '!'仅出现在条件表达式中
    FuncRParams,  // 函数实参表  → Exp { ',' Exp }
    
    // 基于一元表达式
    MulExp,  // 乘除模表达式  → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    AddExp,  // 加减表达式  → MulExp | AddExp ('+' | '−') MulExp
    
    Cond,       // 条件表达式   → LOrExp
    RelExp,  // 关系表达式  → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    EqExp,   // 相等性表达式  → RelExp | EqExp ('==' | '!=') RelExp
    LAndExp, // 逻辑与表达式  → EqExp | LAndExp '&&' EqExp
    LOrExp,  // 逻辑或表达式  LAndExp | LOrExp '||' LAndExp
    
    ConstExp,   // 常量表达式  AddExp 注：使用的Ident 必须是常量
}
