package MipsCode;

public class ItemInteger {
    private String name;
    private Boolean isConst;
    
    public ItemInteger(String name, Boolean isConst) {
        this.name = name;
        this.isConst = isConst;
    }
    
    public String getName() {
        return name;
    }
}
