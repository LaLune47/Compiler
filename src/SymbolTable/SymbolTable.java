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
    
    public void addChild(SymbolTable table) {
        children.add(table);
    }
    
//    public boolean isConst(String ident) {
//        if (!items.containsKey(ident)) {
//            return false;
//        } else {
//            SingleItem item = items.get(ident);
//            return item.isConst();
//        }
//    }
//
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
