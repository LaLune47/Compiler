package MipsCode;

public class FinalCode {
    public mipsOp op;       //操作符
    public String z = null;           //结果
    public String x = null;           //左操作符
    public String y = null;           //右操作符
    
    public FinalCode(mipsOp op, String z, String x, String y) {
        this.op = op;
        this.z = z;
        this.x = x;
        this.y = y;
    }
    
    public FinalCode(mipsOp op, String z, String x) {
        this.op = op;
        this.z = z;
        this.x = x;
    }
    
    public FinalCode(mipsOp op, String z) {
        this.op = op;
        this.z = z;
    }
    
    public FinalCode(mipsOp op) {
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
    
    }
}
