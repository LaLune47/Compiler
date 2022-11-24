import AST.BranchNode;
import AST.MyError;
import AST.Node;
import MidCode.MidCode;
import MipsCode.MipsGenerator;
import SymbolTable.SymbolTable;
import component.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();    // 词法分析
        //for (Token token: tokenList) {
        //    System.out.println(token.toString());
        //}
        
        ArrayList<MyError> errorList = new ArrayList<>();
        
        SyntaxParser syntaxParser = new SyntaxParser(tokenList,errorList);
        Node ast = syntaxParser.parseAndBuildAst();   // 语法分析+部分错误处理
        //ast.printNode();
        
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(ast,errorList);
        SymbolTable symbolTable = symbolTableBuilder.buildSymbolTable();  // 符号表,部分错误处理+中间代码生成
        
        errorList.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((MyError) o1).getLine() - ((MyError) o2).getLine();
            }
        });
        //PrintStream ps = new PrintStream(Config.errorPath);
        //System.setOut(ps);
        //for (MyError error: errorList) {
        //    System.out.println(error.toString());
        //}
        //ps.close();
        
        ArrayList<MidCode> midCodes = symbolTableBuilder.getMidCodes();
        HashMap<String,String> conStrings = symbolTableBuilder.getConStrings();
        for (MidCode midCode: midCodes) {
            System.out.println(midCode.toString());
        }
        
        //PrintStream ps = new PrintStream(Config.mipsPath);
        //System.setOut(ps);
        //MipsGenerator mipsGenerator = new MipsGenerator(midCodes,conStrings);  // mips代码生成
        //mipsGenerator.printMips();
        //ps.close();
    }
}
