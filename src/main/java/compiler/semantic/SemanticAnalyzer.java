package compiler.semantic;

import compiler.parser.MiniCBaseVisitor;
import compiler.parser.MiniCParser;
import org.antlr.v4.runtime.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer extends MiniCBaseVisitor<String> {
    private final List<String> errors = new ArrayList<>();
    private final Deque<Map<String, SemanticSymbol>> scopes = new ArrayDeque<>();
    private final Deque<String> scopeNames = new ArrayDeque<>();
    private int blockCounter = 0;
    private String currentFunctionName = "";
    private String currentFunctionReturnType = "void";
    private boolean currentFunctionHasReturn = false;

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String visitProgram(MiniCParser.ProgramContext ctx) {
        enterScope("global");

        declareRuntimeFunctions();

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            if (declaration.functionDefinition() != null) {
                declareFunction(declaration.functionDefinition());
            }
        }

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            if (declaration.declaration() != null) {
                visit(declaration.declaration());
            }
        }

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            if (declaration.functionDefinition() != null) {
                visit(declaration.functionDefinition());
            }
        }

        exitScope();

        return "void";
    }

    @Override
    public String visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx) {
        String previousFunctionName = currentFunctionName;
        String previousReturnType = currentFunctionReturnType;
        boolean previousFunctionHasReturn = currentFunctionHasReturn;

        currentFunctionName = ctx.Identifier().getText();
        currentFunctionReturnType = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);
        currentFunctionHasReturn = false;

        enterScope(ctx.Identifier().getText());

        if (ctx.parameterList() != null) {
            for (MiniCParser.ParameterContext parameter : ctx.parameterList().parameter()) {
                declareParameter(parameter);
            }
        }

        visit(ctx.compoundStatement());

        if (!currentFunctionReturnType.equals("void") && !currentFunctionHasReturn) {
            Token token = ctx.Identifier().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - la funcion " + currentFunctionName
                    + " debe retornar un valor de tipo " + currentFunctionReturnType);
        }

        exitScope();

        currentFunctionName = previousFunctionName;
        currentFunctionReturnType = previousReturnType;
        currentFunctionHasReturn = previousFunctionHasReturn;

        return "void";
    }

    @Override
    public String visitCompoundStatement(MiniCParser.CompoundStatementContext ctx) {
        enterScope("block" + (++blockCounter));

        for (MiniCParser.BlockItemContext item : ctx.blockItem()) {
            visit(item);
        }

        exitScope();

        return "void";
    }

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String baseType = ctx.typeSpecifier().getText();

        for (MiniCParser.InitDeclaratorContext declarator : ctx.initDeclaratorList().initDeclarator()) {
            String id = declarator.Identifier().getText();
            String type = buildType(baseType, declarator.pointer() != null);
            Token token = declarator.Identifier().getSymbol();

            int arrayDimensions = declarator.arraySuffix().size();

            if (arrayDimensions > 0) {
                type += buildArraySuffix(declarator);
            }

            SemanticSymbol symbol = new SemanticSymbol(
                    id,
                    type,
                    arrayDimensions > 0 ? "ARRAY" : "VARIABLE",
                    getCurrentScope(),
                    token.getLine(),
                    token.getCharPositionInLine(),
                    arrayDimensions,
                    baseType,
                    new ArrayList<>()
            );

            if (!declare(symbol)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - identificador redeclarado en el mismo ambito: " + id);
            }

            if (declarator.expr() != null) {
                String exprType = visit(declarator.expr());

                if (!isAssignable(type, exprType)) {
                    errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                            + " - no se puede asignar " + exprType + " a " + type);
                }
            }
        }

        return "void";
    }

    @Override
    public String visitReturnStatement(MiniCParser.ReturnStatementContext ctx) {
        currentFunctionHasReturn = true;

        String returnType = "void";

        if (ctx.expr() != null) {
            returnType = visit(ctx.expr());
        }

        Token token = ctx.RETURN().getSymbol();

        if (currentFunctionReturnType.equals("void") && ctx.expr() != null) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - una funcion void no debe retornar un valor");
            return returnType;
        }

        if (!currentFunctionReturnType.equals("void") && ctx.expr() == null) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - una funcion " + currentFunctionReturnType + " debe retornar un valor");
            return returnType;
        }

        if (!isAssignable(currentFunctionReturnType, returnType)) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - return incompatible. Se esperaba " + currentFunctionReturnType + " y se obtuvo " + returnType);
        }

        return returnType;
    }

    @Override
    public String visitIfStatement(MiniCParser.IfStatementContext ctx) {
        String conditionType = visit(ctx.expr());

        if (!isConditionType(conditionType)) {
            Token token = ctx.IF().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - la condicion del if debe ser bool o int");
        }

        for (MiniCParser.StatementContext statement : ctx.statement()) {
            visit(statement);
        }

        return "void";
    }

    @Override
    public String visitWhileStatement(MiniCParser.WhileStatementContext ctx) {
        String conditionType = visit(ctx.expr());

        if (!isConditionType(conditionType)) {
            Token token = ctx.WHILE().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - la condicion del while debe ser bool o int");
        }

        visit(ctx.statement());

        return "void";
    }

    @Override
    public String visitForStatement(MiniCParser.ForStatementContext ctx) {
        for (MiniCParser.ExprContext expr : ctx.expr()) {
            visit(expr);
        }

        visit(ctx.statement());

        return "void";
    }

    @Override
    public String visitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx) {
        visit(ctx.statement());

        String conditionType = visit(ctx.expr());

        if (!isConditionType(conditionType)) {
            Token token = ctx.DO().getSymbol();
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - la condicion del do while debe ser bool o int");
        }

        return "void";
    }

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() != null) {
            String leftType = visit(ctx.lvalue());
            String rightType = visit(ctx.assignmentExpr());

            if (!isAssignable(leftType, rightType)) {
                Token token = ctx.lvalue().Identifier().getSymbol();
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - no se puede asignar " + rightType + " a " + leftType);
            }

            return leftType;
        }

        return visit(ctx.logicalOrExpr());
    }

    @Override
    public String visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        String resultType = visit(ctx.logicalAndExpr(0));

        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            String rightType = visit(ctx.logicalAndExpr(i));

            if (!isConditionType(resultType) || !isConditionType(rightType)) {
                errors.add("Error semantico - operador || requiere operandos bool o int");
            }

            resultType = "bool";
        }

        return resultType;
    }

    @Override
    public String visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        String resultType = visit(ctx.equalityExpr(0));

        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            String rightType = visit(ctx.equalityExpr(i));

            if (!isConditionType(resultType) || !isConditionType(rightType)) {
                errors.add("Error semantico - operador && requiere operandos bool o int");
            }

            resultType = "bool";
        }

        return resultType;
    }

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        String resultType = visit(ctx.relationalExpr(0));

        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            String rightType = visit(ctx.relationalExpr(i));

            if (!areComparable(resultType, rightType)) {
                errors.add("Error semantico - comparacion incompatible entre " + resultType + " y " + rightType);
            }

            resultType = "bool";
        }

        return resultType;
    }

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        String resultType = visit(ctx.additiveExpr(0));

        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            String rightType = visit(ctx.additiveExpr(i));

            if (!isNumeric(resultType) || !isNumeric(rightType)) {
                errors.add("Error semantico - operador relacional requiere operandos numericos");
            }

            resultType = "bool";
        }

        return resultType;
    }

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        String resultType = visit(ctx.multiplicativeExpr(0));

        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String rightType = visit(ctx.multiplicativeExpr(i));

            if (!isNumeric(resultType) || !isNumeric(rightType)) {
                errors.add("Error semantico - operador aritmetico requiere operandos numericos");
            }

            resultType = "int";
        }

        return resultType;
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        String resultType = visit(ctx.unaryExpr(0));

        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String rightType = visit(ctx.unaryExpr(i));

            if (!isNumeric(resultType) || !isNumeric(rightType)) {
                errors.add("Error semantico - operador aritmetico requiere operandos numericos");
            }

            resultType = "int";
        }

        return resultType;
    }

    @Override
    public String visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) {
            return visit(ctx.primary());
        }

        String operator = ctx.getChild(0).getText();
        String type = visit(ctx.unaryExpr());
        Token token = ctx.getStart();

        if (operator.equals("!")) {
            if (!isConditionType(type)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - operador ! requiere bool o int");
            }

            return "bool";
        }

        if (operator.equals("-")) {
            if (!isNumeric(type)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - operador - requiere tipo numerico");
            }

            return "int";
        }

        if (operator.equals("&")) {
            if (!isAddressable(ctx.unaryExpr())) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - operador & requiere una variable, parametro o elemento de arreglo");
            }

            if (type.equals("unknown")) {
                return "unknown";
            }

            return type + "*";
        }

        if (operator.equals("*")) {
            if (!isPointer(type)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - operador * requiere un puntero y se obtuvo " + type);
                return "unknown";
            }

            return dereferenceType(type);
        }

        return type;
    }

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.IntegerConst() != null) {
            return "int";
        }

        if (ctx.CharConst() != null) {
            return "char";
        }

        if (ctx.StringLiteral() != null) {
            return "string";
        }

        if (ctx.TRUE() != null || ctx.FALSE() != null) {
            return "bool";
        }

        if (ctx.expr() != null) {
            return visit(ctx.expr());
        }

        if (ctx.call() != null) {
            return visit(ctx.call());
        }

        if (ctx.lvalue() != null) {
            return visit(ctx.lvalue());
        }

        return "unknown";
    }

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String id = ctx.Identifier().getText();
        SemanticSymbol symbol = lookup(id);
        Token token = ctx.Identifier().getSymbol();

        if (symbol == null) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - identificador no declarado: " + id);
            return "unknown";
        }

        if (symbol.kind.equals("FUNCTION")) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - una funcion no puede usarse como variable: " + id);
            return symbol.type;
        }

        int indexCount = ctx.expr().size();

        if (indexCount > 0 && !symbol.kind.equals("ARRAY")) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - el identificador no es un arreglo: " + id);
        }

        if (indexCount > symbol.arrayDimensions) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - demasiados indices para el arreglo: " + id);
        }

        for (MiniCParser.ExprContext indexExpr : ctx.expr()) {
            String indexType = visit(indexExpr);

            if (!indexType.equals("int")) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - el indice del arreglo debe ser int");
            }
        }

        if (indexCount == 0) {
            return symbol.type;
        }

        if (indexCount >= symbol.arrayDimensions) {
            return symbol.baseType;
        }

        return symbol.type;
    }

    @Override
    public String visitCall(MiniCParser.CallContext ctx) {
        String id = ctx.Identifier().getText();
        SemanticSymbol symbol = lookup(id);
        Token token = ctx.Identifier().getSymbol();

        if (symbol == null) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - funcion no declarada: " + id);
            return "unknown";
        }

        if (!symbol.kind.equals("FUNCTION")) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - el identificador no es una funcion: " + id);
            return "unknown";
        }

        List<MiniCParser.ExprContext> args = ctx.argumentList() == null
                ? new ArrayList<>()
                : ctx.argumentList().expr();

        if (args.size() != symbol.parameterTypes.size()) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - cantidad incorrecta de parametros en llamada a " + id
                    + ". Se esperaban " + symbol.parameterTypes.size()
                    + " y se recibieron " + args.size());
        }

        int limit = Math.min(args.size(), symbol.parameterTypes.size());

        for (int i = 0; i < limit; i++) {
            String argumentType = visit(args.get(i));
            String expectedType = symbol.parameterTypes.get(i);

            if (!isAssignable(expectedType, argumentType)) {
                errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - parametro " + (i + 1) + " incompatible en llamada a " + id
                        + ". Se esperaba " + expectedType + " y se obtuvo " + argumentType);
            }
        }

        return symbol.type;
    }

    private void declareFunction(MiniCParser.FunctionDefinitionContext ctx) {
        String id = ctx.Identifier().getText();
        String returnType = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);
        Token token = ctx.Identifier().getSymbol();

        List<String> parameterTypes = new ArrayList<>();

        if (ctx.parameterList() != null) {
            for (MiniCParser.ParameterContext parameter : ctx.parameterList().parameter()) {
                String type = buildType(parameter.typeSpecifier().getText(), parameter.pointer() != null);

                if (!parameter.arraySuffix().isEmpty()) {
                    type += buildArraySuffix(parameter);
                }

                parameterTypes.add(type);
            }
        }

        SemanticSymbol symbol = new SemanticSymbol(
                id,
                returnType,
                "FUNCTION",
                getCurrentScope(),
                token.getLine(),
                token.getCharPositionInLine(),
                0,
                returnType,
                parameterTypes
        );

        if (!declare(symbol)) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - funcion redeclarada: " + id);
        }
    }

    private void declareParameter(MiniCParser.ParameterContext ctx) {
        String id = ctx.Identifier().getText();
        String type = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);
        Token token = ctx.Identifier().getSymbol();

        int arrayDimensions = ctx.arraySuffix().size();

        if (arrayDimensions > 0) {
            type += buildArraySuffix(ctx);
        }

        SemanticSymbol symbol = new SemanticSymbol(
                id,
                type,
                arrayDimensions > 0 ? "ARRAY" : "PARAMETER",
                getCurrentScope(),
                token.getLine(),
                token.getCharPositionInLine(),
                arrayDimensions,
                ctx.typeSpecifier().getText(),
                new ArrayList<>()
        );

        if (!declare(symbol)) {
            errors.add("Error semantico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                    + " - parametro redeclarado: " + id);
        }
    }

    private void declareRuntimeFunctions() {
        declareRuntimeFunction("print_int", "void", "int");
        declareRuntimeFunction("print_char", "void", "char");
        declareRuntimeFunction("print_bool", "void", "bool");
        declareRuntimeFunction("print_str", "void", "string");
        declareRuntimeFunction("println", "void");
        declareRuntimeFunction("read_int", "int");
        declareRuntimeFunction("read_char", "char");
        declareRuntimeFunction("read_str", "void", "string", "int");
    }

    private void declareRuntimeFunction(String id, String returnType, String... parameterTypes) {
        List<String> params = new ArrayList<>();

        for (String parameterType : parameterTypes) {
            params.add(parameterType);
        }

        declare(new SemanticSymbol(
                id,
                returnType,
                "FUNCTION",
                getCurrentScope(),
                0,
                0,
                0,
                returnType,
                params
        ));
    }

    private boolean declare(SemanticSymbol symbol) {
        Map<String, SemanticSymbol> currentScope = scopes.peekLast();

        if (currentScope.containsKey(symbol.id)) {
            return false;
        }

        currentScope.put(symbol.id, symbol);
        return true;
    }

    @SuppressWarnings("unchecked")
    private SemanticSymbol lookup(String id) {
        Object[] scopeArray = scopes.toArray();

        for (int i = scopeArray.length - 1; i >= 0; i--) {
            Map<String, SemanticSymbol> scope = (Map<String, SemanticSymbol>) scopeArray[i];

            if (scope.containsKey(id)) {
                return scope.get(id);
            }
        }

        return null;
    }

    private void enterScope(String name) {
        scopes.addLast(new LinkedHashMap<>());
        scopeNames.addLast(name);
    }

    private void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.removeLast();
        }

        if (!scopeNames.isEmpty()) {
            scopeNames.removeLast();
        }
    }

    private String getCurrentScope() {
        return String.join("/", scopeNames);
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

    private boolean isPointer(String type) {
        return type != null && type.endsWith("*");
    }

    private String dereferenceType(String type) {
        if (!isPointer(type)) {
            return "unknown";
        }

        return type.substring(0, type.length() - 1);
    }

    private boolean isAddressable(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() == null) {
            return false;
        }

        return ctx.primary().lvalue() != null;
    }

    private boolean isAssignable(String targetType, String sourceType) {
        if (targetType.equals(sourceType)) {
            return true;
        }

        if (sourceType.equals("unknown")) {
            return true;
        }

        if (targetType.equals("unknown")) {
            return true;
        }

        if (isPointer(targetType) || isPointer(sourceType)) {
            return targetType.equals(sourceType);
        }

        return targetType.equals("int") && sourceType.equals("char");
    }

    private boolean areComparable(String leftType, String rightType) {
        if (leftType.equals(rightType)) {
            return true;
        }

        return isNumeric(leftType) && isNumeric(rightType);
    }

    private boolean isNumeric(String type) {
        return type.equals("int") || type.equals("char");
    }

    private boolean isConditionType(String type) {
        return type.equals("bool") || type.equals("int") || type.equals("char");
    }

    private static class SemanticSymbol {
        private final String id;
        private final String type;
        private final String kind;
        private final String scope;
        private final int line;
        private final int column;
        private final int arrayDimensions;
        private final String baseType;
        private final List<String> parameterTypes;

        private SemanticSymbol(String id, String type, String kind, String scope, int line, int column,
                               int arrayDimensions, String baseType, List<String> parameterTypes) {
            this.id = id;
            this.type = type;
            this.kind = kind;
            this.scope = scope;
            this.line = line;
            this.column = column;
            this.arrayDimensions = arrayDimensions;
            this.baseType = baseType;
            this.parameterTypes = parameterTypes;
        }
    }
}