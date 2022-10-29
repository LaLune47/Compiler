package SymbolTable;

public class ArraySpace {
    private Integer dimension1;
    private Integer dimension2;
    
    
    public ArraySpace() {
        this.dimension1 = -1;
        this.dimension2 = -1;
    }
    
    public ArraySpace(Integer dimension1) {
        this.dimension1 = dimension1;
        this.dimension2 = -1;
    }
    
    public ArraySpace(Integer dimension1,Integer dimension2) {
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
    }
}
