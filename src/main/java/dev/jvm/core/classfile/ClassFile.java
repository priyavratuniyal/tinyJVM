package dev.jvm.core.classfile;

import java.util.List;

public class ClassFile {
    public int magic;
    public int minorVersion;
    public int majorVersion;
    public List<ConstantPoolEntry> constantPool;
    public int accessFlags;
    public int thisClass;
    public int superClass;
    public List<Integer> interfaces;
    public List<MethodInfo> methods;

    public static class MethodInfo {
        public int accessFlags;
        public int nameIndex;
        public int descriptorIndex;
    }
}