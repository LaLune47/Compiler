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
    private ArrayList<FuncDef> funcs;
    private HashMap<String,SingleItem> items;
    // todo 顺序有意义，符号表这里应该要改改
    private Node bindingNode;   // block，CompUnit
    private Integer startLine;
    private Integer endLine;
    
    public SymbolTable(Integer depth,SymbolTable parent) {
        if (depth == 0) {
            this.parent = null;
            this.funcs = new ArrayList<>();
        } else {
            this.parent = parent;
            this.funcs = null;
        }
        this.depth = depth;
        this.children = new ArrayList<>();
        this.items = new HashMap<>();
    }
    
    public void setLine(Integer startLine,Integer endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }
    
    public void setBindingNode(Node bindingNode) {  // 双向绑定
        this.bindingNode = bindingNode;
        if (bindingNode instanceof BranchNode) {
            ((BranchNode) bindingNode).setSymbolTable(this);
        }
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
            funcs.add(func);
        }
    }
    
    private boolean hasDefineFunc(FuncDef newFunc) {
        for(FuncDef func:funcs) {
            if (func.getIdent().equals(newFunc.getIdent())) {
                return true;
            }
        }
        return false;
    }
    
    public void addChild(SymbolTable table) {
        children.add(table);
    }
    
    public boolean isConst(String ident) {
        if (!items.containsKey(ident)) {
            return false;
        } else {
            SingleItem item = items.get(ident);
            return item.isConst();
        }
    }
    
    public Integer getValue(String ident) {
        if (!items.containsKey(ident)) {
            return null;
        } else {
            SingleItem item = items.get(ident);
            return item.getInit();
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
