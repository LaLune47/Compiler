import component.Token;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws FileNotFoundException {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();
//        PrintStream ps = new PrintStream(Config.outputFilePath);
//        System.setOut(ps);
//        for (Token token: tokenList) {
//            System.out.println(token.toString());
//        }
//        ps.close();
        SyntaxParser syntaxParser = new SyntaxParser(tokenList);
        syntaxParser.parseAndBuildAst();
    }
}
