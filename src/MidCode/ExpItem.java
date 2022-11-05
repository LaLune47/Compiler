package MidCode;

import component.Token;
import component.TokenTYPE;

public class ExpItem {
    private Integer localNum;
    
    private midOp op;
    private ExpItem item1;
    private ExpItem item2;
    private Boolean isNum;
    private Boolean isIdent;
    private Token intConst;   // INTCON, IntConst
    private Token ident;      // IDENFR, Ident
    
    // 两元计算,对称  x,y, 加减乘除模/比较
    // 取数组值      x:数组ident, y:数组下标
    // 返回t
    
    public ExpItem(midOp op, ExpItem item1, ExpItem item2, Integer localNum) {
        isIdent = false;
        isNum = false;
        this.item1 = item1;
        this.item2 = item2;
        this.op = op;
        this.localNum = localNum;
    }
    
    public ExpItem(Token token) {
        if (token.getType().equals(TokenTYPE.IDENFR)) {
            isIdent = true;
            isNum = false;
            this.ident = token;
        } else {
            isIdent = false;
            isNum = true;
            this.intConst = token;
        }
    }
    
    public ExpItem(String str,Integer num) {
        if (str.equals("intConst")) {
            this.isIdent = false;
            this.isNum = true;
            this.intConst = new Token(-1,TokenTYPE.INTCON,num.toString());
        } else if (str.equals("retValue") || str.equals("scan")) {
            this.isIdent = false;
            this.isNum = false;
            this.localNum = num;
        }
    }
    
    public String getStr() {
        if (this.isNum) {
            return this.intConst.getValue();
        } else if (this.isIdent) {
            return this.ident.getValue();
        } else {
            return "t&" + localNum.toString();
        }
    }
    
    public MidCode toMidCode() {
        return new MidCode(op,this.getStr(),item1.getStr(),item2.getStr());
    }
}
