package MipsCode;

public class ItemInteger {
    private String name;
    private Integer offset; // 相对栈顶
    private IntegerTable table;
    
    public ItemInteger(String name,Integer offset) {
        this.name = name;
        this.offset = offset;
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
}
