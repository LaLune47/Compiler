import AST.BranchNode;
import AST.LeafNode;
import AST.Node;
import component.NonTerminator;
import component.Token;
import component.TokenTYPE;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder {
    private List<Token> tokenList;
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
    
    // todo 非终结符也到读到语法结构里啊。。。蠢猪。。。
    // todo length设置的问题没有解决
    // todo 回来之后先来个Decl的最小实例，试验一下
    public Node CompUnit() {
        // todo  语法分析主体
        NonTerminator nonTerminator = NonTerminator.CompUnit;
        BranchNode root = new BranchNode(nonTerminator);
        
//        while (!peekEqualTo(2,TokenTYPE.LPARENT)) {   // Decl
//            Node child1 = Decl();
//            root.addChild(child1);
//        }
        
//        while (!peek(1).getType().equals(TokenTYPE.MAINTK)) {  // FuncDef
//            Node child2 = FuncDef();
//            root.addChild(child2);
//        }
//
        Node child3 = MainFuncDef();
        root.addChild(child3);
        return root;
    }
    
//    public Node Decl() {
//        BranchNode decl =new BranchNode(NonTerminator.Decl);
//        if (peekEqualTo(1,TokenTYPE.INTTK)) {
//            Node constDecl = ConstDecl();
//            decl.addChild(constDecl);
//        }
////        } else {
////            Node varDecl = VarDecl();
////            decl.addChild(varDecl);
////        }
//        return decl;
//    }
    
//    public Node FuncDef() {
//        BranchNode funcDef = new BranchNode(NonTerminator.FuncDef);
//
//        Node constDef = ConstDef();
//        funcDef.addChild(constDef);
//
//        while (curToken().equals(',')) {
//            constDef = ConstDef();
//            funcDef.addChild(constDef);
//        }
//
//        return funcDef;
//    }
//
    
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
        Node child1 = BlockItem();
        currentNode.addChild(child1);
        addLeafChild(currentNode); // }
        
        return currentNode;
    }
    
    // （不输出）语句块项   Decl | Stmt
    public Node BlockItem() {
        Node currentNode = new BranchNode(NonTerminator.BlockItem);
    
        addLeafChild(currentNode); // "," 测试用
        
        return currentNode;
    }
    
//    public Node ConstDecl() {
//        Node constDecl = new BranchNode(NonTerminator.ConstDecl);
//
//        constDecl.addLeafChild(curToken());//  "const"
//        index += 1;
//        constDecl.addLeafChild(curToken()); // "int"
//        index += 1;
//
//        Node constDef = ConstDef();
//        constDecl.addChild(constDef);
//
//        while (curEqualTo(TokenTYPE.COMMA)) {
//            constDecl.addLeafChild(curToken()); // ","
//            index += 1;
//            constDef = ConstDef();
//            constDecl.addChild(constDef);
//        }
//
//        constDecl.addLeafChild(curToken()); // ";"
//        index += 1;
//
//        return constDecl;
//    }
    
//    public Node VarDecl() {
//
//    }

//    public Node ConstDef() {   // Ident { '[' ConstExp ']' } '=' ConstInitVal
//        Node constDef = new BranchNode(NonTerminator.ConstDef);
//
//        constDef.addLeafChild(curToken()); // Ident
//        index += 1;
//
//        while (curEqualTo(TokenTYPE.LBRACK)) {
//            constDef.addLeafChild(curToken()); // '['
//            index += 1;
//            Node node = ConstExp();
//            constDef.addChild(node);
//            constDef.addLeafChild(curToken()); // ']'
//            index += 1;
//        }
//        constDef.addLeafChild(curToken()); // '='
//        index += 1;
//
//        Node node = ConstInitVal();
//        constDef.addChild(node);
//
//        return constDef;
//    }
    
//    public Node ConstExp() {
//
//    }
    
//    public Node ConstInitVal() {    // ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
//        Node constInitVal = new BranchNode(NonTerminator.ConstInitVal);
//
//        if (curEqualTo(TokenTYPE.LBRACE)) {
//            constInitVal.addLeafChild(curToken()); // '{'
//            index += 1;
//
//            while (curEqualTo(T))
//        }
//    }
}
