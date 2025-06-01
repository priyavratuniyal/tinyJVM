public class Main {
    public static void main(String[] args) {
        // Bytecode for:
        // public static int simpleAddition() {
        //     int a = 10;
        //     int b = 20;
        //     int c = a + b;
        //     return c;
        // }
        //
        // Using javap -c on a class with the above method (simplified):
        //   0: bipush        10
        //   2: bipush        20
        //   4: iadd
        //   5: ireturn

        byte[] code = new byte[] {
            0x10, (byte) 10, // bipush 10
            0x10, (byte) 20, // bipush 20
            0x60,            // iadd
            (byte) 0xac      // ireturn
        };

        // maxStackSize can be determined by static analysis of the bytecode.
        // For this simple case:
        // bipush 10: stack size 1
        // bipush 20: stack size 2
        // iadd: pops 2, pushes 1. stack size 1
        // ireturn: pops 1. stack size 0
        // So, max stack needed is 2. We'll give it 10 for safety.
        // localVarSize is 0 as we are not using local variables in this bytecode.
        JFrame frame = new JFrame(code, 10, 0);
        BytecodeInterpreter interpreter = new BytecodeInterpreter();

        try {
            int result = interpreter.execute(frame);
            System.out.println("Returned: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
