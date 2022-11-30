package MipsCode;

public class ItemInteger {
    private String name;
    private Integer offset; // 相对栈顶
    private IntegerTable table;
    private Boolean isPointer;
    
    public ItemInteger(String name,Integer offset,Boolean isPointer) {
        this.name = name;
        this.offset = offset;
        this.isPointer = isPointer;
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getOffset() {
        return offset;
    }
    
    public void setTable(IntegerTable table) {
        this.table = table;
    }
    
    public boolean isGlobal() {
        if (table != null) {
            return table.isGlobal();
        }
        return false;
    }
    
    public Boolean getPointer() {
        return isPointer;
    }
}
