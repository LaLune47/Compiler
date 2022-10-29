import AST.BranchNode;
import AST.MyError;
import AST.Node;
import SymbolTable.SymbolTable;
import component.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();    // 词法分析
        
        ArrayList<MyError> errorList = new ArrayList<>();
        
        SyntaxParser syntaxParser = new SyntaxParser(tokenList,errorList);
        Node ast = syntaxParser.parseAndBuildAst();   // 语法分析+部分错误处理
        
        Visitor visitor = new Visitor(ast);
        
        SymbolTable symbolTable = visitor.buildSymbolTable();  // 符号表建立
        ((BranchNode) ast).setSymbolTable(symbolTable);
        
        visitor.errorHandling(errorList);          // 错误处理完成
        
        // todo errorList 按照行数排序
        
        //PrintStream ps = new PrintStream(Config.outputFilePath);
        //System.setOut(ps);
        for (MyError error: errorList) {
            System.out.println(error.toString());
        }
        //ps.close();
    }
}
