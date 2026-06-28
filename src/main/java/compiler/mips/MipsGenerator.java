package compiler.mips;

import compiler.ir.TacInstruction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MipsGenerator {
    private final List<String> lines = new ArrayList<>();
    private final Map<String, VarInfo> globals = new LinkedHashMap<>();
    private final Map<String, String> stringLabels = new LinkedHashMap<>();
    private final List<FunctionInfo> functions = new ArrayList<>();
    private final List<String> pendingParams = new ArrayList<>();

    private FunctionInfo currentFunction;
    private int stringCounter = 0;

    public List<String> generate(List<TacInstruction> instructions) {
        lines.clear();
        globals.clear();
        stringLabels.clear();
        functions.clear();
        pendingParams.clear();
        currentFunction = null;
        stringCounter = 0;

        prepareProgram(instructions);
        emitDataSection();
        emitTextSection();

        return lines;
    }

    private void prepareProgram(List<TacInstruction> instructions) {
        FunctionInfo function = null;

        for (TacInstruction instruction : instructions) {
            String text = instruction.toString().trim();

            if (text.isEmpty()) {
                continue;
            }

            collectStringLiterals(text);

            if (text.startsWith("func ")) {
                function = new FunctionInfo(getFunctionName(text));
                functions.add(function);
                continue;
            }

            if (text.startsWith("endfunc ")) {
                function = null;
                continue;
            }

            if (function == null) {
                if (text.startsWith("decl ")) {
                    VarInfo global = parseDeclaration(text, true, false);
                    globals.put(global.name, global);
                }

                continue;
            }

            function.body.add(text);

            if (text.startsWith("paramdecl ")) {
                VarInfo parameter = parseParamDeclaration(text);
                function.addLocal(parameter);
                function.parameters.add(parameter);
                continue;
            }

            if (text.startsWith("decl ")) {
                function.addLocal(parseDeclaration(text, false, false));
                continue;
            }

            collectTemporary(function, text);
        }

        for (FunctionInfo info : functions) {
            info.calculateFrame();
        }
    }

    private void collectTemporary(FunctionInfo function, String text) {
        if (!text.contains(" = ")) {
            return;
        }

        String target = text.split(" = ", 2)[0].trim();

        if (target.matches("t\\d+")) {
            function.addLocal(new VarInfo(target, "int", false, false, false));
        }
    }

    private VarInfo parseDeclaration(String text, boolean global, boolean parameter) {
        String rest = text.substring("decl ".length()).trim();
        String[] parts = rest.split("\\s+", 2);

        String type = parts[0].trim();
        String name = parts[1].trim();

        return new VarInfo(name, type, global, parameter, isArrayType(type));
    }

    private VarInfo parseParamDeclaration(String text) {
        String rest = text.substring("paramdecl ".length()).trim();
        String[] parts = rest.split("\\s+", 2);

        String type = parts[0].trim();
        String name = parts[1].trim();

        return new VarInfo(name, type, false, true, isArrayType(type));
    }

    private void collectStringLiterals(String text) {
        for (String literal : findStringLiterals(text)) {
            getStringLabel(literal);
        }
    }

    private List<String> findStringLiterals(String text) {
        List<String> literals = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inside = false;
        boolean escaping = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (!inside) {
                if (c == '"') {
                    inside = true;
                    current.setLength(0);
                    current.append(c);
                }

                continue;
            }

            current.append(c);

            if (escaping) {
                escaping = false;
                continue;
            }

            if (c == '\\') {
                escaping = true;
                continue;
            }

            if (c == '"') {
                inside = false;
                literals.add(current.toString());
            }
        }

        return literals;
    }

    private void emitDataSection() {
        lines.add(".data");
        lines.add("# datos globales y literales de cadena");

        lines.add("_str_newline: .asciiz \"\\n\"");
        lines.add("_str_true: .asciiz \"true\"");
        lines.add("_str_false: .asciiz \"false\"");

        for (Map.Entry<String, String> entry : stringLabels.entrySet()) {
            lines.add(entry.getValue() + ": .asciiz " + entry.getKey());
        }

        // alinear los datos a palabra antes de reservar enteros/arreglos
        // esto evita errores en mars por sw/lw en direcciones no alineadas
        lines.add(".align 2");

        for (VarInfo global : globals.values()) {
            if (global.array) {
                lines.add(global.name + ": .space " + global.sizeBytes());
            } else {
                lines.add(global.name + ": .word 0");
            }
        }
    }

    private void emitTextSection() {
        lines.add("");
        lines.add(".text");
        lines.add(".globl main");

        for (FunctionInfo function : functions) {
            emitFunction(function);
        }

        emitRuntime();
    }

    private void emitFunction(FunctionInfo function) {
        currentFunction = function;
        pendingParams.clear();

        lines.add("");
        lines.add(function.name + ":");
        lines.add("# prologo abi o32");
        lines.add("addiu $sp, $sp, -" + function.frameSize);
        lines.add("sw $ra, " + (function.frameSize - 4) + "($sp)");
        lines.add("sw $fp, " + (function.frameSize - 8) + "($sp)");
        lines.add("move $fp, $sp");

        storeIncomingParameters(function);

        for (String text : function.body) {
            translateInstruction(text);
        }

        lines.add(function.returnLabel + ":");
        lines.add("# epilogo abi o32");

        if (function.name.equals("main")) {
            lines.add("li $v0, 10");
            lines.add("syscall");
        } else {
            lines.add("lw $ra, " + (function.frameSize - 4) + "($fp)");
            lines.add("lw $fp, " + (function.frameSize - 8) + "($fp)");
            lines.add("addiu $sp, $sp, " + function.frameSize);
            lines.add("jr $ra");
        }

        currentFunction = null;
    }

    private void storeIncomingParameters(FunctionInfo function) {
        for (int i = 0; i < function.parameters.size(); i++) {
            VarInfo parameter = function.parameters.get(i);

            if (i < 4) {
                storeVariable(parameter, "$a" + i);
            } else {
                int callerOffset = function.frameSize + ((i - 4) * 4);
                lines.add("lw $t0, " + callerOffset + "($fp)");
                storeVariable(parameter, "$t0");
            }
        }
    }

    private void translateInstruction(String text) {
        if (text.isEmpty() || text.startsWith("decl ") || text.startsWith("paramdecl ")) {
            return;
        }

        if (text.endsWith(":")) {
            lines.add(text);
            return;
        }

        if (text.startsWith("goto ")) {
            lines.add("j " + text.substring("goto ".length()).trim());
            return;
        }

        if (text.startsWith("ifFalse ")) {
            emitConditionalJump(text, false);
            return;
        }

        if (text.startsWith("if ")) {
            emitConditionalJump(text, true);
            return;
        }

        if (text.startsWith("param ")) {
            pendingParams.add(text.substring("param ".length()).trim());
            return;
        }

        if (text.startsWith("call ")) {
            emitCall(null, text.substring("call ".length()).trim());
            return;
        }

        if (text.startsWith("return")) {
            emitReturn(text);
            return;
        }

        if (text.contains(" = call ")) {
            String[] parts = text.split(" = call ", 2);
            emitCall(parts[0].trim(), parts[1].trim());
            return;
        }

        if (text.contains(" = ")) {
            emitAssignment(text);
        }
    }

    private void emitConditionalJump(String text, boolean jumpWhenTrue) {
        String[] parts = text.split("\\s+");

        if (parts.length != 4 || !parts[2].equals("goto")) {
            lines.add("# salto condicional no reconocido: " + text);
            return;
        }

        loadValue(parts[1], "$t0");

        if (jumpWhenTrue) {
            lines.add("bne $t0, $zero, " + parts[3]);
        } else {
            lines.add("beq $t0, $zero, " + parts[3]);
        }
    }

    private void emitAssignment(String text) {
        String[] parts = text.split(" = ", 2);

        if (parts.length != 2) {
            return;
        }

        String target = parts[0].trim();
        String expression = parts[1].trim();
        String[] expressionParts = expression.split("\\s+");

        if (expressionParts.length == 3) {
            emitBinaryAssignment(target, expressionParts[0], expressionParts[1], expressionParts[2]);
            return;
        }

        loadValue(expression, "$t0");
        storeTarget(target, "$t0");
    }

    private void emitBinaryAssignment(String target, String left, String operator, String right) {
        loadValue(left, "$t0");
        loadValue(right, "$t1");

        switch (operator) {
            case "+": lines.add("addu $t2, $t0, $t1"); break;
            case "-": lines.add("subu $t2, $t0, $t1"); break;
            case "*": lines.add("mul $t2, $t0, $t1"); break;
            case "/": lines.add("div $t0, $t1"); lines.add("mflo $t2"); break;
            case "%": lines.add("div $t0, $t1"); lines.add("mfhi $t2"); break;
            case "<": lines.add("slt $t2, $t0, $t1"); break;
            case ">": lines.add("slt $t2, $t1, $t0"); break;
            case "<=": lines.add("slt $t2, $t1, $t0"); lines.add("xori $t2, $t2, 1"); break;
            case ">=": lines.add("slt $t2, $t0, $t1"); lines.add("xori $t2, $t2, 1"); break;
            case "==": lines.add("subu $t2, $t0, $t1"); lines.add("sltiu $t2, $t2, 1"); break;
            case "!=": lines.add("subu $t2, $t0, $t1"); lines.add("sltu $t2, $zero, $t2"); break;
            case "&&": lines.add("sltu $t0, $zero, $t0"); lines.add("sltu $t1, $zero, $t1"); lines.add("and $t2, $t0, $t1"); break;
            case "||": lines.add("or $t2, $t0, $t1"); lines.add("sltu $t2, $zero, $t2"); break;
            default: lines.add("# operador no soportado: " + operator); lines.add("li $t2, 0"); break;
        }

        storeTarget(target, "$t2");
    }

    private void emitCall(String target, String callText) {
        String[] parts = callText.split(",");

        if (parts.length < 2) {
            lines.add("# llamada no reconocida: " + callText);
            pendingParams.clear();
            return;
        }

        String functionName = parts[0].trim();
        int argumentCount = Integer.parseInt(parts[1].trim());
        int extraCount = Math.max(0, argumentCount - 4);

        for (int i = argumentCount - 1; i >= 4; i--) {
            lines.add("addiu $sp, $sp, -4");
            loadValue(pendingParams.get(i), "$t0");
            lines.add("sw $t0, 0($sp)");
        }

        int registerLimit = Math.min(4, argumentCount);

        for (int i = 0; i < registerLimit; i++) {
            loadValue(pendingParams.get(i), "$a" + i);
        }

        lines.add("jal " + functionName);

        if (extraCount > 0) {
            lines.add("addiu $sp, $sp, " + (extraCount * 4));
        }

        if (target != null && !target.isEmpty()) {
            storeTarget(target, "$v0");
        }

        pendingParams.clear();
    }

    private void emitReturn(String text) {
        if (text.equals("return")) {
            lines.add("j " + currentFunction.returnLabel);
            return;
        }

        String value = text.substring("return ".length()).trim();
        loadValue(value, "$v0");
        lines.add("j " + currentFunction.returnLabel);
    }

    private void loadValue(String value, String register) {
        value = value.trim();

        if (isInteger(value)) { lines.add("li " + register + ", " + value); return; }
        if (value.equals("true")) { lines.add("li " + register + ", 1"); return; }
        if (value.equals("false")) { lines.add("li " + register + ", 0"); return; }
        if (isCharLiteral(value)) { lines.add("li " + register + ", " + charValue(value)); return; }
        if (isStringLiteral(value)) { lines.add("la " + register + ", " + getStringLabel(value)); return; }

        if (value.startsWith("!") && value.length() > 1) {
            loadValue(value.substring(1), register);
            lines.add("sltiu " + register + ", " + register + ", 1");
            return;
        }

        if (value.startsWith("-") && value.length() > 1 && !isInteger(value)) {
            loadValue(value.substring(1), register);
            lines.add("subu " + register + ", $zero, " + register);
            return;
        }

        if (value.startsWith("&") && value.length() > 1) {
            emitAddress(value.substring(1), register);
            return;
        }

        if (value.startsWith("*") && value.length() > 1) {
            loadValue(value.substring(1), register);
            lines.add("lw " + register + ", 0(" + register + ")");
            return;
        }

        if (isArrayAccess(value)) {
            emitAddress(value, "$t9");
            lines.add("lw " + register + ", 0($t9)");
            return;
        }

        VarInfo variable = resolveVariable(value);

        if (variable != null) {
            loadVariable(variable, register);
            return;
        }

        lines.add("# valor no soportado: " + value);
        lines.add("li " + register + ", 0");
    }

    private void storeTarget(String target, String register) {
        target = target.trim();

        if (isArrayAccess(target)) {
            emitAddress(target, "$t9");
            lines.add("sw " + register + ", 0($t9)");
            return;
        }

        VarInfo variable = resolveVariable(target);

        if (variable != null) {
            storeVariable(variable, register);
            return;
        }

        lines.add("# destino no soportado: " + target);
    }

    private void loadVariable(VarInfo variable, String register) {
        if (variable.global) {
            if (variable.array) {
                lines.add("la " + register + ", " + variable.name);
            } else {
                lines.add("lw " + register + ", " + variable.name);
            }
            return;
        }

        if (variable.array && variable.parameter) {
            lines.add("lw " + register + ", " + variable.offset + "($fp)");
            return;
        }

        if (variable.array) {
            lines.add("addiu " + register + ", $fp, " + variable.offset);
            return;
        }

        lines.add("lw " + register + ", " + variable.offset + "($fp)");
    }

    private void storeVariable(VarInfo variable, String register) {
        if (variable.global) {
            lines.add("sw " + register + ", " + variable.name);
            return;
        }

        lines.add("sw " + register + ", " + variable.offset + "($fp)");
    }

    private void emitAddress(String target, String register) {
        if (!isArrayAccess(target)) {
            VarInfo variable = resolveVariable(target);
            if (variable == null) { lines.add("# direccion no soportada: " + target); lines.add("li " + register + ", 0"); return; }
            if (variable.global) { lines.add("la " + register + ", " + variable.name); }
            else if (variable.array && variable.parameter) { lines.add("lw " + register + ", " + variable.offset + "($fp)"); }
            else { lines.add("addiu " + register + ", $fp, " + variable.offset); }
            return;
        }

        ArrayAccess access = parseArrayAccess(target);
        VarInfo variable = resolveVariable(access.name);
        if (variable == null) { lines.add("# arreglo no declarado: " + access.name); lines.add("li " + register + ", 0"); return; }
        if (variable.global) { lines.add("la " + register + ", " + variable.name); }
        else if (variable.array && variable.parameter) { lines.add("lw " + register + ", " + variable.offset + "($fp)"); }
        else { lines.add("addiu " + register + ", $fp, " + variable.offset); }

        emitArrayOffset(access, variable, "$t8");
        lines.add("addu " + register + ", " + register + ", $t8");
    }

    private void emitArrayOffset(ArrayAccess access, VarInfo variable, String register) {
        if (access.indices.isEmpty()) { lines.add("li " + register + ", 0"); return; }

        if (access.indices.size() == 1) {
            loadValue(access.indices.get(0), register);
            lines.add("sll " + register + ", " + register + ", 2");
            return;
        }

        loadValue(access.indices.get(0), register);
        int cols = variable.dimensions.size() > 1 ? variable.dimensions.get(1) + 1 : 1;
        lines.add("li $t7, " + cols);
        lines.add("mul " + register + ", " + register + ", $t7");
        loadValue(access.indices.get(1), "$t7");
        lines.add("addu " + register + ", " + register + ", $t7");
        lines.add("sll " + register + ", " + register + ", 2");
    }

    private VarInfo resolveVariable(String name) {
        if (currentFunction != null && currentFunction.locals.containsKey(name)) {
            return currentFunction.locals.get(name);
        }
        return globals.get(name);
    }

    private String getStringLabel(String literal) {
        if (!stringLabels.containsKey(literal)) {
            stringCounter++;
            stringLabels.put(literal, "_str_lit_" + stringCounter);
        }
        return stringLabels.get(literal);
    }

    private String getFunctionName(String text) { return text.substring("func ".length(), text.length() - 1).trim(); }
    private boolean isArrayAccess(String value) { return value.matches("[A-Za-z_][A-Za-z0-9_]*\\[.*]"); }
    private boolean isArrayType(String type) { return type.contains("[") && type.contains("]"); }
    private boolean isInteger(String value) { return value.matches("-?\\d+"); }
    private boolean isCharLiteral(String value) { return value.matches("'([^'\\\\]|\\\\.)'"); }
    private boolean isStringLiteral(String value) { return value.startsWith("\"") && value.endsWith("\""); }
    private int align8(int value) { return ((value + 7) / 8) * 8; }

    private int charValue(String value) {
        if (value.length() >= 4 && value.charAt(1) == '\\') {
            char escaped = value.charAt(2);
            switch (escaped) {
                case 'n': return 10;
                case 't': return 9;
                case 'r': return 13;
                case '0': return 0;
                case '\\': return 92;
                case '\'': return 39;
                case '"': return 34;
                default: return escaped;
            }
        }
        return value.charAt(1);
    }

    private ArrayAccess parseArrayAccess(String text) {
        int firstBracket = text.indexOf('[');
        String name = text.substring(0, firstBracket);
        List<String> indices = new ArrayList<>();
        int index = firstBracket;
        while (index < text.length()) {
            int start = text.indexOf('[', index);
            int end = findMatchingBracket(text, start);
            if (start < 0 || end < 0) { break; }
            indices.add(text.substring(start + 1, end).trim());
            index = end + 1;
        }
        return new ArrayAccess(name, indices);
    }

    private int findMatchingBracket(String text, int start) {
        int level = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') { level++; }
            else if (c == ']') { level--; if (level == 0) { return i; } }
        }
        return -1;
    }

    private void emitRuntime() {
        lines.add("");
        lines.add("# runtime minimo para qtspim/mars");
        lines.add("print_int:"); lines.add("li $v0, 1"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("print_char:"); lines.add("li $v0, 11"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("print_bool:"); lines.add("li $v0, 1"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("print_str:"); lines.add("li $v0, 4"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("println:"); lines.add("la $a0, _str_newline"); lines.add("li $v0, 4"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("read_int:"); lines.add("li $v0, 5"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("read_char:"); lines.add("li $v0, 12"); lines.add("syscall"); lines.add("jr $ra");
        lines.add(""); lines.add("read_str:"); lines.add("li $v0, 8"); lines.add("syscall"); lines.add("jr $ra");
    }

    private class FunctionInfo {
        private final String name;
        private final String returnLabel;
        private final List<String> body = new ArrayList<>();
        private final Map<String, VarInfo> locals = new LinkedHashMap<>();
        private final List<VarInfo> parameters = new ArrayList<>();
        private int frameSize = 8;
        private FunctionInfo(String name) { this.name = name; this.returnLabel = "_end_" + name; }
        private void addLocal(VarInfo variable) { locals.putIfAbsent(variable.name, variable); }
        private void calculateFrame() {
            int offset = 0;
            for (VarInfo variable : locals.values()) {
                variable.offset = offset;
                offset += variable.sizeBytes();
            }
            frameSize = align8(offset + 8);
        }
    }

    private static class VarInfo {
        private final String name;
        private final String type;
        private final boolean global;
        private final boolean parameter;
        private final boolean array;
        private final List<Integer> dimensions = new ArrayList<>();
        private int offset = 0;
        private VarInfo(String name, String type, boolean global, boolean parameter, boolean array) {
            this.name = name; this.type = type; this.global = global; this.parameter = parameter; this.array = array; parseDimensions();
        }
        private void parseDimensions() {
            String rest = type;
            while (rest.contains("[") && rest.contains("]")) {
                int start = rest.indexOf('['); int end = rest.indexOf(']', start);
                if (start < 0 || end < 0) { return; }
                dimensions.add(Integer.parseInt(rest.substring(start + 1, end)));
                rest = rest.substring(end + 1);
            }
        }
        private int sizeBytes() {
            if (!array) { return 4; }
            if (parameter) { return 4; }
            int count = 1;
            for (Integer dimension : dimensions) { count *= (dimension + 1); }
            return count * 4;
        }
    }

    private static class ArrayAccess {
        private final String name;
        private final List<String> indices;
        private ArrayAccess(String name, List<String> indices) { this.name = name; this.indices = indices; }
    }
}
