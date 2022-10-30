package AST;

import SymbolTable.SymbolTable;
import component.NonTerminator;

import java.util.ArrayList;

public class BranchNode implements Node {
    private NonTerminator nonTerminator;
    private ArrayList<Node> children;
    private Node parent;
    private SymbolTable symbolTable;
    
    public BranchNode(NonTerminator nonTerminator) {
        this.nonTerminator = nonTerminator;
        this.children = new ArrayList<>();
        this.parent = null;
        this.symbolTable = null;
    }
    
    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    @Override
    public void addChild(Node child) {
        this.children.add(child);
    }
    
    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    public ArrayList<Node> getChildren() {
        return this.children;
    }
    
    @Override
    public Node getParent() {
        return this.parent;
    }
    
    @Override
    public void printNode() {
        for (Node child:children) {
            child.printNode();
        }
        if (!nonTerminator.equals(NonTerminator.Decl)
                && !nonTerminator.equals(NonTerminator.BType)
                && !nonTerminator.equals(NonTerminator.BlockItem)) {
            System.out.println("<" + nonTerminator + ">");
        }
    }
    
    public NonTerminator getNonTerminator() {
        return nonTerminator;
    }
}
