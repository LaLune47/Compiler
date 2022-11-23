package AST;

import MidCode.midOp;
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

    public midOp toOp() {
        if (this == null) {
            return midOp.DEFAULT;
        }
        switch (token.getType()) {
            case PLUS:
                return midOp.PLUSOP;
            case MINU:
                return midOp.MINUOP;
            case MULT:
                return midOp.MULTOP;
            case DIV:
                return midOp.DIVOP;
            case MOD:
                return midOp.MODOP;
            case LSS:
                return midOp.LSSOP; // <
            case LEQ:
                return midOp.LEQOP; // <=
            case GRE:
                return midOp.GREOP; // >
            case GEQ:
                return midOp.GEQOP; // >=
            case EQL:
                return midOp.EQLOP; // ==
            case NEQ:
                return midOp.NEQOP; // !=
            default:
                return midOp.DEFAULT;
        }
    }
}
