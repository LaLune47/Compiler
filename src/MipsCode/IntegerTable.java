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
}
