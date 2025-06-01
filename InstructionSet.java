public class InstructionSet {

    public static void bipush(JFrame frame) {
        // In Java, bytes are signed. code[frame.pc++] will be sign-extended to int.
        // This is the correct behavior for bipush.
        int value = frame.code[frame.pc++];
        frame.operandStack.push(value);
    }

    public static void iadd(JFrame frame) {
        int b = frame.operandStack.pop();
        int a = frame.operandStack.pop();
        frame.operandStack.push(a + b);
    }

    public static int ireturn(JFrame frame) {
        return frame.operandStack.pop();
    }
}
