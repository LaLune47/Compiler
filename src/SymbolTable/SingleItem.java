package SymbolTable;

public class SingleItem {
    private Variability variability;
    private Dimension dimension;
    private ArraySpace arraySpace;
    private String ident;
    
    public SingleItem(Variability variability,Dimension dimension,ArraySpace arraySpace,String ident) {
        this.variability = variability;
        this.dimension = dimension;
        this.arraySpace = arraySpace;
        this.ident = ident;
    }
}
