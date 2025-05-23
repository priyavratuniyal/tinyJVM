package dev.jvm.testclasses;

public class SimpleClass {
    private static final int MEANING_OF_LIFE = 42;
    private String message = "Hello";
    public static final String PUBLIC_STATIC_FINAL_STRING = "AConstantString";

    public SimpleClass() {
    }

    public String greet(String name) {
        return message + ", " + name + "!";
    }

    private int getMeaningOfLife() {
        return MEANING_OF_LIFE;
    }

    public static void main(String[] args) {
        SimpleClass sc = new SimpleClass();
        System.out.println(sc.greet("World"));
    }

    public void anotherMethod(int p1, String p2) {}
}
