package AST;

import component.Token;
import component.TokenTYPE;

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
    
    @Override
    public LeafNode getFirstLeafNode() {
        return null;
    }
    
    @Override
    public Integer getLine() {
        return token.getLine();
    }
    
    public TokenTYPE getTokenType() {
        return token.getType();
    }
    
    public String getValue() {
        return token.getValue();
    }
    
    @Override
    public Node unwrap() {
        return null;
    }
    
    @Override
    public Node getFirstChild() {
        return null;
    }
}
