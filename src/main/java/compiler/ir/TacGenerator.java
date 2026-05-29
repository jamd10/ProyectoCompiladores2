package compiler.ir;

import compiler.ast.AstNode;
import compiler.ast.FunctionNode;
import compiler.ast.VariableNode;
import compiler.ast.ExpressionNode;
import java.util.ArrayList;
import java.util.List;

public class TacGenerator {
    private List<String> instructions;
    private int tempCounter;

    public TacGenerator() {
        this.instructions = new ArrayList<>();
        this.tempCounter = 0;
    }

    public List<String> generateTac(AstNode root) {
        visit(root);
        return instructions;
    }

    private void visit(AstNode node) {
        if (node instanceof FunctionNode) {
            visitFunction((FunctionNode) node);
        } else if (node instanceof VariableNode) {
            visitVariable((VariableNode) node);
        } else if (node instanceof ExpressionNode) {
            visitExpression((ExpressionNode) node);
        }
        // Add more node types as needed
    }

    private void visitFunction(FunctionNode functionNode) {
        // Generate TAC for function
        instructions.add("func " + functionNode.getName());
        for (AstNode stmt : functionNode.getBody()) {
            visit(stmt);
        }
        instructions.add("endfunc");
    }

    private void visitVariable(VariableNode variableNode) {
        // Generate TAC for variable declaration
        instructions.add("declare " + variableNode.getName());
    }

    private void visitExpression(ExpressionNode expressionNode) {
        // Generate TAC for expressions
        String tempVar = "t" + (tempCounter++);
        instructions.add(tempVar + " = " + expressionNode.getOperator() + " " + expressionNode.getLeft() + ", " + expressionNode.getRight());
    }
}