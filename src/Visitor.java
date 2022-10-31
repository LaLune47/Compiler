import AST.BranchNode;
import AST.LeafNode;
import AST.MyError;
import AST.Node;
import component.NonTerminator;
import component.TokenTYPE;

import java.util.ArrayList;

public class Visitor {
    private Node ast;
    
    public Visitor(Node ast) {
        this.ast = ast;
    }
    
    private boolean typeCheckBranch(Node node, NonTerminator nonTerminator) {
        if (node instanceof BranchNode) {
            return ((BranchNode)node).getNonTerminator().equals(nonTerminator);
        }
        return false;
    }
    
    private boolean typeCheckLeaf(Node node, TokenTYPE type) {
        return ((LeafNode)node).getType().equals(type);
    }
    
    private Node childIterator(Node parent, int index) {
        if (parent instanceof LeafNode) {
            System.out.println("Leafnode-parent error!");
            return null;
        } else {
            ArrayList<Node> children = ((BranchNode) parent).getChildren();
            if (children != null && children.size() > index) {
                return children.get(index);
            } else {
                System.out.println(((BranchNode) parent).getNonTerminator().toString() + "--children error");
                return null;
            }
        }
    }
    
    public void errorHandling(ArrayList<MyError> errorList) {
        // 首先全局来一个 符号表，从ast上得到
        // todo
        
        // errorlist.addError
    }
}
