package SymbolTable;

import AST.LeafNode;
import AST.Node;
import component.TokenTYPE;

import java.util.ArrayList;

public class SingleItem {
    private Boolean isConst;
    private Integer dimension;
    private String ident = null;
    private Integer defineLine;
    private Integer initValue;  // todo const初值，有一定作用
    
    private Integer space1 = 0;   // 维度1
    private Integer space2 = 0;   // 维度2
    private ArrayList<Integer> arrayInitValue = new ArrayList<>();
    
    //private boolean isInit;
//    private Node initValue = null;
//    private ArrayList<Node> initValueArray1 = null;
//    private ArrayList<ArrayList<Node>> initValueArray2 = null;  // todo数组的坑
    // 存入初值的和arraySpace的表达式都是addExp
    
    public SingleItem(Boolean isConst,Integer dimension,String ident) {
        this.isConst = isConst;
        this.dimension = dimension;
        this.ident = ident;
    }
    
    public SingleItem(Boolean isConst,Integer dimension) {
        this.isConst = isConst;
        this.dimension = dimension;
    }
    
    public SingleItem(Boolean isConst) {
        this.isConst = isConst;
    }
    
    public void setSpace1(Integer space1) {
        this.space1 = space1;
    }
    
    public void setSpace2(Integer space2) {
        this.space2 = space2;
    }
    
    public Integer getSpace2() {
        return space2;
    }
    
    public Integer getDimension() {
        return dimension;
    }
    
    public Integer getArrayValue1(Integer index) {
        if (arrayInitValue.size() > index) {
            return arrayInitValue.get(index);
        } else {
            System.out.println("getArrayValue1----error");
            return 0;
        }
    }
    
    public Integer getArrayValue2(Integer i,Integer j) {
        if (arrayInitValue.size() > i * space2 + j) {
            return arrayInitValue.get(i * space2 + j);
        } else {
            System.out.println("getArrayValue2----error");
            return 0;
        }
    }
    
    public void addArrayInit(Integer value) {
        arrayInitValue.add(value);
    }

//    private Integer tranArray(Integer i,Integer j,Integer space2) {
//        return i * space2 + j;
//    }
    
    public void setDefineLine(Integer defineLine) {
        this.defineLine = defineLine;
    }
    
    public Integer getDefineLine() {
        return defineLine;
    }
    
    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }
    
    private boolean typeCheckLeaf(Node node, TokenTYPE type) {
        if (node instanceof LeafNode) {
            return ((LeafNode)node).getTokenType().equals(type);
        }
        return false;
    }
    
    public void setIdent(String ident) {
        this.ident = ident;
    }
    
    public String getIdent() {
        return ident;
    }
    
    public Boolean getIsConst() {
        return isConst;
    }
    
    public void setInit(Integer initValue) {
        this.initValue = initValue;
    }
    
    public Integer getInit() {
        return initValue;
    }
    
    // todo 变量和数组多层初始化的问题，需要迁移到SymbolTableBuilder类中
//    public void setInitValue(Node node) {
//        //ConstInitVal, // 常量初值   ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
//        // InitVal,      // 变量初值   Exp | '{' [ InitVal { ',' InitVal } ] '}
//        // 最多只有两层所以采用了暴力手段
//        // node 为 InitVal 或者 ConstInitVal
//        this.isInit = true;
//
//        ArrayList<Node> children = node.getChildren();
//        if (children.size() > 0 && typeCheckLeaf(children.get(0),TokenTYPE.LBRACE)) {
//            Node firstInit = children.get(1);
//            ArrayList<Node> firstInitChildren = firstInit.getChildren();
//            if (firstInitChildren.size() > 0 && typeCheckLeaf(firstInitChildren.get(0),TokenTYPE.LBRACE)) {
//                // 两层
//                int index = 1;
//                while(index < children.size()) {
//                    Node curInit = children.get(index);
//                    ArrayList<Node> tempList = (ArrayList<Node>) parseInitVal(curInit).clone();
//                    initValueArray2.add(tempList);
//                    index += 2;
//                }
//            } else {
//                // 一层
//                initValueArray1 = (ArrayList<Node>) parseInitVal(node).clone();
//            }
//        } else {
//            initValue = unwrap(unwrap(node));
//        }
//    }
//
//    private ArrayList<Node> parseInitVal(Node InitValNode) {
//        // 特殊:InitValNode为单层,
//        // 即child 一定为 '{' + InitValNode(Exp) + ',' +...+ InitValNode(Exp) + '}'
//        // 返回时 一定已经unwrap到addExp
//        int index = 1;
//        ArrayList<Node> addExpNode = new ArrayList<>();
//        ArrayList<Node> children = InitValNode.getChildren();
//        while(index < children.size()) {
//            Node subInitValNode = children.get(index);
//            addExpNode.add(unwrap(unwrap(subInitValNode)));
//            index += 2;
//        }
//        return addExpNode;
//    }
}
