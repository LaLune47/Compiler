import AST.BranchNode;
import AST.MyError;
import AST.Node;
import SymbolTable.SymbolTable;

import java.util.ArrayList;

// 遍历器
public class Visitor {
    private Node ast;
    
    public Visitor(Node ast) {
        this.ast = ast;
    }
    
    public SymbolTable buildSymbolTable() {
        // todo
        return
    }
    
    public void errorHandling(ArrayList<MyError> errorList) {
        // 首先全局来一个 符号表，从ast上得到
        // todo
        
        // errorlist.addError
    }
}
