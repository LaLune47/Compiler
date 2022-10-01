import component.Token;
import java.util.ArrayList;
import java.util.List;

/*
    词法解读器
*/
public class LexParser {
    private CompilerReader reader;
    private List<Token> tokenList;
    
    public LexParser(String inputFilePath) {
        this.reader = new CompilerReader(inputFilePath);
        this.tokenList = new ArrayList<>();
    }
    
    public List<Token> parse() {
        // todo
        return this.tokenList;
    }
}
