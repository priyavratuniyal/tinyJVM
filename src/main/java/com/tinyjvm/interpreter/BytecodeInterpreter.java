package com.tinyjvm.interpreter;

public class BytecodeInterpreter {

    public int execute(JFrame frame) {
        byte[] code = frame.code;

        while (true) {
            int opcode = Byte.toUnsignedInt(code[frame.pc++]);

            switch (opcode) {
                case 0x10: // bipush
                    InstructionSet.bipush(frame);
                    break;

                case 0x60: // iadd
                    InstructionSet.iadd(frame);
                    break;

                case 0xac: // ireturn
                    return InstructionSet.ireturn(frame);

                default:
                    throw new UnsupportedOperationException("Opcode not implemented: 0x" + Integer.toHexString(opcode));
            }
        }
    }
}
