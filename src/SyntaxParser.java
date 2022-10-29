import AST.AstBuilder;
import AST.MyError;
import AST.Node;
import component.Token;

import java.util.ArrayList;
import java.util.List;

/*
    语法解读器
    建树ast，更新自己的ast，整个程序可以没有信息损耗的全部变成树（包含一部分错误处理，漏掉符号的）
*/

public class SyntaxParser {
    private List<Token> tokenList;
    private Node ast;
    private ArrayList<MyError> errorList;
    
    public SyntaxParser(List<Token> tokenList, ArrayList<MyError> errorList) {
        this.tokenList = tokenList;
        this.ast = null;
        this.errorList = errorList;
    }
    
    public Node parseAndBuildAst() {  // 建树,完成部分错误处理(需要改变树的结构的拿一些)
        AstBuilder astBuilder = new AstBuilder(tokenList,errorList);
        this.ast = astBuilder.CompUnit();
        return ast;
    }
}
