package MipsCode;

import MidCode.MidCode;
import MidCode.midOp;


import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class MipsGenerator {
    private ArrayList<MidCode> midCodes;
    private HashMap<String,String> conStrings;
    private ArrayList<FinalCode> finalCodes;
    private static FinalCode space;
    private Integer itemNum;
    private boolean has_j_main = false;
    private boolean inMain = false;
    private IntegerTable curTable = new IntegerTable(null);
    private ArrayList<MidCode> paraRs;
    private String curFunc;
    private HashMap<String,Integer> funcLength = new HashMap<>();
    private ArrayList<String> blockStack = new ArrayList<>();
    private String funcBlockStr = null;
    
    public MipsGenerator(ArrayList<MidCode> midCodes,HashMap<String,String> conStrings) {
        this.midCodes = midCodes;
        this.conStrings = conStrings;
        this.finalCodes = new ArrayList<>();
        space = new FinalCode(mipsOp.space);
        itemNum = 0;
        paraRs = new ArrayList<>();
        //setMainLength();
        genMips();
        
        finalCodes = regOptimize(finalCodes);
    }
    
    private void setMainLength() {
        int i = 0;
        for (MidCode midCode:midCodes) {
            i++;
            if (midCode.op.equals(midOp.MAIN)) {
                break;
            }
        }
        Integer length = midCodes.size() - i + 5;
        funcLength.put("main",length);
    }
    
    private boolean isImm(String origin) {
        if (origin == null) {
            return false;
        }
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
    
    private ItemArray findItemArray(String name) {
        IntegerTable table = curTable;
        while (table != null) {
            if (table.containsArray(name)) {
                return table.getItemArray(name);
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
    
    private Integer getArrayOffset(String name) {
        if (findItemArray(name) != null) {
            return findItemArray(name).getOffset();
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
    
    private Boolean getGlobalArray(String name) {
        if (findItemArray(name) != null) {
            return findItemArray(name).isGlobal();
        } else {
            return false;
        }
    }
    
    private Boolean isPointer(String name) {
        if (findItem(name) != null) {
            return findItem(name).getPointer();
        } else {
            return false;
        }
    }
    
    private void loadValue(String ident,String regName) {  //load, origin为数字或者已定义的ident
        if (isImm(ident)) {
            finalCodes.add(new FinalCode(mipsOp.li,regName,"","", parseInt(ident)));
        } else {
            boolean isGlobal = getGlobal(ident);
            Integer offset = getOffset(ident);
            if (isGlobal) {
                finalCodes.add(new FinalCode(mipsOp.lw, regName, "$gp", "", 4 * offset));
            } else {
                finalCodes.add(new FinalCode(mipsOp.lw, regName, "$fp", "", -4 * offset));
            }
        }
    }
    
    private void loadAddress(String ident,String regName, String regIndexOffset) {
        Boolean isPointer = isPointer(ident);
        if (isPointer) {
            loadValue(ident,regName);
            finalCodes.add(new FinalCode(mipsOp.add, regName, regName, regIndexOffset));
            return;
        }
        boolean isGlobal = getGlobalArray(ident);
        Integer offset = getArrayOffset(ident);
        if (isGlobal) {
            finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, regIndexOffset, "$gp"));
            finalCodes.add(new FinalCode(mipsOp.addi,regName,regIndexOffset,"",4 * offset));
        } else {
            finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, "$fp",regIndexOffset));
            finalCodes.add(new FinalCode(mipsOp.addi,regName,regIndexOffset,"",-4 * offset));
        }
    }
    
    private void loadArrayValue(String ident,String regName, String regIndexOffset) {
        Boolean isPointer = isPointer(ident);
        if (isPointer) {
            loadValue(ident,"$t2");  // 获得地址
            finalCodes.add(new FinalCode(mipsOp.add, "$t2", regIndexOffset, "$t2"));
            finalCodes.add(new FinalCode(mipsOp.lw, regName, "$t2", "", 0));
        } else {
            boolean isGlobal = getGlobalArray(ident);
            Integer offset = getArrayOffset(ident);
            if (isGlobal) {
                finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, regIndexOffset, "$gp"));
                finalCodes.add(new FinalCode(mipsOp.lw, regName, regIndexOffset, "", 4 * offset));  //因为数组编号从0开始
            } else {
                finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, "$fp",regIndexOffset));
                finalCodes.add(new FinalCode(mipsOp.lw, regName, regIndexOffset, "", -4 * offset));
            }
        }
    }

    private void storeValue(String ident, String regName,boolean isNew) {
        if (isNew) {
            define(ident,false);
        }
        boolean isGlobal = getGlobal(ident);
        Integer offset = getOffset(ident);
        if (isGlobal) {
            finalCodes.add(new FinalCode(mipsOp.sw, regName, "$gp", "", 4 * offset));
        } else {
            finalCodes.add(new FinalCode(mipsOp.sw, regName, "$fp", "", -4 * offset));
        }
    }
    
    private void storeArrayValue(String ident, String regName, String regIndexOffset) {
        Boolean isPointer = isPointer(ident);
        if (isPointer) {
            loadValue(ident,"$t2");  // 获得地址
            finalCodes.add(new FinalCode(mipsOp.add, "$t2", regIndexOffset, "$t2"));
            finalCodes.add(new FinalCode(mipsOp.sw, regName, "$t2", "", 0));
        } else {
            boolean isGlobal = getGlobalArray(ident);
            Integer offset = getArrayOffset(ident);
            if (isGlobal) {
                finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, regIndexOffset, "$gp"));
                finalCodes.add(new FinalCode(mipsOp.sw, regName, regIndexOffset, "", 4 * offset));  //因为数组编号从0开始
            } else {
                finalCodes.add(new FinalCode(mipsOp.add, regIndexOffset, "$fp",regIndexOffset));
                finalCodes.add(new FinalCode(mipsOp.sw, regName, regIndexOffset, "", -4 * offset));
            }
        }
    }
    
    private void define(String ident,Boolean isPointer) {
        if (curTable.contains(ident)) {
            return;
        }
        ItemInteger item = new ItemInteger(ident,itemNum,isPointer);
        itemNum++;
        curTable.addItem(item);
        item.setTable(curTable);
    }
    
    private void defineArray(String ident,Integer arrayLen) {
        // if (curTable.contains(ident)) { return; }  数据约束，不会出现重复定义
        ItemArray item = null;
        if (curTable.getParent() == null) {
            item = new ItemArray(ident,itemNum,arrayLen);
            itemNum += arrayLen;
        } else {
            itemNum += arrayLen - 1;
            item = new ItemArray(ident,itemNum,arrayLen);
            itemNum++;
        }
        curTable.addItemArray(item);
        item.setTable(curTable);
    }
    
    private boolean isTemp(String str) {
        if (str.length() >=2 && str.charAt(0) == 't' && str.charAt(1) == '&') {
            return true;
        } else {
            return false;
        }
    }
    
    private void genMips() {
        // 数据区  字符串常量
        finalCodes.add(new FinalCode(mipsOp.data));
        for(String str:conStrings.keySet()) {
            finalCodes.add(new FinalCode(mipsOp.conString,conStrings.get(str),str));
        }
        
        finalCodes.add(space);
        
        Integer itemInFunc = 0;
        // 为了预防递归问题，必须先行计算函数长度
        for (Integer i = 0;i < midCodes.size();i++) {
            MidCode midCode = midCodes.get(i);
        
            switch (midCode.op) {
                case BLOCK:
                    if (midCode.x.equals("start")) {
                        blockStack.add(midCode.z);
                    } else {
                        int index = blockStack.size() - 1;
                        String str = blockStack.remove(index);
                        if (str.equals(funcBlockStr)) {
                            funcLength.put(curFunc,itemInFunc + 8);
                            curFunc = null;
                            funcBlockStr = null;
                            itemInFunc = 0;
                        }
                    }
                    break;
                case FUNC:
                case MAIN:
                    curFunc = midCode.z;
                    itemInFunc = 0;
                    if (i >= 1 && midCodes.get(i - 1) != null) {
                        funcBlockStr = midCodes.get(i - 1).z;
                    }
                    break;
                case PARA:   // 形参为数组时，传入指针，只占一个位置
                case VAR:
                case CONST:
                    itemInFunc++;
                    break;
                case ASSIGNOP:
                case PLUSOP:
                case MINUOP:
                case MULTOP:
                case DIVOP:
                case MODOP:
                case SCAN:
                case RETVALUE:
                case LSSOP:
                case LEQOP:
                case GREOP:
                case GEQOP:
                case EQLOP:
                case NEQOP:
                case GetARRAY:
                    if (isTemp(midCode.z)) {
                        itemInFunc++;
                    }
                    break;
                case ARRAY:
                    Integer arraySpace = parseInt(midCode.x);
                    if (midCode.y != null) {
                        arraySpace = arraySpace * parseInt(midCode.y);
                    }
                    itemInFunc += arraySpace;
                    break;
                default:
                    break;
            }
        }
        
        // 全局变量分配 + 函数分配 + main函数
        finalCodes.add(new FinalCode(mipsOp.text));
        for (Integer i = 0;i < midCodes.size();i++) {
            MidCode midCode = midCodes.get(i);
            
            switch (midCode.op) {
                case VAR:
                    if (midCode.x != null) {
                        loadValue(midCode.x,"$t0");
                        storeValue(midCode.z,"$t0",true);
                    } else {
                        define(midCode.z,false);
                    }
                    break;
                case CONST:
                    loadValue(midCode.x,"$t0");
                    storeValue(midCode.z,"$t0",true);
                    break;
                case ASSIGNOP:
                    loadValue(midCode.x,"$t0");
                    storeValue(midCode.z,"$t0",isTemp(midCode.z));
                    break;
                case PLUSOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.add,"$t2","$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------plus"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case MINUOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sub,"$t2","$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------minu"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case MULTOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.mult,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mflo,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------mul"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case DIVOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.div,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mflo,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------div"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case MODOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.div,"$t0","$t1"));
                    finalCodes.add(new FinalCode(mipsOp.mfhi,"$t2"));
                    finalCodes.add(new FinalCode(mipsOp.debug,"------mod"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case SCAN:
                    finalCodes.add(new FinalCode(mipsOp.li,"$v0","","",5));
                    finalCodes.add(new FinalCode(mipsOp.syscall));
                    storeValue(midCode.z,"$v0",isTemp(midCode.z));
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
                case BLOCK:
                    if (midCode.x.equals("start")) {
                        curTable = new IntegerTable(curTable);
                        //blockStack.add(midCode.z);
                    } else {
                        curTable = curTable.getParent();
                        //int index = blockStack.size() - 1;
                        //String str = blockStack.remove(index);
                        //if (str.equals(funcBlockStr)) {
                        //    funcLength.put(curFunc,itemNum + 5);  // 防止出意外？多留一点
                        //    curFunc = null;
                        //    funcBlockStr = null;
                        //}
                        finalCodes.add(new FinalCode(mipsOp.space));
                    }
                    break;
                case FUNC:
                    if (!has_j_main) {
                        finalCodes.add(new FinalCode(mipsOp.j,"main"));
                        finalCodes.add(new FinalCode(mipsOp.space));
                        has_j_main = true;
                    }
                    finalCodes.add(new FinalCode(mipsOp.label,midCode.z));
                    curFunc = midCode.z;
                    itemNum = 0;
                    //if (i >= 1 && midCodes.get(i - 1) != null) {
                    //    funcBlockStr = midCodes.get(i - 1).z;
                    //}
                    break;
                case PARA: // 跟var类似   当为数组时,isPointer = true
                    if (midCode.x.equals("0")) {
                        define(midCode.z,false);
                    } else {
                        define(midCode.z,true);
                    }
                    break;
                case RET:
                    if (inMain) {
                       finalCodes.add(new FinalCode(mipsOp.li,"$v0","","",10));
                       finalCodes.add(new FinalCode(mipsOp.syscall));
                    } else {
                        if (midCode.z != null) {
                            loadValue(midCode.z,"$v0");
                        }
                        finalCodes.add(new FinalCode(mipsOp.jr,"$ra"));
                    }
                    break;
                case PUSH:
                    paraRs.add(midCode);
                    break;
                case CALL:
                    // 传入形参
                    int j = 0;
                    for (MidCode paraR:paraRs) {
                        if (paraR.x == null) {
                            loadValue(paraR.z,"$t0");  // 传入为值
                        } else {
                            if (paraR.x != null && paraR.y != null && !paraR.y.equals("array")) {  // 2传1的情况
                                loadValue(paraR.x, "$t0");  // 第几行
                                finalCodes.add(new FinalCode(mipsOp.li, "$t1", "", "", parseInt(paraR.y) * 4)); // space2,顺便乘个4
                                finalCodes.add(new FinalCode(mipsOp.mult, "$t1", "$t0", ""));
                                finalCodes.add(new FinalCode(mipsOp.mflo, "$t1")); // 得到最后偏移量
                            } else {
                                finalCodes.add(new FinalCode(mipsOp.li, "$t1", "", "", 0));
                            }
                            loadAddress(paraR.z,"$t0","$t1");
                        }
                        finalCodes.add(new FinalCode(mipsOp.sw, "$t0", "$sp", "", -4 * j));
                        j++;
                    }
                    for (int k = 0;k < j;k++) {
                        int lengthPara = paraRs.size();
                        paraRs.remove(lengthPara - 1);
                    }
                    
                    int len = 0;
                    if (funcLength.containsKey(midCode.z)) {
                        len = funcLength.get(midCode.z);
                    }
                    finalCodes.add(new FinalCode(mipsOp.addi, "$sp", "$sp", "",-4 * len));
                    finalCodes.add(new FinalCode(mipsOp.sw, "$fp", "$sp", "",4)); // 从0开始的话担心纠缠不清
                    finalCodes.add(new FinalCode(mipsOp.sw, "$ra", "$sp", "",8));
                    finalCodes.add(new FinalCode(mipsOp.addi, "$fp", "$sp", "",4 * len));
                        // fp到原始sp的位置，上一个运行栈的末尾此处运行栈的开始，与形参也可以衔接上
                    finalCodes.add(new FinalCode(mipsOp.jal,midCode.z));
                    finalCodes.add(new FinalCode(mipsOp.nop));
                    finalCodes.add(new FinalCode(mipsOp.lw, "$fp", "$sp", "",4));
                    finalCodes.add(new FinalCode(mipsOp.lw, "$ra", "$sp", "",8));
                    finalCodes.add(new FinalCode(mipsOp.addi, "$sp", "$sp", "",4 * len));
                    
                    break;
                case RETVALUE:
                    storeValue(midCode.z,"$v0",isTemp(midCode.z));
                    break;
                    
                case MAIN:
                    if (!has_j_main) {
                        finalCodes.add(new FinalCode(mipsOp.j,"main"));
                        finalCodes.add(new FinalCode(mipsOp.space));
                        has_j_main = true;
                    }
                    inMain = true;
                    finalCodes.add(new FinalCode(mipsOp.label,"main"));
                    itemNum = 0;
                    Integer lenMain = funcLength.get("main");
                    finalCodes.add(new FinalCode(mipsOp.move,"$fp", "$sp"));
                    finalCodes.add(new FinalCode(mipsOp.addi,"$sp","$sp","",-4*lenMain));
                    break;
                    
                case BEQZ:
                    loadValue(midCode.z, "$t0");
                    finalCodes.add(new FinalCode(mipsOp.beqz,"$t0","label__" + midCode.x));
                    break;
                case GOTO:
                    finalCodes.add(new FinalCode(mipsOp.j,"label__" + midCode.z));
                    break;
                case LABEL:
                    finalCodes.add(new FinalCode(mipsOp.label,"label__" + midCode.z));
                    break;
                    
                case LSSOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.slt,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case LEQOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sle,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case GREOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sgt,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case GEQOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sge,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case EQLOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.seq,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                case NEQOP:
                    loadValue(midCode.x, "$t0");
                    loadValue(midCode.y, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sne,"$t2","$t0","$t1"));
                    storeValue(midCode.z,"$t2",isTemp(midCode.z));
                    break;
                    
                case ARRAY:
                    Integer arraySpace = parseInt(midCode.x);
                    if (midCode.y != null) {
                        arraySpace = arraySpace * parseInt(midCode.y);
                    }
                    defineArray(midCode.z,arraySpace);
                    break;
                case AssignARRAY:   // z + "[" + x + "]" + " = " + y;
                    loadValue(midCode.y,"$t0");
                    loadValue(midCode.x, "$t1");
                    finalCodes.add(new FinalCode(mipsOp.sll, "$t1", "$t1", "", 2));
                    storeArrayValue(midCode.z,"$t0","$t1");
                    break;
                case GetARRAY:     //z + " = " + x + "[" + y + "]";
                    loadValue(midCode.y, "$t0");
                    finalCodes.add(new FinalCode(mipsOp.sll, "$t0", "$t0", "", 2));
                    loadArrayValue(midCode.x,"$t1","$t0");
                    storeValue(midCode.z,"$t1",isTemp(midCode.z));
                    break;
                    
                case EXIT:
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
                    //System.out.println(code.z);
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
                case j:
                    System.out.println("j " + code.z);
                    break;
                case label:
                    System.out.println("\n" + code.z+":");
                    break;
                case jr:
                    System.out.println("jr " + code.z);
                    break;
                case addi:
                    System.out.println("addi " + code.z + "," + code.x + "," + code.imm);
                    break;
                case move:
                    System.out.println("move " + code.z + "," + code.x);
                    break;
                case jal:
                    System.out.println("jal " + code.z);
                    break;
                case nop:
                    System.out.println("nop");
                    break;
                    
                case beqz:
                    System.out.println("beqz " + code.z + "," + code.x);
                    break;
                case slt:
                    System.out.println("slt " + code.z + "," + code.x + "," + code.y);
                    break;
                case sle:
                    System.out.println("sle " + code.z + "," + code.x + "," + code.y);
                    break;
                case sgt:
                    System.out.println("sgt " + code.z + "," + code.x + "," + code.y);
                    break;
                case sge:
                    System.out.println("sge " + code.z + "," + code.x + "," + code.y);
                    break;
                case seq:
                    System.out.println("seq " + code.z + "," + code.x + "," + code.y);
                    break;
                case sne:
                    System.out.println("sne " + code.z + "," + code.x + "," + code.y);
                    break;
                case sll:
                    System.out.println("sll " + code.z + "," + code.x + "," + code.imm);
                    break;
                    
                    
                default:
                    break;
            }
        }
    }
    
    private ArrayList<FinalCode> regOptimize(ArrayList<FinalCode> finalCodes) {
        Integer len = finalCodes.size();
        Integer i = 0;
        for (i = 1;i < len;i++) {
            FinalCode pre = finalCodes.get(i - 1);
            FinalCode cur = finalCodes.get(i);
            if (pre.op.equals(mipsOp.sw) && cur.op.equals(mipsOp.lw)
                    && pre.x.equals(cur.x) && pre.z.equals(cur.z) && pre.imm.equals(cur.imm)) {
                finalCodes.remove(i);
                i--;
                len--;
            }
        }
        return finalCodes;
    }
}
