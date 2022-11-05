import AST.BranchNode;
import AST.MyError;
import AST.Node;
import MidCode.MidCode;
import MipsCode.MipsGenerator;
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
        SymbolTable symbolTable = symbolTableBuilder.buildSymbolTable();  // 符号表建立+错误生成补丁+中间代码生成
        // todo 纠葛的顺序，之后好好调整
        ArrayList<MidCode> midCodes = symbolTableBuilder.getMidCodes();
        ArrayList<String> conStrings = symbolTableBuilder.getConStrings();
        
//        symbolTable.setBindingNode(ast);
//        symbolTable.print();
//        Visitor visitor = new Visitor(ast);
//        visitor.errorHandling(errorList);            // 废弃：错误处理完成// todo errorList 按照行数排序输出
        
        //PrintStream ps = new PrintStream(Config.outputFilePath);
        //System.setOut(ps);
        for (MidCode midCode: midCodes) {
            System.out.println(midCode.toString());
        }
        //ps.close();
    
        MipsGenerator mipsGenerator = new MipsGenerator(midCodes,conStrings);
        mipsGenerator.printMips();
    }
}
