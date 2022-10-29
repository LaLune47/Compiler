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
}
