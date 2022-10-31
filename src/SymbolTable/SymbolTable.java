package SymbolTable;

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
    
    public void addItem(SingleItem item) {
        items.put(item.getIdent(),item);
    }
    
    public void addAllItem(ArrayList<SingleItem> items) {
        if (items == null) {
            return;
        }
        for(SingleItem item:items) {
            addItem(item);
        }
    }
    
    public void addFunc(FuncDef func) {
        funcs.put(func.getIdent(),func);
    }
    
    public void addChild(SymbolTable table) {
        children.add(table);
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
