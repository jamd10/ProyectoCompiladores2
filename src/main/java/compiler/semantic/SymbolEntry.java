package compiler.semantic;

public class SymbolEntry {
    public enum Kind {
        VARIABLE,
        ARRAY,
        FUNCTION,
        PARAMETER
    }

    private final String id;
    private final String type;
    private final Kind kind;
    private final String scope;
    private final int level;
    private final int line;
    private final int column;
    private final String signature;

    public SymbolEntry(String id, String type, Kind kind, String scope, int level, int line, int column, String signature) {
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.scope = scope;
        this.level = level;
        this.line = line;
        this.column = column;
        this.signature = signature;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public String getScope() {
        return scope;
    }

    public int getLevel() {
        return level;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-12s %-12s %-25s %-8s %-10s %-20s",
                id, type, kind, scope, level, line + ":" + column, signature);
    }
}