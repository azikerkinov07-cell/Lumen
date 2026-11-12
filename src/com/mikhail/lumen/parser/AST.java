package com.mikhail.lumen.parser;

import java.util.List;

public class AST {
    public interface Node {}

    public record Program(List<Function> functions) implements Node {}

    public record Function(String name, String returnType, List<Statement> body) implements Node {}

    public interface Statement extends Node {}

    public record VariableDeclaration(String type, String name, Expression value) implements Statement {}

    public record PrintStatement(boolean newline, List<Expression> arguments) implements Statement {}

    public interface Expression extends Node {}

    public record Literal(Object value) implements Expression {}

    public record Variable(String name) implements Expression {}

    public record Call(String name, List<Expression> arguments) implements Expression {}
}