import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
    封装读文件的部分
 */
public class MyReader {
    private final String inputFilePath;
    private Integer pos;
    private Integer line;
    private final StringBuffer buffer;
    
    private final StringBuffer fileText;
    // todo 保存文件全文的笨办法,需要将文件先预先读到字符串里，增加了很多遍数，后期看需不需要更改
    
    public MyReader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        this.pos = 0;
        this.line = 1;
        this.buffer = new StringBuffer();
        this.fileText = new StringBuffer();
        
        try {
            File file = new File(inputFilePath);
            FileReader fileReader = new FileReader(file);
            
            int tempChar;
            while ((tempChar = fileReader.read()) != -1) {
                fileText.append((char) tempChar);
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("finish construct");
        System.out.println("pos:"+pos+" line:"+line+" buffer:"+buffer);
        System.out.println(fileText);
        System.out.println(fileText.length());
    }
    
    public void addChar() {
        buffer.append(fileText.charAt(pos));
    }
    
    public void step() {
        pos++;
    }
    
    public boolean isEnd() {
        return pos == fileText.length();
    }
    
    public void rollback() {
        pos--;
    }
    
    public String getBuffer() {
        return buffer.toString();
    }
    
    public void resetBuffer() {
        buffer.setLength(0);
    }
    
    public void addLine() {
        line++;
    }
    
    public Integer getLine() {
        return line;
    }
    
    // 符号读入判断函数
    public Boolean isSpace() {
        char c = fileText.charAt(pos);
        return c == ' ' ||
                c == (char) 9 || c == (char) 11 ||   // 水平垂直制表
                c == (char) 12;                      // 换页符
    }
    
    public Boolean isNewlineN() {
        return fileText.charAt(pos) == '\n';
    }
    
    public Boolean isNewlineR() {
        return fileText.charAt(pos) == '\r';
    }
    
    public boolean isAlpha() {
        char c = fileText.charAt(pos);
        return (c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A');
    }
    
    public boolean isUnderline() {
        return fileText.charAt(pos) == '_';
    }
    
    public boolean isDigit() {
        char c = fileText.charAt(pos);
        return c <= '9' && c >= '0';
    }
    
    public boolean isZero() {
        return fileText.charAt(pos) == '0';
    }
    
    public boolean isQuotation() { //双引号
        return fileText.charAt(pos) == '\"';
    }
    
    public boolean isEqualSign() {
        return fileText.charAt(pos) == '=';
    }
    
    public boolean isExclamation() { // 感叹号
        return fileText.charAt(pos) == '!';
    }
    
    public boolean isGT() { // >
        return fileText.charAt(pos) == '>';
    }
    
    public boolean isLT() { // <
        return fileText.charAt(pos) == '<';
    }
    
    public boolean isVerticalSign() { // |
        return fileText.charAt(pos) == '|';
    }
    
    public boolean isAndSign() { // &
        return fileText.charAt(pos) == '&';
    }
    
    public boolean isSlash() {   // /
        return fileText.charAt(pos) == '/';
    }
    
    public boolean isMul() {
        return fileText.charAt(pos) == '*';
    }
    
    public boolean isMulSlash() {
        return fileText.charAt(pos) == '*' && fileText.charAt(pos + 1) == '/';
    }
    
    public boolean isSEMICN() {
        return fileText.charAt(pos) == ';';
    }
    
    public boolean isMOD() {
        return fileText.charAt(pos) == '%';
    }
    
    public boolean isCOMMA() {
        return fileText.charAt(pos) == ',';
    }
    
    public boolean isLPARENT() {
        return fileText.charAt(pos) == '(';
    }
    
    public boolean isRPARENT() {
        return fileText.charAt(pos) == ')';
    }
    
    public boolean isLBRACK() {
        return fileText.charAt(pos) == '[';
    }
    
    public boolean isRBRACK() {
        return fileText.charAt(pos) == ']';
    }
    
    public boolean isPLUS() {
        return fileText.charAt(pos) == '+';
    }
    
    public boolean isLBRACE() {
        return fileText.charAt(pos) == '{';
    }
    
    public boolean isMINU() {
        return fileText.charAt(pos) == '-';
    }
    
    public boolean isRBRACE() {
        return fileText.charAt(pos) == '}';
    }
}