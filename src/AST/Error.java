package AST;
import component.ErrorTYPE;

public class Error {
    private Integer line;
    private ErrorTYPE type;
    private String additionalInfo;
    
    public Error(ErrorTYPE errorTYPE) {
        this.line = 0;
        this.type = errorTYPE;
        this.additionalInfo = null;
    }
    
    public void setLine(Integer line) {
        this.line = line;
    }
    
    @Override
    public String toString() {
        return "line=" + line + ", type=" + type;
    }
}
