
package com.mikhail.lumen.parser;

import com.mikhail.lumen.lexer.Token;
import com.mikhail.lumen.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.mikhail.lumen.parser.AST.*;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<Function> functions = new ArrayList<>();

        while (!check(TokenType.EOF)) {
            functions.add(function());
        }

        return new Program(functions);
    }

    private Function function() {
        consume(TokenType.FUNC, "Expected 'func'");

        String name = consume(
                TokenType.IDENTIFIER,
                "Expected function name"
        ).getLexeme();

        consume(TokenType.LEFT_PAREN, "Expected '('");
        consume(TokenType.RIGHT_PAREN, "Expected ')'");

        consume(TokenType.COLON, "Expected ':'");

        String returnType = type();

        consume(TokenType.LEFT_BRACE, "Expected '{'");

        List<Statement> body = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            body.add(statement());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}'");

        return new Function(name, returnType, body);
    }

    private Statement statement() {
        if (match(TokenType.NUMBER, TokenType.BOOLEAN, TokenType.STRING)) {
            return variableDeclaration(previous());
        }

        if (match(TokenType.PRINT, TokenType.PRINTLN)) {
            return printStatement(previous());
        }

        throw error(peek(), "Unexpected token: " + peek().getLexeme());
    }

    private VariableDeclaration variableDeclaration(Token typeToken) {
        String type = typeToken.getLexeme();

        String name = consume(
                TokenType.IDENTIFIER,
                "Expected variable name"
        ).getLexeme();

        consume(TokenType.EQUAL, "Expected '='");

        Expression value = expression();

        return new VariableDeclaration(type, name, value);
    }

    private PrintStatement printStatement(Token printToken) {
        boolean newline = printToken.getType() == TokenType.PRINTLN;

        consume(TokenType.LEFT_PAREN, "Expected '('");

        List<Expression> arguments = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')'");

        return new PrintStatement(newline, arguments);
    }

    private Expression expression() {
        if (match(TokenType.NUMBER_LITERAL)) {
            String value = previous().getLexeme();

            Object number = value.contains(".")
                    ? Double.parseDouble(value)
                    : Long.parseLong(value);

            return new Literal(number);
        }

        if (match(TokenType.STRING_LITERAL)) {
            return new Literal(previous().getLexeme());
        }

        if (match(TokenType.BOOLEAN_LITERAL)) {
            return new Literal(
                    Boolean.parseBoolean(previous().getLexeme())
            );
        }

        if (match(TokenType.INPUT)) {
            consume(TokenType.LEFT_PAREN, "Expected '('");

            List<Expression> arguments = new ArrayList<>();

            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    arguments.add(expression());
                } while (match(TokenType.COMMA));
            }

            consume(TokenType.RIGHT_PAREN, "Expected ')'");

            return new Call("input", arguments);
        }

        if (match(TokenType.IDENTIFIER)) {
            String name = previous().getLexeme();

            if (match(TokenType.LEFT_PAREN)) {
                List<Expression> arguments = new ArrayList<>();

                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(TokenType.COMMA));
                }

                consume(TokenType.RIGHT_PAREN, "Expected ')'");

                return new Call(name, arguments);
            }

            return new Variable(name);
        }

        throw error(peek(), "Expected expression");
    }

    private String type() {
        if (match(
                TokenType.NUMBER,
                TokenType.BOOLEAN,
                TokenType.STRING,
                TokenType.VOID
        )) {
            return previous().getLexeme();
        }

        throw error(peek(), "Expected type");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        return peek().getType() == type;
    }

    private Token advance() {
        if (!check(TokenType.EOF)) {
            position++;
        }

        return tokens.get(position - 1);
    }

    private Token previous() {
        return tokens.get(position - 1);
    }

    private Token peek() {
        return tokens.get(position);
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException(
                message + " at line " + token.getLine()
        );
    }
}