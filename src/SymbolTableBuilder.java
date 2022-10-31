import AST.BranchNode;
import AST.LeafNode;
import AST.MyError;
import AST.Node;
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

import java.util.ArrayList;

// 构造符号表，解决重定义类错误，b
public class SymbolTableBuilder {
    private Node ast;
    private ArrayList<MyError> errorList;
    
    public SymbolTableBuilder(Node ast,ArrayList<MyError> errorList) {
        this.ast = ast;
        this.errorList = errorList;
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
            SymbolTable childTable = parseFuncBlock(block,1,rootTable,null,FuncType.INT);
    
            rootTable.addChild(childTable);
        }
        return rootTable;
    }
    
    private SymbolTable parseFuncBlock(Node block,Integer depth,SymbolTable parentTable,
                                       ArrayList<SingleItem> parameters,FuncType funcType) {
        SymbolTable symbolTable = parseBlock(block,depth,parentTable);
        symbolTable.addAllItem(parameters,errorList);
        funcReturnError(block,funcType);
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
                    if (typeCheckBranch(declOrStmt.getFirstChild(),NonTerminator.Block)) {
                        Node subBlock = declOrStmt.getFirstChild();
                        SymbolTable subTable = parseBlock(subBlock,depth + 1,table);
                        table.addChild(subTable);
                    }
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
        } else if (4 <= children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
            if (7 <= children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
                item.setDimension(Dimension.Array2);
                item.setArraySpace(new ArraySpace(children.get(2),children.get(5)));
            } else {
                item.setDimension(Dimension.Array1);
                item.setArraySpace(new ArraySpace(children.get(2)));
            }
        }
    
        if (index < children.size() && index != 0) {
            item.setInitValue(children.get(index));
        }
    
        table.addItem(item,errorList);
    }
    
    //ConstDef,  // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    private void parseConstDef(Node node,SymbolTable table) {
        // node是 ConstDef
        SingleItem item = new SingleItem(Variability.CONST);
        item.setDefineLine(node.getLine());
        Integer index = 0;// ConstInitVal位置
        ArrayList<Node> children = node.getChildren();
        
        if (2 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.ASSIGN)) {
            item.setDimension(Dimension.Single);
            item.setArraySpace(new ArraySpace());
            item.setIdent(((LeafNode)children.get(0)).getValue());
            index = 2;
        } else if (5 < children.size() && typeCheckLeaf(children.get(1),TokenTYPE.LBRACK)) {
            item.setIdent(((LeafNode)children.get(0)).getValue());
            if (8 < children.size() && typeCheckLeaf(children.get(4),TokenTYPE.LBRACK)) {
                item.setDimension(Dimension.Array2);
                item.setArraySpace(new ArraySpace(children.get(2),children.get(5)));
                index = 8;
            } else {
                item.setDimension(Dimension.Array1);
                item.setArraySpace(new ArraySpace(children.get(2)));
                index = 5;
            }
        }
        
        if (index < children.size()) {
            item.setInitValue(children.get(index));
        }
        
        table.addItem(item,errorList);
    }
    
    private void parseFuncDef(Node node,SymbolTable table) {
        FuncDef funcDef = new FuncDef();
        funcDef.setDefineLine(node.getLine());
        ArrayList<Node> children = node.getChildren();
        if (children.size() >= 5) {
            Node type = node.getFirstChild().unwrap();
            funcDef.setType(tranType(type));
            LeafNode ident = (LeafNode) children.get(1);
            funcDef.setIdent(ident.getValue());
        }
        
        ArrayList<SingleItem> parameters = null;
        if (children.size() == 6) { // 有参数
            parameters = parseFuncFParams(children.get(4));
            funcDef.addAllParams(parameters);
        }
        table.addFunc(funcDef,errorList);
        
        int length = children.size();
        Node subBlock = children.get(length - 1);
        SymbolTable subTable = parseFuncBlock(subBlock,1,table,parameters,funcDef.getType());
        table.addChild(subTable);
        funcDef.setSymbolTable(subTable);
    }
    
    private ArrayList<SingleItem> parseFuncFParams(Node funcFParamsNode) {
        //FuncFParams,  // 函数形参表   FuncFParam { ',' FuncFParam }
        ArrayList<Node> children = funcFParamsNode.getChildren();
        ArrayList<SingleItem> params = new ArrayList<>();
        
        int index = 0;
        while(index < children.size()) {
            params.add(parseFuncFParam(children.get(index)));
        }
        
        return params;
    }
    
    private SingleItem parseFuncFParam(Node funcFParamNode) {
        //FuncFParam,  // 函数形参   BType Ident ['[' ']' { '[' ConstExp ']' }]
        ArrayList<Node> children = funcFParamNode.getChildren();
        SingleItem item = new SingleItem(Variability.PARA);
        item.setDefineLine(funcFParamNode.getLine());
        if (children.size() >= 2) {
            LeafNode ident = (LeafNode)children.get(1);
            item.setIdent(ident.getValue());
        }
        
        if (children.size() == 2) {
            item.setDimension(Dimension.Single);
            item.setArraySpace(new ArraySpace());
        } else if (children.size() == 4) {
            item.setDimension(Dimension.Array1);
            ArraySpace arraySpace = new ArraySpace();
            arraySpace.setIgnoreFirst(true);
            item.setArraySpace(arraySpace);
        } else {
            item.setDimension(Dimension.Array2);
            item.setArraySpace(new ArraySpace(true,children.get(5).unwrap()));
        }
        return item;
    }
    
    private boolean typeCheckBranch(Node node, NonTerminator nonTerminator) {
        if (node instanceof BranchNode) {
            return ((BranchNode)node).getNonTerminator().equals(nonTerminator);
        }
        return false;
    }
    
    private boolean typeCheckLeaf(Node node, TokenTYPE type) {
        return ((LeafNode)node).getTokenType().equals(type);
    }
    
    private FuncType tranType(Node type) {
        if (typeCheckLeaf(type,TokenTYPE.INTTK)) {
            return FuncType.INT;
        } else if (typeCheckLeaf(type,TokenTYPE.VOIDTK)) {
            return FuncType.VOID;
        }
        return null;
    }
}
