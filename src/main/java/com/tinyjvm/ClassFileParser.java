package com.tinyjvm;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClassFileParser {
    public ClassFile parse(Path classFilePath) throws IOException {
        byte[] bytes = Files.readAllBytes(classFilePath);
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(classFilePath))) {
            ClassFile classFile = new ClassFile();

            // Parse magic number (0xCAFEBABE)
            classFile.magic = dis.readInt();
            if (classFile.magic != 0xCAFEBABE) {
                throw new IllegalArgumentException("Invalid .class file (bad magic number)");
            }

            // Version info
            classFile.minorVersion = dis.readUnsignedShort();
            classFile.majorVersion = dis.readUnsignedShort();

            // Constant pool
            int constantPoolCount = dis.readUnsignedShort();
            classFile.constantPool = new ArrayList<>(constantPoolCount - 1);

            for (int i = 1; i < constantPoolCount; i++) {
                int tag = dis.readUnsignedByte();
                switch (tag) {
                    case 1: // UTF-8 string
                        int length = dis.readUnsignedShort();
                        byte[] utf8Bytes = new byte[length];
                        dis.readFully(utf8Bytes);
                        classFile.constantPool.add(new ConstantPoolEntry(tag, new String(utf8Bytes)));
                        break;
                    case 3: // Integer
                        classFile.constantPool.add(new ConstantPoolEntry(tag, dis.readInt()));
                        break;
                    case 5: // Long
                        classFile.constantPool.add(new ConstantPoolEntry(tag, dis.readLong()));
                        i++; // Long takes two slots in constant pool
                        break;
                    case 7: // Class reference
                        classFile.constantPool.add(new ConstantPoolEntry(tag, dis.readUnsignedShort()));
                        break;
                    case 8: // String reference
                        classFile.constantPool.add(new ConstantPoolEntry(tag, dis.readUnsignedShort()));
                        break;
                    case 9, 10, 11: // Fieldref, Methodref, InterfaceMethodref
                        classFile.constantPool.add(new ConstantPoolEntry(tag,
                                new int[] {dis.readUnsignedShort(), dis.readUnsignedShort()}));
                        break;
                    case 12: // NameAndType
                        classFile.constantPool.add(new ConstantPoolEntry(tag,
                                new int[] {dis.readUnsignedShort(), dis.readUnsignedShort()}));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown constant pool tag: " + tag);
                }
            }

            // Class metadata
            classFile.accessFlags = dis.readUnsignedShort();
            classFile.thisClass = dis.readUnsignedShort();
            classFile.superClass = dis.readUnsignedShort();

            // Interfaces
            int interfacesCount = dis.readUnsignedShort();
            classFile.interfaces = new ArrayList<>(interfacesCount);
            for (int i = 0; i < interfacesCount; i++) {
                classFile.interfaces.add(dis.readUnsignedShort());
            }

            // Methods (simplified - we're not parsing attributes yet)
            int methodsCount = dis.readUnsignedShort();
            classFile.methods = new ArrayList<>(methodsCount);
            for (int i = 0; i < methodsCount; i++) {
                ClassFile.MethodInfo method = new ClassFile.MethodInfo();
                method.accessFlags = dis.readUnsignedShort();
                method.nameIndex = dis.readUnsignedShort();
                method.descriptorIndex = dis.readUnsignedShort();

                // Skip attributes for now
                int attributesCount = dis.readUnsignedShort();
                for (int j = 0; j < attributesCount; j++) {
                    dis.readUnsignedShort(); // name index
                    int attributeLength = dis.readInt();
                    dis.skipBytes(attributeLength);
                }

                classFile.methods.add(method);
            }

            return classFile;
        }
    }
}