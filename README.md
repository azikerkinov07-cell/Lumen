# Lumen

**Lumen** is a custom programming language written in Java.

It is designed with a simple syntax, readable code, and a lightweight compiler pipeline.

## Features

- Custom syntax and keywords
- Static type declarations
- Functions
- Variables
- String interpolation
- Built-in `print` and `println`
- Semantic analysis
- Lexer and parser
- Java-based compilation pipeline

## Example

```java
func main(): void {
    number age = 12
    string name = "Mikhail"
    boolean active = true

    println("Name: $name. Age: $age. Active: $active.")
}
```

## Requirements

- Java JDK 17 or later
- Linux, Windows, or macOS

## Build

Compile the Lumen compiler:

```bash
javac -d build $(find src -name "*.java")
```

## Usage

Run a Lumen program:

```bash
java -cp build com.mikhail.lumen.Main run examples/main.lm
```

Compile without running:

```bash
java -cp build com.mikhail.lumen.Main compile examples/main.lm
```

## How It Works

Lumen currently uses a Java-based compilation pipeline:

1. **Lexer** — converts source code into tokens.
2. **Parser** — builds an Abstract Syntax Tree (AST).
3. **Semantic Analyzer** — checks declarations and types.
4. **Code Generator** — generates Java source code.
5. **Java Compiler** — compiles generated Java into JVM bytecode.

## Project Status

Lumen is an experimental programming language under active development.

Some language features and compiler capabilities are still being implemented.