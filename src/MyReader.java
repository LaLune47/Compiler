import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
    封装读文件的部分
 */
public class MyReader {
    private String inputFilePath;
    private Integer pos;
    private Integer line;
    private StringBuffer token;
    
    private StringBuffer fileText;
    // todo 保存文件全文的笨办法,需要将文件先预先读到字符串里，增加了很多遍数，后期看需不需要更改
    
    public MyReader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        this.pos = 0;
        this.line = 1;
        this.token = new StringBuffer();
        
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
    }
}