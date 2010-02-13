/**
 * Entity: A recognizer for PL
 *
 * This grammar defines a simple language to define Entities in
 * terms of tuples and KTCVs.
 */
grammar Entity;

options {
	output = AST;
    ASTLabelType = CommonTree;
}

tokens {
	ENTITY;
	KTCV;
	TUPLE;
	CONSTRAINTS = 'Constraints';
	// Data types
	BOOLEAN = 'Boolean';
	DATE = 'Date';
	DOUBLE = 'Double';
	ENTITY = 'Entity';
	INTEGER = 'Integer';
	LONG = 'Long';
	SHORT = 'Short';
	STRING = 'String';
}

@header {
	package com.inexas.pl.pl;
}

@members {
}

@lexer::header {
	package com.inexas.pl.pl;
}

parse
	:	entity EOF -> entity
	;

entity
	:	tuple -> ^(ENTITY tuple)
	;

tuple
	:	id Cardinality? '{' tupleMembers '}' -> ^(TUPLE id Cardinality? tupleMembers)
	;

tupleMembers
	:	( ktcv | tuple )+
	;

ktcv
	:	id tcv* ';' -> ^(KTCV id tcv*)
	;

tcv
	:	':'! dataType
	|	'?'! id
	|	'='! id
	;

id 	:	Id^ | dataType
	;
	
dataType
	:	BOOLEAN
	|	DATE
	|	DOUBLE
	|	ENTITY
	|	INTEGER
	|	LONG
	|	SHORT
	|	STRING
	;
	
// L E X E R

Cardinality
	:	Digit+ '..' ( Digit+ | '*' )
	;

Id
	:	Letter ( Letter | Digit | '_' )*
	;

String
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"' {
			setText(getText().substring(1, getText().length()-1));	
		}
	;

fragment
EscapeSequence
	:	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
	|	UnicodeEscape
	|	OctalEscape
	;

fragment
UnicodeEscape
	:	'\\' 'u' HexDigit HexDigit HexDigit HexDigit
	;
	
fragment
OctalEscape
	:	'\\' ('0'..'3') ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7')
	;

fragment
HexDigit
	:	('0'..'9'|'a'..'f'|'A'..'F')
	;

fragment
Letter
	:	( 'a'..'z' | 'A'..'Z' )
	;

fragment
Digit
	:	'0'..'9'
	;

WS
	:	(' ' | '\t' | '\n' | '\r')+ { $channel=HIDDEN; }
	;

SL_COMMENT
	:	'//'( ~('\r' | '\n') )* { $channel=HIDDEN; }
	;

ML_COMMENT
	:	'/*' (~'*')* '*' ('*' | ( ~('*' | '/') (~'*')* '*') )* '/' { $channel=HIDDEN; }
	;


