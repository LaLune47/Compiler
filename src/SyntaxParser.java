import AST.AstPrinter;
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
    private Integer index = 0;
    
    public SyntaxParser(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.ast = null;
        this.index = 0;
    }
    
    private void next() {
        index++;
    }
    
    public Node parseAndBuildAst() {  // 建树同时也完成了
        AstBuilder astBuilder = new AstBuilder();
        this.ast = astBuilder.CompUnit(tokenList,index);
        AstPrinter astPrinter = new AstPrinter(ast);
        astPrinter.PrintSyntaxParser();
        return ast;
    }
    
    public Node getAst() {
        return ast;
    }
}
