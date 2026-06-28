package compiler.analysis;

import compiler.parser.MiniCBaseVisitor;
import compiler.parser.MiniCParser;

public class SyntaxSummaryVisitor extends MiniCBaseVisitor<Void> {
    private int functions;
    private int declarations;
    private int arrays;
    private int ifStatements;
    private int whileStatements;
    private int forStatements;
    private int doWhileStatements;
    private int returnStatements;
    private int calls;
    private int blocks;

    @Override
    public Void visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx) {
        functions++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
        declarations += ctx.initDeclaratorList().initDeclarator().size();

        for (MiniCParser.InitDeclaratorContext declarator : ctx.initDeclaratorList().initDeclarator()) {
            if (!declarator.arraySuffix().isEmpty()) {
                arrays++;
            }
        }

        return visitChildren(ctx);
    }

    @Override
    public Void visitIfStatement(MiniCParser.IfStatementContext ctx) {
        ifStatements++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitWhileStatement(MiniCParser.WhileStatementContext ctx) {
        whileStatements++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitForStatement(MiniCParser.ForStatementContext ctx) {
        forStatements++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx) {
        doWhileStatements++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitReturnStatement(MiniCParser.ReturnStatementContext ctx) {
        returnStatements++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitCall(MiniCParser.CallContext ctx) {
        calls++;
        return visitChildren(ctx);
    }

    @Override
    public Void visitCompoundStatement(MiniCParser.CompoundStatementContext ctx) {
        blocks++;
        return visitChildren(ctx);
    }

    public void print() {
        System.out.println();
        System.out.println("================ RESUMEN SINTACTICO ================");
        System.out.println("Funciones: " + functions);
        System.out.println("Declaraciones: " + declarations);
        System.out.println("Arreglos: " + arrays);
        System.out.println("Bloques: " + blocks);
        System.out.println("If: " + ifStatements);
        System.out.println("While: " + whileStatements);
        System.out.println("For: " + forStatements);
        System.out.println("Do while: " + doWhileStatements);
        System.out.println("Return: " + returnStatements);
        System.out.println("Llamadas: " + calls);
    }
}