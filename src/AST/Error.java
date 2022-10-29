package AST;
import component.ErrorTYPE;

public class Error {
    private Integer line;
    private ErrorTYPE type;
    private String additionalInfo;
    
    public Error() {
        this.line = 0;
        this.type = null;
        this.additionalInfo = null;
    }
}
