package compiler.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TacOptimizer {
    private final Map<String, String> constants = new HashMap<>();

    public List<TacInstruction> optimize(List<TacInstruction> instructions) {
        List<TacInstruction> optimizedInstructions = new ArrayList<>();

        constants.clear();

        for (TacInstruction instruction : instructions) {
            String text = instruction.toString();
            String optimizedText = optimizeInstruction(text);

            optimizedInstructions.add(new TacInstruction(optimizedText));
        }

        return optimizedInstructions;
    }

    private String optimizeInstruction(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String trimmedText = text.trim();

        if (isScopeBoundary(trimmedText)) {
            constants.clear();
            return text;
        }

        if (isLabel(trimmedText)) {
            constants.clear();
            return text;
        }

        if (isJump(trimmedText)) {
            return optimizeJump(trimmedText);
        }

        if (trimmedText.startsWith("param ")) {
            return optimizeParam(trimmedText);
        }

        if (trimmedText.startsWith("return ")) {
            return optimizeReturn(trimmedText);
        }

        if (trimmedText.startsWith("call ")) {
            clearVariableConstants();
            return text;
        }

        if (trimmedText.contains(" = call ")) {
            return optimizeCallAssignment(trimmedText);
        }

        if (trimmedText.contains(" = ")) {
            return optimizeAssignment(trimmedText);
        }

        return text;
    }

    private String optimizeAssignment(String text) {
        String[] parts = text.split(" = ", 2);

        if (parts.length != 2) {
            return text;
        }

        String target = parts[0].trim();
        String expression = parts[1].trim();

        String optimizedExpression = optimizeExpression(expression);

        if (isConstant(optimizedExpression) && isAssignableTarget(target)) {
            constants.put(target, optimizedExpression);
        } else {
            constants.remove(target);
        }

        return target + " = " + optimizedExpression;
    }

    private String optimizeExpression(String expression) {
        String[] parts = expression.split(" ");

        if (parts.length == 1) {
            return resolveValue(parts[0]);
        }

        if (parts.length != 3) {
            return expression;
        }

        String left = resolveValue(parts[0]);
        String operator = parts[1];
        String right = resolveValue(parts[2]);

        String foldedValue = foldOperation(left, operator, right);

        if (foldedValue != null) {
            return foldedValue;
        }

        return left + " " + operator + " " + right;
    }

    private String foldOperation(String left, String operator, String right) {
        if (isInteger(left) && isInteger(right)) {
            return foldIntegerOperation(left, operator, right);
        }

        if (isBoolean(left) && isBoolean(right)) {
            return foldBooleanOperation(left, operator, right);
        }

        return null;
    }

    private String foldIntegerOperation(String leftValue, String operator, String rightValue) {
        int left = Integer.parseInt(leftValue);
        int right = Integer.parseInt(rightValue);

        if ((operator.equals("/") || operator.equals("%")) && right == 0) {
            return null;
        }

        switch (operator) {
            case "+":
                return String.valueOf(left + right);
            case "-":
                return String.valueOf(left - right);
            case "*":
                return String.valueOf(left * right);
            case "/":
                return String.valueOf(left / right);
            case "%":
                return String.valueOf(left % right);
            case "==":
                return String.valueOf(left == right);
            case "!=":
                return String.valueOf(left != right);
            case "<":
                return String.valueOf(left < right);
            case "<=":
                return String.valueOf(left <= right);
            case ">":
                return String.valueOf(left > right);
            case ">=":
                return String.valueOf(left >= right);
            default:
                return null;
        }
    }

    private String foldBooleanOperation(String leftValue, String operator, String rightValue) {
        boolean left = Boolean.parseBoolean(leftValue);
        boolean right = Boolean.parseBoolean(rightValue);

        switch (operator) {
            case "&&":
                return String.valueOf(left && right);
            case "||":
                return String.valueOf(left || right);
            case "==":
                return String.valueOf(left == right);
            case "!=":
                return String.valueOf(left != right);
            default:
                return null;
        }
    }

    private String optimizeParam(String text) {
        String value = text.substring("param ".length()).trim();

        return "param " + resolveValue(value);
    }

    private String optimizeReturn(String text) {
        String value = text.substring("return ".length()).trim();

        return "return " + resolveValue(value);
    }

    private String optimizeJump(String text) {
        if (text.startsWith("ifFalse ")) {
            String[] parts = text.split(" ");

            if (parts.length == 4 && parts[2].equals("goto")) {
                String condition = resolveValue(parts[1]);
                return "ifFalse " + condition + " goto " + parts[3];
            }
        }

        if (text.startsWith("if ")) {
            String[] parts = text.split(" ");

            if (parts.length == 4 && parts[2].equals("goto")) {
                String condition = resolveValue(parts[1]);
                return "if " + condition + " goto " + parts[3];
            }
        }

        return text;
    }

    private String optimizeCallAssignment(String text) {
        String[] parts = text.split(" = call ", 2);

        if (parts.length == 2) {
            constants.remove(parts[0].trim());
        }

        clearVariableConstants();

        return text;
    }

    private String resolveValue(String value) {
        return constants.getOrDefault(value, value);
    }

    private boolean isScopeBoundary(String text) {
        return text.startsWith("func ") || text.startsWith("endfunc ");
    }

    private boolean isLabel(String text) {
        return text.endsWith(":");
    }

    private boolean isJump(String text) {
        return text.startsWith("goto ")
                || text.startsWith("if ")
                || text.startsWith("ifFalse ");
    }

    private boolean isAssignableTarget(String target) {
        return !target.contains("[") && !target.contains("]");
    }

    private boolean isConstant(String value) {
        return isInteger(value)
                || isBoolean(value)
                || isChar(value)
                || isString(value);
    }

    private boolean isInteger(String value) {
        return value.matches("-?\\d+");
    }

    private boolean isBoolean(String value) {
        return value.equals("true") || value.equals("false");
    }

    private boolean isChar(String value) {
        return value.matches("'([^'\\\\]|\\\\.)'");
    }

    private boolean isString(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    private void clearVariableConstants() {
        List<String> keysToRemove = new ArrayList<>();

        for (String key : constants.keySet()) {
            if (!key.matches("t\\d+")) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            constants.remove(key);
        }
    }
}