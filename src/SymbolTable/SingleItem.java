package SymbolTable;

import AST.LeafNode;
import AST.Node;
import component.TokenTYPE;

import java.util.ArrayList;

public class SingleItem {
    private Variability variability;
    private Dimension dimension;
    private ArraySpace arraySpace;
    private String ident;
    private Node initValue;
    private ArrayList<Node> initValueArray1;
    private ArrayList<ArrayList<Node>> initValueArray2;
    // 存入初值的和arraySpace的表达式都是addExp
    
    public SingleItem(Variability variability,Dimension dimension,ArraySpace arraySpace,String ident) {
        this.variability = variability;
        this.dimension = dimension;
        this.arraySpace = arraySpace;
        this.ident = ident;
        this.initValue = null;
        this.initValueArray1 = null;
        this.initValueArray2 = null;
    }
    
    public SingleItem(Variability variability) {
        this.variability = variability;
        this.dimension = null;
        this.arraySpace =  null;
        this.ident =  null;
    }
    
    public SingleItem(Variability variability,String ident) {
        this.variability = variability;
        this.dimension = null;
        this.arraySpace =  null;
        this.ident = ident;
    }
    
    public void setArraySpace(ArraySpace arraySpace) {
        this.arraySpace = arraySpace;
    }
    
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
        if (dimension.equals(Dimension.Array2)) {
            initValueArray2 = new ArrayList<>();
        }
    }
    
    public void setInitValue(Node node) {
        //ConstInitVal, // 常量初值   ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        // InitVal,      // 变量初值   Exp | '{' [ InitVal { ',' InitVal } ] '}
        // 最多只有两层所以采用了暴力手段
    
        // node 为 InitVal 或者 ConstInitVal
        ArrayList<Node> children = node.getChildren();
        if (children.size() > 0 && typeCheckLeaf(children.get(0),TokenTYPE.LBRACE)) {
            Node firstInit = children.get(1);
            ArrayList<Node> firstInitChildren = firstInit.getChildren();
            if (firstInitChildren.size() > 0 && typeCheckLeaf(firstInitChildren.get(0),TokenTYPE.LBRACE)) {
                // 两层
                int index = 1;
                while(index < children.size()) {
                    Node curInit = children.get(index);
                    ArrayList<Node> tempList = (ArrayList<Node>) parseInitVal(curInit).clone();
                    initValueArray2.add(tempList);
                    index += 2;
                }
            } else {
                // 一层
                initValueArray1 = (ArrayList<Node>) parseInitVal(node).clone();
            }
        } else {
            initValue = unwrap(unwrap(node));
        }
    }
    
    private ArrayList<Node> parseInitVal(Node InitValNode) {
        // 特殊:InitValNode为单层,
        // 即child 一定为 '{' + InitValNode(Exp) + ',' +...+ InitValNode(Exp) + '}'
        // 返回时 一定已经unwrap到addExp
        int index = 1;
        ArrayList<Node> addExpNode = new ArrayList<>();
        ArrayList<Node> children = InitValNode.getChildren();
        while(index < children.size()) {
            Node subInitValNode = children.get(index);
            addExpNode.add(unwrap(unwrap(subInitValNode)));
            index += 2;
        }
        return addExpNode;
    }
    
    private boolean typeCheckLeaf(Node node, TokenTYPE type) {
        return ((LeafNode)node).getType().equals(type);
    }
    
    private Node unwrap(Node node) { // 去掉一层
        Node tempNode = node;
        if (!node.getChildren().isEmpty()) {
            tempNode = node.getChildren().get(0);  // addExp;
        }
        return tempNode;
    }
    
    public void setIdent(String ident) {
        this.ident = ident;
    }
    
    public String getIdent() {
        return ident;
    }
}
