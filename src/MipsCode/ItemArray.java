package MipsCode;

public class ItemArray {
    private String name;
    private Integer offset; // 相对栈顶
    private Integer length;
    private IntegerTable table;
    
    public ItemArray(String name,Integer offset,Integer length) {
        this.name = name;
        this.offset = offset;
        this.length = length;
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