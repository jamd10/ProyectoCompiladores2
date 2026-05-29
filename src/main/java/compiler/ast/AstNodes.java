package compiler.ast;

import java.util.List;

public abstract class AstNode {
    public abstract <T> T accept(AstVisitor<T> visitor);
}

public class ProgramNode extends AstNode {
    private List<DeclarationNode> declarations;

    public ProgramNode(List<DeclarationNode> declarations) {
        this.declarations = declarations;
    }

    public List<DeclarationNode> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}

public class DeclarationNode extends AstNode {
    private String identifier;
    private TypeNode type;

    public DeclarationNode(String identifier, TypeNode type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TypeNode getType() {
        return type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDeclaration(this);
    }
}

public class TypeNode extends AstNode {
    private String typeName;

    public TypeNode(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitType(this);
    }
}

public class BinaryExpressionNode extends AstNode {
    private AstNode left;
    private String operator;
    private AstNode right;

    public BinaryExpressionNode(AstNode left, String operator, AstNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public AstNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public AstNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}

// Additional AST node classes can be added here for other constructs like statements, function definitions, etc.