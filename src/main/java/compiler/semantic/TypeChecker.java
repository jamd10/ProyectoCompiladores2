package compiler.semantic;

import compiler.ast.AstNode;
import compiler.ast.IdentifierNode;
import compiler.ast.TypeNode;
import compiler.semantic.SymbolTable;

public class TypeChecker {
    private SymbolTable symbolTable;

    public TypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void check(AstNode node) {
        if (node instanceof IdentifierNode) {
            checkIdentifier((IdentifierNode) node);
        } else if (node instanceof TypeNode) {
            checkType((TypeNode) node);
        }
        // Add additional checks for other AST node types as needed
    }

    private void checkIdentifier(IdentifierNode node) {
        if (!symbolTable.isDeclared(node.getName())) {
            reportError("Variable '" + node.getName() + "' is not declared.", node);
        }
    }

    private void checkType(TypeNode node) {
        // Implement type checking logic for TypeNode
    }

    private void reportError(String message, AstNode node) {
        System.err.println("Error at line " + node.getLine() + ", column " + node.getColumn() + ": " + message);
    }
}