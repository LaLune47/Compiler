package MidCode;

public class MidCode {
    public Operation op;       //操作符
    public String z = null;           //结果
    public String x = null;           //左操作符
    public String y = null;           //右操作符
    
    public MidCode(Operation op,String z,String x,String y) {
        this.op = op;
        this.z = z;
        this.x = x;
        this.y = y;
    }
    
    public MidCode(Operation op,String z,String x) {
        this.op = op;
        this.z = z;
        this.x = x;
    }
    
    public MidCode(Operation op,String z) {
        this.op = op;
        this.z = z;
    }
    
    public MidCode(Operation op) {
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
            case LABEL:
                if (x.equals("start")) {
                    return "<--------LABEL " + z + " " + x + "--------";
                } else {
                    return "---------LABEL " + z + " " + x + "--------->";
                }
            case PARA:
                if (x.equals("0")) {
                    return "para int " + z;
                } else if (x.equals("1")) {
                    return "para int " + z + "[]";
                } else {
                    return "para int " + z + "[][" + y + "]";
                }
            case MAIN:
                return "\nMAIN\n";
            case EXIT:
                return "\n      vg<------------------EXIT------------------>";
            default:
                return null;
        }
    }
}
