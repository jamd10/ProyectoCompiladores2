package compiler.ir;

public class TacInstruction {
    private final String text;

    public TacInstruction(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}