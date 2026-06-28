package compiler.analysis;

import compiler.parser.MiniCLexer;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

public class LexicalSummary {
    private int totalTokens;
    private int identifiers;
    private int integerLiterals;
    private int charLiterals;
    private int stringLiterals;
    private int keywords;
    private int operators;
    private int delimiters;
    private int lexicalErrors;

    public static LexicalSummary fromTokens(CommonTokenStream tokens) {
        LexicalSummary summary = new LexicalSummary();

        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF) {
                continue;
            }

            summary.totalTokens++;

            switch (token.getType()) {
                case MiniCLexer.Identifier:
                    summary.identifiers++;
                    break;

                case MiniCLexer.IntegerConst:
                    summary.integerLiterals++;
                    break;

                case MiniCLexer.CharConst:
                    summary.charLiterals++;
                    break;

                case MiniCLexer.StringLiteral:
                    summary.stringLiterals++;
                    break;

                case MiniCLexer.INT:
                case MiniCLexer.CHAR:
                case MiniCLexer.BOOL:
                case MiniCLexer.VOID:
                case MiniCLexer.STRING:
                case MiniCLexer.IF:
                case MiniCLexer.ELSE:
                case MiniCLexer.WHILE:
                case MiniCLexer.FOR:
                case MiniCLexer.DO:
                case MiniCLexer.RETURN:
                case MiniCLexer.TRUE:
                case MiniCLexer.FALSE:
                    summary.keywords++;
                    break;

                case MiniCLexer.AND:
                case MiniCLexer.OR:
                case MiniCLexer.EQ:
                case MiniCLexer.NEQ:
                case MiniCLexer.LE:
                case MiniCLexer.GE:
                case MiniCLexer.LT:
                case MiniCLexer.GT:
                case MiniCLexer.ASSIGN:
                case MiniCLexer.PLUS:
                case MiniCLexer.MINUS:
                case MiniCLexer.STAR:
                case MiniCLexer.DIV:
                case MiniCLexer.MOD:
                case MiniCLexer.NOT:
                case MiniCLexer.AMP:
                    summary.operators++;
                    break;

                case MiniCLexer.SEMI:
                case MiniCLexer.COMMA:
                case MiniCLexer.LPAREN:
                case MiniCLexer.RPAREN:
                case MiniCLexer.LBRACE:
                case MiniCLexer.RBRACE:
                case MiniCLexer.LBRACK:
                case MiniCLexer.RBRACK:
                    summary.delimiters++;
                    break;

                case MiniCLexer.ERROR_CHAR:
                case MiniCLexer.UnclosedString:
                case MiniCLexer.UnclosedChar:
                case MiniCLexer.InvalidCharLiteral:
                case MiniCLexer.UnclosedBlockComment:
                    summary.lexicalErrors++;
                    break;

                default:
                    break;
            }
        }

        return summary;
    }

    public void print() {
        System.out.println();
        System.out.println("================ RESUMEN LEXICO ================");
        System.out.println("Total tokens: " + totalTokens);
        System.out.println("Identificadores: " + identifiers);
        System.out.println("Literales enteros: " + integerLiterals);
        System.out.println("Literales char: " + charLiterals);
        System.out.println("Literales string: " + stringLiterals);
        System.out.println("Palabras reservadas: " + keywords);
        System.out.println("Operadores: " + operators);
        System.out.println("Delimitadores: " + delimiters);
        System.out.println("Errores lexicos: " + lexicalErrors);
    }
}