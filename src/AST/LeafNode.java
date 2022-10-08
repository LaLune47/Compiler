package AST;

import component.Token;

import java.util.ArrayList;

public class LeafNode implements Node {
    private Token token;
    private Node parent;
    
    public LeafNode(Token token) {
        this.token = token;
        this.parent = null;
    }
    
    @Override
    public void addChild(Node child) {
        return;
    }
    
    @Override
    public ArrayList<Node> getChildren() {
        return new ArrayList<>();
    }
    
    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    @Override
    public Node getParent() {
        return this.parent;
    }
    
    @Override
    public void printNode() {
        System.out.println(token.toString());
    }
}
