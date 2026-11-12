package com.mikhail.lumen.semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, String> symbols = new HashMap<>();

    public void define(String name, String type) {
        if (symbols.containsKey(name)) {
            throw new SemanticError("Variable '" + name + "' is already declared.");
        }

        symbols.put(name, type);
    }

    public String resolve(String name) {
        String type = symbols.get(name);

        if (type == null) {
            throw new SemanticError("Undefined variable '" + name + "'.");
        }

        return type;
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
}