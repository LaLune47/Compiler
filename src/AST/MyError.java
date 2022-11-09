package AST;
import component.ErrorTYPE;

public class MyError {
    private Integer line;
    private ErrorTYPE type;
    private String additionalInfo;
    
    public MyError(ErrorTYPE errorTYPE) {
        this.line = 0;
        this.type = errorTYPE;
        this.additionalInfo = null;
    }
    
    public void setLine(Integer line) {
        this.line = line;
    }
    
    public Integer getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        //return "line=" + line + ", type=" + type;
        return line + " " + typeChange(type);
    }
    
    public String typeChange(ErrorTYPE errorTYPE) {
        switch (errorTYPE) {
            case WrongString_a:
                return "a";
            case Redefine_b:
                return "b";
            case Undefine_c:
                return "c";
            case FuncParamNum_d:
                return "d";
            case FuncParamType_e:
                return "e";
            case UnmatchReturn_f:
                return "f";
            case MissReturn_g:
                return "g";
            case ChangeConst_h:
                return "h";
            case MissSEMICN_i:
                return "i";
            case MissRPARENT_j:
                return "j";
            case MissRBRACK_k:
                return "k";
            case PrintMismatch_l:
                return "l";
            case LoopLogout_m:
                return "m";
            default:
                return null;
        }
    }
}
