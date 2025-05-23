package dev.jvm.core.classfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ClassFileParserTest {

    private ClassFileParser parser;

    // Path to the compiled SimpleClass.class in test resources
    private static final String SIMPLE_CLASS_RESOURCE_PATH = "/dev/jvm/core/classfile/SimpleClass.class";
    // Path to the compiled EmptyClass.class in test resources
    private static final String EMPTY_CLASS_RESOURCE_PATH = "/dev/jvm/core/classfile/EmptyClass.class";

    @BeforeEach
    void setUp() {
        parser = new ClassFileParser();
    }

    private Path getResourcePath(String resourceName) throws Exception {
        return Paths.get(getClass().getResource(resourceName).toURI());
    }

    @Test
    void testParseValidSimpleClass() throws Exception {
        Path simpleClassPath = getResourcePath(SIMPLE_CLASS_RESOURCE_PATH);
        ClassFile classFile = parser.parse(simpleClassPath);

        assertNotNull(classFile);
        assertEquals(0xCAFEBABE, classFile.magic, "Magic number should be 0xCAFEBABE");
        assertEquals(52, classFile.majorVersion, "Major version should correspond to Java 8 (52)"); 
                                                     // Assuming compilation with Java 8 target
        
        // Constant Pool Checks (examples - expand as needed)
        assertTrue(classFile.constantPool.size() > 10, "Constant pool should have several entries");

        // Check for class name: dev/jvm/testclasses/SimpleClass
        Optional<ConstantPoolEntry> classNameEntry = classFile.constantPool.stream()
            .filter(cp -> cp.tag == 7) // CONSTANT_Class
            .map(cp -> classFile.constantPool.get(((Integer)cp.value) - 1)) // Get the UTF8 entry
            .filter(cpUtf8 -> cpUtf8.tag == 1 && "dev/jvm/testclasses/SimpleClass".equals(cpUtf8.value))
            .findFirst();
        assertTrue(classNameEntry.isPresent(), "Class name 'dev/jvm/testclasses/SimpleClass' not found in constant pool");

        // Check for a method name: e.g., "greet"
        Optional<ConstantPoolEntry> methodNameEntry = classFile.constantPool.stream()
            .filter(cp -> cp.tag == 1 && "greet".equals(cp.value)) // CONSTANT_Utf8
            .findFirst();
        assertTrue(methodNameEntry.isPresent(), "Method name 'greet' not found in constant pool");
        
        // Check for a string constant: e.g., "Hello" (from `message` field initializer)
        // Note: String constants used in code like "Hello" are directly in the pool.
        // The value of the `message` field itself if not `final static` might be set via constructor/bytecode.
        // Let's look for "AConstantString" which is a public static final.
         Optional<ConstantPoolEntry> stringConstantEntry = classFile.constantPool.stream()
            .filter(cp -> cp.tag == 1 && "AConstantString".equals(cp.value)) // CONSTANT_Utf8 for the string value
            .findFirst();
        assertTrue(stringConstantEntry.isPresent(), "String constant 'AConstantString' not found in constant pool");


        // Method Checks
        // For SimpleClass: <init>, greet, getMeaningOfLife, main, anotherMethod (5 methods)
        assertEquals(5, classFile.methods.size(), "Should have 5 methods in SimpleClass"); 
                                                 // (constructor, greet, getMeaningOfLife, main, anotherMethod)

        // Example: Check details for the 'greet' method
        // This requires finding the "greet" UTF8, then its Methodref, then its NameAndType
        // This is a more involved check and might be simplified or made more robust.
    }

    @Test
    void testParseValidEmptyClass() throws Exception {
        Path emptyClassPath = getResourcePath(EMPTY_CLASS_RESOURCE_PATH);
        ClassFile classFile = parser.parse(emptyClassPath);

        assertNotNull(classFile);
        assertEquals(0xCAFEBABE, classFile.magic, "Magic number should be 0xCAFEBABE");
        assertEquals(52, classFile.majorVersion, "Major version should correspond to Java 8 (52)");

        // Empty class typically has one method: the default constructor <init>
        assertEquals(1, classFile.methods.size(), "Empty class should have one method (default constructor)");
        
        // Check for constructor name "<init>"
        ConstantPoolEntry constructorNameEntry = classFile.constantPool.get(classFile.methods.get(0).nameIndex -1);
        assertEquals("<init>", constructorNameEntry.value, "Constructor name should be <init>");
    }

    @Test
    void testParseInvalidMagicNumber(@TempDir Path tempDir) throws IOException {
        Path invalidFile = tempDir.resolve("InvalidClass.class");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(invalidFile.toFile()))) {
            dos.writeInt(0xDEADBEEF); // Invalid magic number
            dos.writeUTF("Some other data");
        }

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(invalidFile);
        });
        assertTrue(exception.getMessage().contains("bad magic number"), "Exception message should indicate bad magic number");
    }

    @Test
    void testFileNotFound() {
        Path nonExistentFile = Paths.get("non_existent_class_file.class");
        assertThrows(IOException.class, () -> {
            parser.parse(nonExistentFile);
        }, "Parsing a non-existent file should throw IOException");
    }
}
