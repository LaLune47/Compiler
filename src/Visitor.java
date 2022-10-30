import AST.BranchNode;
import AST.LeafNode;
import AST.MyError;
import AST.Node;
import SymbolTable.SymbolTable;
import SymbolTable.SingleItem;
import SymbolTable.Variability;
import SymbolTable.Dimension;
import SymbolTable.ArraySpace;
import component.NonTerminator;
import component.TokenTYPE;

import java.util.ArrayList;

// 遍历器
public class Visitor {
    private Node ast;
    
    public Visitor(Node ast) {
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
        Node block = null;  // todo 看看会不会报指针错误
        if (4 < mainFunc.getChildren().size()) {
            block = mainFunc.getChildren().get(4);
            SymbolTable childTable = parseBlock(block,1,rootTable);
            rootTable.addChild(childTable);
        }
        return rootTable;
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
        if (typeCheckBranch(node,NonTerminator.ConstDecl)) {
            int index = 2;
            ArrayList<Node> children = node.getChildren();
            while(index < children.size() && typeCheckBranch(children.get(index),NonTerminator.ConstDef)) {
                parseConstDef(children.get(index),table);
                index += 2;
            }
        }
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
        // todo 解析函数定义
    }
    
    private boolean typeCheckBranch(Node node, NonTerminator nonTerminator) {
        return ((BranchNode)node).getNonTerminator().equals(nonTerminator);
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
    
    public void errorHandling(ArrayList<MyError> errorList) {
        // 首先全局来一个 符号表，从ast上得到
        // todo
        
        // errorlist.addError
    }
}
