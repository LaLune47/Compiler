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
    nor, // 实现取反功能， nor    $s1, $s2, $s3 ( 其中$s3为0，相当于对$s2按位取反）
    slt, // LSSOP, <
    sle, // LEQOP, <=
    sgt, // GREOP, >
    sge, // GEQOP, >=
    seq, // EQLOP, ==
    sne, // NEQOP, !=
}
