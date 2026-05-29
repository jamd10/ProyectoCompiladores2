package compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import static org.junit.jupiter.api.Assertions.*;

public class LexerParserTest {
    private MiniCLexer lexer;
    private MiniCParser parser;

    @BeforeEach
    public void setUp() {
        // Setup code for initializing lexer and parser
    }

    @Test
    public void testValidProgram() {
        String input = "int main() { return 0; }";
        lexer = new MiniCLexer(CharStreams.fromString(input));
        TokenStream tokens = new CommonTokenStream(lexer);
        parser = new MiniCParser(tokens);
        
        // Parse the input and assert the parse tree
        assertNotNull(parser.program());
    }

    @Test
    public void testInvalidProgram() {
        String input = "int main() { return; }"; // Missing expression after return
        lexer = new MiniCLexer(CharStreams.fromString(input));
        TokenStream tokens = new CommonTokenStream(lexer);
        parser = new MiniCParser(tokens);
        
        // Expect a recognition exception due to invalid syntax
        assertThrows(RecognitionException.class, () -> {
            parser.program();
        });
    }

    @Test
    public void testLexerTokens() {
        String input = "int a = 5;";
        lexer = new MiniCLexer(CharStreams.fromString(input));
        TokenStream tokens = new CommonTokenStream(lexer);
        
        tokens.fill();
        assertEquals(5, tokens.size()); // Check number of tokens
        assertEquals(MiniCLexer.INT, tokens.get(0).getType()); // Check first token type
        assertEquals(MiniCLexer.IDENTIFIER, tokens.get(1).getType()); // Check second token type
    }

    // Additional tests for other valid and invalid Mini-C programs can be added here
}