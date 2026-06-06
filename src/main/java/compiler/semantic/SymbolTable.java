package compiler.semantic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SymbolTable {
    private final List<SymbolEntry> symbols = new ArrayList<>();
    private final Deque<String> scopeStack = new ArrayDeque<>();

    public SymbolTable() {
        scopeStack.addLast("global");
    }

    public void enterScope(String scopeName) {
        scopeStack.addLast(scopeName);
    }

    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.removeLast();
        }
    }

    public String getCurrentScope() {
        return String.join("/", scopeStack);
    }

    public int getCurrentLevel() {
        return scopeStack.size() - 1;
    }

    public boolean declare(SymbolEntry entry) {
        if (isDeclaredInCurrentScope(entry.getId())) {
            return false;
        }

        symbols.add(entry);
        return true;
    }

    public boolean isDeclaredInCurrentScope(String id) {
        String currentScope = getCurrentScope();

        for (SymbolEntry entry : symbols) {
            if (entry.getId().equals(id) && entry.getScope().equals(currentScope)) {
                return true;
            }
        }

        return false;
    }

    public SymbolEntry lookup(String id) {
        List<String> scopes = new ArrayList<>(scopeStack);

        for (int i = scopes.size(); i >= 1; i--) {
            String scope = String.join("/", scopes.subList(0, i));

            for (SymbolEntry entry : symbols) {
                if (entry.getId().equals(id) && entry.getScope().equals(scope)) {
                    return entry;
                }
            }
        }

        return null;
    }

    public List<SymbolEntry> getSymbols() {
        return symbols;
    }

    public void print() {
        System.out.println("\n--------------------------------- TABLA DE SIMBOLOS ---------------------------------");
        System.out.printf("%-15s %-12s %-12s %-25s %-8s %-10s %-20s%n",
                "ID", "TIPO", "CLASE", "AMBITO", "NIVEL", "LINEA", "FIRMA");

        for (SymbolEntry entry : symbols) {
            System.out.println(entry);
        }

        System.out.println("-------------------------------------------------------------------------------------");
    }
}