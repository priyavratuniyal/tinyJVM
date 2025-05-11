package dev.jvm;

import dev.jvm.core.classfile.ClassFile;
import dev.jvm.core.classfile.ClassFileParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <classfile>");
            return;
        }

        try {
            Path classFilePath = Paths.get(args[0]);
            ClassFileParser parser = new ClassFileParser();
            ClassFile classFile = parser.parse(classFilePath);

            System.out.println("Class file parsed successfully!");
            System.out.printf("Version: %d.%d%n", classFile.majorVersion, classFile.minorVersion);
            System.out.println("Constant pool entries: " + classFile.constantPool.size());
            System.out.println("Methods: " + classFile.methods.size());

            // Print first few constant pool entries
            System.out.println("\nFirst 5 constant pool entries:");
            for (int i = 0; i < Math.min(5, classFile.constantPool.size()); i++) {
                System.out.println((i+1) + ": " + classFile.constantPool.get(i));
            }
        } catch (Exception e) {
            System.err.println("Error parsing class file:");
            e.printStackTrace();
        }
    }
}