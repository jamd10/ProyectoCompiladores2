// Generated from MiniC.g4 by ANTLR 4.13.2
package compiler.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MiniCParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MiniCVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MiniCParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MiniCParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternalDeclaration(MiniCParser.ExternalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(MiniCParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(MiniCParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(MiniCParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(MiniCParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclaratorList(MiniCParser.InitDeclaratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#initDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclarator(MiniCParser.InitDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#arraySuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArraySuffix(MiniCParser.ArraySuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#pointer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointer(MiniCParser.PointerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#typeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSpecifier(MiniCParser.TypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(MiniCParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#blockItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockItem(MiniCParser.BlockItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MiniCParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(MiniCParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(MiniCParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(MiniCParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#doWhileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoWhileStatement(MiniCParser.DoWhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(MiniCParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(MiniCParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MiniCParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#assignmentExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#logicalOrExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#logicalAndExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#equalityExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpr(MiniCParser.EqualityExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#relationalExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpr(MiniCParser.RelationalExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#additiveExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#unaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(MiniCParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(MiniCParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(MiniCParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(MiniCParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniCParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalue(MiniCParser.LvalueContext ctx);
}