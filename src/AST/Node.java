package AST;
import component.Token;

import java.util.ArrayList;
import java.util.List;

public interface Node {
    public void addChild(Node child);
    public ArrayList<Node> getChildren();
    public void setParent(Node parent);
    public Node getParent();
    public void printNode();
}
