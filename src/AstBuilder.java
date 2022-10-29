import AST.BranchNode;
import AST.LeafNode;
import AST.Node;
import component.NonTerminator;
import component.Token;
import component.TokenTYPE;

import java.util.List;

// 语法分析 + todo 部分错误处理
// - i，j，k，括号分号补全，记录错误【ijk】
//- 处理printf和字符串的报错，记录错误【a,l】
public class AstBuilder {
    private final List<Token> tokenList;
    private Integer index;
    
    public AstBuilder(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.index = 0;
    }
    
    public Token peek(Integer step) {
        return tokenList.get(index + step);
    }
    
    public Token curToken() {
        if (tokenList.size() > index) {
            return tokenList.get(index);
        } else {
            return null;
        }
    }
    
    public void addLeafChild(Node currentNode) {
        LeafNode leafNode = new LeafNode(curToken());
        currentNode.addChild(leafNode);
        index += 1;
    }
    
    public boolean curEqualTo(TokenTYPE type) {
        return curToken().getType().equals(type);
    }
    
    public boolean peekEqualTo(Integer step,TokenTYPE type) {
        return peek(step).getType().equals(type);
    }
    
    // 编译单元 {Decl} {FuncDef} MainFuncDef
    public Node CompUnit() {
        BranchNode root = new BranchNode(NonTerminator.CompUnit);
        
        while (!peekEqualTo(2,TokenTYPE.LPARENT)) {   // Decl
            Node child1 = Decl();
            root.addChild(child1);
        }
        
        while (!peekEqualTo(1,TokenTYPE.MAINTK)) {  // FuncDef
            Node child2 = FuncDef();
            root.addChild(child2);
        }

        Node child3 = MainFuncDef();
        root.addChild(child3);
        return root;
    }
    
    // 声明 ConstDecl | VarDecl
    public Node Decl() {
        BranchNode currentNode = new BranchNode(NonTerminator.Decl);
        if (curEqualTo(TokenTYPE.CONSTTK)) {
            Node child = ConstDecl();
            currentNode.addChild(child);
        } else {
            Node child = VarDecl();
            currentNode.addChild(child);
        }
        return currentNode;
    }
    
    // 函数定义  FuncType Ident '(' [FuncFParams] ')' Block
    public Node FuncDef() {
        BranchNode currentNode = new BranchNode(NonTerminator.FuncDef);

        Node child1 = FuncType();
        currentNode.addChild(child1);
        
        addLeafChild(currentNode);  // Ident
        addLeafChild(currentNode);  // '('
        if (!curEqualTo(TokenTYPE.RPARENT)) {
            Node child2 = FuncFParams();
            currentNode.addChild(child2);
        }
        addLeafChild(currentNode);  // ')'
        Node child3 = Block();
        currentNode.addChild(child3);
        
        return currentNode;
    }
    
    // 函数类型   'void' | 'int'
    public Node FuncType() {
        Node currentNode = new BranchNode(NonTerminator.FuncType);
        addLeafChild(currentNode);  // 'void' | 'int'
        return currentNode;
    }
    
    // 函数形参表   FuncFParam { ',' FuncFParam }
    public Node FuncFParams() {
        Node currentNode = new BranchNode(NonTerminator.FuncFParams);
        Node child1 = FuncFParam();
        currentNode.addChild(child1);
        while (curEqualTo(TokenTYPE.COMMA)) {
            addLeafChild(currentNode);
            Node child2 = FuncFParam();
            currentNode.addChild(child2);
        }
        return currentNode;
    }
    
    // 函数形参   BType Ident [  '[' ']' { '[' ConstExp ']' }  ]
    public Node FuncFParam() {
        Node currentNode = new BranchNode(NonTerminator.FuncFParam);
        Node child1 = BType();
        currentNode.addChild(child1);
        addLeafChild(currentNode);  // Ident
        if (curEqualTo(TokenTYPE.LBRACK)) {
            addLeafChild(currentNode);  // [
            addLeafChild(currentNode);  // ]
            while (curEqualTo(TokenTYPE.LBRACK)) {
                addLeafChild(currentNode);  // [
                Node child2 = ConstExp();
                currentNode.addChild(child2);
                addLeafChild(currentNode);  // ]
            }
        }
        return currentNode;
    }
    
    // 主函数定义  'int' 'main' '(' ')' Block
    public Node MainFuncDef() {
        Node currentNode = new BranchNode(NonTerminator.MainFuncDef);
    
        addLeafChild(currentNode); // INT
        addLeafChild(currentNode); // MAIN
        addLeafChild(currentNode); // (
        addLeafChild(currentNode); // )
        
        Node child1 = Block();
        currentNode.addChild(child1);
        return currentNode;
    }
    
    // 语句块  '{' { BlockItem } '}'
    public Node Block() {
        Node currentNode = new BranchNode(NonTerminator.Block);
    
        addLeafChild(currentNode); // {
        while (!curEqualTo(TokenTYPE.RBRACE)) {
            Node child1 = BlockItem();
            currentNode.addChild(child1);
        }
        addLeafChild(currentNode); // }
        
        return currentNode;
    }
    
    // （不输出）语句块项   Decl | Stmt
    public Node BlockItem() {
        Node currentNode = new BranchNode(NonTerminator.BlockItem);
        
        if (curEqualTo(TokenTYPE.CONSTTK) || curEqualTo(TokenTYPE.INTTK)) {
            Node child = Decl();
            currentNode.addChild(child);
        } else {
            Node child = Stmt();
            currentNode.addChild(child);
        }
        return currentNode;
    }
    
    // 常量声明   'const' BType ConstDef { ',' ConstDef } ';'
    public Node ConstDecl() {
        Node currentNode = new BranchNode(NonTerminator.ConstDecl);
        
        addLeafChild(currentNode);  // 'const'
        
        Node child1 = BType();  // Btype
        currentNode.addChild(child1);

        Node child2 = ConstDef();
        currentNode.addChild(child2);

        while (curEqualTo(TokenTYPE.COMMA)) {
            addLeafChild(currentNode);
            Node child3 = ConstDef();
            currentNode.addChild(child3);
        }
    
        addLeafChild(currentNode);  // ';'
        return currentNode;
    }
    
    // 基本类型 'int'
    public Node BType() {
        Node currentNode = new BranchNode(NonTerminator.BType);
        addLeafChild(currentNode);  // 'int'
        return currentNode;
    }
    
    // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    public Node ConstDef() {
        Node currentNode = new BranchNode(NonTerminator.ConstDef);

        addLeafChild(currentNode);   // Ident
        
        while (curEqualTo(TokenTYPE.LBRACK)) {
            addLeafChild(currentNode);   // [
            Node child1 = ConstExp();
            currentNode.addChild(child1);
            addLeafChild(currentNode);   // ]
        }
        addLeafChild(currentNode);   // '='
    
        Node child2 = ConstInitVal();
        currentNode.addChild(child2);

        return currentNode;
    }
    
    // 常量初值   ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public Node ConstInitVal() {
        Node currentNode = new BranchNode(NonTerminator.ConstInitVal);

        if (curEqualTo(TokenTYPE.LBRACE)) {
            addLeafChild(currentNode);   // '{'
            if (!curEqualTo(TokenTYPE.RBRACE)) {
                Node child1 = ConstInitVal();
                currentNode.addChild(child1);
                while (curEqualTo(TokenTYPE.COMMA)) {
                    addLeafChild(currentNode);  // ','
                    Node child2 = ConstInitVal();
                    currentNode.addChild(child2);
                }
            }
            addLeafChild(currentNode);  // '}'
        } else {
            Node child1 = ConstExp();
            currentNode.addChild(child1);
        }
        
        return currentNode;
    }
    
    // 常量表达式  AddExp 注：使用的Ident 必须是常量
    public Node ConstExp() {
        Node currentNode = new BranchNode(NonTerminator.ConstExp);
        Node child = AddExp();
        currentNode.addChild(child);
        return currentNode;
    }
    
    // 涉及左递归文法的问题
    // 加减表达式  → MulExp | AddExp ('+' | '−') MulExp
    public Node AddExp() {
        Node curAddNode = new BranchNode(NonTerminator.AddExp);  // 当前的add
        
        Node child1 = MulExp();
        curAddNode.addChild(child1);
    
        while(curEqualTo(TokenTYPE.PLUS) || curEqualTo(TokenTYPE.MINU)) {
            Node newAddNode = new BranchNode(NonTerminator.AddExp);
            newAddNode.addChild(curAddNode);
            addLeafChild(newAddNode);  // +|-
            Node mul = MulExp();
            newAddNode.addChild(mul);
            curAddNode = newAddNode;
        }
        return curAddNode;
    }
    
    // 乘除模表达式  → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public Node MulExp() {
        Node curMulNode = new BranchNode(NonTerminator.MulExp);  // 当前的add
    
        Node child1 = UnaryExp();
        curMulNode.addChild(child1);
    
        while(curEqualTo(TokenTYPE.MULT) || curEqualTo(TokenTYPE.DIV) || curEqualTo(TokenTYPE.MOD)) {
            Node newMulNode = new BranchNode(NonTerminator.MulExp);
            newMulNode.addChild(curMulNode);
            addLeafChild(newMulNode);  // */%
            Node unaryExp = UnaryExp();
            newMulNode.addChild(unaryExp);
            curMulNode = newMulNode;
        }
        return curMulNode;
    }
    
    private boolean isUnaryOp() {
        return curEqualTo(TokenTYPE.PLUS) || curEqualTo(TokenTYPE.MINU) || curEqualTo(TokenTYPE.NOT);
    }
    
    // 一元表达式  → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public Node UnaryExp() {
        Node currentNode = new BranchNode(NonTerminator.UnaryExp);
        if (curEqualTo(TokenTYPE.IDENFR) && peekEqualTo(1,TokenTYPE.LPARENT)) {
            addLeafChild(currentNode);  // Ident
            addLeafChild(currentNode);  // (
            if (!curEqualTo(TokenTYPE.RPARENT)) {
                Node child = FuncRParams();
                currentNode.addChild(child);
            }
            addLeafChild(currentNode);  // )
        } else if (isUnaryOp()) {
            Node child1 = UnaryOp();
            currentNode.addChild(child1);
            Node child2 = UnaryExp();
            currentNode.addChild(child2);
        } else {
            Node child = PrimaryExp();
            currentNode.addChild(child);
        }
        return currentNode;
    }
    
    public Node UnaryOp() {
        Node currentNode = new BranchNode(NonTerminator.UnaryOp);
        addLeafChild(currentNode);   // +-!
        return currentNode;
    }
    
    // 基本表达式  → '(' Exp ')' | LVal | Number
    public Node PrimaryExp() {
        Node currentNode = new BranchNode(NonTerminator.PrimaryExp);
        if (curEqualTo(TokenTYPE.LPARENT)) {
            addLeafChild(currentNode);  // (
            Node child = Exp();
            currentNode.addChild(child);
            addLeafChild(currentNode);  // )
        } else if (curEqualTo(TokenTYPE.IDENFR)) {
            Node child = LVal();
            currentNode.addChild(child);
        } else {
            Node child = Number2();
            currentNode.addChild(child);
        }
        return currentNode;
    }
    
    // 表达式  → AddExp
    public Node Exp() {
        Node currentNode = new BranchNode(NonTerminator.Exp);
        Node child = AddExp();
        currentNode.addChild(child);
        return currentNode;
    }
    
    // 数值 → IntConst
    public Node Number2() {
        Node currentNode = new BranchNode(NonTerminator.Number);
        addLeafChild(currentNode);   // IntConst
        return currentNode;
    }
    
    // 左值表达式  → Ident {'[' Exp ']'}
    public Node LVal() {
        Node currentNode = new BranchNode(NonTerminator.LVal);
        
        addLeafChild(currentNode);  // Ident
        while (curEqualTo(TokenTYPE.LBRACK)) {
            addLeafChild(currentNode);  // [
            Node child = Exp();
            currentNode.addChild(child);
            addLeafChild(currentNode);  // ]
        }
        return currentNode;
    }
    
    // 函数实参表  → Exp { ',' Exp }
    public Node FuncRParams() {
        Node currentNode = new BranchNode(NonTerminator.FuncRParams);
        Node child = Exp();
        currentNode.addChild(child);
        while (curEqualTo(TokenTYPE.COMMA)) {
            addLeafChild(currentNode);  // ,
            Node child2 = Exp();
            currentNode.addChild(child2);
        }
        return currentNode;
    }
    
    // 变量声明   BType VarDef { ',' VarDef } ';'
    public Node VarDecl() {
        Node currentNode = new BranchNode(NonTerminator.VarDecl);
        
        Node child1 = BType();
        currentNode.addChild(child1);
        Node child2 = VarDef();
        currentNode.addChild(child2);
        
        while (curEqualTo(TokenTYPE.COMMA)) {
            addLeafChild(currentNode);  // ,
            Node child3 = VarDef();
            currentNode.addChild(child3);
        }
        addLeafChild(currentNode);  // ;
        return currentNode;
    }
    
    // 变量定义  Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public Node VarDef() {
        Node currentNode = new BranchNode(NonTerminator.VarDef);
        addLeafChild(currentNode);  //  Ident
        
        while (curEqualTo(TokenTYPE.LBRACK)) {
            addLeafChild(currentNode);  // [
            Node child1 = ConstExp();
            currentNode.addChild(child1);
            addLeafChild(currentNode);  // ]
        }
        
        if (curEqualTo(TokenTYPE.ASSIGN)) {
            addLeafChild(currentNode);  // =
            Node child2 = InitVal();
            currentNode.addChild(child2);
        }
        return currentNode;
    }
    
    // 变量初值   Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public Node InitVal() {
        Node currentNode = new BranchNode(NonTerminator.InitVal);
        if (!curEqualTo(TokenTYPE.LBRACE)) {
            Node child = Exp();
            currentNode.addChild(child);
        } else {
            addLeafChild(currentNode);  // {
            if (!curEqualTo(TokenTYPE.RBRACE)) {
                Node child1 = InitVal();
                currentNode.addChild(child1);
                while (curEqualTo(TokenTYPE.COMMA)) {
                    addLeafChild(currentNode);  // ,
                    Node child2 = InitVal();
                    currentNode.addChild(child2);
                }
            }
            addLeafChild(currentNode);  // }
        }
        return currentNode;
    }
    
    /* 语句  多种情况,主要分为
                LVal左值表达式类，exp或者getint()
                [EXP] ';'
                Block嵌套
                if、while等关键词
            */
    public Node Stmt() {
        Node currentNode = new BranchNode(NonTerminator.Stmt);
        if (curEqualTo(TokenTYPE.LBRACE)) {
            Node child = Block();
            currentNode.addChild(child);
        } else if (curEqualTo(TokenTYPE.IFTK)) {
            addLeafChild(currentNode);  // if
            addLeafChild(currentNode);  // (
            Node child1 = Cond();
            currentNode.addChild(child1);
            addLeafChild(currentNode);  // )
            Node child2 = Stmt();
            currentNode.addChild(child2);
            if (curEqualTo(TokenTYPE.ELSETK)) {
                addLeafChild(currentNode);  // else
                Node child3 = Stmt();
                currentNode.addChild(child3);
            }
        } else if (curEqualTo(TokenTYPE.WHILETK)) {
            addLeafChild(currentNode);  // while
            addLeafChild(currentNode);  // (
            Node child1 = Cond();
            currentNode.addChild(child1);
            addLeafChild(currentNode);   // )
            Node child2 = Stmt();
            currentNode.addChild(child2);
        } else if (curEqualTo(TokenTYPE.BREAKTK) || curEqualTo(TokenTYPE.CONTINUETK)) {
            addLeafChild(currentNode);  // break | continue
            addLeafChild(currentNode);  // ;
        } else if (curEqualTo(TokenTYPE.RETURNTK)) {
            addLeafChild(currentNode);  // return
            if (!curEqualTo(TokenTYPE.SEMICN)) {
                Node child = Exp();
                currentNode.addChild(child);
            }
            addLeafChild(currentNode);  // ;
        } else if (curEqualTo(TokenTYPE.PRINTFTK)) {
            addLeafChild(currentNode);   // printf
            addLeafChild(currentNode);   // (
            addLeafChild(currentNode);   // FormatString
            while (curEqualTo(TokenTYPE.COMMA)) {
                addLeafChild(currentNode);   // ,
                Node child = Exp();
                currentNode.addChild(child);
            }
            addLeafChild(currentNode);   // )
            addLeafChild(currentNode);   // ;
        }   /*
             LVal '=' Exp ';'
            | [Exp] ';' //有无Exp两种情况
            | LVal '=' 'getint''('')'';'
            */
        else if (curEqualTo(TokenTYPE.SEMICN)) {
            addLeafChild(currentNode);  // [Exp] ';' ,没有Exp的情况
        } else if (stmtCondition()) {
            Node child = Exp();
            currentNode.addChild(child);
            addLeafChild(currentNode);  // [Exp] ';' ,有Exp的部分情况（exp不为Lval）
        } else {
            Integer temp = index;
            Node lVal = LVal();
            if (!curEqualTo(TokenTYPE.ASSIGN)) {  // [Exp] ';' ,有Exp的部分情况（exp为Lval）
                index = temp;
                Node exp = Exp();
                currentNode.addChild(exp);
                addLeafChild(currentNode);  //';'
            } else {
                currentNode.addChild(lVal);
                addLeafChild(currentNode);  // =
                if (curEqualTo(TokenTYPE.GETINTTK)) {
                    addLeafChild(currentNode);  // getint
                    addLeafChild(currentNode);  // (
                    addLeafChild(currentNode);  // )
                    addLeafChild(currentNode);  // ;
                } else {
                    Node child = Exp();
                    currentNode.addChild(child);
                    addLeafChild(currentNode);  // ;
                }
            }
        }
        return currentNode;
    }
    
    private boolean stmtCondition() {   // 一些判断第一个token不为Lval的情况
        return curEqualTo(TokenTYPE.LPARENT) ||
                curEqualTo(TokenTYPE.INTCON) || isUnaryOp()
                || (curEqualTo(TokenTYPE.IDENFR) && peekEqualTo(1,TokenTYPE.LPARENT));
    }
    
    // 条件表达式   → LOrExp
    public Node Cond() {
        Node currentNode = new BranchNode(NonTerminator.Cond);
        Node child = LOrExp();
        currentNode.addChild(child);
        return currentNode;
    }
    
    // 逻辑或表达式  LAndExp | LOrExp '||' LAndExp
    public Node LOrExp() {
        Node curOrNode = new BranchNode(NonTerminator.LOrExp);  // 当前的Or
    
        Node child1 = LAndExp();
        curOrNode.addChild(child1);
    
        while (curEqualTo(TokenTYPE.OR)) {
            Node newOrNode = new BranchNode(NonTerminator.LOrExp);
            newOrNode.addChild(curOrNode);
            addLeafChild(newOrNode);  //  ||
            Node and = LAndExp();
            newOrNode.addChild(and);
            curOrNode = newOrNode;
        }
        return curOrNode;
    }
    
    // 逻辑与表达式  → EqExp | LAndExp '&&' EqExp
    public Node LAndExp() {
        Node curAndNode = new BranchNode(NonTerminator.LAndExp);  // 当前的and
    
        Node child1 = EqExp();
        curAndNode.addChild(child1);
    
        while (curEqualTo(TokenTYPE.AND)) {
            Node newAndNode = new BranchNode(NonTerminator.LAndExp);
            newAndNode.addChild(curAndNode);
            addLeafChild(newAndNode);  // &&
            Node eq = EqExp();
            newAndNode.addChild(eq);
            curAndNode = newAndNode;
        }
        return curAndNode;
    }
    
    // 相等性表达式  → RelExp | EqExp ('==' | '!=') RelExp
    public Node EqExp() {
        Node curEqNode = new BranchNode(NonTerminator.EqExp);  // 当前的eq
    
        Node child1 = RelExp();
        curEqNode.addChild(child1);
    
        while (curEqualTo(TokenTYPE.EQL) || curEqualTo(TokenTYPE.NEQ)) {
            Node newEqNode = new BranchNode(NonTerminator.EqExp);
            newEqNode.addChild(curEqNode);
            addLeafChild(newEqNode);  // == | !=
            Node rel = RelExp();
            newEqNode.addChild(rel);
            curEqNode = newEqNode;
        }
        return curEqNode;
    }
    
    // 关系表达式  → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public Node RelExp() {
        Node curRelNode = new BranchNode(NonTerminator.RelExp);  // 当前的and
    
        Node child1 = AddExp();
        curRelNode.addChild(child1);
    
        while (curEqualTo(TokenTYPE.GEQ) || curEqualTo(TokenTYPE.GRE)
                || curEqualTo(TokenTYPE.LEQ) || curEqualTo(TokenTYPE.LSS)) {
            Node newRelNode = new BranchNode(NonTerminator.RelExp);
            newRelNode.addChild(curRelNode);
            addLeafChild(newRelNode);  // '<' | '>' | '<=' | '>='
            Node add = AddExp();
            newRelNode.addChild(add);
            curRelNode = newRelNode;
        }
        return curRelNode;
    }
}
