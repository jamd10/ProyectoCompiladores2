package compiler;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

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
        errors.add("Error sintactico linea " + line + ":" + charPositionInLine
                + " - " + buildMessage(offendingSymbol, msg));
    }

    private String buildMessage(Object offendingSymbol, String msg) {
        String text = symbolText(offendingSymbol);

        if (text == null) {
            return msg;
        }

        return msg + " (cerca de '" + text + "')";
    }

    private String symbolText(Object offendingSymbol) {
        if (!(offendingSymbol instanceof Token token)) {
            return null;
        }

        if (token.getType() == Token.EOF) {
            return "<EOF>";
        }

        return escape(token.getText());
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
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