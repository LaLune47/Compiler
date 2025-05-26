# SysY-to-MIPS32 Compiler

This project is a Java-based compiler developed as a course project for the 'Compiler Principles' course at Beihang University in Fall 2022. It translates source programs written in **SysY** (a simplified C-like language) into **MIPS32 assembly code**. It covers the full compilation pipeline from lexical analysis to code generation.

---

## Overview

**Key Features:**
- Handwritten **recursive descent parser** for syntax analysis
- Construction of **Abstract Syntax Tree (AST)**
- Semantic checking and **symbol table management**
- Generation of **intermediate representation (IR)** using three-address code
- Translation of IR into **MIPS32 assembly**
- Supports functions, control flow, arrays, I/O, and expressions

---

## Project Structure

```text
Compiler-master/
├── src/
│   ├── Compiler.java                // Entry point
│   ├── LexParser.java               // Lexical analyzer
│   ├── SyntaxParser.java            // Syntax analyzer (uses AstBuilder)
│   ├── SymbolTableBuilder.java      // Semantic analyzer + IR generator
│   ├── MipsCode/
│   │   ├── MipsGenerator.java       // MIPS code generator
│   │   └── ...          
│   ├── MidCode/
│   │   ├── MidCode.java             // Three-address code representation
│   │   ├── midOp.java               // Enumeration of IR operations
│   │   └── ...
│   ├── AST/
│   │   ├── AstBuilder.java          // AST construction
│   │   ├── Node.java, LeafNode.java, BranchNode.java
│   │   └── ...
│   └── component/                   // Token, token types, and helpers
├── README.md                        // Project documentation
├── Config.java                      // I/O file path configuration
└── .idea/, *.iml                    // IntelliJ project files
```

---

## Compilation Pipeline

```text
1. Source File (SysY)
   ↓
2. Lexical Analysis (LexParser)
   ↓
3. Syntax Analysis → AST (SyntaxParser + AstBuilder)
   ↓
4. Semantic Analysis + IR Generation (SymbolTableBuilder)
   ↓
5. MIPS32 Assembly Generation (MipsGenerator)
   ↓
6. Output: .s file (MIPS32 code)
```

---

## Getting Started

### Requirements

- Java 8 or higher
- IntelliJ IDEA (recommended)

### Compile & Run

1. Place your SysY source code in the input path specified by `Config.inputFilePath`.
2. Compile and run `Compiler.java` (e.g., via IntelliJ or command line).
3. The resulting MIPS assembly code will be output to `Config.mipsPath`.


---

## Example Output

Given a SysY program like:

```c
int main() {
    int a = 3 + 5;
    printf("%d\n", a);
    return 0;
}
```

The compiler will produce MIPS code similar to:

```asm
li $t0, 3
li $t1, 5
add $t2, $t0, $t1
move $a0, $t2
li $v0, 1
syscall
...
```