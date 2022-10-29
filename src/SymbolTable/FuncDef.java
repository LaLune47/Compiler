package SymbolTable;

import component.NonTerminator;

import java.util.ArrayList;

public class FuncDef {
    private FuncType type;
    private ArrayList<SingleItem> parameters;
    private String ident;
    
    public FuncDef(FuncType type,ArrayList<SingleItem> parameters,String ident) {
        this.type = type;
        this.parameters = parameters;
        this.ident = ident;
    }
}
