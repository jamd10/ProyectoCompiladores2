package compiler;

import compiler.analysis.LexicalSummary;
import compiler.analysis.SyntaxSummaryVisitor;
import compiler.ast.AstPrinterVisitor;
import compiler.ir.TacGenerator;
import compiler.ir.TacInstruction;
import compiler.ir.TacOptimizer;
import compiler.mips.MipsGenerator;
import compiler.parser.MiniCLexer;
import compiler.parser.MiniCParser;
import compiler.semantic.SemanticAnalyzer;
import compiler.semantic.SymbolTableBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String[] TEST_FILES = {
            "src/main/resources/examples/correct/programa1.mc",
            "src/main/resources/examples/correct/programa2.mc",
            "src/main/resources/examples/correct/programa3.mc",
            "src/main/resources/examples/correct/programa4.mc",
            "src/main/resources/examples/errors/error_lexico.mc",
            "src/main/resources/examples/errors/error_sintactico1.mc",
            "src/main/resources/examples/errors/error_sintactico2.mc",
            "src/main/resources/examples/errors/error_comentario_bloque.mc"
    };

    private static final String[] SEMANTIC_CORRECT_TEST_FILES = {
            "src/main/resources/examples/semantic/correct_semantico1.mc",
            "src/main/resources/examples/semantic/correct_semantico2.mc"
    };

    private static final String[] SEMANTIC_ERROR_TEST_FILES = {
            "src/main/resources/examples/semantic/error_variable_no_declarada.mc",
            "src/main/resources/examples/semantic/error_redeclaracion.mc",
            "src/main/resources/examples/semantic/error_funcion_no_declarada.mc",
            "src/main/resources/examples/semantic/error_parametros.mc",
            "src/main/resources/examples/semantic/error_tipo_asignacion.mc",
            "src/main/resources/examples/semantic/error_indice_arreglo.mc",
            "src/main/resources/examples/semantic/error_return_tipo.mc",
            "src/main/resources/examples/semantic/error_void_retorna_valor.mc",
            "src/main/resources/examples/semantic/error_funcion_sin_return.mc",
            "src/main/resources/examples/semantic/error_parametro_tipo.mc",
            "src/main/resources/examples/semantic/error_variable_como_funcion.mc",
            "src/main/resources/examples/semantic/error_funcion_como_variable.mc",
            "src/main/resources/examples/semantic/error_demasiados_indices.mc",
            "src/main/resources/examples/semantic/error_semantico_multiple.mc",
            "src/main/resources/examples/semantic/error_puntero_asignacion.mc",
            "src/main/resources/examples/semantic/error_dereferencia_no_puntero.mc",
            "src/main/resources/examples/semantic/error_direccion_no_lvalue.mc"
    };

    private static final String[] TAC_TEST_FILES = {
            "src/main/resources/examples/correct/programa1.mc",
            "src/main/resources/examples/correct/programa2.mc",
            "src/main/resources/examples/correct/programa3.mc",
            "src/main/resources/examples/correct/programa4.mc",
            "src/main/resources/examples/semantic/correct_semantico1.mc",
            "src/main/resources/examples/semantic/correct_semantico2.mc"
    };

    private static final String[] TAC_OPT_TEST_FILES = {
            "src/main/resources/examples/correct/programa_optimizacion.mc"
    };

    private static final String[] MIPS_TEST_FILES = {
            "src/main/resources/examples/correct/programa_optimizacion.mc",
            "src/main/resources/examples/correct/programa_mips_funciones.mc"
    };

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        if (hasArgument(args, "--test")) {
            boolean allPassed = runTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        if (hasArgument(args, "--semantic-test") || hasArgument(args, "--fase2-test")) {
            boolean allPassed = runSemanticTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        if (hasArgument(args, "--tac-test")) {
            boolean allPassed = runTacTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        if (hasArgument(args, "--tac-opt-test")) {
            boolean allPassed = runTacOptTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        if (hasArgument(args, "--mips-test")) {
            boolean allPassed = runMipsTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        String sourceFilePath = firstNonFlag(args);
        if (sourceFilePath == null) {
            System.out.println("Error: no se indico un archivo .mc de entrada.\n");
            printUsage();
            System.exit(2);
            return;
        }

        CompilerOptions options = CompilerOptions.fromArgs(args);

        CompilationResult result = analyzeFile(sourceFilePath, options);
        printResultSummary(result);

        System.exit(result.isSuccess() ? 0 : 1);
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

            if (options.showSymbols) {
                symbolTableBuilder.getSymbolTable().print();
            }

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.visit(tree);

            result.semanticErrors.addAll(semanticAnalyzer.getErrors());

            if (!result.semanticErrors.isEmpty()) {
                printErrors("ERRORES SEMANTICOS", result.semanticErrors);
            }

            if (options.showTac || options.showOptimizedTac || options.showMips || options.writeAssembly || options.dumpIr) {
                if (result.semanticErrors.isEmpty()) {
                    TacGenerator tacGenerator = new TacGenerator();
                    tacGenerator.visit(tree);

                    List<TacInstruction> tacInstructions = tacGenerator.getInstructions();

                    if (options.showTac || options.dumpIr) {
                        System.out.println();
                        System.out.println("================ CODIGO INTERMEDIO TAC ================");

                        for (TacInstruction instruction : tacInstructions) {
                            System.out.println(instruction);
                        }
                    }

                    List<TacInstruction> mipsSourceInstructions = tacInstructions;

                    if (options.showOptimizedTac || options.optimize) {
                        TacOptimizer tacOptimizer = new TacOptimizer();
                        mipsSourceInstructions = tacOptimizer.optimize(tacInstructions);
                    }

                    if (options.showOptimizedTac || (options.dumpIr && options.optimize)) {
                        System.out.println();
                        System.out.println("================ CODIGO INTERMEDIO TAC OPTIMIZADO ================");

                        for (TacInstruction instruction : mipsSourceInstructions) {
                            System.out.println(instruction);
                        }
                    }

                    if (options.showMips || options.writeAssembly) {
                        MipsGenerator mipsGenerator = new MipsGenerator();
                        List<String> mipsLines = mipsGenerator.generate(mipsSourceInstructions);

                        if (options.showMips) {
                            System.out.println();
                            System.out.println("================ CODIGO MIPS32 ================");

                            for (String line : mipsLines) {
                                System.out.println(line);
                            }
                        }

                        if (options.writeAssembly) {
                            writeAssemblyFile(sourceFilePath, options, mipsLines);
                        }
                    }
                } else {
                    // si la semantica falla, no conviene generar codigo intermedio ni codigo mips
                    if (options.showTac || options.dumpIr) {
                        System.out.println();
                        System.out.println("================ CODIGO INTERMEDIO TAC ================");
                        System.out.println("TAC omitido por errores semanticos.");
                    }

                    if (options.showMips || options.writeAssembly) {
                        System.out.println();
                        System.out.println("================ CODIGO MIPS32 ================");
                        System.out.println("MIPS omitido por errores semanticos.");
                    }
                }
            }

            return result;

        } catch (IOException e) {
            result.readErrors.add("Error: no se pudo leer el archivo " + sourceFilePath);
            result.readErrors.add(e.getMessage());
            return result;
        }
    }

    private static void writeAssemblyFile(String sourceFilePath, CompilerOptions options, List<String> mipsLines) {
        String outputPath = options.outputPath;

        if (outputPath == null || outputPath.isBlank()) {
            outputPath = sourceFilePath.replaceAll("\\.mc$", "") + ".s";
        }

        try {
            Files.write(Path.of(outputPath), mipsLines, StandardCharsets.UTF_8);
            System.out.println();
            System.out.println("Archivo MIPS32 generado: " + outputPath);
        } catch (IOException e) {
            System.out.println();
            System.out.println("Error: no se pudo escribir el archivo MIPS32 " + outputPath);
            System.out.println(e.getMessage());
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

        System.out.println("Tabla de simbolos: generada");

        if (result.semanticErrors.isEmpty()) {
            System.out.println("Analisis semantico: correcto");
        } else {
            System.out.println("Analisis semantico: con errores");
        }
    }

    private static boolean runTests() {
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
        options.showTac = false;
        options.showOptimizedTac = false;
        options.showMips = false;

        for (String testFile : TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);
            boolean expectedCorrect = testFile.contains("/correct/") || testFile.contains("\\correct\\");

            boolean passed;

            if (expectedCorrect) {
                passed = result.readErrors.isEmpty() && result.lexicalSuccess && result.syntaxSuccess && result.semanticErrors.isEmpty();
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

        return failedCount == 0;
    }

    private static boolean runSemanticTests() {
        System.out.println();
        System.out.println("================ EJECUCION DE PRUEBAS SEMANTICAS ================");

        int successCount = 0;
        int failedCount = 0;

        CompilerOptions options = new CompilerOptions();
        options.showTokens = false;
        options.showTree = false;
        options.showAst = false;
        options.showSymbols = false;
        options.showLexicalSummary = false;
        options.showSyntaxSummary = false;
        options.showTac = false;
        options.showOptimizedTac = false;
        options.showMips = false;

        System.out.println();
        System.out.println("================ PROGRAMAS SEMANTICAMENTE CORRECTOS ================");

        for (String testFile : SEMANTIC_CORRECT_TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);

            boolean passed = result.readErrors.isEmpty()
                    && result.lexicalSuccess
                    && result.syntaxSuccess
                    && result.semanticErrors.isEmpty();

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-80s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("================ PROGRAMAS CON ERRORES SEMANTICOS ================");

        for (String testFile : SEMANTIC_ERROR_TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);

            boolean passed = result.readErrors.isEmpty()
                    && result.lexicalSuccess
                    && result.syntaxSuccess
                    && !result.semanticErrors.isEmpty();

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-80s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("Pruebas semanticas correctas: " + successCount);
        System.out.println("Pruebas semanticas fallidas: " + failedCount);

        return failedCount == 0;
    }

    private static boolean runTacTests() {
        System.out.println();
        System.out.println("================ EJECUCION DE PRUEBAS TAC ================");

        int successCount = 0;
        int failedCount = 0;

        CompilerOptions options = new CompilerOptions();
        options.showTokens = false;
        options.showTree = false;
        options.showAst = false;
        options.showSymbols = false;
        options.showLexicalSummary = false;
        options.showSyntaxSummary = false;
        options.showTac = true;
        options.showOptimizedTac = false;
        options.showMips = false;

        for (String testFile : TAC_TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);

            boolean passed = result.readErrors.isEmpty()
                    && result.lexicalSuccess
                    && result.syntaxSuccess
                    && result.semanticErrors.isEmpty();

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-80s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("Pruebas TAC correctas: " + successCount);
        System.out.println("Pruebas TAC fallidas: " + failedCount);

        return failedCount == 0;
    }

    private static boolean runTacOptTests() {
        System.out.println();
        System.out.println("================ EJECUCION DE PRUEBAS TAC OPTIMIZADO ================");

        int successCount = 0;
        int failedCount = 0;

        CompilerOptions options = new CompilerOptions();
        options.showTokens = false;
        options.showTree = false;
        options.showAst = false;
        options.showSymbols = false;
        options.showLexicalSummary = false;
        options.showSyntaxSummary = false;
        options.showTac = true;
        options.showOptimizedTac = true;
        options.showMips = false;

        for (String testFile : TAC_OPT_TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);

            boolean passed = result.readErrors.isEmpty()
                    && result.lexicalSuccess
                    && result.syntaxSuccess
                    && result.semanticErrors.isEmpty();

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-80s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("Pruebas TAC optimizado correctas: " + successCount);
        System.out.println("Pruebas TAC optimizado fallidas: " + failedCount);

        return failedCount == 0;
    }

    private static boolean runMipsTests() {
        System.out.println();
        System.out.println("================ EJECUCION DE PRUEBAS MIPS32 ================");

        int successCount = 0;
        int failedCount = 0;

        CompilerOptions options = new CompilerOptions();
        options.showTokens = false;
        options.showTree = false;
        options.showAst = false;
        options.showSymbols = false;
        options.showLexicalSummary = false;
        options.showSyntaxSummary = false;
        options.showTac = false;
        options.showOptimizedTac = false;
        options.showMips = true;

        for (String testFile : MIPS_TEST_FILES) {
            CompilationResult result = analyzeFile(testFile, options);

            boolean passed = result.readErrors.isEmpty()
                    && result.lexicalSuccess
                    && result.syntaxSuccess
                    && result.semanticErrors.isEmpty();

            if (passed) {
                successCount++;
            } else {
                failedCount++;
            }

            System.out.printf("%-80s %s%n", testFile, passed ? "OK" : "FALLO");
        }

        System.out.println();
        System.out.println("Pruebas MIPS32 correctas: " + successCount);
        System.out.println("Pruebas MIPS32 fallidas: " + failedCount);

        return failedCount == 0;
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
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --tac\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --tac-opt\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --mips\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc -S -o salida.s\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc -S -o salida.s -O --dump-ir\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --fase1\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --fase2\"");
        System.out.println(".\\gradlew.bat run --args=\"ruta/archivo.mc --all\"");
        System.out.println(".\\gradlew.bat run --args=\"--test\"");
        System.out.println(".\\gradlew.bat run --args=\"--semantic-test\"");
        System.out.println(".\\gradlew.bat run --args=\"--fase2-test\"");
        System.out.println(".\\gradlew.bat run --args=\"--tac-test\"");
        System.out.println(".\\gradlew.bat run --args=\"--tac-opt-test\"");
        System.out.println(".\\gradlew.bat run --args=\"--mips-test\"");
    }

    private static boolean hasArgument(String[] args, String expectedArgument) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(expectedArgument)) {
                return true;
            }
        }

        return false;
    }

    private static String getArgumentValue(String[] args, String argumentName) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase(argumentName)) {
                return args[i + 1];
            }
        }

        return null;
    }

    /**
     * Devuelve el primer argumento que sea una ruta (no una opcion).
     * Ignora flags con - o -- y salta el valor que sigue a -o.
     */
    private static String firstNonFlag(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-o")) {
                i++; // saltar el valor de -o (la ruta de salida)
                continue;
            }

            if (arg.startsWith("-")) {
                continue;
            }

            return arg;
        }

        return null;
    }

    private static class CompilerOptions {
        private boolean showTokens;
        private boolean showTree;
        private boolean showAst;
        private boolean showSymbols;
        private boolean showLexicalSummary;
        private boolean showSyntaxSummary;
        private boolean showTac;
        private boolean showOptimizedTac;
        private boolean showMips;
        private boolean writeAssembly;
        private boolean optimize;
        private boolean dumpIr;
        private String outputPath;

        private static CompilerOptions fromArgs(String[] args) {
            CompilerOptions options = new CompilerOptions();

            boolean fase1 = hasArgument(args, "--fase1");
            boolean fase2 = hasArgument(args, "--fase2");

            options.writeAssembly = hasArgument(args, "-S");
            options.optimize = hasArgument(args, "-O") || hasArgument(args, "--tac-opt") || hasArgument(args, "--mips");
            options.dumpIr = hasArgument(args, "--dump-ir");
            options.outputPath = getArgumentValue(args, "-o");

            options.showTokens = fase1 || hasArgument(args, "--tokens") || hasArgument(args, "--all");
            options.showTree = fase1 || hasArgument(args, "--tree") || hasArgument(args, "--all");
            options.showAst = fase1 || hasArgument(args, "--ast") || hasArgument(args, "--all");
            options.showSymbols = fase1 || fase2 || hasArgument(args, "--symbols") || hasArgument(args, "--all");
            options.showLexicalSummary = fase1 || hasArgument(args, "--lexical-summary") || hasArgument(args, "--all");
            options.showSyntaxSummary = fase1 || hasArgument(args, "--syntax-summary") || hasArgument(args, "--all");
            options.showTac = hasArgument(args, "--tac") || hasArgument(args, "--tac-opt") || options.dumpIr || hasArgument(args, "--all");
            options.showOptimizedTac = hasArgument(args, "--tac-opt") || hasArgument(args, "--all");
            options.showMips = hasArgument(args, "--mips") || hasArgument(args, "--all");

            if (!options.showTokens && !options.showTree && !options.showAst && !options.showSymbols
                    && !options.showLexicalSummary && !options.showSyntaxSummary && !options.showTac
                    && !options.showOptimizedTac && !options.showMips && !options.writeAssembly && !options.dumpIr) {
                options.showTokens = true;
                options.showLexicalSummary = true;
                options.showTree = true;
                options.showSyntaxSummary = true;
                options.showAst = true;
                options.showSymbols = true;
                options.showTac = false;
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
        private final List<String> semanticErrors = new ArrayList<>();

        private CompilationResult(String filePath) {
            this.filePath = filePath;
        }

        boolean isSuccess() {
            return readErrors.isEmpty()
                    && lexicalSuccess
                    && syntaxSuccess
                    && semanticErrors.isEmpty();
        }
    }
}