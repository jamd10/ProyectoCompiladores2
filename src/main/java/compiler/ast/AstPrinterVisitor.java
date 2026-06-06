package compiler.ast;

import compiler.parser.MiniCBaseVisitor;
import compiler.parser.MiniCParser;

import java.util.List;

public class AstPrinterVisitor extends MiniCBaseVisitor<Void> {
    private final StringBuilder output = new StringBuilder();
    private int indentLevel = 0;

    public String getOutput() {
        return output.toString();
    }

    private void printLine(String text) {
        output.append("  ".repeat(Math.max(0, indentLevel)));
        output.append(text);
        output.append(System.lineSeparator());
    }

    @Override
    public Void visitProgram(MiniCParser.ProgramContext ctx) {
        printLine("Program");
        indentLevel++;

        for (MiniCParser.ExternalDeclarationContext declaration : ctx.externalDeclaration()) {
            visit(declaration);
        }

        indentLevel--;
        return null;
    }

    @Override
    public Void visitExternalDeclaration(MiniCParser.ExternalDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx) {
        String functionName = ctx.Identifier().getText();
        String returnType = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);

        printLine("Function: " + functionName);
        indentLevel++;
        printLine("Return type: " + returnType);
        printLine("Signature: " + buildFunctionSignature(ctx));

        if (ctx.parameterList() != null) {
            printLine("Parameters");
            indentLevel++;

            for (MiniCParser.ParameterContext parameter : ctx.parameterList().parameter()) {
                printLine(buildParameter(parameter));
            }

            indentLevel--;
        }

        visit(ctx.compoundStatement());

        indentLevel--;
        return null;
    }

    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String baseType = ctx.typeSpecifier().getText();

        for (MiniCParser.InitDeclaratorContext declarator : ctx.initDeclaratorList().initDeclarator()) {
            String id = declarator.Identifier().getText();
            String type = buildType(baseType, declarator.pointer() != null);

            if (!declarator.arraySuffix().isEmpty()) {
                type += buildArraySuffix(declarator);
            }

            printLine("Declaration: " + type + " " + id);

            if (declarator.expr() != null) {
                indentLevel++;
                printLine("Init: " + declarator.expr().getText());
                indentLevel--;
            }
        }

        return null;
    }

    @Override
    public Void visitCompoundStatement(MiniCParser.CompoundStatementContext ctx) {
        printLine("Block");
        indentLevel++;

        for (MiniCParser.BlockItemContext item : ctx.blockItem()) {
            visit(item);
        }

        indentLevel--;
        return null;
    }

    @Override
    public Void visitBlockItem(MiniCParser.BlockItemContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitStatement(MiniCParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitIfStatement(MiniCParser.IfStatementContext ctx) {
        printLine("If");
        indentLevel++;
        printLine("Condition: " + ctx.expr().getText());

        printLine("Then");
        indentLevel++;
        visit(ctx.statement(0));
        indentLevel--;

        if (ctx.statement().size() > 1) {
            printLine("Else");
            indentLevel++;
            visit(ctx.statement(1));
            indentLevel--;
        }

        indentLevel--;
        return null;
    }

    @Override
    public Void visitWhileStatement(MiniCParser.WhileStatementContext ctx) {
        printLine("While");
        indentLevel++;
        printLine("Condition: " + ctx.expr().getText());
        visit(ctx.statement());
        indentLevel--;
        return null;
    }

    @Override
    public Void visitForStatement(MiniCParser.ForStatementContext ctx) {
        printLine("For");
        indentLevel++;

        List<MiniCParser.ExprContext> expressions = ctx.expr();

        if (expressions.size() > 0) {
            printLine("Init: " + expressions.get(0).getText());
        }

        if (expressions.size() > 1) {
            printLine("Condition: " + expressions.get(1).getText());
        }

        if (expressions.size() > 2) {
            printLine("Step: " + expressions.get(2).getText());
        }

        visit(ctx.statement());

        indentLevel--;
        return null;
    }

    @Override
    public Void visitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx) {
        printLine("DoWhile");
        indentLevel++;
        visit(ctx.statement());
        printLine("Condition: " + ctx.expr().getText());
        indentLevel--;
        return null;
    }

    @Override
    public Void visitReturnStatement(MiniCParser.ReturnStatementContext ctx) {
        if (ctx.expr() != null) {
            printLine("Return: " + ctx.expr().getText());
        } else {
            printLine("Return");
        }

        return null;
    }

    @Override
    public Void visitExpressionStatement(MiniCParser.ExpressionStatementContext ctx) {
        if (ctx.expr() != null) {
            printLine("Expression: " + ctx.expr().getText());
        }

        return null;
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

                signature.append(buildParameter(parameter));
            }
        }

        signature.append(")");

        return signature.toString();
    }

    private String buildParameter(MiniCParser.ParameterContext ctx) {
        String type = buildType(ctx.typeSpecifier().getText(), ctx.pointer() != null);

        if (!ctx.arraySuffix().isEmpty()) {
            type += buildArraySuffix(ctx);
        }

        return type + " " + ctx.Identifier().getText();
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