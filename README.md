# ☕ tinyJVM

Ever wondered how Java actually runs your code behind the scenes?

This project is all about building a super simplified version of the Java Virtual Machine (JVM) — using Java itself. We’ll start by understanding `.class` files, parsing bytecode, and eventually simulating parts of the JVM like stack frames and threading.

The goal is to learn by doing, without using any external libraries like ASM or BCEL. 

Just plain Java.

---

## 📚 Learning by Building — Article Series

Each article in the series focuses on one major piece of the puzzle:

1. ✅ **Parsing `.class` Files Without Any Libraries**
2. 🚧 **Building a Bytecode Interpreter in Java**
3. 🔜 **Simulating JVM Stack Frames and Memory**
4. 🔜 **Creating a Custom Threading Model for the JVM**
5. 🔜 **Final Architecture + Reflections on Building a JVM**

---

## 🧩 JVM Feature Progress

Here’s what our custom JVM can do so far, and what’s coming up next!

| Feature                                | Status   | What it Means                                                       |
|----------------------------------------|----------|---------------------------------------------------------------------|
| 🔍 Read `.class` Files                 | ✅ Done   | Parses the raw bytes of Java `.class` files manually                |
| 🧠 Constant Pool Parsing               | ✅ Done   | Reads class names, method names, strings, and other metadata        |
| 🔐 Access Flags                        | 🚧 WIP   | Understands keywords like `public`, `final`, `abstract`             |
| 🧬 This Class & Super Class Info       | 🚧 WIP   | Figures out what class is being defined and what it extends         |
| 📑 Interfaces                          | 🚧 WIP   | Lists all interfaces a class implements                             |
| 📦 Fields                              | 🚧 WIP   | Parses variable declarations and their types                        |
| 🔧 Methods                             | 🚧 WIP   | Reads method signatures and other metadata                          |
| 🧾 Code Attribute (Bytecode)           | 🚧 WIP   | Grabs the actual bytecode from methods                              |
| 🔄 Bytecode Interpreter                |          | Starts running instructions like a real JVM                         |
---

## 🚀 Getting Started

Want to run it yourself?

1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/jvm-in-java.git
   cd jvm-in-java

2. Open it in IntelliJ:
   - Select Java Project (no need for Maven/Gradle right now)
   - Make sure you're using Java 17+
   

3. Run the Main.java class to see the class file parsing in action!

## 💡 Who This is For
1. You’ve just started learning Java and want to dig deeper
2. You’ve always been curious how the JVM works
3. You learn best by building real things from scratch

No advanced Java knowledge is required — we’ll explain everything as we go, step-by-step. You’ll come out the other side with serious systems thinking and byte-level confidence!


## 🔧 Tech Stack
- Java 17
- No external libraries (pure Java parsing)
- Designed to mimic JVM behavior in small, understandable steps

## 📝 License

MIT License — because learning should be free and open for everyone.
