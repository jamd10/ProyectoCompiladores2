// Generated from MiniC.g4 by ANTLR 4.13.2
package compiler.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MiniCParser}.
 */
public interface MiniCListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(MiniCParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(MiniCParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(MiniCParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(MiniCParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(MiniCParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(MiniCParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(MiniCParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(MiniCParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclaratorList(MiniCParser.InitDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclaratorList(MiniCParser.InitDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclarator(MiniCParser.InitDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclarator(MiniCParser.InitDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#arraySuffix}.
	 * @param ctx the parse tree
	 */
	void enterArraySuffix(MiniCParser.ArraySuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#arraySuffix}.
	 * @param ctx the parse tree
	 */
	void exitArraySuffix(MiniCParser.ArraySuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#pointer}.
	 * @param ctx the parse tree
	 */
	void enterPointer(MiniCParser.PointerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#pointer}.
	 * @param ctx the parse tree
	 */
	void exitPointer(MiniCParser.PointerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(MiniCParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(MiniCParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(MiniCParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(MiniCParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void enterBlockItem(MiniCParser.BlockItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#blockItem}.
	 * @param ctx the parse tree
	 */
	void exitBlockItem(MiniCParser.BlockItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MiniCParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MiniCParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(MiniCParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(MiniCParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(MiniCParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(MiniCParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(MiniCParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(MiniCParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#doWhileStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoWhileStatement(MiniCParser.DoWhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#doWhileStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(MiniCParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(MiniCParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(MiniCParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(MiniCParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(MiniCParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(MiniCParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#assignmentExpr}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpr(MiniCParser.AssignmentExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#assignmentExpr}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpr(MiniCParser.AssignmentExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#logicalOrExpr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#logicalOrExpr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#logicalAndExpr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#logicalAndExpr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#equalityExpr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(MiniCParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#equalityExpr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(MiniCParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#relationalExpr}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpr(MiniCParser.RelationalExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#relationalExpr}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpr(MiniCParser.RelationalExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(MiniCParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(MiniCParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(MiniCParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(MiniCParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(MiniCParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(MiniCParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(MiniCParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(MiniCParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniCParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(MiniCParser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniCParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(MiniCParser.LvalueContext ctx);
}