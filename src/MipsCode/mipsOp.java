package MipsCode;

public enum mipsOp {
    debug,
    space,
    data,
    text,
    conString,
    li,
    sw,
    lw,
    add,
    sub,
    mult,
    div,
    mflo,
    mfhi,
    syscall,
    la,
    j,
    label,
    jr,
    addi,
    move,
    jal,
    nop,
    
    beqz,
    slt, // LSSOP, <
    sle, // LEQOP, <=
    sgt, // GREOP, >
    sge, // GEQOP, >=
    seq, // EQLOP, ==
    sne, // NEQOP, !=
    
    sll,
}
