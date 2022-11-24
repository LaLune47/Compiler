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
    private Integer initValue;
    
    private Integer space1 = 0;   // 维度1
    private Integer space2 = 0;   // 维度2
    private ArrayList<Integer> arrayInitValue = new ArrayList<>();

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
}
