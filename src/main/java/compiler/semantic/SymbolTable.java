package compiler.semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Stack<Map<String, SymbolInfo>> scopes;

    public SymbolTable() {
        scopes = new Stack<>();
        enterScope(); // Start with a global scope
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    public void define(String name, SymbolInfo info) {
        if (!scopes.isEmpty()) {
            scopes.peek().put(name, info);
        }
    }

    public SymbolInfo lookup(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, SymbolInfo> scope = scopes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // Not found
    }

    public static class SymbolInfo {
        private final String type;
        private final boolean isFunction;

        public SymbolInfo(String type, boolean isFunction) {
            this.type = type;
            this.isFunction = isFunction;
        }

        public String getType() {
            return type;
        }

        public boolean isFunction() {
            return isFunction;
        }
    }
}