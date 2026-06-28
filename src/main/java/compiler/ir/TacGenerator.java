package compiler.ir;

import compiler.parser.MiniCBaseVisitor;
import compiler.parser.MiniCParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TacGenerator extends MiniCBaseVisitor<String> {
    private final List<TacInstruction> instructions = new ArrayList<>();
    private final Map<String, String> functionReturnTypes = new HashMap<>();
    private int tempCounter = 0;
    private int labelCounter = 0;

    public List<TacInstruction> getInstructions() {
        return instructions;
    }

    @Override
    public String visitProgram(MiniCParser.ProgramContext ctx) {
        declareRuntimeFunctions();

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            if (declaration.functionDefinition() != null) {
                MiniCParser.FunctionDefinitionContext function = declaration.functionDefinition();
                String functionName = function.Identifier().getText();
                String returnType = buildType(function.typeSpecifier().getText(), function.pointer() != null);

                functionReturnTypes.put(functionName, returnType);
            }
        }

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            visit(declaration);
        }

        return "";
    }

    @Override
    public String visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx) {
        emit("");
        emit("func " + ctx.Identifier().getText() + ":");

        if (ctx.parameterList() != null) {
            for (MiniCParser.ParameterContext parameter : ctx.parameterList().parameter()) {
                emit("paramdecl " + buildParameterType(parameter) + " " + parameter.Identifier().getText());
            }
        }

        visit(ctx.compoundStatement());

        emit("endfunc " + ctx.Identifier().getText());

        return "";
    }

    @Override
    public String visitCompoundStatement(MiniCParser.CompoundStatementContext ctx) {
        for (MiniCParser.BlockItemContext item : ctx.blockItem()) {
            visit(item);
        }

        return "";
    }

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String baseType = ctx.typeSpecifier().getText();

        for (MiniCParser.InitDeclaratorContext declarator : ctx.initDeclaratorList().initDeclarator()) {
            String id = declarator.Identifier().getText();
            String declarationType = buildType(baseType, declarator.pointer() != null) + buildArraySuffix(declarator);

            emit("decl " + declarationType + " " + id);

            if (declarator.expr() != null) {
                String value = visit(declarator.expr());

                if (!value.isEmpty()) {
                    emit(id + " = " + value);
                }
            }
        }

        return "";
    }

    @Override
    public String visitExpressionStatement(MiniCParser.ExpressionStatementContext ctx) {
        if (ctx.expr() != null) {
            visit(ctx.expr());
        }

        return "";
    }

    @Override
    public String visitReturnStatement(MiniCParser.ReturnStatementContext ctx) {
        if (ctx.expr() != null) {
            String value = visit(ctx.expr());

            if (!value.isEmpty()) {
                emit("return " + value);
            } else {
                emit("return");
            }
        } else {
            emit("return");
        }

        return "";
    }

    @Override
    public String visitIfStatement(MiniCParser.IfStatementContext ctx) {
        String elseLabel = newLabel("else");
        String endLabel = newLabel("endif");

        String condition = visit(ctx.expr());

        emit("ifFalse " + condition + " goto " + elseLabel);

        visit(ctx.statement(0));

        if (ctx.statement().size() > 1) {
            emit("goto " + endLabel);
            emit(elseLabel + ":");
            visit(ctx.statement(1));
            emit(endLabel + ":");
        } else {
            emit(elseLabel + ":");
        }

        return "";
    }

    @Override
    public String visitWhileStatement(MiniCParser.WhileStatementContext ctx) {
        String startLabel = newLabel("while");
        String endLabel = newLabel("endwhile");

        emit(startLabel + ":");

        String condition = visit(ctx.expr());

        emit("ifFalse " + condition + " goto " + endLabel);

        visit(ctx.statement());

        emit("goto " + startLabel);
        emit(endLabel + ":");

        return "";
    }

    @Override
    public String visitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx) {
        String startLabel = newLabel("dowhile");

        emit(startLabel + ":");

        visit(ctx.statement());

        String condition = visit(ctx.expr());

        emit("if " + condition + " goto " + startLabel);

        return "";
    }

    @Override
    public String visitForStatement(MiniCParser.ForStatementContext ctx) {
        String startLabel = newLabel("for");
        String endLabel = newLabel("endfor");

        List<MiniCParser.ExprContext> expressions = ctx.expr();

        if (!expressions.isEmpty()) {
            visit(expressions.get(0));
        }

        emit(startLabel + ":");

        if (expressions.size() > 1) {
            String condition = visit(expressions.get(1));
            emit("ifFalse " + condition + " goto " + endLabel);
        }

        visit(ctx.statement());

        if (expressions.size() > 2) {
            visit(expressions.get(2));
        }

        emit("goto " + startLabel);
        emit(endLabel + ":");

        return "";
    }

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() != null) {
            String left = buildLValue(ctx.lvalue());
            String right = visit(ctx.assignmentExpr());

            if (!right.isEmpty()) {
                emit(left + " = " + right);
            }

            return left;
        }

        return visit(ctx.logicalOrExpr());
    }

    @Override
    public String visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        String left = visit(ctx.logicalAndExpr(0));

        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.logicalAndExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        String left = visit(ctx.equalityExpr(0));

        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.equalityExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        String left = visit(ctx.relationalExpr(0));

        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.relationalExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        String left = visit(ctx.additiveExpr(0));

        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.additiveExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        String left = visit(ctx.multiplicativeExpr(0));

        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.multiplicativeExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        String left = visit(ctx.unaryExpr(0));

        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String operator = ctx.getChild((i * 2) - 1).getText();
            String right = visit(ctx.unaryExpr(i));
            String temp = newTemp();

            emit(temp + " = " + left + " " + operator + " " + right);

            left = temp;
        }

        return left;
    }

    @Override
    public String visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) {
            return visit(ctx.primary());
        }

        String operator = ctx.getChild(0).getText();
        String value = visit(ctx.unaryExpr());
        String temp = newTemp();

        emit(temp + " = " + operator + value);

        return temp;
    }

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.IntegerConst() != null) {
            return ctx.IntegerConst().getText();
        }

        if (ctx.CharConst() != null) {
            return ctx.CharConst().getText();
        }

        if (ctx.StringLiteral() != null) {
            return ctx.StringLiteral().getText();
        }

        if (ctx.TRUE() != null) {
            return "true";
        }

        if (ctx.FALSE() != null) {
            return "false";
        }

        if (ctx.expr() != null) {
            return visit(ctx.expr());
        }

        if (ctx.call() != null) {
            return visit(ctx.call());
        }

        if (ctx.lvalue() != null) {
            return buildLValue(ctx.lvalue());
        }

        return "";
    }

    @Override
    public String visitCall(MiniCParser.CallContext ctx) {
        String id = ctx.Identifier().getText();
        int argumentCount = 0;

        if (ctx.argumentList() != null) {
            for (MiniCParser.ExprContext expr : ctx.argumentList().expr()) {
                String argument = visit(expr);
                emit("param " + argument);
                argumentCount++;
            }
        }

        String returnType = functionReturnTypes.getOrDefault(id, "unknown");

        if (returnType.equals("void")) {
            emit("call " + id + ", " + argumentCount);
            return "";
        }

        String temp = newTemp();

        emit(temp + " = call " + id + ", " + argumentCount);

        return temp;
    }

    private String buildLValue(MiniCParser.LvalueContext ctx) {
        StringBuilder value = new StringBuilder(ctx.Identifier().getText());

        for (MiniCParser.ExprContext expr : ctx.expr()) {
            String index = visit(expr);
            value.append("[");
            value.append(index);
            value.append("]");
        }

        return value.toString();
    }

    private String buildType(String baseType, boolean isPointer) {
        if (isPointer) {
            return baseType + "*";
        }

        return baseType;
    }

    private String buildParameterType(MiniCParser.ParameterContext ctx) {
        return buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null) + buildArraySuffix(ctx);
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

    private void declareRuntimeFunctions() {
        functionReturnTypes.put("print_int", "void");
        functionReturnTypes.put("print_char", "void");
        functionReturnTypes.put("print_bool", "void");
        functionReturnTypes.put("print_str", "void");
        functionReturnTypes.put("println", "void");
        functionReturnTypes.put("read_int", "int");
        functionReturnTypes.put("read_char", "char");
        functionReturnTypes.put("read_str", "void");
    }

    private String newTemp() {
        tempCounter++;
        return "t" + tempCounter;
    }

    private String newLabel(String prefix) {
        labelCounter++;
        return "L_" + prefix + "_" + labelCounter;
    }

    private void emit(String text) {
        instructions.add(new TacInstruction(text));
    }
}