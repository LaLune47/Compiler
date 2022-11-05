package MipsCode;

import MidCode.MidCode;
import MidCode.midOp;


import java.util.ArrayList;

public class MipsGenerator {
    private ArrayList<MidCode> midCodes;
    private ArrayList<String> conStrings;
    private ArrayList<FinalCode> finalCodes;
    private static FinalCode space;
    private Integer gloIntNum;
    
    public MipsGenerator(ArrayList<MidCode> midCodes,ArrayList<String> conStrings) {
        this.midCodes = midCodes;
        this.conStrings = conStrings;
        this.finalCodes = new ArrayList<>();
        space = new FinalCode(mipsOp.space);
        gloIntNum = 0;
        genMips();
    }
    
    //private void storeValue()
    
    private void defineInteger(MidCode midCode,IntegerTable table) {
        Boolean isConst = midCode.op.equals(midOp.CONST);
        ItemInteger item = new ItemInteger(midCode.z,isConst);
        table.addItem(item);
        if (midCode.x != null) {  // 存在初值
            finalCodes.add(new FinalCode(mipsOp.li,"$t0",midCode.x));
            finalCodes.add(new FinalCode(mipsOp.sw,"$t0","gp",null,gloIntNum*4));
        }
        gloIntNum++;
    }
    
//    private String getInitVal(String constExp,IntegerTable table) {
//        if ()
//    }
    
    private void genMips() {
        // 数据区  字符串常量+ todo 数组
        finalCodes.add(new FinalCode(mipsOp.data));
        Integer strNum = 0;
        for (String conString:conStrings) {
            finalCodes.add(new FinalCode(mipsOp.conString,strNum.toString(),conString));
            strNum++;
        }
        finalCodes.add(space);
        
        // 全局变量分配 + 函数分配 + main函数
        IntegerTable globalTable = new IntegerTable(null);
        finalCodes.add(new FinalCode(mipsOp.text));
        for (MidCode midCode:midCodes) {
            if (midCode.op.equals(midOp.VAR) || midCode.op.equals(midOp.CONST)) {
                defineInteger(midCode,globalTable);
            }
        }
        
        
        
    }
    
    public void printMips() {
        int strNum = 0;
        for (FinalCode code:finalCodes) {
            switch (code.op) {
                case space:
                    System.out.println();
                    break;
                case data:
                    System.out.println(".data");
                    break;
                case text:
                    System.out.println(".text");
                    break;
                case conString:
                    System.out.println("s_" + code.z +": .asciiz \"" + code.x + "\"");
                    break;
                case li:
                    System.out.println("li " + code.z + "," + code.x);
                    break;
                case sw:
                    System.out.println("sw " + code.z + "," + code.imm + "("+ code.x + ")");
                    break;
                    
                default:
                    break;
            }
        }
    }
}
