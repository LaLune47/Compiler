package MipsCode;

import MidCode.MidCode;

import java.util.ArrayList;

public class MipsGenerator {
    private ArrayList<MidCode> midCodes;
    private ArrayList<String> conStrings;
    private ArrayList<FinalCode> finalCodes;
    
    public MipsGenerator(ArrayList<MidCode> midCodes,ArrayList<String> conStrings) {
        this.midCodes = midCodes;
        this.conStrings = conStrings;
        this.finalCodes = new ArrayList<>();
    }
}
