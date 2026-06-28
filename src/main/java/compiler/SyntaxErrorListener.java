package compiler;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyntaxErrorListener extends BaseErrorListener {

    private final List<String> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {

        String symbolText = offendingSymbol != null
                ? offendingSymbol.toString()
                : "unknown";

        addError(line, charPositionInLine, symbolText, msg);
    }

    private void addError(int line, int column, String symbol, String message) {
        errors.add(String.format(
                "Syntax error at line %d, column %d near '%s': %s",
                line, column, symbol, message
        ));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void clear() {
        errors.clear();
    }
}