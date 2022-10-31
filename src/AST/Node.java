package AST;

import java.util.ArrayList;

public interface Node {
    public void addChild(Node child);
    
    public ArrayList<Node> getChildren();
    
    public void setParent(Node parent);
    
    public Node getParent();
    
    public void printNode();
    
    public LeafNode getFirstLeafNode();
    
    public LeafNode getLastLeafNode();
    
    public Integer getLine();
    
    public Node unwrap();
    
    public Node getFirstChild();
    
    public Node childIterator(Integer index);
}
