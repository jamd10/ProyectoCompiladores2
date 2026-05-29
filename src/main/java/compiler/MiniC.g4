grammar MiniC;

program : (declaration | funcDef)* EOF ;

declaration : typeSpecifier declaratorList ';' ;

declaratorList: declarator (',' declarator)* ;

declarator : Identifier ('[' IntegerConst ']')*
            | '*' declarator ;

typeSpecifier : 'int' | 'char' | 'bool' | 'void' | 'string' ;

funcDef : typeSpecifier Identifier '(' params? ')' compoundStmt ;

params : param (',' param)* ;

param : typeSpecifier declarator ;

compoundStmt : '{' (declaration | statement)* '}' ;

statement : compoundStmt
          | ifStmt | whileStmt | forStmt | doWhileStmt
          | assignStmt | returnStmt | exprStmt ;

ifStmt : 'if' '(' expr ')' statement ('else' statement)? ;

whileStmt : 'while' '(' expr ')' statement ;

forStmt : 'for' '(' exprStmt expr? ';' expr? ')' statement ;

doWhileStmt : 'do' statement 'while' '(' expr ')' ';' ;

assignStmt : lvalue '=' expr ';' ;

returnStmt : 'return' expr? ';' ;

exprStmt : expr? ';' ;

expr : logicalOrExpr ;

logicalOrExpr : logicalAndExpr ('||' logicalAndExpr)* ;

logicalAndExpr: equalityExpr ('&&' equalityExpr)* ;

equalityExpr : relationalExpr (('==' | '!=') relationalExpr)* ;

relationalExpr: additiveExpr (('<'|'>'|'<='|'>=') additiveExpr)* ;

additiveExpr : multiplicativeExpr (('+'|'-') multiplicativeExpr)* ;

multiplicativeExpr : unaryExpr (('*'|'/'|'%') unaryExpr)* ;

unaryExpr : ('!'|'-'|'*'|'&') unaryExpr | primary ;

primary : IntegerConst | CharConst | StringLiteral | 'true' | 'false'
         | '(' expr ')' | lvalue | call ;

call : Identifier '(' (expr (',' expr)*)? ')' ;

lvalue : Identifier ('[' expr ']')* ;

// Léxico clave
Identifier : [A-Za-z_] [A-Za-z0-9_]* ;
IntegerConst : [0-9]+ ;
CharConst : '\'' . '\'' ;
StringLiteral : '"' (~['\n'\r])* '"' ;
WS : [ \t\r\n]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;