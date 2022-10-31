import AST.BranchNode;
import AST.MyError;
import AST.Node;
import SymbolTable.SymbolTable;
import component.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();    // 词法分析
        
        ArrayList<MyError> errorList = new ArrayList<>();
        
        SyntaxParser syntaxParser = new SyntaxParser(tokenList,errorList);
        Node ast = syntaxParser.parseAndBuildAst();   // 语法分析+部分错误处理
        
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(ast,errorList);
        SymbolTable symbolTable = symbolTableBuilder.buildSymbolTable();  // 符号表建立
        ((BranchNode) ast).setSymbolTable(symbolTable);
        symbolTable.print();
        
        Visitor visitor = new Visitor(ast);
        visitor.errorHandling(errorList);            // 错误处理完成
        
        //PrintStream ps = new PrintStream(Config.outputFilePath);
        //System.setOut(ps);
        for (MyError error: errorList) {
            // todo errorList 按照行数排序输出
            System.out.println(error.toString());
        }
        //ps.close();
    }
}
