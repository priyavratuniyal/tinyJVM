public class JFrame {
    public int pc;
    public OperandStack operandStack;
    public LocalVariables localVariables;
    public byte[] code;

    public JFrame(byte[] code, int maxStackSize, int localVarSize) {
        this.code = code;
        this.operandStack = new OperandStack(maxStackSize);
        this.localVariables = new LocalVariables(localVarSize);
        this.pc = 0;
    }
}
