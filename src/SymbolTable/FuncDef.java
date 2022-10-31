package SymbolTable;
import SymbolTable.SymbolTable;

import component.NonTerminator;

import java.util.ArrayList;

public class FuncDef {
    private FuncType type;
    private ArrayList<SingleItem> parameters;
    private String ident;
    private SymbolTable symbolTable;
    private Integer defineLine;
    
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
    
    public String getIdent() {
        return ident;
    }
    
    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    
    public void setDefineLine(Integer defineLine) {
        this.defineLine = defineLine;
    }
    
    public Integer getDefineLine() {
        return defineLine;
    }
    
    public FuncType getType() {
        return type;
    }
}
