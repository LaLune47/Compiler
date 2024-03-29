package MipsCode;

import java.util.ArrayList;
import java.util.HashMap;

public class IntegerTable {
    private IntegerTable parent;
    private HashMap<String,ItemInteger> itemIntegers;
    private HashMap<String,ItemArray> itemArrays;
    private boolean isGlobal;
    
    public IntegerTable(IntegerTable parent) {
        this.parent = parent;
        if (parent == null) {
            isGlobal = true;
        } else {
            isGlobal = false;
        }
        itemIntegers = new HashMap<>();
        itemArrays = new HashMap<>();
    }
    
    public void addItem(ItemInteger itemInteger) {
        itemIntegers.put(itemInteger.getName(),itemInteger);
    }
    
    public void addItemArray(ItemArray itemArray) {
        itemArrays.put(itemArray.getName(),itemArray);
    }
    
    public boolean contains(String name) {
        return itemIntegers.containsKey(name);
    }
    
    public boolean containsArray(String name) {
        return itemArrays.containsKey(name);
    }
    
    public ItemInteger getItem(String name) {
        if (contains(name)) {
            return itemIntegers.get(name);
        } else {
            return null;
        }
    }
    
    public ItemArray getItemArray(String name) {
        if (containsArray(name)) {
            return itemArrays.get(name);
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
    
//    public Integer length() {
//        return itemIntegers.size();
//    }
}
