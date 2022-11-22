import AST.BranchNode;
import AST.LeafNode;
import AST.MyError;
import AST.Node;
import MidCode.MidCode;
import MidCode.midOp;
import MidCode.ExpItem;
import SymbolTable.SymbolTable;
import SymbolTable.SingleItem;
import SymbolTable.FuncDef;
import component.ErrorTYPE;
import component.NonTerminator;
import component.TokenTYPE;
import component.Token;

import java.util.ArrayList;
import java.util.HashMap;

// 部分错误处理+中间代码生成
// -b 同作用域重定义问题
// -c 未定义问题
// -g 有返回值的函数缺少return语句
// -f 无返回值的函数存在不匹配的return语句
// -h 不能改变常量的值
// -d 函数参数个数不匹配d
public class SymbolTableBuilder {
    private Node ast;
    private ArrayList<MyError> errorList;
    private ArrayList<MidCode> midCodes;
    private HashMap<String,String> conStrings;
    private boolean inVoidTypeFunc = false;
    private static Integer blockNum = 1;
    private static Integer localNum = 1; // 局部变量编号
    private static Integer strNum = 0;
    private SymbolTable calculatingTable = null; // 从addExp、Cond、CalExp下去
    
    public SymbolTableBuilder(Node ast,ArrayList<MyError> errorList) {
        this.ast = ast;
        this.errorList = errorList;
        midCodes = new ArrayList<>();
        conStrings = new HashMap<>();
    }
    
    public ArrayList<MidCode> getMidCodes() {
        return midCodes;
    }
    
    public HashMap<String, String> getConStrings() {
        return conStrings;
    }
    
    public SymbolTable buildSymbolTable() {
        int depth = 0;
        SymbolTable rootTable = new SymbolTable(0,null);
        
        ArrayList<Node> children = ast.getChildren();
        for(Node child: children) {
            if (typeCheckBranch(child,NonTerminator.Decl)) {
                parseDecl(child,rootTable);
            } else if (typeCheckBranch(child,NonTerminator.FuncDef)) {
                parseFuncDef(child,rootTable);
            }
        }
        
        int length = children.size();
        Node mainFunc = children.get(length - 1);
        Node block = null;
        if (4 < mainFunc.getChildren().size()) {
            block = mainFunc.getChildren().get(4);
            
            Integer curNum = blockNum;
            midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"start"));
            blockNum++;
            midCodes.add(new MidCode(midOp.MAIN));
            
            SymbolTable childTable = parseFuncBlock(block,1,rootTable,null,true);
            
            midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"end"));
            midCodes.add(new MidCode(midOp.EXIT));
            
            rootTable.addChild(childTable);
        }
        return rootTable;
    }
    
    private SymbolTable parseFuncBlock(Node block,Integer depth,SymbolTable parentTable,
                                       ArrayList<SingleItem> parameters,Boolean isInt) {
        if (isInt) {
            missReturnError(block);
        } else {
            inVoidTypeFunc = true;
        }
        SymbolTable symbolTable = parseBlock(block,depth,parentTable,true,parameters);
        inVoidTypeFunc = false;
        
        int length = midCodes.size();
        if (!midCodes.get(length - 1).isRet()) {
            midCodes.add(new MidCode(midOp.RET));  // 标记函数结束的,多出来一句
        }
        return symbolTable;
    }
    
    //Block,     // 语句块  '{' { BlockItem } '}'
    //BlockItem, // （不输出）语句块项   Decl | Stmt
    //stmt ----  return
    private void missReturnError(Node block) {
        Integer size = block.getChildren().size();
        Node blockItem = block.childIterator(size - 2);
        if (!checkReturn(blockItem)) {     // -g 有返回值的函数缺少return语句
            MyError error = new MyError(ErrorTYPE.MissReturn_g);
            error.setLine(block.childIterator(size - 1).getLine());   // 括号行号
            errorList.add(error);
        }
    }
    
    private boolean checkReturn(Node blockItem) {
        Node leaf = blockItem.getFirstLeafNode();
        if (leaf != null) {
            TokenTYPE tokenTYPE = ((LeafNode)leaf).getTokenType();
            if (tokenTYPE != null) {
                return tokenTYPE.equals(tokenTYPE.RETURNTK);
            }
        }
        return false;
    }
    
    private SymbolTable parseBlock(Node block,Integer depth,SymbolTable parentTable,
                                   Boolean isFuncBlock,ArrayList<SingleItem> parameters) {
        // 返回该层级 Block对应的符号表，并且与parent符号表连接
        SymbolTable table = new SymbolTable(depth,parentTable);
        if (isFuncBlock) {
            table.addAllItem(parameters,errorList);
        }
    
        for (Node childNode: block.getChildren()) {
            if (childNode instanceof LeafNode) {
                continue;
            } else {
                Node declOrStmt = childNode.unwrap();   // BlockItem  Decl | Stmt
                if (typeCheckBranch(declOrStmt,NonTerminator.Decl)) {
                    parseDecl(declOrStmt,table);
                } else {
                    Stmt(declOrStmt,table,depth);
                }
            }
        }
        return table;
    }
    
    //ConstDecl, // 常量声明   'const' BType ConstDef { ',' ConstDef } ';'
    //VarDecl,   // 变量声明   BType VarDef { ',' VarDef } ';'
    private void parseDecl(Node node,SymbolTable table) {
        // node是 Decl
        Node curNode = node.unwrap();
        if (typeCheckBranch(curNode,NonTerminator.ConstDecl)) {
            int index = 2;
            ArrayList<Node> children = curNode.getChildren();
            while(index < children.size() && typeCheckBranch(children.get(index),NonTerminator.ConstDef)) {
                parseConstDef(children.get(index),table);
                index += 2;
            }
        } else if (typeCheckBranch(curNode,NonTerminator.VarDecl)) {
            int index = 1;
            ArrayList<Node> children = curNode.getChildren();
            while(index < children.size() && typeCheckBranch(children.get(index),NonTerminator.VarDef)) {
                parseVarDef(children.get(index),table);
                index += 2;
            }
        }
    }
    
    //VarDef,  // 变量定义  Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private void parseVarDef(Node node,SymbolTable table) {
        // node是 VarDef
        SingleItem item = new SingleItem(false);
        item.setDefineLine(node.getLine());
        ArrayList<Node> children = node.getChildren();
        
        Integer index = 0;// InitVal位置,跟常量不一样，还存在没有初值的情况
    
        MidCode midCode = null;
        
        if (!children.isEmpty()) {
            item.setIdent(node.getFirstLeafNode().getValue());
        }
        int length = children.size();
        if (length - 2 >= 0 && typeCheckLeaf(children.get(length - 2),TokenTYPE.ASSIGN)) {
            index = length - 1;
        }
        
        if (children.size() == 1 || (children.size() >= 2 && !typeCheckLeaf(children.get(1),TokenTYPE.LBRACK))) {
            item.setDimension(0);
            midCode = new MidCode(midOp.VAR,item.getIdent());
            if (children.size() == 3) {
                Node addExp = children.get(2).unwrap().unwrap();
                calculatingTable = table;
                midCode.setX(AddExp(addExp).getStr());
                calculatingTable = null;
            }
            midCodes.add(midCode);
        } else if (4 <= children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
            midCode = new MidCode(midOp.ARRAY,item.getIdent());
    
            Integer space1 = CalConst(children.get(2),table);
            item.setSpace1(space1);
            midCode.setX(space1.toString());
    
            if (7 <= children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
                item.setDimension(2);
                Integer space2 = CalConst(children.get(5),table);
                item.setSpace2(space2);
                midCode.setY(space2.toString());
                midCodes.add(midCode);
                if (9 == children.size()) {
                    praseInitVal(children.get(8),space1,space2,item,table);
                }
            } else {
                item.setDimension(1);
                midCodes.add(midCode);
                if (6 == children.size()) {
                    praseInitVal(children.get(5),space1,0,item,table);
                }
            }
        }
        table.addItem(item,errorList);
        //midCodes.add(midCode);
    }
    
    private void praseInitVal(Node Init, Integer space1,Integer space2,
                              SingleItem item,SymbolTable table) {
        if (space2 == 0) {
            for(Integer i = 0;i < space1;i++) {
                Node node = Init.childIterator(2 * i + 1);
                
                Node addExp = node.unwrap().unwrap();
                calculatingTable = table;
                MidCode midCode = new MidCode(midOp.AssignARRAY,item.getIdent(),i.toString(),AddExp(addExp).getStr());
                midCodes.add(midCode);
                calculatingTable = null;
            }
        } else {
            for(Integer i = 0;i < space1;i++)  {
                Node outNode = Init.childIterator(2 * i + 1);
                for(Integer j = 0;j < space2;j++) {
                    Node node = outNode.childIterator(2 * j + 1);
    
                    Node addExp = node.unwrap().unwrap();
                    calculatingTable = table;
                    MidCode midCode = new MidCode(midOp.AssignARRAY,item.getIdent(),
                        tranArray(i,j,space2).toString(),AddExp(addExp).getStr());
                    midCodes.add(midCode);
                    calculatingTable = null;
                }
            }
        }
    }
    
    //ConstDef,  // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    private void parseConstDef(Node node,SymbolTable table) {
        // node是 ConstDef
        SingleItem item = new SingleItem(true);
        item.setDefineLine(node.getLine());
        ArrayList<Node> children = node.getChildren();
        
        MidCode midCode = null;
        
        if (2 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.ASSIGN)) {
            item.setDimension(0);
            item.setIdent(((LeafNode)children.get(0)).getValue());
            midCode = new MidCode(midOp.CONST,item.getIdent());
            if (children.size() == 3) {
                Integer initValue = CalConst(children.get(2).getFirstChild(),table);
                item.setInit(initValue);
                midCode.setX(initValue.toString());
            }
            midCodes.add(midCode);
        } else if (5 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
            item.setIdent(((LeafNode)children.get(0)).getValue());
            midCode = new MidCode(midOp.ARRAY,item.getIdent());
    
            Integer space1 = CalConst(children.get(2),table);
            item.setSpace1(space1);
            midCode.setX(space1.toString());
            
            if (8 < children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
                item.setDimension(2);
                Integer space2 = CalConst(children.get(5),table);
                item.setSpace2(space2);
                midCode.setY(space2.toString());
                midCodes.add(midCode);
                praseConstInitVal(children.get(8),space1,space2,item,table);
            } else {
                item.setDimension(1);
                midCodes.add(midCode);
                praseConstInitVal(children.get(5),space1,0,item,table);
            }
        }
        table.addItem(item,errorList);
        //midCodes.add(midCode);
    }
    
    private void praseConstInitVal(Node conInit, Integer space1,Integer space2,
                                   SingleItem item,SymbolTable table) {
        // {ConstInitVal,ConstInitVal}, -> constExp
        // [4][2] {ConstInitVal,ConstInitVal，ConstInitVal,ConstInitVal},
        if (space2 == 0) {
            for(Integer i = 0;i < space1;i++) {
                Node node = conInit.childIterator(2 * i + 1);
                Integer init = CalConst(node.unwrap(),table);
                item.addArrayInit(init);
                MidCode midCode = new MidCode(midOp.AssignARRAY,item.getIdent(),i.toString(),init.toString());
                midCodes.add(midCode);
            }
        } else {
            for(Integer i = 0;i < space1;i++)  {
                Node outNode = conInit.childIterator(2 * i + 1);
                for(Integer j = 0;j < space2;j++) {
                    Node node = outNode.childIterator(2 * j + 1);
                    Integer init = CalConst(node.unwrap(),table);
                    item.addArrayInit(init);
                    MidCode midCode = new MidCode(midOp.AssignARRAY,item.getIdent(),
                            tranArray(i,j,space2).toString(),init.toString());
                    midCodes.add(midCode);
                }
            }
        }
    }
    
    private Integer tranArray(Integer i,Integer j,Integer space2) {
        return i * space2 + j;
    }
    
    private void parseFuncDef(Node node,SymbolTable table) {
        //FuncDef  函数定义  FuncType Ident '(' [FuncFParams] ')' Block
        FuncDef funcDef = new FuncDef();
        funcDef.setDefineLine(node.getLine());
        ArrayList<Node> children = node.getChildren();
        
        Integer curNum = blockNum;
        blockNum++;
        midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"start"));
        
        if (children.size() >= 5) {
            Token funcType = node.getFirstLeafNode().getToken();
            funcDef.setInt(funcType.getType().equals(TokenTYPE.INTTK));
            LeafNode ident = (LeafNode) children.get(1);
            funcDef.setIdent(ident.getValue());
            MidCode code = new MidCode(midOp.FUNC,ident.getValue(),node.getFirstLeafNode().getValue());
            midCodes.add(code);
        }
        
        // 解析参数
        ArrayList<SingleItem> parameters = null;
        if (children.size() == 6) { // 有参数
            parameters = parseFuncFParams(children.get(3));
            funcDef.addAllParams(parameters);
        }
        table.addFunc(funcDef,errorList);
        
        // 解析函数块
        int length = children.size();
        Node subBlock = children.get(length - 1);
        SymbolTable subTable = parseFuncBlock(subBlock,1,table,parameters,funcDef.judgeInt());
        table.addChild(subTable);
        funcDef.setSymbolTable(subTable);
        
        midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"end"));
    }
    
    private ArrayList<SingleItem> parseFuncFParams(Node funcFParamsNode) {
        //FuncFParams,  // 函数形参表   FuncFParam { ',' FuncFParam }
        ArrayList<Node> children = funcFParamsNode.getChildren();
        ArrayList<SingleItem> params = new ArrayList<>();
        
        int index = 0;
        while(index < children.size()) {
            SingleItem item = parseFuncFParam(children.get(index));
            params.add(item);
            index += 2;
        }
        
        return params;
    }
    
    private SingleItem parseFuncFParam(Node funcFParamNode) {
        //FuncFParam,  // 函数形参   BType Ident ['[' ']' { '[' ConstExp ']' }]
        ArrayList<Node> children = funcFParamNode.getChildren();
        SingleItem item = new SingleItem(false);  // 形参相当于变量
        item.setDefineLine(funcFParamNode.getLine());
        
        MidCode midCode = new MidCode(midOp.PARA);
        if (children.size() >= 2) {
            LeafNode ident = (LeafNode)children.get(1);
            item.setIdent(ident.getValue());
            midCode.setZ(ident.getValue());
        }
        
        if (children.size() == 2) {   // todo 函数形参,数组
            item.setDimension(0);
            midCode.setX("0");
        } else if (children.size() == 4) {
            item.setDimension(1);
            midCode.setX("1");
        } else {
            item.setDimension(2);
            midCode.setX("2");
        }
        midCodes.add(midCode);
        return item;
    }
    
    private boolean typeCheckBranch(Node node, NonTerminator nonTerminator) {
        if (node instanceof BranchNode) {
            return ((BranchNode)node).getNonTerminator().equals(nonTerminator);
        }
        return false;
    }
    
    private boolean typeCheckLeaf(Node node, TokenTYPE type) {
        if (node instanceof LeafNode) {
            return ((LeafNode)node).getTokenType().equals(type);
        }
        return false;
    }
    
    private void Stmt(Node stmt,SymbolTable table,Integer depth){
    /*  Stmt → // todo 中间代码生成后半部分,控制流部分
        第一次: | Block
        第一次: | 'return' [Exp] ';' // f i
        第一次: | [Exp] ';'
        第一次: LVal '=' Exp ';'
        第一次: | LVal '=' 'getint''('')'';' // h i j
        第一次: | 'printf''('FormatString{,Exp}')'';' // i j l
        
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        | 'while' '(' Cond ')' Stmt // j
        | 'break' ';' | 'continue' ';' // i m
    */
        if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.Block)) {
            Node subBlock = stmt.getFirstChild();
            
            Integer curNum = blockNum;
            midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"start"));
            blockNum++;
            
            SymbolTable subTable = parseBlock(subBlock,depth + 1,table,false,null);
            
            midCodes.add(new MidCode(midOp.FUNC_BLOCK,curNum.toString(),"end"));
            
            table.addChild(subTable);
        } else if (typeCheckLeaf(stmt.getFirstLeafNode(),TokenTYPE.RETURNTK)) {
            if (typeCheckLeaf(stmt.childIterator(1),TokenTYPE.SEMICN)) {  // return ;
                // 什么都不用做，外层做了
            } else {
                calculatingTable = table;
                ExpItem z = AddExp(stmt.childIterator(1).unwrap());
                calculatingTable = null;
                midCodes.add(new MidCode(midOp.RET,z.getStr()));
                if (inVoidTypeFunc) {
                    MyError error = new MyError(ErrorTYPE.UnmatchReturn_f);
                    error.setLine(stmt.getFirstLeafNode().getLine());  // return 所在行号
                    errorList.add(error);
                }
            }
        } else if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.Exp)) {
            calculatingTable = table;
            AddExp(stmt.childIterator(0).unwrap());
            calculatingTable = null;
        }
        else if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.LVal)) {
            // LVal '=' Exp ';'    LVal '=' 'getint''('')'';'
            LeafNode ident = stmt.getFirstLeafNode();
            undefineError(ident.getToken(),table,false);
            changeConstError(ident.getToken(),table);
            
            ExpItem xItem;
            if (typeCheckBranch(stmt.childIterator(2),NonTerminator.Exp)) {
                calculatingTable = table;
                xItem = AddExp(stmt.childIterator(2).unwrap());
                calculatingTable = null;
            } else {
                xItem = new ExpItem("scan",localNum);
                localNum++;
                midCodes.add(new MidCode(midOp.SCAN,xItem.getStr()));
            }
            
            Node lVal = stmt.getFirstChild();
            ExpItem z = new ExpItem(lVal.getFirstLeafNode().getToken());
            if (lVal.getLastLeafNode().getTokenType().equals(TokenTYPE.RBRACK)) { // 左值表达式是数组元素
                ExpItem y = xItem;
                calculatingTable = table;
                ExpItem x = lvalArrayIndex(lVal);
                calculatingTable = null;
                midCodes.add(new MidCode(midOp.AssignARRAY,z.getStr(),x.getStr(), y.getStr()));
            } else {
                midCodes.add(new MidCode(midOp.ASSIGNOP,z.getStr(),xItem.getStr()));
            }
        }
        else if (typeCheckLeaf(stmt.getFirstLeafNode(),TokenTYPE.PRINTFTK)) {
            // 'printf''('FormatString{,Exp}')'';' // i j l
            String origin = stmt.childIterator(2).getFirstLeafNode().getValue();  //自带引号
            ArrayList<MidCode> printCodes = new ArrayList<>();
            
            StringBuffer buffer = new StringBuffer();
            
            int index = 4; // exp 最开始可能出现的地方
            for(int i = 1;i < origin.length()-1;i++) {
                if (!judge_d(origin,i)) {
                    buffer.append(origin.substring(i,i + 1));
                } else {
                    if (buffer.length() != 0) {
                        printCodes.add(new MidCode(midOp.PRINTSTR,buffer.toString()));
                        //midCodes.add(new MidCode(midOp.STRCON,buffer.toString()));
                        if (!conStrings.containsKey(buffer.toString())) {
                            conStrings.put(buffer.toString(),"s_"+strNum);
                            strNum++;
                        }
                        buffer.setLength(0);
                    }
                    calculatingTable = table;
                    ExpItem exp = AddExp(stmt.childIterator(index).unwrap());
                    calculatingTable = null;
                    printCodes.add(new MidCode(midOp.PRINTEXP,exp.getStr()));
                    
                    buffer.setLength(0);
                    i++;
                    index += 2;
                }
            }
            if (buffer.length() != 0) {
                printCodes.add(new MidCode(midOp.PRINTSTR,buffer.toString()));
                //midCodes.add(new MidCode(midOp.STRCON,buffer.toString()));
                if (!conStrings.containsKey(buffer.toString())) {
                    conStrings.put(buffer.toString(),"s_"+strNum);
                    strNum++;
                }
            }
            midCodes.addAll(printCodes);
        }
    }
    
    private boolean judge_d(String str,Integer index) {
        if (str.length() <= 1 || index == str.length() - 1) {
            return false;
        }
        return str.substring(index,index + 1).equals("%") && str.substring(index+1,index + 2).equals("d");
    }
    
    private void undefineError(Token token,SymbolTable table,Boolean isFunc) {
        String ident = token.getValue();
        if (!isFunc && table.findItem(ident) == null || isFunc && table.findFunc(ident) == null) {
            MyError error = new MyError(ErrorTYPE.Undefine_c);
            error.setLine(token.getLine());
            errorList.add(error);
        }
    }
    
    private void changeConstError(Token token,SymbolTable table) {
        String ident = token.getValue();
        SingleItem item = table.findItem(ident);
        if (item != null && item.getIsConst()) {
            MyError error = new MyError(ErrorTYPE.ChangeConst_h);
            error.setLine(token.getLine());
            errorList.add(error);
        }
    }
    
    private Boolean paraNumError(Integer paraRNum,Token token,SymbolTable table) {
        String ident = token.getValue();
        FuncDef func = table.findFunc(ident);
        if (func != null && func.getParaNum() != paraRNum) {
            MyError error = new MyError(ErrorTYPE.FuncParamNum_d);
            error.setLine(token.getLine());
            errorList.add(error);
            return true;
        }
        return false;
    }
    
    private void paraTypeError(Node funcRParams,Token token,SymbolTable table) {
        // FuncRParams → Exp { ',' Exp }
        if (funcRParams == null) {
            return;
        }
        String ident = token.getValue();
        FuncDef func = table.findFunc(ident);
        ArrayList<SingleItem> parameters = null;
        if (func != null) {
            parameters = func.getParameters();
        }
        if (parameters != null) {
            for (int i = 0;i < parameters.size();i++) {
                paraTypeCheck(parameters.get(i),funcRParams.childIterator(2*i),table,token.getLine());
            }
        }
    }
    
    private void paraTypeCheck(SingleItem item,Node exp,SymbolTable table,Integer line) {
        int dimension = 0;
        // 正常都是0维，把void算成-1
        // void/int : exp,addExp,mulExp,unaryExp, Ident '(' [FuncRParams] ')' 函数调用
        // 0/1/2:     exp,addExp,mulExp,unaryExp, PrimaryExp, LVal ,Ident {'[' Exp ']'}
        if (exp.unwrap() != null && exp.unwrap().unwrap() != null
                && exp.unwrap().unwrap().unwrap() != null
                && typeCheckBranch(exp.unwrap().unwrap().unwrap(),NonTerminator.UnaryExp)) {
            Node unaryExp = exp.unwrap().unwrap().unwrap();
            if (unaryExp.unwrap() != null && unaryExp.unwrap().unwrap() != null
                    &&  typeCheckBranch(unaryExp.unwrap().unwrap(),NonTerminator.LVal)) {
                Node lVal = unaryExp.unwrap().unwrap();
                String ident = lVal.getFirstLeafNode().getValue();
                int num = (lVal.getChildren().size() - 1) / 3;  // 实参括号数
                dimension = table.findItem_dimension(ident) - num;
            } else if (typeCheckLeaf(unaryExp.getFirstChild(),TokenTYPE.IDENFR)) {
                String ident = unaryExp.getFirstLeafNode().getValue();
                dimension = table.findFunc_returnType(ident);
            }
        }
        if (item != null && dimension != item.getDimension()) {
            MyError error = new MyError(ErrorTYPE.FuncParamType_e);
            error.setLine(line);
            errorList.add(error);
        }
    }
    
    private ExpItem lvalArrayIndex(Node lVal) {
        // Ident '[' Exp ']'  或者 Ident '[' Exp ']' '[' Exp ']'
        if (lVal.getChildren()!= null && lVal.getChildren().size() == 4) {
            return AddExp(lVal.childIterator(2).unwrap());
        } else {
            String ident = lVal.getFirstLeafNode().getValue();
            Integer space2 = calculatingTable.findItem_space2(ident);
            
            ExpItem i = AddExp(lVal.childIterator(2).unwrap());
            ExpItem itemSpace2 = new ExpItem("intConst",space2);
            ExpItem i_space2 = new ExpItem(midOp.MULTOP,i,itemSpace2,localNum);
            localNum++;
            midCodes.add(i_space2.toMidCode());
            ExpItem j = AddExp(lVal.childIterator(5).unwrap());
            ExpItem index = new ExpItem(midOp.PLUSOP,i_space2,j,localNum);
            localNum++;
            midCodes.add(index.toMidCode());
            return index;
        }
    }
    
    private ExpItem AddExp(Node addNode) {   // 上面出现的Exp,ConstExp,都转换到AddExp处理
        int i = 0;
        
        if (typeCheckBranch(addNode.childIterator(i),NonTerminator.MulExp)) {
            return MulExp(addNode.childIterator(i));
        } else {
            ExpItem x = AddExp(addNode.childIterator(i));
            i++;
            midOp op = addNode.childIterator(i).getFirstLeafNode().toOp();
            i++;
            ExpItem y = MulExp(addNode.childIterator(i));
            
            ExpItem z = new ExpItem(op,x,y,localNum);
            localNum++;
            midCodes.add(z.toMidCode());
            return z;
        }
    }
    
    private ExpItem MulExp(Node mulNode) {
        int i = 0;
        if (typeCheckBranch(mulNode.childIterator(i),NonTerminator.UnaryExp)) {
            return UnaryExp(mulNode.childIterator(i));
        } else {
            ExpItem x = MulExp(mulNode.childIterator(i));
            i++;
            midOp op = mulNode.childIterator(i).getFirstLeafNode().toOp();
            i++;
            ExpItem y = UnaryExp(mulNode.childIterator(i));
            
            ExpItem z = new ExpItem(op,x,y,localNum);
            localNum++;
            midCodes.add(z.toMidCode());
            return z;
        }
    }
    
    private ExpItem UnaryExp(Node unaryNode) {
        if (typeCheckBranch(unaryNode.childIterator(0),NonTerminator.PrimaryExp)) { //  PrimaryExp
            // '(' Exp ')' | LVal | Number
            Node primaryExp = unaryNode.childIterator(0);
            if (primaryExp.childIterator(0) instanceof LeafNode) {
                Node exp = primaryExp.childIterator(1);
                return AddExp(exp.unwrap());
            } else if (typeCheckBranch(primaryExp.childIterator(0),NonTerminator.Number)) {
                Node number = primaryExp.childIterator(0);
                return new ExpItem(number.getFirstLeafNode().getToken());
            } else {
                // LVal → Ident {'[' Exp ']'}
                // LVal → Ident
                Node lVal = primaryExp.childIterator(0);
                
                LeafNode ident = lVal.getFirstLeafNode();
                undefineError(ident.getToken(),calculatingTable,false);
                
                if (lVal.getLastLeafNode().getTokenType().equals(TokenTYPE.RBRACK)) { // 左值表达式是数组元素
                    ExpItem y = lvalArrayIndex(lVal);
                    ExpItem x = new ExpItem(lVal.getFirstLeafNode().getToken());
                    ExpItem z = new ExpItem("getARRAY",localNum);
                    localNum++;
                    midCodes.add(new MidCode(midOp.GetARRAY,z.getStr(),x.getStr(), y.getStr()));
                    return z;
                } else {
                    return new ExpItem(lVal.getFirstLeafNode().getToken());
                }
            }
        } else if (typeCheckBranch(unaryNode.childIterator(0),NonTerminator.UnaryOp)) { // UnaryOp UnaryExp
            //UnaryOp,  '+' | '−' | '!' '!'仅出现在条件表达式中  todo !补充实现，因为暂时没有条件表达式
            ExpItem x = new ExpItem("intConst",0);
            midOp op = unaryNode.childIterator(0).getFirstLeafNode().toOp();
            ExpItem y = UnaryExp(unaryNode.childIterator(1));
            ExpItem z = new ExpItem(op,x,y,localNum);
            localNum++;
            midCodes.add(z.toMidCode());
            return z;
        } else { // ident '(' [FuncRParams] ')'
            // FuncRParams → Exp { ',' Exp }
            // todo 函数调用,参数类型不一致
            
            LeafNode ident = unaryNode.getFirstLeafNode();
            undefineError(ident.getToken(),calculatingTable,true);
            
            Node funcRParams = null;
            int i = 0;
            int paraNum = 0;
            if (unaryNode.getChildren() != null && unaryNode.getChildren().size() == 4) {
                funcRParams = unaryNode.childIterator(2);
                while (i < funcRParams.getChildren().size()) {
                    ExpItem paraReal = AddExp(funcRParams.childIterator(i).unwrap());
                    midCodes.add(new MidCode(midOp.PUSH,paraReal.getStr()));  // todo 数组地址，指针变量区分问题
                    i += 2;
                    paraNum += 1;
                }
            }
            
            Boolean hasNumError = paraNumError(paraNum,ident.getToken(),calculatingTable);
            if (!hasNumError) {
                paraTypeError(funcRParams,ident.getToken(),calculatingTable);
            }
            
            midCodes.add(new MidCode(midOp.CALL,unaryNode.getFirstLeafNode().getValue()));
            ExpItem retValue = new ExpItem("retValue",localNum);
            localNum++;
            midCodes.add(new MidCode(midOp.RETVALUE,retValue.getStr()));
            
            return retValue;
        }
    }
    
    private Integer CalConst(Node node,SymbolTable table) {
        if (node instanceof LeafNode) {
            Token intCon = ((LeafNode) node).getToken();
            return Integer.parseInt(intCon.getValue());
        } else {
            BranchNode curNode = (BranchNode) node;
            NonTerminator type = curNode.getNonTerminator();
            switch (type) {
                case ConstExp:
                case Exp:  // 嵌套有exp的情况,但是最外层不是constExp还是会出错
                case Number:
                    return CalConst(curNode.unwrap(),table);
                case AddExp:
                    if (curNode.getChildren().size() == 1) {
                        return CalConst(curNode.unwrap(),table);
                    } else {
                        Integer a = CalConst(curNode.childIterator(0),table);
                        Integer b = CalConst(curNode.childIterator(2),table);
                        Node op = curNode.childIterator(1);
                        if (typeCheckLeaf(op,TokenTYPE.PLUS)) {
                            return a + b;
                        } else {
                            return a - b;
                        }
                    }
                case MulExp:
                    if (curNode.getChildren().size() == 1) {
                        return CalConst(curNode.unwrap(),table);
                    } else {
                        Integer a = CalConst(curNode.childIterator(0),table);
                        Integer b = CalConst(curNode.childIterator(2),table);
                        Node op = curNode.childIterator(1);
                        if (typeCheckLeaf(op,TokenTYPE.MULT)) {
                            return a * b;
                        } else if (typeCheckLeaf(op,TokenTYPE.DIV)) {
                            return a / b;
                        } else {
                            return a % b;
                        }
                    }
                case UnaryExp:
                    if (typeCheckLeaf(curNode.childIterator(0), TokenTYPE.IDENFR)) {
                        System.out.println("ConstExp---函数调用错误");
                        return null;
                    } else if (typeCheckBranch(curNode.childIterator(0), NonTerminator.PrimaryExp)) {
                        return CalConst(curNode.unwrap(),table);
                    } else {  //+-UnaryExp
                        Integer a = CalConst(curNode.childIterator(1),table);
                        Node op = curNode.childIterator(0);
                        if (typeCheckLeaf(op,TokenTYPE.PLUS)) {
                            return a;
                        } else if (typeCheckLeaf(op,TokenTYPE.MINU)) {
                            return -a;
                        }
                    }
                case PrimaryExp:
                    // 基本表达式  → '(' Exp ')' | LVal | Number
                    if (curNode.getChildren().size() == 3) {
                        return CalConst(curNode.childIterator(1),table);
                    } else {
                        return CalConst(curNode.unwrap(),table);
                    }
                case LVal:
                    // LVal  → Ident {'[' Exp ']'}
                    String ident = curNode.getFirstLeafNode().getValue();
                    if (curNode.getLastLeafNode().getTokenType().equals(TokenTYPE.RBRACK)) { // 左值表达式是数组元素
                        Integer i = CalConst(curNode.childIterator(2),table);
                        if (curNode.getChildren() != null && curNode.getChildren().size() == 4) {
                            return table.getArrayValue1(ident,i);
                        } else if (curNode.getChildren() != null && curNode.getChildren().size() == 7) {
                            Integer j = CalConst(curNode.childIterator(5),table);
                            return table.getArrayValue2(ident,i,j);
                        }
                    } else {
                        return table.getValue(ident);
                    }
                default:
                    System.out.println("const解析错误");
                    return 0;
            }
        }
    }
    
//    private Cond() {   // 最底层就是AddExp,调用实现
//    }
}
