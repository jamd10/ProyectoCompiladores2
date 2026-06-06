package compiler.semantic;

import compiler.parser.MiniCBaseVisitor;
import compiler.parser.MiniCParser;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableBuilder extends MiniCBaseVisitor<Void> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<String> errors = new ArrayList<>();
    private int blockCounter = 0;

    public SymbolTableBuilder() {
        declareRuntimeFunction("print_int", "void print_int(int x)");
        declareRuntimeFunction("print_char", "void print_char(char c)");
        declareRuntimeFunction("print_bool", "void print_bool(bool b)");
        declareRuntimeFunction("print_str", "void print_str(string s)");
        declareRuntimeFunction("println", "void println()");
        declareRuntimeFunction("read_int", "int read_int()");
        declareRuntimeFunction("read_char", "char read_char()");
        declareRuntimeFunction("read_str", "void read_str(string buf, int maxlen)");
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public Void visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx) {
        String functionName = ctx.Identifier().getText();
        String returnType = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);
        String signature = buildFunctionSignature(ctx);

        Token token = ctx.Identifier().getSymbol();

        SymbolEntry entry = new SymbolEntry(
                functionName,
                returnType,
                SymbolEntry.Kind.FUNCTION,
                symbolTable.getCurrentScope(),
                symbolTable.getCurrentLevel(),
                token.getLine(),
                token.getCharPositionInLine(),
                signature
        );

        if (!symbolTable.declare(entry)) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - funcion redeclarada: " + functionName);
        }

        symbolTable.enterScope(functionName);

        if (ctx.parameterList() != null) {
            for (MiniCParser.ParameterContext parameter : ctx.parameterList().parameter()) {
                declareParameter(parameter);
            }
        }

        visit(ctx.compoundStatement());

        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String baseType = ctx.typeSpecifier().getText();

        for (MiniCParser.InitDeclaratorContext declarator : ctx.initDeclaratorList().initDeclarator()) {
            String id = declarator.Identifier().getText();
            String type = buildType(baseType, declarator.pointer() != null);
            Token token = declarator.Identifier().getSymbol();

            SymbolEntry.Kind kind = declarator.arraySuffix().isEmpty()
                    ? SymbolEntry.Kind.VARIABLE
                    : SymbolEntry.Kind.ARRAY;

            if (!declarator.arraySuffix().isEmpty()) {
                type += buildArraySuffix(declarator);
            }

            SymbolEntry entry = new SymbolEntry(
                    id,
                    type,
                    kind,
                    symbolTable.getCurrentScope(),
                    symbolTable.getCurrentLevel(),
                    token.getLine(),
                    token.getCharPositionInLine(),
                    type + " " + id
            );

            if (!symbolTable.declare(entry)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - identificador redeclarado en el mismo ambito: " + id);
            }

            if (declarator.expr() != null) {
                visit(declarator.expr());
            }
        }

        return null;
    }

    @Override
    public Void visitCompoundStatement(MiniCParser.CompoundStatementContext ctx) {
        symbolTable.enterScope("block" + (++blockCounter));

        for (MiniCParser.BlockItemContext item : ctx.blockItem()) {
            visit(item);
        }

        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitLvalue(MiniCParser.LvalueContext ctx) {
        String id = ctx.Identifier().getText();

        if (symbolTable.lookup(id) == null) {
            Token token = ctx.Identifier().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - identificador no declarado: " + id);
        }

        return visitChildren(ctx);
    }

    @Override
    public Void visitCall(MiniCParser.CallContext ctx) {
        String id = ctx.Identifier().getText();

        SymbolEntry entry = symbolTable.lookup(id);

        if (entry == null) {
            Token token = ctx.Identifier().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - funcion no declarada: " + id);
        } else if (entry.getKind() != SymbolEntry.Kind.FUNCTION) {
            Token token = ctx.Identifier().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - el identificador no es una funcion: " + id);
        }

        return visitChildren(ctx);
    }

    private void declareRuntimeFunction(String id, String signature) {
        SymbolEntry entry = new SymbolEntry(
                id,
                "runtime",
                SymbolEntry.Kind.FUNCTION,
                symbolTable.getCurrentScope(),
                symbolTable.getCurrentLevel(),
                0,
                0,
                signature
        );

        symbolTable.declare(entry);
    }

    private void declareParameter(MiniCParser.ParameterContext ctx) {
        String id = ctx.Identifier().getText();
        String type = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);
        Token token = ctx.Identifier().getSymbol();

        if (!ctx.arraySuffix().isEmpty()) {
            type += buildArraySuffix(ctx);
        }

        SymbolEntry entry = new SymbolEntry(
                id,
                type,
                SymbolEntry.Kind.PARAMETER,
                symbolTable.getCurrentScope(),
                symbolTable.getCurrentLevel(),
                token.getLine(),
                token.getCharPositionInLine(),
                type + " " + id
        );

        if (!symbolTable.declare(entry)) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - parametro redeclarado: " + id);
        }
    }

    private String buildFunctionSignature(MiniCParser.FunctionDefinitionContext ctx) {
        StringBuilder signature = new StringBuilder();

        signature.append(buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null));
        signature.append(" ");
        signature.append(ctx.Identifier().getText());
        signature.append("(");

        if (ctx.parameterList() != null) {
            for (int i = 0; i < ctx.parameterList().parameter().size(); i++) {
                MiniCParser.ParameterContext parameter = ctx.parameterList().parameter(i);

                if (i > 0) {
                    signature.append(", ");
                }

                signature.append(buildType(parameter.typeSpecifier().getText(), parameter.pointer() != null));
                signature.append(" ");
                signature.append(parameter.Identifier().getText());
            }
        }

        signature.append(")");
        return signature.toString();
    }

    private String buildType(String baseType, boolean isPointer) {
        if (isPointer) {
            return baseType + "*";
        }

        return baseType;
    }

    private String buildArraySuffix(MiniCParser.InitDeclaratorContext ctx) {
        StringBuilder suffix = new StringBuilder();

        for (MiniCParser.ArraySuffixContext arraySuffix : ctx.arraySuffix()) {
            suffix.append("[");
            suffix.append(arraySuffix.IntegerConst().getText());
            suffix.append("]");
        }

        return suffix.toString();
    }

    private String buildArraySuffix(MiniCParser.ParameterContext ctx) {
        StringBuilder suffix = new StringBuilder();

        for (MiniCParser.ArraySuffixContext arraySuffix : ctx.arraySuffix()) {
            suffix.append("[");
            suffix.append(arraySuffix.IntegerConst().getText());
            suffix.append("]");
        }

        return suffix.toString();
    }
}