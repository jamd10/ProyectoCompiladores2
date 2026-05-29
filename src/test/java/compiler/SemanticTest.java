package compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemanticTest {

    private TypeChecker typeChecker;
    private SymbolTable symbolTable;

    @BeforeEach
    void setUp() {
        symbolTable = new SymbolTable();
        typeChecker = new TypeChecker(symbolTable);
    }

    @Test
    void testValidVariableDeclaration() {
        String code = "int a;";
        assertTrue(typeChecker.check(code), "Valid variable declaration should pass.");
    }

    @Test
    void testInvalidVariableDeclaration() {
        String code = "int 1a;"; // Invalid identifier
        assertFalse(typeChecker.check(code), "Invalid variable declaration should fail.");
    }

    @Test
    void testTypeCompatibility() {
        String code = "int a; a = 5;"; // Valid assignment
        assertTrue(typeChecker.check(code), "Type compatibility should be valid.");
    }

    @Test
    void testIncompatibleTypeAssignment() {
        String code = "bool b; b = 5;"; // Invalid assignment
        assertFalse(typeChecker.check(code), "Incompatible type assignment should fail.");
    }

    @Test
    void testFunctionDeclaration() {
        String code = "int func(int x) { return x; }";
        assertTrue(typeChecker.check(code), "Valid function declaration should pass.");
    }

    @Test
    void testFunctionReturnTypeMismatch() {
        String code = "int func() { return true; }"; // Invalid return type
        assertFalse(typeChecker.check(code), "Function return type mismatch should fail.");
    }

    @Test
    void testArrayDeclaration() {
        String code = "int arr[10];";
        assertTrue(typeChecker.check(code), "Valid array declaration should pass.");
    }

    @Test
    void testArrayIndexing() {
        String code = "int arr[10]; arr[0] = 5;";
        assertTrue(typeChecker.check(code), "Valid array indexing should pass.");
    }

    @Test
    void testInvalidArrayIndexing() {
        String code = "int arr[10]; arr[10] = 5;"; // Out of bounds
        assertFalse(typeChecker.check(code), "Invalid array indexing should fail.");
    }
}