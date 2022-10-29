package AST;

import component.NonTerminator;

import java.util.ArrayList;

public class BranchNode implements Node {
    private NonTerminator nonTerminator;
    private ArrayList<Node> children;
    private Node parent;
    private ArrayList<Error> errors; //TODO 错误排序，写一个compareTo就行
    
    public BranchNode(NonTerminator nonTerminator) {
        this.nonTerminator = nonTerminator;
        this.children = new ArrayList<>();
        this.parent = null;
        this.errors = new ArrayList<>();
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
    
    @Override
    public void printError() {
        for (Node child:children) {
            child.printError();
        }
        for (Error error:errors) {
            System.out.println(error.toString());
        }
    }
    
    @Override
    public void addError(Error error) {
        errors.add(error);
    }
}
