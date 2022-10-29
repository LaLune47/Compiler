import AST.BranchNode;
import AST.MyError;
import AST.Node;
import SymbolTable.SymbolTable;

import java.util.ArrayList;

// 遍历器
public class Visitor {
    private Node ast;
    private SymbolTable symbolTable;
    
    public Visitor(Node ast) {
        this.ast = ast;
        this.symbolTable = ((BranchNode) ast).getSymbolTable();
    }
    
    public SymbolTable buildSymbolTable() {
        // todo
        return
    }
    
    public void errorHandling(ArrayList<MyError> errorList) {
        // todo
        
        // errorlist.addError
    }
}
