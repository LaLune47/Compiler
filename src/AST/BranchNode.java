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
    
    @Override
    public LeafNode getFirstLeafNode() {
        if (this == null) {
            return null;
        }
        Node node = this;
        while (node instanceof BranchNode) {
            node = ((BranchNode)node).unwrap();
        }
        return (LeafNode) node;
    }
    
    @Override
    public LeafNode getLastLeafNode() {
        Node node = this;
        while (node instanceof BranchNode) {
            if (!node.getChildren().isEmpty()) {
                int size = node.getChildren().size();
                node = node.getChildren().get(size - 1);
            }
        }
        return (LeafNode) node;
    }
    
    @Override
    public Integer getLine() {
        return this.getFirstLeafNode().getLine();
    }
    
    @Override
    public Node unwrap() { // 去掉一层
        Node tempNode = this;
        if (tempNode.getChildren() != null &&!tempNode.getChildren().isEmpty()) {
            tempNode = this.getChildren().get(0);
        }
        return tempNode;
    }
    
    @Override
    public Node getFirstChild() {
        return unwrap();
    }
    
    @Override
    public Node childIterator(Integer index) {
        Node parent = this;
        if (this == null) {
            return null;
        }
        ArrayList<Node> children = ((BranchNode) parent).getChildren();
        if (children != null && children.size() > index) {
            return children.get(index);
        } else {
            System.out.println(((BranchNode) parent).getNonTerminator().toString() + "--children error");
            return null;
        }
    }
}
