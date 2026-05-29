package compiler.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface MiniCParserListener extends ParseTreeListener {
    void enterProgram(MiniCParser.ProgramContext ctx);
    void exitProgram(MiniCParser.ProgramContext ctx);
    
    void enterDeclaration(MiniCParser.DeclarationContext ctx);
    void exitDeclaration(MiniCParser.DeclarationContext ctx);
    
    void enterFuncDef(MiniCParser.FuncDefContext ctx);
    void exitFuncDef(MiniCParser.FuncDefContext ctx);
    
    void enterCompoundStmt(MiniCParser.CompoundStmtContext ctx);
    void exitCompoundStmt(MiniCParser.CompoundStmtContext ctx);
    
    void enterIfStmt(MiniCParser.IfStmtContext ctx);
    void exitIfStmt(MiniCParser.IfStmtContext ctx);
    
    void enterWhileStmt(MiniCParser.WhileStmtContext ctx);
    void exitWhileStmt(MiniCParser.WhileStmtContext ctx);
    
    void enterForStmt(MiniCParser.ForStmtContext ctx);
    void exitForStmt(MiniCParser.ForStmtContext ctx);
    
    void enterDoWhileStmt(MiniCParser.DoWhileStmtContext ctx);
    void exitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx);
    
    void enterAssignStmt(MiniCParser.AssignStmtContext ctx);
    void exitAssignStmt(MiniCParser.AssignStmtContext ctx);
    
    void enterReturnStmt(MiniCParser.ReturnStmtContext ctx);
    void exitReturnStmt(MiniCParser.ReturnStmtContext ctx);
    
    void enterExprStmt(MiniCParser.ExprStmtContext ctx);
    void exitExprStmt(MiniCParser.ExprStmtContext ctx);
    
    void enterExpr(MiniCParser.ExprContext ctx);
    void exitExpr(MiniCParser.ExprContext ctx);
    
    void enterLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx);
    void exitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx);
    
    void enterLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx);
    void exitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx);
    
    void enterEqualityExpr(MiniCParser.EqualityExprContext ctx);
    void exitEqualityExpr(MiniCParser.EqualityExprContext ctx);
    
    void enterRelationalExpr(MiniCParser.RelationalExprContext ctx);
    void exitRelationalExpr(MiniCParser.RelationalExprContext ctx);
    
    void enterAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
    void exitAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
    
    void enterMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
    void exitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
    
    void enterUnaryExpr(MiniCParser.UnaryExprContext ctx);
    void exitUnaryExpr(MiniCParser.UnaryExprContext ctx);
    
    void enterPrimary(MiniCParser.PrimaryContext ctx);
    void exitPrimary(MiniCParser.PrimaryContext ctx);
    
    void enterCall(MiniCParser.CallContext ctx);
    void exitCall(MiniCParser.CallContext ctx);
    
    void enterLvalue(MiniCParser.LvalueContext ctx);
    void exitLvalue(MiniCParser.LvalueContext ctx);
}