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
import component.NonTerminator;
import component.TokenTYPE;

import java.util.ArrayList;

// 遍历器
public class SymbolTableBuilder {
    private Node ast;
    
    public SymbolTableBuilder(Node ast) {
        this.ast = ast;
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
            SymbolTable childTable = parseBlock(block,1,rootTable);
            rootTable.addChild(childTable);
        }
        return rootTable;
    }
    
    private SymbolTable parseFuncBlock(Node block,Integer depth,SymbolTable parentTable,ArrayList<SingleItem> parameters) {
        SymbolTable symbolTable = parseBlock(block,depth,parentTable);
        symbolTable.addAllItem(parameters);
        return symbolTable;
    }
    
    private SymbolTable parseBlock(Node block,Integer depth,SymbolTable parentTable) {
        // 返回该层级 Block对应的符号表，并且与parent符号表连接
        SymbolTable table = new SymbolTable(depth,parentTable);
    
        for (Node childNode: block.getChildren()) {
            if (childNode instanceof LeafNode) {
                continue;
            } else {
                Node declOrStmt = unwrap(childNode);   // BlockItem  Decl | Stmt
                if (typeCheckBranch(declOrStmt,NonTerminator.Decl)) {
                    parseDecl(declOrStmt,table);
                } else {
                    if (typeCheckBranch(getFirstChild(declOrStmt),NonTerminator.Block)) {
                        Node subBlock = getFirstChild(declOrStmt);
                        SymbolTable subTable = parseBlock(subBlock,depth + 1,table);
                        table.addChild(subTable);
                    }
                }
            }
        }
        return table;
    }
    
    //ConstDecl, // 常量声明   'const' BType ConstDef { ',' ConstDef } ';'
    //VarDecl,   // 变量声明   BType VarDef { ',' VarDef } ';'
    private void parseDecl(Node node,SymbolTable table) {
        // node是 Decl
        Node curNode = unwrap(node);
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
    
        table.addItem(item);
    }
    
    //ConstDef,  // 常数定义   Ident { '[' ConstExp ']' } '=' ConstInitVal
    private void parseConstDef(Node node,SymbolTable table) {
        // node是 ConstDef
        SingleItem item = new SingleItem(Variability.CONST);
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
        
        table.addItem(item);
    }
    
    private void parseFuncDef(Node node,SymbolTable table) {
        FuncDef funcDef = new FuncDef();
        ArrayList<Node> children = node.getChildren();
        if (children.size() >= 5) {
            Node type = unwrap(getFirstChild(node));
            funcDef.setType(tranType(type));
            LeafNode ident = (LeafNode) children.get(1);
            funcDef.setIdent(ident.getValue());
        }
        
        ArrayList<SingleItem> parameters = null;
        if (children.size() == 6) { // 有参数
            parameters = parseFuncFParams(children.get(4));
            funcDef.addAllParams(parameters);
        }
        table.addFunc(funcDef);
        
        int length = children.size();
        Node subBlock = children.get(length - 1);
        SymbolTable subTable = parseFuncBlock(subBlock,1,table,parameters);
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
            item.setArraySpace(new ArraySpace(true,unwrap(children.get(5))));
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
        return ((LeafNode)node).getType().equals(type);
    }
    
    private Node unwrap(Node node) { // 去掉一层
        Node tempNode = node;
        if (!node.getChildren().isEmpty()) {
            tempNode = node.getChildren().get(0);
        }
        return tempNode;
    }
    
    private Node getFirstChild(Node node) {
        return unwrap(node);
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
