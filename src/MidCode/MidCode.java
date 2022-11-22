package MidCode;

public class MidCode {
    public midOp op;       //操作符
    public String z = null;           //结果
    public String x = null;           //左操作符
    public String y = null;           //右操作符
    
    public MidCode(midOp op, String z, String x, String y) {
        this.op = op;
        this.z = z;
        this.x = x;
        this.y = y;
    }
    
    public MidCode(midOp op, String z, String x) {
        this.op = op;
        this.z = z;
        this.x = x;
    }
    
    public MidCode(midOp op, String z) {
        this.op = op;
        this.z = z;
    }
    
    public MidCode(midOp op) {
        this.op = op;
    }
    
    public void setZ(String z) {
        this.z = z;
    }
    
    public void setX(String x) {
        this.x = x;
    }
    
    public void setY(String y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        switch (op) {
            case VAR:
                if (x == null) {
                    return "var int " + z;
                } else {
                    return "var int " + z + " = " + x;
                }
            case CONST:
                if (x == null) {
                    return "const int " + z;
                } else {
                    return "const int " + z + " = " + x;
                }
            case FUNC:
                return x + " " + z + "( )";   // z 为ident,x为返回种类
            case FUNC_BLOCK:
                if (x.equals("start")) {
                    return "<--------FUNC_BLOCK " + z + " " + x + "--------";
                } else {
                    return "---------FUNC_BLOCK " + z + " " + x + "--------->";
                }
            case PARA:
                if (x.equals("0")) {   // todo 形参数组
                    return "para int " + z;
                } else if (x.equals("1")) {
                    return "para int " + z + "[]";
                } else {
                    return "para int " + z + "[][" + y + "]";
                }
            case MAIN:
                return "\nMAIN\n";
            case EXIT:
                return "\n<------------------EXIT------------------>";
            case PLUSOP:
                return z + " = " + x + " + " + y;
            case MINUOP:
                return z + " = " + x + " - " + y;
            case MULTOP:
                return z + " = " + x + " * " + y;
            case DIVOP:
                return z + " = " + x + " / " + y;
            case MODOP:
                return z + " = " + x + " % " + y;
            case PUSH:
                return "push " + z;  // todo 实参数组
            case RETVALUE:
                return "retValue " + z;
            case CALL:
                return "call " + z;
            case RET:              // 标记funcBlock结束
                if (z != null) {
                    return "RET  " + z;
                }
                else {
                    return "RET null";
                }
            case SCAN:
                return "scan " + z;
            case ASSIGNOP:
                return z + " = " + x;
            case AssignARRAY:
                return z + "[" + x + "]" + " = " + y;
                
            case GetARRAY:
                return z + " = " + x + "[" + y + "]";
                
            case STRCON:
                return "const str " + z;
            case PRINTSTR:   //写字符串
                return "print \"" + z + "\"";
            case PRINTEXP:   //写变量
                return "print " + z;
            case ARRAY:
                if (y == null) {
                    return "array " + z + "[" + x + "]";
                } else {
                    return "array " + z + "[" + x + "]" + "[" + y + "]";
                }
            default:
                return null;
        }
    }
    
    public boolean isRet() {
        return op.equals(midOp.RET);
    }
}
