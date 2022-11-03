package AST;

import MidCode.Operation;
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
        return this;
    }
    
    @Override
    public LeafNode getLastLeafNode() {
        return this;
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
    
    public Token getToken() {
        return token;
    }
    
    @Override
    public Node unwrap() {
        return null;
    }
    
    @Override
    public Node getFirstChild() {
        return null;
    }
    
    @Override
    public Node childIterator(Integer index) {
        System.out.println("Leafnode-parent error!");
        return null;
    }

    public Operation toOp() {
        if (this == null) {
            return Operation.DEFAULT;
        }
        switch (token.getType()) {
            case PLUS:
                return Operation.PLUSOP;
            case MINU:
                return Operation.MINUOP;
            case MULT:
                return Operation.MULTOP;
            case DIV:
                return Operation.DIVOP;
            case MOD:
                return Operation.MODOP;
            default:
                return Operation.DEFAULT;
        }
    }
}
