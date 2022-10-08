import AST.Node;
import component.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();
//        PrintStream ps = new PrintStream(Config.outputFilePath);
//        System.setOut(ps);
//        for (Token token: tokenList) {
//            System.out.println(token.toString());
//        }
//        ps.close();
        SyntaxParser syntaxParser = new SyntaxParser(tokenList);
        Node ast = syntaxParser.parseAndBuildAst();
    
        PrintStream ps = new PrintStream(Config.outputFilePath);
        System.setOut(ps);
        ast.printNode();
        ps.close();
    }
}
