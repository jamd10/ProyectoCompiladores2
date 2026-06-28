package compiler.gui;

import compiler.SyntaxErrorListener;
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
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GuiMain extends JFrame {
    private final JTextArea sourceEditor = new JTextArea();
    private final JTextArea lineNumbers = new JTextArea("1");
    private final JTabbedPane outputTabs = new JTabbedPane();
    private final Map<String, JTextArea> outputAreas = new LinkedHashMap<>();
    private final JLabel statusLabel = new JLabel("listo");
    private final JCheckBox optimizeCheck = new JCheckBox("optimizar tac (-O)", true);

    private File currentFile;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyLookAndFeel();

            GuiMain gui = new GuiMain();
            gui.setVisible(true);
        });
    }

    public GuiMain() {
        super("Mini-C Compiler Studio");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);

        buildLayout();
        installShortcuts();
        loadSampleCode();
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // si nimbus no esta disponible, swing usa el estilo por defecto
        }
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(24, 24, 28));

        root.add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildEditorPanel(), buildOutputPanel());
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        root.add(splitPane, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridLayout(1, 0, 8, 0));
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        toolbar.setBackground(new Color(34, 34, 40));

        toolbar.add(createButton("abrir .mc", this::openFile));
        toolbar.add(createButton("guardar", this::saveFile));
        toolbar.add(createButton("fase 1", () -> runAnalysis(AnalysisMode.FASE1)));
        toolbar.add(createButton("fase 2", () -> runAnalysis(AnalysisMode.FASE2)));
        toolbar.add(createButton("tac", () -> runAnalysis(AnalysisMode.TAC)));
        toolbar.add(createButton("tac opt", () -> runAnalysis(AnalysisMode.TAC_OPT)));
        toolbar.add(createButton("mips", () -> runAnalysis(AnalysisMode.MIPS)));
        toolbar.add(createButton("generar .s", this::exportAssembly));
        toolbar.add(optimizeCheck);

        optimizeCheck.setBackground(new Color(34, 34, 40));
        optimizeCheck.setForeground(Color.WHITE);
        optimizeCheck.setFocusPainted(false);

        return toolbar;
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.addActionListener(event -> action.run());
        return button;
    }

    private JPanel buildEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("codigo fuente .mc"));

        sourceEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        sourceEditor.setTabSize(4);
        sourceEditor.setLineWrap(false);
        sourceEditor.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateLineNumbers));

        lineNumbers.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        lineNumbers.setBackground(new Color(38, 38, 45));
        lineNumbers.setForeground(new Color(170, 170, 180));
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scrollPane = new JScrollPane(sourceEditor);
        scrollPane.setRowHeaderView(lineNumbers);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("salidas del compilador"));

        addOutputTab("resumen");
        addOutputTab("tokens");
        addOutputTab("lexico");
        addOutputTab("parse tree");
        addOutputTab("sintactico");
        addOutputTab("ast");
        addOutputTab("simbolos");
        addOutputTab("semantica");
        addOutputTab("tac");
        addOutputTab("tac opt");
        addOutputTab("mips");

        panel.add(outputTabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        panel.setBackground(new Color(34, 34, 40));

        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void addOutputTab(String title) {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        area.setLineWrap(false);

        outputAreas.put(title, area);
        outputTabs.addTab(title, new JScrollPane(area));
    }

    private void installShortcuts() {
        sourceEditor.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveFile");
        sourceEditor.getActionMap().put("saveFile", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        sourceEditor.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "runFase1");
        sourceEditor.getActionMap().put("runFase1", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAnalysis(AnalysisMode.FASE1);
            }
        });
    }

    private void loadSampleCode() {
        sourceEditor.setText(
                "int suma(int a, int b) {\n" +
                "    int r;\n" +
                "    r = a + b;\n" +
                "    return r;\n" +
                "}\n\n" +
                "int main() {\n" +
                "    int x = 10;\n" +
                "    int y = 20;\n" +
                "    int z;\n\n" +
                "    z = suma(x, y);\n\n" +
                "    print_str(\"resultado = \");\n" +
                "    print_int(z);\n" +
                "    println();\n\n" +
                "    return 0;\n" +
                "}\n"
        );
        updateLineNumbers();
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("archivos Mini-C (*.mc)", "mc"));

        if (currentFile != null) {
            chooser.setCurrentDirectory(currentFile.getParentFile());
        }

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            currentFile = chooser.getSelectedFile();
            sourceEditor.setText(Files.readString(currentFile.toPath(), StandardCharsets.UTF_8));
            updateLineNumbers();
            status("archivo abierto: " + currentFile.getName());
        } catch (IOException e) {
            showError("no se pudo abrir el archivo", e);
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("archivos Mini-C (*.mc)", "mc"));

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            currentFile = ensureExtension(chooser.getSelectedFile(), ".mc");
        }

        try {
            Files.writeString(currentFile.toPath(), sourceEditor.getText(), StandardCharsets.UTF_8);
            status("archivo guardado: " + currentFile.getName());
        } catch (IOException e) {
            showError("no se pudo guardar el archivo", e);
        }
    }

    private File ensureExtension(File file, String extension) {
        if (file.getName().toLowerCase().endsWith(extension)) {
            return file;
        }

        return new File(file.getParentFile(), file.getName() + extension);
    }

    private void runAnalysis(AnalysisMode mode) {
        clearOutputs();
        status("analizando...");

        SwingWorker<CompilationView, Void> worker = new SwingWorker<>() {
            @Override
            protected CompilationView doInBackground() {
                return compile(sourceEditor.getText(), mode);
            }

            @Override
            protected void done() {
                try {
                    CompilationView view = get();
                    renderCompilation(view);
                    status(view.success ? "analisis completado correctamente" : "analisis completado con errores");
                } catch (Exception e) {
                    showError("fallo la ejecucion del compilador", e);
                    status("error");
                }
            }
        };

        worker.execute();
    }

    private void exportAssembly() {
        clearOutputs();
        status("generando mips...");

        SwingWorker<CompilationView, Void> worker = new SwingWorker<>() {
            @Override
            protected CompilationView doInBackground() {
                return compile(sourceEditor.getText(), AnalysisMode.MIPS);
            }

            @Override
            protected void done() {
                try {
                    CompilationView view = get();
                    renderCompilation(view);

                    if (!view.success || view.mipsLines.isEmpty()) {
                        status("no se genero el archivo .s por errores");
                        return;
                    }

                    JFileChooser chooser = new JFileChooser();

                    if (currentFile != null) {
                        String name = currentFile.getName().replaceAll("\\.mc$", ".s");
                        chooser.setSelectedFile(new File(currentFile.getParentFile(), name));
                    } else {
                        chooser.setSelectedFile(new File("programa.s"));
                    }

                    if (chooser.showSaveDialog(GuiMain.this) != JFileChooser.APPROVE_OPTION) {
                        status("generacion cancelada");
                        return;
                    }

                    File outputFile = ensureExtension(chooser.getSelectedFile(), ".s");
                    Files.write(outputFile.toPath(), view.mipsLines, StandardCharsets.UTF_8);
                    status("archivo generado: " + outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(GuiMain.this,
                            "archivo MIPS32 generado:\n" + outputFile.getAbsolutePath(),
                            "generacion completada",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    showError("no se pudo generar el archivo .s", e);
                    status("error");
                }
            }
        };

        worker.execute();
    }

    private CompilationView compile(String source, AnalysisMode mode) {
        CompilationView view = new CompilationView();

        try {
            CharStream input = CharStreams.fromString(source);

            MiniCLexer lexer = new MiniCLexer(input);
            SyntaxErrorListener lexerErrorListener = new SyntaxErrorListener();

            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            view.outputs.put("tokens", formatTokens(tokens));
            view.outputs.put("lexico", captureOutput(() -> LexicalSummary.fromTokens(tokens).print()));

            List<String> lexicalErrors = getLexicalErrors(tokens, lexerErrorListener);

            if (!lexicalErrors.isEmpty()) {
                view.outputs.put("semantica", formatErrors("ERRORES LEXICOS", lexicalErrors));
                view.outputs.put("resumen", buildSummary(false, false, false, "errores lexicos encontrados"));
                view.success = false;
                return view;
            }

            MiniCParser parser = new MiniCParser(tokens);
            SyntaxErrorListener parserErrorListener = new SyntaxErrorListener();

            parser.removeErrorListeners();
            parser.addErrorListener(parserErrorListener);

            ParseTree tree = parser.program();

            if (parserErrorListener.hasErrors()) {
                view.outputs.put("semantica", formatErrors("ERRORES SINTACTICOS", parserErrorListener.getErrors()));
                view.outputs.put("resumen", buildSummary(true, false, false, "errores sintacticos encontrados"));
                view.success = false;
                return view;
            }

            view.outputs.put("parse tree", prettyParseTree(tree, parser));
            view.outputs.put("sintactico", captureOutput(() -> {
                SyntaxSummaryVisitor syntaxSummaryVisitor = new SyntaxSummaryVisitor();
                syntaxSummaryVisitor.visit(tree);
                syntaxSummaryVisitor.print();
            }));
            view.outputs.put("ast", captureOutput(() -> {
                AstPrinterVisitor astPrinterVisitor = new AstPrinterVisitor();
                astPrinterVisitor.visit(tree);
                System.out.print(astPrinterVisitor.getOutput());
            }));

            SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder();
            symbolTableBuilder.visit(tree);

            view.outputs.put("simbolos", captureOutput(() -> symbolTableBuilder.getSymbolTable().print()));

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.visit(tree);

            List<String> semanticErrors = new ArrayList<>();
            semanticErrors.addAll(symbolTableBuilder.getErrors());
            semanticErrors.addAll(semanticAnalyzer.getErrors());

            if (!semanticErrors.isEmpty()) {
                view.outputs.put("semantica", formatErrors("ERRORES SEMANTICOS", semanticErrors));
                view.outputs.put("resumen", buildSummary(true, true, false, "errores semanticos encontrados"));
                view.success = false;
                return view;
            }

            view.outputs.put("semantica", "sin errores semanticos.\n");

            if (mode == AnalysisMode.TAC || mode == AnalysisMode.TAC_OPT || mode == AnalysisMode.MIPS) {
                TacGenerator tacGenerator = new TacGenerator();
                tacGenerator.visit(tree);

                List<TacInstruction> tacInstructions = tacGenerator.getInstructions();
                view.outputs.put("tac", formatTac(tacInstructions));

                List<TacInstruction> optimizedInstructions = tacInstructions;

                if (mode == AnalysisMode.TAC_OPT || mode == AnalysisMode.MIPS || optimizeCheck.isSelected()) {
                    TacOptimizer tacOptimizer = new TacOptimizer();
                    optimizedInstructions = tacOptimizer.optimize(tacInstructions);
                    view.outputs.put("tac opt", formatTac(optimizedInstructions));
                }

                if (mode == AnalysisMode.MIPS) {
                    MipsGenerator mipsGenerator = new MipsGenerator();
                    view.mipsLines = mipsGenerator.generate(optimizedInstructions);
                    view.outputs.put("mips", String.join(System.lineSeparator(), view.mipsLines));
                }
            }

            view.outputs.put("resumen", buildSummary(true, true, true, "compilacion correcta"));
            view.success = true;
            return view;

        } catch (Exception e) {
            view.outputs.put("resumen", "error interno del gui:\n" + e.getMessage());
            view.success = false;
            return view;
        }
    }

    private String formatTokens(CommonTokenStream tokens) {
        StringBuilder output = new StringBuilder();

        output.append("================ TOKENS ================\n");
        output.append(String.format("%-22s %-30s %-10s %-10s%n", "TOKEN", "LEXEMA", "LINEA", "COLUMNA"));
        output.append("----------------------------------------------------------------------------\n");

        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF) {
                continue;
            }

            String symbolicName = MiniCLexer.VOCABULARY.getSymbolicName(token.getType());

            if (symbolicName == null) {
                symbolicName = MiniCLexer.VOCABULARY.getLiteralName(token.getType());
            }

            output.append(String.format("%-22s %-30s %-10d %-10d%n",
                    symbolicName,
                    "'" + formatLexeme(token.getText()) + "'",
                    token.getLine(),
                    token.getCharPositionInLine()));
        }

        return output.toString();
    }

    private String prettyParseTree(ParseTree tree, Parser parser) {
        StringBuilder output = new StringBuilder();

        output.append("================ PARSE TREE ANTLR ================\n");
        appendPrettyTree(tree, parser, output, 0);

        return output.toString();
    }

    private void appendPrettyTree(ParseTree node, Parser parser, StringBuilder output, int level) {
        String indent = "  ".repeat(level);

        if (node instanceof ErrorNode) {
            output.append(indent).append("ERROR: ").append(formatLexeme(node.getText())).append("\n");
            return;
        }

        if (node instanceof TerminalNode) {
            Token token = ((TerminalNode) node).getSymbol();

            if (token.getType() == Token.EOF) {
                output.append(indent).append("EOF\n");
                return;
            }

            String tokenName = parser.getVocabulary().getSymbolicName(token.getType());

            if (tokenName == null) {
                tokenName = parser.getVocabulary().getLiteralName(token.getType());
            }

            output.append(indent)
                    .append(tokenName)
                    .append(": '")
                    .append(formatLexeme(token.getText()))
                    .append("'\n");
            return;
        }

        if (node instanceof RuleNode) {
            int ruleIndex = ((RuleNode) node).getRuleContext().getRuleIndex();
            output.append(indent).append(parser.getRuleNames()[ruleIndex]).append("\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            appendPrettyTree(node.getChild(i), parser, output, level + 1);
        }
    }

    private String formatTac(List<TacInstruction> instructions) {
        StringBuilder output = new StringBuilder();

        for (TacInstruction instruction : instructions) {
            output.append(instruction).append("\n");
        }

        return output.toString();
    }

    private List<String> getLexicalErrors(CommonTokenStream tokens, SyntaxErrorListener lexerErrorListener) {
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

    private String formatErrors(String title, List<String> errors) {
        StringBuilder output = new StringBuilder();

        output.append("================ ").append(title).append(" ================\n");

        for (String error : errors) {
            output.append(error).append("\n");
        }

        return output.toString();
    }

    private String buildSummary(boolean lexicalOk, boolean syntaxOk, boolean semanticOk, String message) {
        StringBuilder output = new StringBuilder();

        output.append("================ RESUMEN FINAL ================\n");
        output.append("Analisis lexico: ").append(lexicalOk ? "correcto" : "con errores").append("\n");

        if (lexicalOk) {
            output.append("Analisis sintactico: ").append(syntaxOk ? "correcto" : "con errores").append("\n");
        }

        if (lexicalOk && syntaxOk) {
            output.append("Tabla de simbolos: generada\n");
            output.append("Analisis semantico: ").append(semanticOk ? "correcto" : "con errores").append("\n");
        }

        output.append("Estado: ").append(message).append("\n");

        return output.toString();
    }

    private String captureOutput(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(buffer));
            runnable.run();
        } finally {
            System.setOut(originalOut);
        }

        return buffer.toString();
    }

    private String formatLexeme(String text) {
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

    private void renderCompilation(CompilationView view) {
        for (Map.Entry<String, String> entry : view.outputs.entrySet()) {
            JTextArea area = outputAreas.get(entry.getKey());

            if (area != null) {
                area.setText(entry.getValue());
                area.setCaretPosition(0);
            }
        }

        outputTabs.setSelectedIndex(0);
    }

    private void clearOutputs() {
        for (JTextArea area : outputAreas.values()) {
            area.setText("");
        }
    }

    private void updateLineNumbers() {
        int lines = Math.max(1, sourceEditor.getLineCount());
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= lines; i++) {
            builder.append(i).append("\n");
        }

        lineNumbers.setText(builder.toString());
    }

    private void status(String text) {
        statusLabel.setText(text);
    }

    private void showError(String title, Exception e) {
        JOptionPane.showMessageDialog(this,
                title + "\n" + e.getMessage(),
                "error",
                JOptionPane.ERROR_MESSAGE);
    }

    private enum AnalysisMode {
        FASE1,
        FASE2,
        TAC,
        TAC_OPT,
        MIPS
    }

    private static class CompilationView {
        private final Map<String, String> outputs = new LinkedHashMap<>();
        private List<String> mipsLines = new ArrayList<>();
        private boolean success;
    }

    private interface SimpleChangeHandler {
        void changed();
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final SimpleChangeHandler handler;

        private SimpleDocumentListener(SimpleChangeHandler handler) {
            this.handler = handler;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            handler.changed();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            handler.changed();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            handler.changed();
        }
    }
}
