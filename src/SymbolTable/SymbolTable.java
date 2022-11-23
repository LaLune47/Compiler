package SymbolTable;

import AST.BranchNode;
import AST.MyError;
import AST.Node;
import component.ErrorTYPE;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private Integer depth;
    private SymbolTable parent;
    private ArrayList<SymbolTable> children;
    private HashMap<String,FuncDef> funcs;
    private HashMap<String,SingleItem> items;
    
    public SymbolTable(Integer depth,SymbolTable parent) {
        if (depth == 0) {
            this.parent = null;
            this.funcs = new HashMap<>();
        } else {
            this.parent = parent;
            this.funcs = null;
        }
        this.depth = depth;
        this.children = new ArrayList<>();
        this.items = new HashMap<>();
    }
    
    public SymbolTable initFuncTable(Integer depth,SymbolTable parent,ArrayList<SingleItem> items,ArrayList<MyError> errorList) {
        SymbolTable symbolTable = new SymbolTable(depth,parent);
        symbolTable.addAllItem(items,errorList);
        return symbolTable;
    }
    
    public void addItem(SingleItem item, ArrayList<MyError> errorList) {
        if (hasDefineItem(item)) {
            MyError error = new MyError(ErrorTYPE.Redefine_b);
            error.setLine(item.getDefineLine());
            errorList.add(error);
        } else {
            items.put(item.getIdent(),item);
        }
    }
    
    private boolean hasDefineItem(SingleItem newItem) {
        return items.containsKey(newItem.getIdent());
    }
    
    public void addAllItem(ArrayList<SingleItem> items,ArrayList<MyError> errorList) {
        if (items == null) {
            return;
        }
        for(SingleItem item:items) {
            addItem(item,errorList);
        }
    }
    
    public void addFunc(FuncDef func,ArrayList<MyError> errorList) {
        if (hasDefineFunc(func)) {
            MyError error = new MyError(ErrorTYPE.Redefine_b);
            error.setLine(func.getDefineLine());
            errorList.add(error);
        } else {
            funcs.put(func.getIdent(),func);
        }
        
        if (depth == 0 && items.containsKey(func.getIdent())) {
            MyError error = new MyError(ErrorTYPE.Redefine_b);
            error.setLine(func.getDefineLine());
            errorList.add(error);
        }
    }
    
    private boolean hasDefineFunc(FuncDef newFunc) {
        return funcs.containsKey(newFunc.getIdent());
    }
    
    public HashMap<String, FuncDef> getFuncs() {
        return funcs;
    }
    
    public void addChild(SymbolTable table) {
        children.add(table);
    }

    public Integer getValue(String ident) {
        if (items.containsKey(ident)) {
            SingleItem item = items.get(ident);
            return item.getInit();
        }
        if (this.parent == null) {
            System.out.println("定义错误");
            return null;
        } else {
            return this.parent.getValue(ident);
        }
    }
    
    public SingleItem findItem(String ident) {
        if (items.containsKey(ident)) {
            return items.get(ident);
        }
        if (this.parent == null) {
            return null;
        } else {
            return this.parent.findItem(ident);
        }
    }
    
    public Integer getArrayValue1(String ident,Integer i) {
        SingleItem item = findItem(ident);
        if (item.getDimension() == 1) {
            return item.getArrayValue1(i);
        } else {
            return 0;
        }
    }
    
    public Integer getArrayValue2(String ident,Integer i,Integer j) {
        SingleItem item = findItem(ident);
        if (item.getDimension() == 2) {
            return item.getArrayValue2(i,j);
        } else {
            return 0;
        }
    }
    
    public Integer findItem_space2(String ident) {
        SingleItem item = findItem(ident);
        if (item != null && item.getDimension() == 2) {
            return item.getSpace2();
        } else {
            return 0;
        }
    }
    
    public Integer findItem_dimension(String ident) {
        SingleItem item = findItem(ident);
        if (item != null) {
            return item.getDimension();
        } else {
            return 1111;   //可能会引起错误处理的一些问题，但是按道理题目限制了不会一个东西上出现两个错误，偷点小懒
        }
    }
    
    public SymbolTable findRootTable() {
        SymbolTable table = this;
        while (table.getDepth() != 0) {
            table = table.getParent();
        }
        return table;
    }
    
    public FuncDef findFunc(String ident) {
        SymbolTable rootTable = this.findRootTable();
        HashMap<String,FuncDef> funcDefHashMap = rootTable.getFuncs();
        
        if (funcDefHashMap.containsKey(ident)) {
            return funcDefHashMap.get(ident);
        } else {
            return null;
        }
    }
    
    public Integer findFunc_returnType(String ident) {
        FuncDef funcDef = findFunc(ident);
        if (funcDef == null) {
            return 1111;
        }
        if(funcDef.judgeInt()) {  // int 0维度
            return 0;
        } else {
            return -1;
        }
    }
    
    public Integer getDepth() {
        return depth;
    }
    
    public SymbolTable getParent() {
        return parent;
    }
    
    public void print() {
        String self = "SymbolTable{" +
                    "depth=" + depth +
                    ", funcs=" + funcs +
                    ", items=" + items +
                    '}';
        System.out.println(self);
        for (SymbolTable child:children) {
            child.print();
        }
    }
}
