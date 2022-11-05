package MipsCode;

import MidCode.MidCode;
import MidCode.midOp;


import java.util.ArrayList;
import java.util.HashMap;

public class MipsGenerator {
    private ArrayList<MidCode> midCodes;
    private HashMap<String,String> conStrings;
    private ArrayList<FinalCode> finalCodes;
    private static FinalCode space;
    private Integer itemNum;
    private boolean inMain = false;
    private boolean inFunc = false;
    private IntegerTable curTable = new IntegerTable(null);
    
    public MipsGenerator(ArrayList<MidCode> midCodes,HashMap<String,String> conStrings) {
        this.midCodes = midCodes;
        this.conStrings = conStrings;
        this.finalCodes = new ArrayList<>();
        space = new FinalCode(mipsOp.space);
        itemNum = 0;
        genMips();
    }
    
    private boolean isImm(String origin) {
        char c = origin.charAt(0);
        return c <= '9' && c >= '0' || c == '-';
    }
    
    private ItemInteger findItem(String name) {
        IntegerTable table = curTable;
        while (table != null) {
            if (table.contains(name)) {
                return table.getItem(name);
            }
            table = table.getParent();
        }
        return null;
    }
    
    private Integer getOffset(String name) {
        if (findItem(name) != null) {
            return findItem(name).getOffset();
        } else {
            return 0;
        }
    }
    
    private Boolean getGlobal(String name) {
        if (findItem(name) != null) {
            return findItem(name).isGlobal();
        } else {
            return false;
        }
    }
    
    private void loadValue(String ident,String regName) {  //load, origin为数字或者已定义的ident
        if (isImm(ident)) {
            finalCodes.add(new FinalCode(mipsOp.li,regName,"","",Integer.parseInt(ident)));
        } else {
            boolean isGlobal = getGlobal(ident);
            Integer offset = getOffset(ident);
            if (isGlobal) {
                finalCodes.add(new FinalCode(mipsOp.lw, regName, "$gp", "", 4 * offset));
            } else {
                finalCodes.add(new FinalCode(mipsOp.lw, regName, "$sp", "", -4 * offset));
            }
        }
    }
    
    private void storeValue(String ident, String regName,boolean isNew) {
        if (isNew) {
            define(ident);
        }
        boolean isGlobal = getGlobal(ident);
        Integer offset = getOffset(ident);
        if (isGlobal) {
            finalCodes.add(new FinalCode(mipsOp.sw, regName, "$gp", "", 4 * offset));
        } else {
            finalCodes.add(new FinalCode(mipsOp.sw, regName, "$sp", "", -4 * offset));
        }
    }
    
    private void define(String ident) {
        if (curTable.contains(ident)) {
            return;
        }
        ItemInteger item = new ItemInteger(ident,itemNum);
        itemNum++;
        curTable.addItem(item);
        item.setTable(curTable);
    }
    
    private void genMips() {
        // 数据区  字符串常量+ todo 数组
        finalCodes.add(new FinalCode(mipsOp.data));
        for(String str:conStrings.keySet()) {
            finalCodes.add(new FinalCode(mipsOp.conString,conStrings.get(str),str));
        }
        
        finalCodes.add(space);
        
        // 全局变量分配 + 函数分配 + main函数
        
        finalCodes.add(new FinalCode(mipsOp.text));
        for (MidCode midCode:midCodes) {
            switch (midCode.op) {
                case VAR:
                    if (midCode.x != null) {
                        loadValue(midCode.x,"$t0");
                        storeValue(midCode.z,"$t0",true);
                    } else {
                        define(midCode.z);
                    }
                    break;
                case CONST:
                    loadValue(midCode.x,"$t0");
                    storeValue(midCode.z,"$t0",true);
                    break;
                case ASSIGNOP:
                    loadValue(midCode.x,"$t0");
                    storeValue(midCode.z,"$t0",true);  // 直接把所有中间代码里的中间变量都当作变量
                    break;
                case PLUSOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.add,"$t2","$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------plus"));
                    storeValue(midCode.z,"$t2",true);
                    break;
                case MINUOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sub,"$t2","$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------minu"));
                    storeValue(midCode.z,"$t2",true);
                    break;
                case MULTOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.mult,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mflo,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------mul"));
                    storeValue(midCode.z,"$t2",true);
                    break;
                case DIVOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.div,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mflo,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------div"));
                    storeValue(midCode.z,"$t2",true);
                    break;
                case MODOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.div,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mfhi,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------mod"));
                    storeValue(midCode.z,"$t2",true);
                    break;
                case SCAN:
                    finalCodes.add(new FinalCode(mipsOp.li,"$v0","","",5));
                    finalCodes.add(new FinalCode(mipsOp.syscall));
                    storeValue(midCode.z,"$v0",true);
                    break;
                case PRINTSTR:
                    finalCodes.add(new FinalCode(mipsOp.la,"$a0",conStrings.get(midCode.z)));
                    finalCodes.add(new FinalCode(mipsOp.li,"$v0","","",4));
                    finalCodes.add(new FinalCode(mipsOp.syscall));
                    break;
                case PRINTEXP:
                    loadValue(midCode.z,"$a0");
                    finalCodes.add(new FinalCode(mipsOp.li,"$v0","","",1));
                    finalCodes.add(new FinalCode(mipsOp.syscall));
                    break;
                default:
                    break;
            }
        }
    }
    
    public void printMips() {
        int strNum = 0;
        for (FinalCode code:finalCodes) {
            switch (code.op) {
                case debug:
                    System.out.println(code.z);
                    break;
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
                    System.out.println(code.z +": .asciiz \"" + code.x + "\"");
                    break;
                case li:
                    System.out.println("li " + code.z + "," + code.imm);
                    break;
                case sw:
                    System.out.println("sw " + code.z + "," + code.imm + "("+ code.x + ")");
                    break;
                case lw:
                    System.out.println("lw " + code.z + "," + code.imm + "("+ code.x + ")");
                    break;
                case add:
                    System.out.println("add " + code.z + "," + code.x + "," + code.y);
                    break;
                case sub:
                    System.out.println("sub " + code.z + "," + code.x + "," + code.y);
                    break;
                case mult:
                    System.out.println("mult " + code.z + "," + code.x);
                    break;
                case div:
                    System.out.println("div " + code.z + "," + code.x);
                    break;
                case mflo:
                    System.out.println("mflo " + code.z);
                    break;
                case mfhi:
                    System.out.println("mfhi " + code.z);
                    break;
                case syscall:
                    System.out.println("syscall");
                    break;
                case la:
                    System.out.println("la " + code.z + "," + code.x);
                    break;
                default:
                    break;
            }
        }
    }
}
