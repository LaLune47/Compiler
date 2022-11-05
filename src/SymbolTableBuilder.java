import AST.BranchNode;
import AST.LeafNode;
import AST.MyError;
import AST.Node;
import MidCode.MidCode;
import MidCode.midOp;
import MidCode.ExpItem;
import SymbolTable.SymbolTable;
import SymbolTable.SingleItem;
import SymbolTable.Variability;
import SymbolTable.Dimension;
import SymbolTable.ArraySpace;
import SymbolTable.FuncDef;
import SymbolTable.FuncType;
import component.ErrorTYPE;
import component.NonTerminator;
import component.TokenTYPE;
import component.Token;

import java.util.ArrayList;
import java.util.HashMap;

// 构造符号表，解决重定义类错误，b
public class SymbolTableBuilder {
    private Node ast;
    private ArrayList<MyError> errorList;
    private ArrayList<MidCode> midCodes;
    private HashMap<String,String> conStrings;
    private static Integer blockNum = 1;
    private static Integer localNum = 1; // 局部变量编号
    
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
                // todo 全局变量和函数定义的重复问题
            }
        }
        
        int length = children.size();
        Node mainFunc = children.get(length - 1);
        Node block = null;
        if (4 < mainFunc.getChildren().size()) {
            block = mainFunc.getChildren().get(4);
            Integer curNum = blockNum;
            midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"start"));
            blockNum++;
            midCodes.add(new MidCode(midOp.MAIN));
            SymbolTable childTable = parseFuncBlock(block,1,rootTable,null,FuncType.INT);
            midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"end"));
            midCodes.add(new MidCode(midOp.EXIT));
            rootTable.addChild(childTable);
        }
        return rootTable;
    }
    
    private SymbolTable parseFuncBlock(Node block,Integer depth,SymbolTable parentTable,
                                       ArrayList<SingleItem> parameters,FuncType funcType) {
        SymbolTable symbolTable = parseBlock(block,depth,parentTable);
        symbolTable.addAllItem(parameters,errorList);
        funcReturnError(block,funcType);
        
        int length = midCodes.size();
        if (!midCodes.get(length - 1).isRet()) {
            midCodes.add(new MidCode(midOp.RET));  // 标记函数结束的,多出来一句
        }
        return symbolTable;
    }
    
    // todo return 的处理存在bug，看看定义
    private void funcReturnError(Node block,FuncType funcType) {
        //Block,     // 语句块  '{' { BlockItem } '}'
        //BlockItem, // （不输出）语句块项   Decl | Stmt
        //stmt ----  return
        if (block.getChildren() != null) {
            Integer size = block.getChildren().size();
            Node blockItem = block.childIterator(size - 2);
            if (funcType.equals(FuncType.INT) && !checkReturn(blockItem)) {
                MyError error = new MyError(ErrorTYPE.MissReturn_g);
                error.setLine(block.childIterator(size - 1).getLine());   // 括号行号
                errorList.add(error);
            } else if (funcType.equals(FuncType.VOID) && checkReturn(blockItem)) {
                MyError error = new MyError(ErrorTYPE.SurplusReturn_f);
                error.setLine(blockItem.getLine());
                errorList.add(error);
            }
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
    
    private SymbolTable parseBlock(Node block,Integer depth,SymbolTable parentTable) {
        // 返回该层级 Block对应的符号表，并且与parent符号表连接
        SymbolTable table = new SymbolTable(depth,parentTable);
    
        for (Node childNode: block.getChildren()) {
            if (childNode instanceof LeafNode) {
                continue;
            } else {
                Node declOrStmt = childNode.unwrap();   // BlockItem  Decl | Stmt
                if (typeCheckBranch(declOrStmt,NonTerminator.Decl)) {
                    parseDecl(declOrStmt,table);
                } else {
                    Stmt(declOrStmt,table,depth);
//                    if (typeCheckBranch(declOrStmt.getFirstChild(),NonTerminator.Block)) {
//                        Node subBlock = declOrStmt.getFirstChild();
//                        Integer curNum = blockNum;
//                        midCodes.add(new MidCode(Operation.LABEL,curNum.toString(),"start"));
//                        blockNum++;
//                        SymbolTable subTable = parseBlock(subBlock,depth + 1,table);
//                        midCodes.add(new MidCode(Operation.LABEL,curNum.toString(),"end"));
//                        table.addChild(subTable);
//                    }
                }
            }
        }
        table.setBindingNode(block);
        table.setLine(block.getFirstLeafNode().getLine(),block.getLastLeafNode().getLine());
        // todo 要不就在这里把错误处理做了得了
        //lValConstError();
        return table;
    }
    
//    private void lValError(Node block,SymbolTable table) {
//        //Block,     // 语句块  '{' { BlockItem } '}'
//        //BlockItem, // （不输出）语句块项   Decl | Stmt
//         /*  Stmt,   LVal左值表达式类，exp或者getint()
//                     [EXP] ';'
//                     Block嵌套
//                     if、while等关键词
//                    */
//        for (Node childNode: block.getChildren()) {
//            if (childNode instanceof LeafNode) {
//                continue;
//            } else {
//                Node declOrStmt = childNode.unwrap();   // BlockItem  Decl | Stmt
//                if (typeCheckBranch(declOrStmt,NonTerminator.Stmt)) {
//                    Node node = declOrStmt.getFirstChild();
//                    if (typeCheckBranch(node,NonTerminator.LVal)) {
//                        // LVal  左值表达式  → Ident {'[' Exp ']'}
//                        String ident = node.getFirstLeafNode().getValue();
//                        // 左值错误   没写完呢还
//                    }
//                }
//            }
//        }
//    }
    
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
        SingleItem item = new SingleItem(Variability.VAR);
        item.setDefineLine(node.getLine());
        Integer index = 0;// InitVal位置
        ArrayList<Node> children = node.getChildren();
    
        MidCode midCode = null;
        
        if (!children.isEmpty()) {
            item.setIdent(((LeafNode)children.get(0)).getValue());
        }
        int length = children.size();
        if (length - 2 >= 0 && typeCheckLeaf(children.get(length - 2),TokenTYPE.ASSIGN)) {
            index = length - 1;
        }
        
        if (children.size() == 1 || (children.size() >= 2 && !typeCheckLeaf(children.get(1),TokenTYPE.LBRACK))) {
            item.setDimension(Dimension.Single);
            item.setArraySpace(new ArraySpace());
            midCode = new MidCode(midOp.VAR,item.getIdent());
            if (children.size() == 3) {
                midCode.setX(setInitValue(children.get(2)).getStr());
            }
        }
//        else if (4 <= children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
//            if (7 <= children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
//                item.setDimension(Dimension.Array2);
//                item.setArraySpace(new ArraySpace(children.get(2),children.get(5)));
//            } else {
//                item.setDimension(Dimension.Array1);
//                item.setArraySpace(new ArraySpace(children.get(2)));
//            }
//        }
        table.addItem(item,errorList);
        midCodes.add(midCode);
    }
    
    //ConstDef,  // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    private void parseConstDef(Node node,SymbolTable table) {
        // node是 ConstDef
        // todo 数组定义这里，错误处理部分好像写多了，代码生成部分写
        SingleItem item = new SingleItem(Variability.CONST);
        item.setDefineLine(node.getLine());
        Integer index = 0;// ConstInitVal位置
        ArrayList<Node> children = node.getChildren();
        
        MidCode midCode = null;
        
        if (2 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.ASSIGN)) {
            item.setDimension(Dimension.Single);
            item.setArraySpace(new ArraySpace());
            item.setIdent(((LeafNode)children.get(0)).getValue());
            midCode = new MidCode(midOp.CONST,item.getIdent());
            index = 2;
            if (children.size() == 3) {
                Integer initValue = CalConst(children.get(2).getFirstChild(),table);  // todo 数组
                item.setInit(initValue);
                midCode.setX(initValue.toString());
            }
        }
//        else if (5 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
//            item.setIdent(((LeafNode)children.get(0)).getValue());
//            if (8 < children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
//                item.setDimension(Dimension.Array2);
//                item.setArraySpace(new ArraySpace(children.get(2),children.get(5)));
//                index = 8;
//            } else {
//                item.setDimension(Dimension.Array1);
//                item.setArraySpace(new ArraySpace(children.get(2)));
//                index = 5;
//            }
//        }
        table.addItem(item,errorList);
        midCodes.add(midCode);
    }
    
    private ExpItem setInitValue(Node node) {
        //ConstInitVal, // 常量初值   ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        // InitVal,      // 变量初值   Exp | '{' [ InitVal { ',' InitVal } ] '}
        // node 为 InitVal 或者 ConstInitVal
        // todo 数组初始化的很多东西，惊喜的是parseInitVal应该已经把多层初始化搞完了，见SingleItem类
        
        Node addExp = node.unwrap().unwrap();
        return AddExp(addExp);
    }
    
    private void parseFuncDef(Node node,SymbolTable table) {
        //FuncDef,  // 函数定义  FuncType Ident '(' [FuncFParams] ')' Block
        FuncDef funcDef = new FuncDef();
        funcDef.setDefineLine(node.getLine());
        ArrayList<Node> children = node.getChildren();
        
        Integer curNum = blockNum;
        blockNum++;
        midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"start"));
        if (children.size() >= 5) {
            Node type = node.getFirstChild().unwrap();
            funcDef.setType(tranType(type));
            LeafNode ident = (LeafNode) children.get(1);
            funcDef.setIdent(ident.getValue());
            MidCode code = new MidCode(midOp.FUNC,ident.getValue(),node.getFirstLeafNode().getValue());
            midCodes.add(code);
        }
        
        ArrayList<SingleItem> parameters = null;
        if (children.size() == 6) { // 有参数
            parameters = parseFuncFParams(children.get(3));
            funcDef.addAllParams(parameters);
        }
        table.addFunc(funcDef,errorList);
        
        int length = children.size();
        Node subBlock = children.get(length - 1);
        SymbolTable subTable = parseFuncBlock(subBlock,1,table,parameters,funcDef.getType());
        table.addChild(subTable);
        funcDef.setSymbolTable(subTable);
        
        midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"end"));
    }
    
    private ArrayList<SingleItem> parseFuncFParams(Node funcFParamsNode) {
        //FuncFParams,  // 函数形参表   FuncFParam { ',' FuncFParam }
        ArrayList<Node> children = funcFParamsNode.getChildren();
        ArrayList<SingleItem> params = new ArrayList<>();
        
        int index = 0;
        while(index < children.size()) {
            params.add(parseFuncFParam(children.get(index)));
            index += 2;
        }
        
        return params;
    }
    
    private SingleItem parseFuncFParam(Node funcFParamNode) {
        //FuncFParam,  // 函数形参   BType Ident ['[' ']' { '[' ConstExp ']' }]
        ArrayList<Node> children = funcFParamNode.getChildren();
        SingleItem item = new SingleItem(Variability.PARA);
        item.setDefineLine(funcFParamNode.getLine());
        
        MidCode midCode = new MidCode(midOp.PARA);
        if (children.size() >= 2) {
            LeafNode ident = (LeafNode)children.get(1);
            item.setIdent(ident.getValue());
            midCode.setZ(ident.getValue());
        }
        
        if (children.size() == 2) {
            item.setDimension(Dimension.Single);
            midCode.setX("0");
            item.setArraySpace(new ArraySpace());
        } else if (children.size() == 4) {
            item.setDimension(Dimension.Array1);
            midCode.setX("1");
            ArraySpace arraySpace = new ArraySpace();
            arraySpace.setIgnoreFirst(true);
            item.setArraySpace(arraySpace);
        } else {
            item.setDimension(Dimension.Array2);
            midCode.setX("2");
            item.setArraySpace(new ArraySpace(true,children.get(5).unwrap()));
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
    
    private FuncType tranType(Node type) {
        if (typeCheckLeaf(type,TokenTYPE.INTTK)) {
            return FuncType.INT;
        } else if (typeCheckLeaf(type,TokenTYPE.VOIDTK)) {
            return FuncType.VOID;
        }
        return null;
    }
    
    private void Stmt(Node stmt,SymbolTable table,Integer depth){
    /*  Stmt →
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
        // todo 各种语句分别处理；
        if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.Block)) {
            Node subBlock = stmt.getFirstChild();
            Integer curNum = blockNum;
            midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"start"));
            blockNum++;
            SymbolTable subTable = parseBlock(subBlock,depth + 1,table);
            midCodes.add(new MidCode(midOp.LABEL,curNum.toString(),"end"));
            table.addChild(subTable);
        } else if (typeCheckLeaf(stmt.getFirstLeafNode(),TokenTYPE.RETURNTK)) {
            if (stmt.childIterator(1).equals(TokenTYPE.SEMICN)) {  // return ;
                // 什么都不用做，外层做了
            } else {
                ExpItem z = AddExp(stmt.childIterator(1).unwrap());
                midCodes.add(new MidCode(midOp.RET,z.getStr()));
            }
        } else if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.Exp)) {
            AddExp(stmt.childIterator(0).unwrap());
        } else if (typeCheckBranch(stmt.getFirstChild(),NonTerminator.LVal)) {
            ExpItem xItem;
            if (typeCheckBranch(stmt.childIterator(2),NonTerminator.Exp)) {
                xItem = AddExp(stmt.childIterator(2).unwrap());
            } else {
                xItem = new ExpItem("scan",localNum);
                localNum++;
                midCodes.add(new MidCode(midOp.SCAN,xItem.getStr()));
            }
            // LVal  Ident {'[' Exp ']'}
            Node lVal = stmt.getFirstChild();
            ExpItem z = new ExpItem(lVal.getFirstLeafNode().getToken());   // todo 数组再改
    
            midCodes.add(new MidCode(midOp.ASSIGNOP,z.getStr(),xItem.getStr()));
        } else if (typeCheckLeaf(stmt.getFirstLeafNode(),TokenTYPE.PRINTFTK)) {
            // 'printf''('FormatString{,Exp}')'';' // i j l
            String origin = stmt.childIterator(2).getFirstLeafNode().getValue();  //自带引号
            ArrayList<MidCode> printCodes = new ArrayList<>();
            
            StringBuffer buffer = new StringBuffer();
            
            int strNum = 0;
            int index = 4; // exp 最开始可能出现的地方
            for(int i = 1;i < origin.length()-1;i++) {
                if (!origin.substring(i,i + 1).equals("%")) {
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
                    ExpItem exp = AddExp(stmt.childIterator(index).unwrap());
                    printCodes.add(new MidCode(midOp.PRINTEXP,exp.getStr()));
                    
                    buffer.setLength(0);
                    i++;
                    index += 2;
                }
            }
            if (buffer.length() != 0) {
                printCodes.add(new MidCode(midOp.PRINTSTR,buffer.toString()));
                midCodes.add(new MidCode(midOp.STRCON,buffer.toString()));
            }
            midCodes.addAll(printCodes);
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
                // LVal → Ident {'[' Exp ']'}  todo 数组实现补充
                // LVal → Ident
                Node lVal = primaryExp.childIterator(0);
                return new ExpItem(lVal.getFirstLeafNode().getToken());
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
            // todo 函数调用的错误处理在这里实现
            
            Node funcRParams = unaryNode.childIterator(2);
            int i = 0;
            while (i < funcRParams.getChildren().size()) {
                ExpItem paraReal = AddExp(funcRParams.childIterator(i).unwrap());
                midCodes.add(new MidCode(midOp.PUSH,paraReal.getStr()));
                i += 2;
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
                    // LVal  → Ident {'[' Exp ']'} todo 数组的情况补充！
                    String ident = curNode.getFirstLeafNode().getValue();
                    return table.getValue(ident);
                default:
                    System.out.println("const解析错误");
                    return 0;
            }
        }
    }
    
//    private Cond() {   // 最底层就是AddExp,调用实现
//    }
}
