package AST;
// todo 部分错误处理
//- 遍历时记录循环层数，【m】
//- 符号表
//        - 函数/变量——重定义和未定义【b，c】
//        - 函数——形参表对比【d，e】
//        - 函数——return类型的对比【f, g】
//        - 变量——常量赋值【h】

public class AstCorrector {
    private Node ast;
    
    public AstCorrector(Node ast) {
        this.ast = ast;
    }
    
    public Node correct() {
        // todo
        return ast;
    }
}
