package com.mikhail.lumen.semantic;

import com.mikhail.lumen.parser.AST;
import com.mikhail.lumen.parser.AST.*;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {
    private final Map<String, String> functions = new HashMap<>();
    private SymbolTable symbols;

    public void analyze(Program program) {
        functions.clear();

        for (Function function : program.functions()) {
            if (functions.containsKey(function.name())) {
                throw new SemanticError(
                        "Function '" + function.name() + "' is already declared."
                );
            }

            functions.put(function.name(), function.returnType());
        }

        for (Function function : program.functions()) {
            analyzeFunction(function);
        }
    }

    private void analyzeFunction(Function function) {
        symbols = new SymbolTable();

        for (Statement statement : function.body()) {
            analyzeStatement(statement);
        }
    }

    private void analyzeStatement(Statement statement) {
        if (statement instanceof VariableDeclaration variable) {
            String name = variable.name();
            String declaredType = variable.type();
            String actualType = analyzeExpression(variable.value());

            if (!declaredType.equals(actualType)) {
                throw new SemanticError(
                        "Cannot assign " + actualType + " to " + declaredType
                                + " variable '" + name + "'."
                );
            }

            symbols.define(name, declaredType);
            return;
        }

        if (statement instanceof PrintStatement print) {
            for (Expression argument : print.arguments()) {
                analyzeExpression(argument);
            }

            return;
        }

        throw new SemanticError(
                "Unknown statement: " + statement.getClass().getSimpleName()
        );
    }

    private String analyzeExpression(Expression expression) {
        if (expression instanceof Literal literal) {
            Object value = literal.value();

            if (value instanceof Number) {
                return "number";
            }

            if (value instanceof Boolean) {
                return "boolean";
            }

            if (value instanceof String string) {
                analyzeInterpolation(string);
                return "string";
            }
        }

        if (expression instanceof Variable variable) {
            return symbols.resolve(variable.name());
        }

        if (expression instanceof Call call) {
            return analyzeCall(call);
        }

        throw new SemanticError(
                "Unknown expression: " + expression.getClass().getSimpleName()
        );
    }

    private String analyzeCall(Call call) {
        String name = call.name();

        if (name.equals("input")) {
            if (call.arguments().size() > 1) {
                throw new SemanticError(
                        "input() accepts at most one argument."
                );
            }

            if (!call.arguments().isEmpty()) {
                String argumentType = analyzeExpression(call.arguments().get(0));

                if (!argumentType.equals("string")) {
                    throw new SemanticError(
                            "input() prompt must be a string."
                    );
                }
            }

            return "string";
        }

        String returnType = functions.get(name);

        if (returnType == null) {
            throw new SemanticError(
                    "Undefined function '" + name + "'."
            );
        }

        if (!call.arguments().isEmpty()) {
            throw new SemanticError(
                    "Function '" + name + "' does not accept arguments yet."
            );
        }

        if (returnType.equals("void")) {
            throw new SemanticError(
                    "Void function '" + name + "' cannot be used as an expression."
            );
        }

        return returnType;
    }

    private void analyzeInterpolation(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '$' || i + 1 >= value.length()) {
                continue;
            }

            int start = i + 1;
            int end = start;

            char first = value.charAt(start);

            if (!Character.isLetter(first) && first != '_') {
                continue;
            }

            while (end < value.length()) {
                char current = value.charAt(end);

                if (!Character.isLetterOrDigit(current) && current != '_') {
                    break;
                }

                end++;
            }

            String name = value.substring(start, end);
            symbols.resolve(name);

            i = end - 1;
        }
    }
}