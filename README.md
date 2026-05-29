# Mini-C Compiler

This project implements a compiler for Mini-C, a subset of the C programming language, using ANTLR4 and Java. The compiler translates Mini-C code into MIPS32 assembly code, adhering to the ABI O32 conventions.

## Project Structure

```
mini-c-compiler
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── compiler
│   │   │   │   ├── Main.java               # Entry point of the compiler application
│   │   │   │   ├── MiniC.g4                # ANTLR4 grammar definition for Mini-C
│   │   │   │   ├── ast
│   │   │   │   │   └── AstNodes.java       # Abstract Syntax Tree (AST) nodes
│   │   │   │   ├── parser
│   │   │   │   │   └── MiniCParserListener.java # Listener for handling parse tree events
│   │   │   │   ├── semantic
│   │   │   │   │   ├── SymbolTable.java     # Manages the symbol table
│   │   │   │   │   └── TypeChecker.java     # Performs semantic analysis
│   │   │   │   ├── ir
│   │   │   │   │   └── TacGenerator.java    # Generates intermediate representation (IR)
│   │   │   │   └── backend
│   │   │   │       └── MipsGenerator.java    # Translates TAC to MIPS32 assembly
│   │   └── resources
│   │       └── examples
│   │           ├── correct                  # Example Mini-C programs (correct)
│   │           └── errors                   # Example Mini-C programs (with errors)
│   └── test
│       └── java
│           └── compiler
│               ├── LexerParserTest.java     # Unit tests for lexer and parser
│               └── SemanticTest.java        # Unit tests for semantic analysis
├── build.gradle                              # Gradle build configuration
├── settings.gradle                           # Gradle settings
└── README.md                                 # Project documentation
```

## Features

- **Lexical Analysis**: Tokenizes Mini-C source code using ANTLR4.
- **Syntax Analysis**: Parses tokens and constructs an Abstract Syntax Tree (AST).
- **Semantic Analysis**: Checks for type compatibility and manages variable scopes.
- **Intermediate Representation**: Generates three-address code (TAC) from the AST.
- **Code Generation**: Translates TAC into MIPS32 assembly code for execution in QtSPIM/MARS.
- **Error Handling**: Reports lexical, syntactic, and semantic errors with detailed messages.

## Setup Instructions

1. **Clone the repository**:
   ```
   git clone <repository-url>
   cd mini-c-compiler
   ```

2. **Build the project**:
   Ensure you have Gradle installed, then run:
   ```
   ./gradlew build
   ```

3. **Run the compiler**:
   Use the following command to compile a Mini-C program:
   ```
   java -cp build/libs/mini-c-compiler.jar compiler.Main <input.mc> -S -o <output.s>
   ```

## Usage

- Place your Mini-C source files in the `src/main/resources/examples/correct` or `src/main/resources/examples/errors` directories for testing.
- Use the provided unit tests in `src/test/java/compiler` to verify the functionality of the compiler components.

## Examples

Refer to the `examples` directory for sample Mini-C programs and their expected outputs.