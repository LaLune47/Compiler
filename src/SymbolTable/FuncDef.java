package SymbolTable;
import SymbolTable.SymbolTable;

import component.NonTerminator;

import java.util.ArrayList;

public class FuncDef {
    private Boolean isInt;
    private ArrayList<SingleItem> parameters;
    private String ident;
    private SymbolTable symbolTable;
    private Integer defineLine;
    
    public FuncDef(Boolean isInt,ArrayList<SingleItem> parameters,String ident) {
        this.isInt = isInt;
        this.parameters = parameters;
        this.ident = ident;
    }
    
    public FuncDef() {
        this.isInt = null;
        this.parameters = new ArrayList<>();
        this.ident = null;
    }
    
    public void setInt(Boolean anInt) {
        isInt = anInt;
    }
    
    public void setIdent(String ident) {
        this.ident = ident;
    }
    
    public void addAllParams(ArrayList<SingleItem> parameters) {
        this.parameters.addAll(parameters);
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
    
    public Boolean judgeInt() {
        return isInt;
    }
    
    public Integer getParaNum() {
        return parameters.size();
    }
    
    public ArrayList<SingleItem> getParameters() {
        return parameters;
    }
}
