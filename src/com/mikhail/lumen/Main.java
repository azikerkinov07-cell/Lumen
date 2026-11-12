
package com.mikhail.lumen;

import com.mikhail.lumen.lexer.Lexer;
import com.mikhail.lumen.parser.Parser;
import com.mikhail.lumen.parser.AST.Program;
import com.mikhail.lumen.semantic.SemanticAnalyzer;
import com.mikhail.lumen.compiler.CodeGenerator;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static final Path BUILD_DIR = Path.of("build");

    public static void main(String[] args) {
        if (args.length != 2) {
            usage();
            return;
        }

        String command = args[0];
        Path sourceFile = Path.of(args[1]);

        if (!command.equals("run") && !command.equals("compile")) {
            usage();
            return;
        }

        if (!Files.exists(sourceFile)) {
            System.err.println("File not found: " + sourceFile);
            System.exit(1);
        }

        try {
            compile(sourceFile, command.equals("run"));
        } catch (Exception exception) {
            System.err.println("Lumen error: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void compile(Path sourceFile, boolean run) throws Exception {
        String source = Files.readString(sourceFile);

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());

        Program program = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);

        Files.createDirectories(BUILD_DIR);

        String javaSource = new CodeGenerator().generate(program);

        Path javaFile = BUILD_DIR.resolve("LumenProgram.java");
        Files.writeString(javaFile, javaSource);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new RuntimeException("JDK compiler not found. Install a JDK.");
        }

        int result = compiler.run(
                null,
                System.out,
                System.err,
                "-d",
                BUILD_DIR.toAbsolutePath().toString(),
                javaFile.toAbsolutePath().toString()
        );

        if (result != 0) {
            throw new RuntimeException("Java compilation failed.");
        }

        System.out.println("Lumen compilation successful.");

        if (run) {
            Process process = new ProcessBuilder(
                    "java",
                    "-cp",
                    BUILD_DIR.toAbsolutePath().toString(),
                    "LumenProgram"
            )
                    .inheritIO()
                    .start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(
                        "Program exited with code " + exitCode
                );
            }
        }
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  lumen run <file.lm>");
        System.out.println("  lumen compile <file.lm>");
    }
}