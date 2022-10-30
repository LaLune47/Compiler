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
    
    public FuncDef() {
        this.type = null;
        this.parameters = new ArrayList<>();
        this.ident = null;
    }
    
    public void setType(FuncType type) {
        this.type = type;
    }
    
    public void setIdent(String ident) {
        this.ident = ident;
    }
    
    public void addAllParams(ArrayList<SingleItem> parameters) {
        parameters.addAll(parameters);
    }
}
