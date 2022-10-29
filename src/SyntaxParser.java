import AST.AstBuilder;
import AST.AstCorrector;
import AST.Node;
import component.Token;

import java.util.List;

/*
    语法解读器
    建树ast，更新自己的ast，整个程序可以没有信息损耗的全部变成树（树中保存错误信息，保存符号表的位置，反正不在这个程序里进行）
*/

public class SyntaxParser {
    private List<Token> tokenList;
    private Node ast;
    
    public SyntaxParser(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.ast = null;
    }
    
    public Node parseAndBuildAst() {  // 建树同时也完成了
        AstBuilder astBuilder = new AstBuilder(tokenList);
        this.ast = astBuilder.CompUnit();
        return ast;
    }
    
    public Node CorrectAst(Node node) {
        AstCorrector astCorrector = new AstCorrector(node);
        this.ast = astCorrector.correct();
        return ast;
    }
    
    public Node getAst() {
        return ast;
    }
}
