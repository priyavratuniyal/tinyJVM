# ☕ tinyJVM: Your Journey into Java's Core

Ever wondered how Java *actually* runs your code behind the scenes? Curious about the magic that turns your `.java` files into executable programs?

This project is your hands-on guide to building a simplified Java Virtual Machine (JVM) from scratch, using Java itself! We'll embark on a learning adventure, starting with the fundamentals of `.class` files, moving through bytecode interpretation, and eventually exploring how the JVM manages memory and threads.

Our primary goal is to **learn by doing**. We'll unravel the mysteries of the JVM piece by piece, without relying on external libraries like ASM or BCEL.

Just plain Java, clear explanations, and your curiosity!

---

## 📚 Learning by Building — Our Step-by-Step Article Series

We've structured this project as an article series, where each installment focuses on a major piece of the JVM puzzle. Follow along, build with us, and watch your own JVM come to life!

1. ✅ **Parsing `.class` Files Without Any Libraries** (Corresponds to "Read `.class` Files" and "Constant Pool Parsing")
2. ✅ **Building a Bytecode Interpreter in Java** (Corresponds to "Bytecode Interpreter")
3. ✅ **Simulating JVM Stack Frames and Memory** (Corresponds to this feature)
4. ✅ **Creating a Custom Threading Model for the JVM** (Corresponds to this feature)
5. 🔜 **Final Architecture + Reflections on Building a JVM** (What we'll learn and achieve overall)

---

## 🧩 JVM Feature Progress: What Can Our tinyJVM Do?

Here’s a snapshot of what our custom JVM can do so far, and what exciting features are on the horizon. Dive in and explore the code for each completed part!

| Feature                                | Status   | What it Means                                                                                                                               |
|----------------------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------|
| 🔍 Read `.class` Files                 | ✅ Done   | Parses the raw bytes of Java `.class` files manually. Learn how Java organises compiled code, from magic numbers to method definitions.       |
| 🧠 Constant Pool Parsing               | ✅ Done   | Reads metadata like class names, method names, and string literals. Understand how the JVM resolves symbols and manages shared data.        |
| 🔐 Access Flags                        | ✅ Done   | Understands keywords like `public`, `final`, `abstract` that modify class, field, or method behavior.                                      |
| 🧬 This Class & Super Class Info       | ✅ Done   | Figures out the current class being defined and its parent class, forming the basis of inheritance.                                         |
| 📑 Interfaces                          | ✅ Done   | Lists all interfaces a class implements, crucial for understanding polymorphism and contract adherence.                                   |
| 📦 Fields                              | ✅ Done   | Parses variable declarations within a class, including their names, types, and modifiers.                                                   |
| 🔧 Methods                             | ✅ Done   | Reads method signatures (name, parameters, return type) and other metadata, defining the behaviors of a class.                            |
| 🧾 Code Attribute (Bytecode)           | ✅ Done   | Grabs the actual bytecode instructions from methods – the low-level commands the JVM executes. This is where the action happens!                 |
| 🔄 Bytecode Interpreter                | ✅ Done   | Executes bytecode instructions one by one. See how the JVM translates abstract operations into concrete actions on the stack and local variables. |
| 📊 Simulating JVM Stack Frames & Memory | ✅ Done   | Manages method calls with stack frames (workspaces for methods) and allocates memory for objects. Explore the runtime data areas crucial for program execution. |
| 🧵 Creating a Custom Threading Model   | ✅ Done   | Implements basic threading constructs, allowing for concurrent execution of different parts of a program. Delve into how the JVM handles this. |
| 🚀 More Features                       | 🔜 Soon  | We're always dreaming up the next steps! Have an idea? Let us know!                                                                          |
---

## 🚀 Getting Started: Let's Run the Code!

Ready to see tinyJVM in action? This section will guide you through setting up and running the project. We encourage you to follow along and experiment!

### 1. Prerequisites

*   Java Development Kit (JDK) 17 or later.
*   Apache Maven.

### 2. Cloning the Repository

```bash
git clone https://github.com/your-username/jvm-in-java.git
cd jvm-in-java
```

### 3. Building the Project

This project uses Apache Maven to manage dependencies and build the source code. To build the project, run the following command in the root directory of the project:

```bash
mvn clean compile
```

This command will clean the project, compile the source code, and prepare it for execution.

### 4. Running the Application

The `Main` class (`com.tinyjvm.Main`) is the entry point of the application. It expects a single argument: the path to a compiled Java `.class` file.

To run the application and parse an example `.class` file (e.g., `HelloWorld.class` located in `src/main/java/com/tinyjvm/examples/`), use the following Maven command:

```bash
mvn exec:java -Dexec.mainClass="com.tinyjvm.Main" -Dexec.args="src/main/java/com/tinyjvm/examples/HelloWorld.class"
```

**Expected Output:**

You should see output indicating that the class file was parsed successfully. When parsing `HelloWorld.class` (or a similar simple class), `com.tinyjvm.Main` will typically print details like the class file version, the number of constant pool entries, the number of methods, and a snippet of the constant pool. The exact output details might evolve as the parser is enhanced, but it will look something like this:

```
Class file parsed successfully!
Class File Version: 52.0 (Java 8)
Constant Pool Count: 25
Access Flags: public super
This Class: com/tinyjvm/examples/HelloWorld
Super Class: java/lang/Object
Interfaces Count: 0
Fields Count: 0
Methods Count: 2
--- Constant Pool ---
  #1: Methodref #4.#19 // java/lang/Object."<init>":()V
  #2: Fieldref #3.#20 // java/lang/System.out:Ljava/io/PrintStream;
  #3: Class #21 // java/lang/System
  ... (more entries) ...
--- Methods ---
Name: <init>, Descriptor: ()V
  Code:
    Max stack: 1, Max locals: 1
    aload_0
    invokespecial #1 // Method java/lang/Object."<init>":()V
    return
... (and other output from Main.java, such as details of other methods) ...
```
(Note: The exact numbers like version, counts, and specific constant pool entries might vary slightly based on your Java compiler version and the specifics of `HelloWorld.class`.)

### 5. IDE Setup (Maven Project)

You can import this project into your favorite IDE as a Maven project.

*   **IntelliJ IDEA**:
    1.  Go to `File > Open...`.
    2.  Navigate to the project's root directory (where `pom.xml` is located).
    3.  Select the `pom.xml` file or the directory itself and click `OK`. IntelliJ IDEA will automatically recognize it as a Maven project.

*   **Eclipse**:
    1.  Go to `File > Import...`.
    2.  Choose `Maven > Existing Maven Projects`.
    3.  Click `Next`.
    4.  For `Root Directory`, click `Browse...` and select the project's root directory.
    5.  Ensure the `pom.xml` file is selected under `Projects`.
    6.  Click `Finish`.

### 6. Java Version Note

**Important:** The project currently uses Java 8 for compilation as specified in `pom.xml`. This is to maintain compatibility with a wider range of systems for initial learners. However, for a complete educational experience and to align with modern Java practices, we **strongly recommend** you update the `<maven.compiler.source>` and `<maven.compiler.target>` properties in your local `pom.xml` to `17` or later. This will allow you to use modern Java features if you decide to expand upon the project.

## 💡 Who is This Project For?

This project is for you if:

*   You’ve started learning Java and want to **dig deeper** into its inner workings.
*   You’re **curious** about how the JVM, a fundamental piece of the Java ecosystem, operates.
*   You **learn best by building** and want a hands-on, from-scratch experience.
*   You want to understand the **link between source code, bytecode, and execution**.

No advanced Java wizardry is required — we’ll explain concepts step-by-step. Our aim is for you to emerge with enhanced systems thinking and byte-level confidence!

## 🛠️ Tech Stack & Design Philosophy

*   **Core Language:** Java (currently targeting Java 8 for `pom.xml` compatibility, with a recommendation for users to switch to 17+ for learning).
*   **Build Tool:** Apache Maven.
*   **External Libraries:** None! We are building this from the ground up to maximize learning.
*   **Design:** We aim to mimic essential JVM behaviors in small, understandable steps, focusing on clarity and educational value.

## 🤝 How to Contribute

We welcome contributions and ideas from everyone! This is a learning project, and the more we collaborate, the more we all learn. Here are a few ways you can get involved:

*   **Experiment with the code:** Try parsing your own compiled Java classes or simple programs. See how `tinyJVM` handles them!
*   **Explore the codebase:** Dive into key files like `ClassFileParser.java`, `BytecodeInterpreter.java`, and other components as they develop. Add comments or questions!
*   **Report issues:** If you find a bug, encounter something confusing, or have a suggestion, please open an issue on GitHub.
*   **Suggest new features or learning modules:** Is there a JVM feature you're curious about? Let us know!
*   **Tackle a `TODO`:** Look for `// TODO:` comments in the code. These are great starting points for contributions.
*   **Help implement more bytecode instructions:** The heart of the JVM is its instruction set. Expanding our interpreter is a fantastic way to learn.
*   **Clarify or clean up code:** For example, the project has a couple of auxiliary `Main.java` files (e.g., in `dev.jvm` and `com.tinyjvm.interpreter`). Contributions to clarify their purpose, integrate them into the main example flow, or remove them if redundant would be valuable.
*   **Improve documentation:** See a way to make the README clearer or add helpful comments in the code? Go for it!

Don't hesitate to ask questions or share your thoughts. Let's learn together!

## 📝 License

MIT License — because learning should be free and open for everyone. We believe in the power of open source to educate and inspire.
