package com.mikhail.lumen.compiler;

import com.mikhail.lumen.parser.AST.*;
import java.util.List;

public class CodeGenerator {
    public String generate(Program program) {
        StringBuilder code = new StringBuilder();

        code.append("import java.util.Scanner;\n\n");
        code.append("public class LumenProgram {\n");
        code.append("    private static final Scanner scanner = new Scanner(System.in);\n\n");

        code.append("    private static String stringify(Object value) {\n");
        code.append("        if (value instanceof Double number && number == Math.floor(number)) {\n");
        code.append("            return Long.toString(number.longValue());\n");
        code.append("        }\n");
        code.append("        return String.valueOf(value);\n");
        code.append("    }\n\n");

        code.append("    private static String input() {\n");
        code.append("        return scanner.nextLine();\n");
        code.append("    }\n\n");

        code.append("    private static String input(String prompt) {\n");
        code.append("        System.out.print(prompt);\n");
        code.append("        return scanner.nextLine();\n");
        code.append("    }\n\n");

        for (Function function : program.functions()) {
            generateFunction(code, function);
        }

        code.append("}\n");

        return code.toString();
    }

    private void generateFunction(StringBuilder code, Function function) {
        boolean isMain = function.name().equals("main");

        code.append("    public static ");
        code.append(javaType(function.returnType()));
        code.append(" ");

        if (isMain) {
            code.append("main(String[] args)");
        } else {
            code.append(function.name()).append("()");
        }

        code.append(" {\n");

        for (Statement statement : function.body()) {
            generateStatement(code, statement);
        }

        code.append("    }\n\n");
    }

    private void generateStatement(StringBuilder code, Statement statement) {
        if (statement instanceof VariableDeclaration variable) {
            code.append("        ");
            code.append(javaType(variable.type())).append(" ");
            code.append(variable.name()).append(" = ");
            code.append(expression(variable.value()));
            code.append(";\n");

            return;
        }

        if (statement instanceof PrintStatement print) {
            String method = print.newline() ? "println" : "print";

            code.append("        System.out.");
            code.append(method).append("(");

            List<Expression> arguments = print.arguments();

            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    code.append(" + ");
                }

                code.append("stringify(");
                code.append(expression(arguments.get(i)));
                code.append(")");
            }

            if (arguments.isEmpty()) {
                code.append("\"\"");
            }

            code.append(");\n");
        }
    }

    private String expression(Expression expression) {
        if (expression instanceof Literal literal) {
            Object value = literal.value();

            if (value instanceof String string) {
                return stringExpression(string);
            }

            if (value instanceof Boolean bool) {
                return bool.toString();
            }

            return value.toString();
        }

        if (expression instanceof Variable variable) {
            return variable.name();
        }

        if (expression instanceof Call call) {
            StringBuilder result = new StringBuilder(call.name());
            result.append("(");

            for (int i = 0; i < call.arguments().size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }

                result.append(expression(call.arguments().get(i)));
            }

            result.append(")");
            return result.toString();
        }

        throw new RuntimeException("Unknown expression: " + expression);
    }

    private String stringExpression(String value) {
        StringBuilder result = new StringBuilder();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (current == '$' && i + 1 < value.length()
                    && (Character.isLetter(value.charAt(i + 1)) || value.charAt(i + 1) == '_')) {

                if (!text.isEmpty()) {
                    result.append("\"").append(escape(text.toString())).append("\" + ");
                    text.setLength(0);
                }

                StringBuilder name = new StringBuilder();

                i++;

                while (i < value.length()
                        && (Character.isLetterOrDigit(value.charAt(i)) || value.charAt(i) == '_')) {
                    name.append(value.charAt(i));
                    i++;
                }

                i--;

                result.append("stringify(").append(name).append(") + ");
            } else {
                text.append(current);
            }
        }

        if (!text.isEmpty() || result.isEmpty()) {
            result.append("\"").append(escape(text.toString())).append("\"");
        } else {
            result.setLength(result.length() - 3);
        }

        return result.toString();
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String javaType(String type) {
        return switch (type) {
            case "number" -> "double";
            case "boolean" -> "boolean";
            case "string" -> "String";
            case "void" -> "void";
            default -> throw new RuntimeException("Unknown type: " + type);
        };
    }
}