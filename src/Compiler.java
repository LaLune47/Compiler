import component.Token;

import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        LexParser lexParser = new LexParser(Config.inputFilePath);
        List<Token> tokenList = lexParser.parse();
        for (Token token: tokenList) {
            System.out.println(token.toString());
        }
    }
}
