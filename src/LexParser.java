import component.Token;
import component.TokenTYPE;

import java.util.ArrayList;
import java.util.List;

/*
    词法解读器
    返回List<Token>，跟后面的环节基本独立
*/
public class LexParser {
    private final MyReader reader;
    private final List<Token> tokenList;
    
    public LexParser(String inputFilePath) {
        this.reader = new MyReader(inputFilePath);
        this.tokenList = new ArrayList<>();
    }
    
    public List<Token> parse() {
        //todo 两层循环寻找\n效率太低，考虑reader部分给予标记
        while (true) {
            reader.resetBuffer();
            TokenTYPE tokenTYPE = null;
            if (reader.isEnd()) {
                break;
            }
            
            while (reader.isSpace() || reader.isNewlineN() || reader.isNewlineR()) {
                // Unix系统里，每行结尾只有“<换行>”，即"\n"
                // Windows系统里面，每行结尾是“<回车><换行>”，即“\r\n”
                if (reader.isNewlineN()) {
                    reader.addLine();
                }
                reader.step();
                if (reader.isEnd()) {
                    break;
                }
            }
    
            if (reader.isEnd()) {
                break;
            }
    
            if (reader.isAlpha() || reader.isUnderline()) {
                while (reader.isAlpha() || reader.isUnderline() || reader.isDigit()) {
                    reader.addChar();
                    reader.step();
                }
                //reader.rollback();
                TokenTYPE tempType = getTokenTYPE(reader.getBuffer());
                if (tempType == TokenTYPE.DEFAULT) {
                    tokenTYPE = TokenTYPE.IDENFR;
                } else {
                    tokenTYPE = tempType;
                }
            } else if (reader.isDigit()) {
                tokenTYPE = TokenTYPE.INTCON;
                if (reader.isZero()) {
                    reader.addChar();
                    reader.step();
                } else {
                    while (reader.isDigit()) {
                        reader.addChar();
                        reader.step();
                    }
                    //reader.rollback();
                }
            } else if (reader.isQuotation()) {   // todo 双引号内有双引号的特殊情况
                tokenTYPE = TokenTYPE.STRCON;
                reader.addChar();
                reader.step();
                while (!reader.isQuotation()) {
                    reader.addChar();
                    reader.step();
                }
                reader.addChar();  // 把后双引号也读进来了
                reader.step();
            } // 矛盾的单双操作符
            else if (reader.isEqualSign()) {
                reader.addChar();
                reader.step();
                if (reader.isEqualSign()) {
                    reader.addChar();
                    reader.step();
                    tokenTYPE = TokenTYPE.EQL;
                } else {
                    tokenTYPE = TokenTYPE.ASSIGN;
                }
            } else if (reader.isExclamation()) {
                reader.addChar();
                reader.step();
                if (reader.isEqualSign()) {
                    reader.addChar();
                    reader.step();
                    tokenTYPE = TokenTYPE.NEQ;
                } else {
                    tokenTYPE = TokenTYPE.NOT;
                }
            } else if (reader.isGT()) {
                reader.addChar();
                reader.step();
                if (reader.isEqualSign()) {
                    reader.addChar();
                    reader.step();
                    tokenTYPE = TokenTYPE.GEQ;
                } else {
                    tokenTYPE = TokenTYPE.GRE;
                }
            } else if (reader.isLT()) {
                reader.addChar();
                reader.step();
                if (reader.isEqualSign()) {
                    reader.addChar();
                    reader.step();
                    tokenTYPE = TokenTYPE.LEQ;
                } else {
                    tokenTYPE = TokenTYPE.LSS;
                }
            } else if (reader.isVerticalSign()) {
                reader.addChar();
                reader.step();
                reader.addChar();  // todo 错误处理
                reader.step();
                tokenTYPE = TokenTYPE.OR;
            } else if (reader.isAndSign()) {
                reader.addChar();
                reader.step();
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.AND;
            } else if (reader.isSlash()) {
                //reader.addChar();
                reader.step();
                if (!reader.isEnd() && reader.isSlash()) {
                    reader.step();
                    while (!reader.isEnd() && !reader.isNewlineN()) {
                        reader.step();
                    }
                    if (reader.isEnd()) {
                        break;
                    }
                    reader.step();
                    reader.addLine();
                    continue;
                } else if (!reader.isEnd() && reader.isMul()) {
                    reader.step();  // 位置到了*下一个
                    while (!reader.isMulSlash()) {
                        reader.step();
                        if (reader.isNewlineN()) {
                            reader.addLine();
                        }
                        //if (reader.isEnd()) {  //todo /**/多行注释，可能出现的错误1、后面不匹配 2.中途结束
                        //    break;
                        //}
                    } // 现在当前位置是*，下一个是/
                    reader.step();
                    reader.step();
                    continue;
                } else {
                    reader.rollback();
                    reader.addChar();
                    reader.step();
                    tokenTYPE = TokenTYPE.DIV;
                }
            } else if (reader.isMul()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.MULT;
            } else if (reader.isSEMICN()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.SEMICN;
            } else if (reader.isMOD()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.MOD;
            } else if (reader.isCOMMA()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.COMMA;
            } else if (reader.isLPARENT()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.LPARENT;
            } else if (reader.isRPARENT()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.RPARENT;
            } else if (reader.isLBRACK()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.LBRACK;
            } else if (reader.isRBRACK()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.RBRACK;
            } else if (reader.isPLUS()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.PLUS;
            } else if (reader.isLBRACE()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.LBRACE;
            } else if (reader.isMINU()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.MINU;
            } else if (reader.isRBRACE()) {
                reader.addChar();
                reader.step();
                tokenTYPE = TokenTYPE.RBRACE;
            }
            
            String value = reader.getBuffer();
            Integer line = reader.getLine();
            Token token = new Token(line,tokenTYPE,value);
            tokenList.add(token);
            //System.out.println(token.toString());
        }
        return this.tokenList;
    }
    
    private TokenTYPE getTokenTYPE(String token) {
        switch (token) {
            case "main":
                return TokenTYPE.MAINTK;
            case "while":
                return TokenTYPE.WHILETK;
            case "const":
                return TokenTYPE.CONSTTK;
            case "getint":
                return TokenTYPE.GETINTTK;
            case "int":
                return TokenTYPE.INTTK;
            case "printf":
                return TokenTYPE.PRINTFTK;
            case "break":
                return TokenTYPE.BREAKTK;
            case "return":
                return TokenTYPE.RETURNTK;
            case "continue":
                return TokenTYPE.CONTINUETK;
            case "if":
                return TokenTYPE.IFTK;
            case "else":
                return TokenTYPE.ELSETK;
            case "void":
                return TokenTYPE.VOIDTK;
            default:
                return TokenTYPE.DEFAULT;
        }
    }
}
