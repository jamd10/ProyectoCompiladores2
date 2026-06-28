package compiler;

import compiler.analysis.LexicalSummary;
import compiler.analysis.SyntaxSummaryVisitor;
import compiler.ast.AstPrinterVisitor;
import compiler.parser.MiniCLexer;
import compiler.parser.MiniCParser;
import compiler.semantic.SymbolTableBuilder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.*;

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
        if (args.length == 0) {
            printUsage();
            return;
        }

        if (hasArg(args, "--test")) {
            boolean allPassed = runTests();
            System.exit(allPassed ? 0 : 1);
            return;
        }

        String file = firstNonFlag(args);
        if (file == null) {
            System.out.println("Error: no se indico un archivo .mc de entrada.\n");
            printUsage();
            System.exit(2);
            return;
        }

        CompilerOptions opt = CompilerOptions.from(args);

        CompilationResult result = analyze(file, opt);
        printSummary(result);

        System.exit(result.isSuccess() ? 0 : 1);
    }

    // ================= ANALISIS =================

    private static CompilationResult analyze(String file, CompilerOptions opt) {
        CompilationResult r = new CompilationResult(file);

        try {
            CharStream input = CharStreams.fromFileName(file);

            MiniCLexer lexer = new MiniCLexer(input);
            SyntaxErrorListener lexerErrors = attachErrors(lexer);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            r.lexicalErrors.addAll(collectLexicalErrors(tokens, lexerErrors));

            if (!r.lexicalErrors.isEmpty()) {
                r.lexicalSuccess = false;
                if (!opt.quiet) printErrors("ERRORES LEXICOS", r.lexicalErrors);
                return r;
            }
            r.lexicalSuccess = true;

            if (opt.showTokens) printTokens(tokens);
            if (opt.showLexicalSummary) LexicalSummary.fromTokens(tokens).print();

            MiniCParser parser = new MiniCParser(tokens);
            SyntaxErrorListener parserErrors = attachErrors(parser);

            ParseTree tree = parser.program();

            if (parserErrors.hasErrors()) {
                r.syntaxErrors.addAll(parserErrors.getErrors());
                r.syntaxSuccess = false;
                if (!opt.quiet) printErrors("ERRORES SINTACTICOS", r.syntaxErrors);
                return r;
            }

            r.syntaxSuccess = true;

            if (opt.showTree) {
                System.out.println("\n=== PARSE TREE ===");
                System.out.println(tree.toStringTree(parser));
            }

            if (opt.showSyntaxSummary) {
                SyntaxSummaryVisitor v = new SyntaxSummaryVisitor();
                v.visit(tree);
                v.print();
            }

            if (opt.showAst) {
                System.out.println("\n=== AST ===");
                AstPrinterVisitor v = new AstPrinterVisitor();
                v.visit(tree);
                System.out.print(v.getOutput());
            }

            SymbolTableBuilder st = new SymbolTableBuilder();
            st.visit(tree);

            r.symbolErrors.addAll(st.getErrors());

            if (opt.showSymbols) {
                st.getSymbolTable().print();
            }

            if (!r.symbolErrors.isEmpty() && !opt.quiet) {
                printErrors("ERRORES DE SIMBOLOS", r.symbolErrors);
            }

            return r;

        } catch (IOException e) {
            r.readErrors.add("No se pudo leer: " + file);
            r.readErrors.add(e.getMessage());
            return r;
        }
    }

    // ================= LEXICAL ERRORS =================

    private static List<String> collectLexicalErrors(CommonTokenStream tokens, SyntaxErrorListener listener) {
        List<String> errors = new ArrayList<>();

        for (Token t : tokens.getTokens()) {
            String msg = switch (t.getType()) {
                case MiniCLexer.ERROR_CHAR ->
                        "simbolo no reconocido: " + t.getText();
                case MiniCLexer.UnclosedString ->
                        "cadena sin cerrar";
                case MiniCLexer.UnclosedChar ->
                        "caracter sin cerrar";
                case MiniCLexer.InvalidCharLiteral ->
                        "literal char invalido: " + t.getText();
                case MiniCLexer.UnclosedBlockComment ->
                        "comentario de bloque sin cerrar";
                default -> null;
            };

            if (msg != null) {
                errors.add(formatError("lexico", t, msg));
            }
        }

        errors.addAll(listener.getErrors());
        return errors;
    }

    private static String formatError(String type, Token t, String msg) {
        return "Error " + type + " linea " + t.getLine() + ":" +
                t.getCharPositionInLine() + " - " + msg;
    }

    // ================= PRINT =================

    private static void printTokens(CommonTokenStream tokens) {
        System.out.println("\n=== TOKENS ===");
        System.out.printf("%-20s %-30s %-10s %-10s%n", "TOKEN", "LEXEMA", "LINEA", "COL");

        for (Token t : tokens.getTokens()) {
            if (t.getType() == Token.EOF) continue;

            String name = MiniCLexer.VOCABULARY.getSymbolicName(t.getType());
            if (name == null) name = MiniCLexer.VOCABULARY.getLiteralName(t.getType());

            System.out.printf("%-20s %-30s %-10d %-10d%n",
                    name, "'" + escape(t.getText()) + "'", t.getLine(), t.getCharPositionInLine());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\r", "\\r");
    }

    private static void printErrors(String title, List<String> errors) {
        System.out.println("\n=== " + title + " ===");
        errors.forEach(System.out::println);
    }

    private static void printSummary(CompilationResult r) {
        System.out.println("\n=== RESUMEN ===");
        System.out.println("Archivo: " + r.filePath);

        if (!r.readErrors.isEmpty()) {
            System.out.println("Error de lectura");
            r.readErrors.forEach(System.out::println);
            return;
        }

        System.out.println("Lexico: " + status(r.lexicalSuccess));
        if (!r.lexicalSuccess) return;

        System.out.println("Sintaxis: " + status(r.syntaxSuccess));
        if (!r.syntaxSuccess) return;

        System.out.println("Simbolos: " +
                (r.symbolErrors.isEmpty() ? "OK" : "con errores"));
    }

    private static String status(boolean ok) {
        return ok ? "OK" : "ERROR";
    }

    // ================= TESTS =================

    private static boolean runTests() {
        System.out.println("\n=== TESTS ===");

        CompilerOptions opt = CompilerOptions.quiet();

        int ok = 0, fail = 0;

        for (String file : TEST_FILES) {
            CompilationResult r = analyze(file, opt);

            boolean expectedOk = file.contains("/correct/");
            boolean passed = expectedOk
                    ? r.lexicalSuccess && r.syntaxSuccess
                    : (!r.lexicalSuccess || !r.syntaxSuccess);

            if (passed) ok++; else fail++;

            System.out.printf("%-60s %s%n", file, passed ? "OK" : "FAIL");
        }

        System.out.println("\nOK: " + ok);
        System.out.println("FAIL: " + fail);

        return fail == 0;
    }

    // ================= UTILS =================

    private static boolean hasArg(String[] args, String a) {
        for (String s : args) if (s.equalsIgnoreCase(a)) return true;
        return false;
    }

    /** Devuelve el primer argumento que no sea una opcion (no empieza con --), o null. */
    private static String firstNonFlag(String[] args) {
        for (String a : args) {
            if (!a.startsWith("--")) return a;
        }
        return null;
    }

    private static SyntaxErrorListener attachErrors(Recognizer<?, ?> recognizer) {
        SyntaxErrorListener listener = new SyntaxErrorListener();
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(listener);
        return listener;
    }

    private static void printUsage() {
        System.out.println("Uso: java compiler.Main <archivo.mc> [opciones]");
        System.out.println();
        System.out.println("Opciones:");
        System.out.println("  --tokens            Muestra la tabla de tokens");
        System.out.println("  --tree              Muestra el parse tree");
        System.out.println("  --ast               Muestra el AST");
        System.out.println("  --symbols           Muestra la tabla de simbolos");
        System.out.println("  --lexical-summary   Muestra el resumen lexico");
        System.out.println("  --syntax-summary    Muestra el resumen sintactico");
        System.out.println("  --all               Activa todas las salidas");
        System.out.println("  --test              Ejecuta la bateria de pruebas");
        System.out.println();
        System.out.println("Sin opciones se muestran tokens, AST y simbolos.");
    }

    // ================= OPTIONS =================

    private static class CompilerOptions {
        boolean showTokens, showTree, showAst, showSymbols, showLexicalSummary, showSyntaxSummary;
        boolean quiet;

        static CompilerOptions from(String[] args) {
            CompilerOptions o = new CompilerOptions();

            boolean all = hasArg(args, "--all");

            o.showTokens = all || hasArg(args, "--tokens");
            o.showTree = all || hasArg(args, "--tree");
            o.showAst = all || hasArg(args, "--ast");
            o.showSymbols = all || hasArg(args, "--symbols");
            o.showLexicalSummary = all || hasArg(args, "--lexical-summary");
            o.showSyntaxSummary = all || hasArg(args, "--syntax-summary");

            if (!o.showTokens && !o.showTree && !o.showAst && !o.showSymbols
                    && !o.showLexicalSummary && !o.showSyntaxSummary) {
                o.showTokens = o.showAst = o.showSymbols = true;
            }

            return o;
        }

        /** Opciones para tests: sin salida de debug y sin imprimir errores. */
        static CompilerOptions quiet() {
            CompilerOptions o = new CompilerOptions();
            o.quiet = true;
            return o;
        }
    }

    // ================= RESULT =================

    private static class CompilationResult {
        String filePath;
        boolean lexicalSuccess;
        boolean syntaxSuccess;

        List<String> readErrors = new ArrayList<>();
        List<String> lexicalErrors = new ArrayList<>();
        List<String> syntaxErrors = new ArrayList<>();
        List<String> symbolErrors = new ArrayList<>();

        CompilationResult(String filePath) {
            this.filePath = filePath;
        }

        boolean isSuccess() {
            return readErrors.isEmpty()
                    && lexicalSuccess
                    && syntaxSuccess
                    && symbolErrors.isEmpty();
        }
    }
}