package compiler;

import compiler.analysis.LexicalSummary;
import compiler.analysis.SyntaxSummaryVisitor;
import compiler.ast.AstPrinterVisitor;
import compiler.parser.MiniCLexer;
import compiler.parser.MiniCParser;
import compiler.semantic.SymbolTableBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String[] TEST_FILES = {
            "src/main/resources/examples/correct/programa1.mc",
            "src/main/resources/examples/correct/programa2.mc",
            "src/main/resources/examples/correct/programa3.mc",
            "src/main/resources/examples/errors/error_lexico.mc",
            "src/main/resources/examples/errors/error_sintactico1.mc",
            "src/main/resources/examples/errors/error_sintactico2.mc",
            "src/main/resources/examples/errors/error_comentario_bloque.mc"
    };

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        if (hasArgument(args, "--test")) {
            runTests();
            return;
        }

        String sourceFilePath = args[0];
        CompilerOptions options = CompilerOptions.fromArgs(args);

        CompilationResult result = analyzeFile(sourceFilePath, options);
        printResultSummary(result);
    }

    private static CompilationResult analyzeFile(String sourceFilePath, CompilerOptions options) {
        CompilationResult result = new CompilationResult(sourceFilePath);

        try {
            CharStream input = CharStreams.fromFileName(sourceFilePath);

            MiniCLexer lexer = new MiniCLexer(input);
            SyntaxErrorListener lexerErrorListener = new SyntaxErrorListener();

            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            result.lexicalErrors.addAll(getLexicalErrors(tokens, lexerErrorListener));

            if (options.showTokens) {
                printTokens(tokens);
            }

            if (options.showLexicalSummary) {
                LexicalSummary.fromTokens(tokens).print();
            }

            if (!result.lexicalErrors.isEmpty()) {
                printErrors("ERRORES LEXICOS", result.lexicalErrors);
                result.lexicalSuccess = false;
                return result;
            }

            result.lexicalSuccess = true;

            MiniCParser parser = new MiniCParser(tokens);
            SyntaxErrorListener parserErrorListener = new SyntaxErrorListener();

            parser.removeErrorListeners();
            parser.addErrorListener(parserErrorListener);

            ParseTree tree = parser.program();

            if (parserErrorListener.hasErrors()) {
                result.syntaxErrors.addAll(parserErrorListener.getErrors());
                result.syntaxSuccess = false;
                printErrors("ERRORES SINTACTICOS", result.syntaxErrors);
                return result;
            }

            result.syntaxSuccess = true;

            if (options.showTree) {
                System.out.println();
                System.out.println("================ PARSE TREE ANTLR ================");
                System.out.println(tree.toStringTree(parser));
            }

            if (options.showSyntaxSummary) {
                SyntaxSummaryVisitor syntaxSummaryVisitor = new SyntaxSummaryVisitor();
                syntaxSummaryVisitor.visit(tree);
                syntaxSummaryVisitor.print();
            }

            if (options.showAst) {
                System.out.println();
                System.out.println("================ AST / RECORRIDO CON VISITOR ================");

                AstPrinterVisitor astPrinterVisitor = new AstPrinterVisitor();
                astPrinterVisitor.visit(tree);
                System.out.print(astPrinterVisitor.getOutput());
            }

            SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder();
            symbolTableBuilder.visit(tree);

            result.symbolErrors.addAll(symbolTableBuilder.getErrors());

            if (options.showSymbols) {
                symbolTableBuilder.getSymbolTable().print();
            }

            if (!result.symbolErrors.isEmpty()) {
                printErrors("ERRORES DE TABLA DE SIMBOLOS", result.symbolErrors);
            }

            return result;

        } catch (IOException e) {
            result.readErrors.add("Error: no se pudo leer el archivo " + sourceFilePath);
            result.readErrors.add(e.getMessage());
            return result;
        }
    }

    private static List<String> getLexicalErrors(CommonTokenStream tokens, SyntaxErrorListener lexerErrorListener) {
        List<String> errors = new ArrayList<>();

        for (Token token : tokens.getTokens()) {
            if (token.getType() == MiniCLexer.ERROR_CHAR) {
                errors.add("Error lexico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - simbolo no reconocido: " + token.getText());
            }

            if (token.getType() == MiniCLexer.UnclosedString) {
                errors.add("Error lexico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - cadena sin cerrar");
            }

            if (token.getType() == MiniCLexer.UnclosedChar) {
                errors.add("Error lexico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - caracter sin cerrar");
            }

            if (token.getType() == MiniCLexer.InvalidCharLiteral) {
                errors.add("Error lexico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - literal char invalido: " + token.getText());
            }

            if (token.getType() == MiniCLexer.UnclosedBlockComment) {
                errors.add("Error lexico linea " + token.getLine() + ":" + token.getCharPositionInLine()
                        + " - comentario de bloque sin cerrar");
            }
        }

        if (lexerErrorListener.hasErrors()) {
            errors.addAll(lexerErrorListener.getErrors());
        }

        return errors;
    }

    private static void printTokens(CommonTokenStream tokens) {
        System.out.println();
        System.out.println("================ TOKENS ================");
        System.out.printf("%-22s %-30s %-10s %-10s%n", "TOKEN", "LEXEMA", "LINEA", "COLUMNA");
        System.out.println("----------------------------------------------------------------------------");

        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF) {
                continue;
            }

            String symbolicName = MiniCLexer.VOCABULARY.getSymbolicName(token.getType());

            if (symbolicName == null) {
                symbolicName = MiniCLexer.VOCABULARY.getLiteralName(token.getType());
            }

            String lexeme = formatLexeme(token.getText());

            System.out.printf("%-22s %-30s %-10d %-10d%n",
                    symbolicName, "'" + lexeme + "'", token.getLine(), token.getCharPositionInLine());
        }
    }

    private static String formatLexeme(String text) {
        if (text == null) {
            return "";
        }

        String formattedText = text
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");

        if (formattedText.length() > 28) {
            return formattedText.substring(0, 25) + "...";
        }

        return formattedText;
    }


    private static void printErrors(String title, List<String> errors) {
        System.out.println();
        System.out.println("================ " + title + " ================");

        for (String error : errors) {
            System.out.println(error);
        }
    }

    private static void printResultSummary(CompilationResult result) {
        System.out.println();
        System.out.println("================ RESUMEN FINAL ================");
        System.out.println("Archivo: " + result.filePath);

        if (!result.readErrors.isEmpty()) {
            System.out.println("Lectura: error");

            for (String error : result.readErrors) {
                System.out.println(error);
            }

            return;
        }

        System.out.println("Analisis lexico: " + (result.lexicalSuccess ? "correcto" : "con errores"));

        if (!result.lexicalSuccess) {
            return;
        }

        System.out.println("Analisis sintactico: " + (result.syntaxSuccess ? "correcto" : "con errores"));

        if (!result.syntaxSuccess) {
            return;
        }

        if (result.symbolErrors.isEmpty()) {
            System.out.println("Tabla de simbolos: generada sin errores");
        } else {
            System.out.println("Tabla de simbolos: generada con advertencias/errores");
        }
    }

    private static void runTests() {
        System.out.println();
        System.out.println("================ EJECUCION DE PRUEBAS ================");

        int successCount = 0;
        int failedCount = 0;

        CompilerOptions options = new CompilerOptions();
        options.showTokens = false;
        options.showTree = false;
        options.showAst = false;
        options.showSymbols = false;
        options.showLexicalSummary = false;
        options.showSyntaxSummary = false;

        for (String testFile : TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);
            boolean expectedCorrect = testFile.contains("/correct/") || testFile.contains("\\correct\\");

            boolean passed;

            if (expectedCorrect) {
                passed = result.readErrors.isEmpty() && result.lexicalSuccess && result.syntaxSuccess;
            } else {
                passed = result.readErrors.isEmpty() && (!result.lexicalSuccess || !result.syntaxSuccess);
            }

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-70s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("Pruebas correctas: " + successCount);
        System.out.println("Pruebas fallidas: " + failedCount);
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --tokens\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --lexical-summary\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --syntax-summary\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --ast\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --symbols\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --tree\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --all\"");
        System.out.println(".\\gradlew.bat run --args=\"--test\"");
    }

    private static boolean hasArgument(String[] args, String expectedArgument) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(expectedArgument)) {
                return true;
            }
        }

        return false;
    }

    private static class CompilerOptions {
        private boolean showTokens;
        private boolean showTree;
        private boolean showAst;
        private boolean showSymbols;
        private boolean showLexicalSummary;
        private boolean showSyntaxSummary;

        private static CompilerOptions fromArgs(String[] args) {
            CompilerOptions options = new CompilerOptions();

            options.showTokens = hasArgument(args, "--tokens") || hasArgument(args, "--all");
            options.showTree = hasArgument(args, "--tree") || hasArgument(args, "--all");
            options.showAst = hasArgument(args, "--ast") || hasArgument(args, "--all");
            options.showSymbols = hasArgument(args, "--symbols") || hasArgument(args, "--all");
            options.showLexicalSummary = hasArgument(args, "--lexical-summary") || hasArgument(args, "--all");
            options.showSyntaxSummary = hasArgument(args, "--syntax-summary") || hasArgument(args, "--all");

            if (!options.showTokens && !options.showTree && !options.showAst && !options.showSymbols
                    && !options.showLexicalSummary && !options.showSyntaxSummary) {
                options.showTokens = true;
                options.showLexicalSummary = true;
                options.showSyntaxSummary = true;
                options.showAst = true;
                options.showSymbols = true;
            }

            return options;
        }
    }

    private static class CompilationResult {
        private final String filePath;
        private boolean lexicalSuccess;
        private boolean syntaxSuccess;
        private final List<String> readErrors = new ArrayList<>();
        private final List<String> lexicalErrors = new ArrayList<>();
        private final List<String> syntaxErrors = new ArrayList<>();
        private final List<String> symbolErrors = new ArrayList<>();

        private CompilationResult(String filePath) {
            this.filePath = filePath;
        }
    }
}