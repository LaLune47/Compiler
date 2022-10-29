package SymbolTable;

import component.NonTerminator;

import java.util.ArrayList;

public class FuncDef {
    private FuncType type;
    private ArrayList<SingleItem> parameters;
    
    public FuncDef(FuncType type,ArrayList<SingleItem> parameters) {
        this.type = type;
        this.parameters = parameters;
    }
}
