package SymbolTable;

import AST.Node;

public class ArraySpace {
    private Node space1;
    private Node space2;
    private boolean ignoreFirst; //标识函数形参省略第一纬
    
    
    public ArraySpace() {
        this.space1 = null;
        this.space2 = null;
        this.ignoreFirst = false;
    }
    
    public ArraySpace(Node constExpNode) {
        Node node = constExpNode;
        if (!constExpNode.getChildren().isEmpty()) {
            node = constExpNode.getChildren().get(0);  // addExp;
        }
        this.space1 = node;
        this.space2 = null;
    }
    
    public ArraySpace(Node constExpNode1,Node constExpNode2) {
        Node node1 = constExpNode1;
        if (!constExpNode1.getChildren().isEmpty()) {
            node1 = constExpNode1.getChildren().get(0);  // addExp;
        }
    
        Node node2 = constExpNode2;
        if (!constExpNode2.getChildren().isEmpty()) {
            node2 = constExpNode2.getChildren().get(0);  // addExp;
        }
        this.space1 = node1;
        this.space2 = node2;
    }
    
    public ArraySpace(boolean ignoreFirst,Node constExpNode2) {
        Node node2 = constExpNode2;
        if (!constExpNode2.getChildren().isEmpty()) {
            node2 = constExpNode2.getChildren().get(0);  // addExp;
        }
        this.space2 = node2;
        setIgnoreFirst(ignoreFirst);
    }
    
    public void setIgnoreFirst(boolean ignoreFirst) {
        this.ignoreFirst = ignoreFirst;
    }
    
    public boolean isIgnoreFirst() {
        return ignoreFirst;
    }
}
