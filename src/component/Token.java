package component;

public class Token {
    private final Integer line;
    private final TokenTYPE type;
    private final String value;
    
    public Token(Integer line,TokenTYPE type,String value) {
        this.line = line;
        this.type = type;
        this.value = value;
    }
    
    public Integer getLine() {
        return line;
    }
    
    public String getValue() {
        return value;
    }
    
    public TokenTYPE getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return type + " " + value;
    }
}
