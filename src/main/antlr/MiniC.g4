grammar MiniC;

// =====================================================
// Parser rules
// =====================================================

program
    : externalDeclaration* EOF
    ;

externalDeclaration
    : functionDefinition
    | declaration
    ;

functionDefinition
    : typeSpecifier pointer? Identifier LPAREN parameterList? RPAREN compoundStatement
    ;

parameterList
    : parameter (COMMA parameter)*
    ;

parameter
    : typeSpecifier pointer? Identifier arraySuffix*
    ;

declaration
    : typeSpecifier initDeclaratorList SEMI
    ;

initDeclaratorList
    : initDeclarator (COMMA initDeclarator)*
    ;

initDeclarator
    : pointer? Identifier arraySuffix* (ASSIGN expr)?
    ;

arraySuffix
    : LBRACK IntegerConst RBRACK
    ;

pointer
    : STAR
    ;

typeSpecifier
    : INT
    | CHAR
    | BOOL
    | VOID
    | STRING
    ;

compoundStatement
    : LBRACE blockItem* RBRACE
    ;

blockItem
    : declaration
    | statement
    ;

statement
    : compoundStatement
    | ifStatement
    | whileStatement
    | forStatement
    | doWhileStatement
    | returnStatement
    | expressionStatement
    ;

ifStatement
    : IF LPAREN expr RPAREN statement (ELSE statement)?
    ;

whileStatement
    : WHILE LPAREN expr RPAREN statement
    ;

forStatement
    : FOR LPAREN expr? SEMI expr? SEMI expr? RPAREN statement
    ;

doWhileStatement
    : DO statement WHILE LPAREN expr RPAREN SEMI
    ;

returnStatement
    : RETURN expr? SEMI
    ;

expressionStatement
    : expr? SEMI
    ;

expr
    : assignmentExpr
    ;

assignmentExpr
    : lvalue ASSIGN assignmentExpr
    | logicalOrExpr
    ;

logicalOrExpr
    : logicalAndExpr (OR logicalAndExpr)*
    ;

logicalAndExpr
    : equalityExpr (AND equalityExpr)*
    ;

equalityExpr
    : relationalExpr ((EQ | NEQ) relationalExpr)*
    ;

relationalExpr
    : additiveExpr ((LT | LE | GT | GE) additiveExpr)*
    ;

additiveExpr
    : multiplicativeExpr ((PLUS | MINUS) multiplicativeExpr)*
    ;

multiplicativeExpr
    : unaryExpr ((STAR | DIV | MOD) unaryExpr)*
    ;

unaryExpr
    : (NOT | MINUS | STAR | AMP) unaryExpr
    | primary
    ;

primary
    : IntegerConst
    | CharConst
    | StringLiteral
    | TRUE
    | FALSE
    | LPAREN expr RPAREN
    | call
    | lvalue
    ;

call
    : Identifier LPAREN argumentList? RPAREN
    ;

argumentList
    : expr (COMMA expr)*
    ;

lvalue
    : Identifier (LBRACK expr RBRACK)*
    ;

// =====================================================
// Reserved words
// =====================================================

INT      : 'int';
CHAR     : 'char';
BOOL     : 'bool';
VOID     : 'void';
STRING   : 'string';

IF       : 'if';
ELSE     : 'else';
WHILE    : 'while';
FOR      : 'for';
DO       : 'do';
RETURN   : 'return';

TRUE     : 'true';
FALSE    : 'false';

// =====================================================
// Operators
// =====================================================

AND      : '&&';
OR       : '||';

EQ       : '==';
NEQ      : '!=';
LE       : '<=';
GE       : '>=';
LT       : '<';
GT       : '>';

ASSIGN   : '=';
PLUS     : '+';
MINUS    : '-';
STAR     : '*';
DIV      : '/';
MOD      : '%';
NOT      : '!';
AMP      : '&';

// =====================================================
// Delimiters
// =====================================================

SEMI     : ';';
COMMA    : ',';
LPAREN   : '(';
RPAREN   : ')';
LBRACE   : '{';
RBRACE   : '}';
LBRACK   : '[';
RBRACK   : ']';

// =====================================================
// Literals and identifiers
// =====================================================

Identifier
    : [A-Za-z_] [A-Za-z0-9_]*
    ;

IntegerConst
    : [0-9]+
    ;

InvalidCharLiteral
    : '\'' (EscapeSequence | ~['\\\r\n]) (EscapeSequence | ~['\\\r\n])+ '\''
    ;

CharConst
    : '\'' (EscapeSequence | ~['\\\r\n]) '\''
    ;

UnclosedChar
    : '\'' (EscapeSequence | ~['\\\r\n])* (EOF | '\r'? '\n')
    ;

StringLiteral
    : '"' (EscapeSequence | ~["\\\r\n])* '"'
    ;

UnclosedString
    : '"' (EscapeSequence | ~["\\\r\n])* (EOF | '\r'? '\n')
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\0]
    ;

// =====================================================
// Comments and whitespace
// =====================================================

WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

UnclosedBlockComment
    : '/*' .*? EOF
    ;

// =====================================================
// Unknown character
// =====================================================

ERROR_CHAR
    : .
    ;