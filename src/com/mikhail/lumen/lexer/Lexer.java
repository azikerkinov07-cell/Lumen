package com.mikhail.lumen.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int position = 0;
    private int line = 1;

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "func", TokenType.FUNC,
            "number", TokenType.NUMBER,
            "boolean", TokenType.BOOLEAN,
            "string", TokenType.STRING,
            "void", TokenType.VOID,
            "print", TokenType.PRINT,
            "println", TokenType.PRINTLN,
            "input", TokenType.INPUT,
            "true", TokenType.BOOLEAN_LITERAL,
            "false", TokenType.BOOLEAN_LITERAL
    );

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            char current = advance();

            if (Character.isWhitespace(current)) {
                if (current == '\n') line++;
                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                identifier(current);
                continue;
            }

            if (Character.isDigit(current)) {
                number(current);
                continue;
            }

            switch (current) {
                case '(' -> addToken(TokenType.LEFT_PAREN, "(");
                case ')' -> addToken(TokenType.RIGHT_PAREN, ")");
                case '{' -> addToken(TokenType.LEFT_BRACE, "{");
                case '}' -> addToken(TokenType.RIGHT_BRACE, "}");
                case ':' -> addToken(TokenType.COLON, ":");
                case ',' -> addToken(TokenType.COMMA, ",");
                case '=' -> addToken(TokenType.EQUAL, "=");
                case '"' -> string();
                default -> throw new RuntimeException(
                        "Unexpected character '" + current + "' at line " + line
                );
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void identifier(char first) {
        StringBuilder value = new StringBuilder().append(first);

        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            value.append(advance());
        }

        String lexeme = value.toString();
        TokenType type = KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER);

        addToken(type, lexeme);
    }

    private void number(char first) {
        StringBuilder value = new StringBuilder().append(first);

        while (!isAtEnd() && Character.isDigit(peek())) {
            value.append(advance());
        }

        if (!isAtEnd() && peek() == '.' && position + 1 < source.length()
                && Character.isDigit(source.charAt(position + 1))) {
            value.append(advance());

            while (!isAtEnd() && Character.isDigit(peek())) {
                value.append(advance());
            }
        }

        addToken(TokenType.NUMBER_LITERAL, value.toString());
    }

    private void string() {
        StringBuilder value = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                throw new RuntimeException("Unterminated string at line " + line);
            }

            value.append(advance());
        }

        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }

        advance();
        addToken(TokenType.STRING_LITERAL, value.toString());
    }

    private char advance() {
        return source.charAt(position++);
    }

    private char peek() {
        return source.charAt(position);
    }

    private boolean isAtEnd() {
        return position >= source.length();
    }

    private void addToken(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, line));
    }
}