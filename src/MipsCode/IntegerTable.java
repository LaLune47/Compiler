package MipsCode;

import java.util.ArrayList;
import java.util.HashMap;

public class IntegerTable {
    private IntegerTable parent;
    private HashMap<String,ItemInteger> itemIntegers;
    private boolean isGlobal;
    
    public IntegerTable(IntegerTable parent) {
        this.parent = parent;
        if (parent == null) {
            isGlobal = true;
        } else {
            isGlobal = false;
        }
        itemIntegers = new HashMap<>();
    }
    
    public void addItem(ItemInteger itemInteger) {
        itemIntegers.put(itemInteger.getName(),itemInteger);
    }
    
    public boolean contains(String name) {
        return itemIntegers.containsKey(name);
    }
    
    public ItemInteger getItem(String name) {
        if (contains(name)) {
            return itemIntegers.get(name);
        } else {
            return null;
        }
    }
    
    public IntegerTable getParent() {
        return parent;
    }
    
    public boolean isGlobal() {
        return isGlobal;
    }
    
    public Integer length() {
        return itemIntegers.size();
    }
}
