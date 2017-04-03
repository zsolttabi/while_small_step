grammar While;

@parser::header {
    import ast.AST;
    import ast.statement.*;
    import ast.expression.*;
    import ast.expression.operations.*;
    import ast.expression.interfaces.*;
    import ast.expression.values.*;
}

@parser::members {

    public static AST parseAst(String input) {
        WhileParser parser = new WhileParser(new CommonTokenStream(new WhileLexer(new ANTLRInputStream(input))));
        return new AST(parser.start().value);
    }

}

start returns [Statement value]:
    s = statement { $value = $s.value; }
;

statement returns [Statement value]:

    id = expression Assign e = expression {
        $value = $id.value == null || $e.value == null ?
         new BadAssignment($id.value, $e.value) :
         new Assignment($id.value, $e.value);
    }
|
    s1 = statement SeqSeparator s2 = statement {
        $value = $s1.value == null || $s2.value == null ?
         new BadSequence($s1.value, $s2.value) :
         new Sequence($s1.value, $s2.value);
    }
|
    If e = expression Then s1 = statement Else s2 = statement {
        $value = $e.value == null || $s1.value == null || $s2.value == null ?
         new BadIf($e.value, $s1.value, $s2.value) :
         new If($e.value, $s1.value, $s2.value);
    }
|
    While e = expression Do s = statement {
        $value = $e.value == null || $s.value == null ?
         new BadWhile($e.value, $s.value) :
         new While($e.value, $s.value);
     }
|
    Skip { $value  = new Skip(); }
;

expression returns [Expression value]:

    Identifier { $value = new Identifier($Identifier.text); }
|
    Int { $value = new IntValue(Integer.parseInt($Int.text)); }
|
    e1 = expression Plus e2 = expression { $value = BinOp.arithOp($Plus.text, $e1.value, $e2.value, Integer::sum); }
|
    e1 = expression Minus e2 = expression { $value = BinOp.arithOp($Minus.text, $e1.value, $e2.value, (i1, i2) -> i1 - i2); }
|
    Minus a = expression { $value = UnOp.intOp($Minus.text, $a.value,  i -> -1 * i); }
|
    True { $value = new BoolValue(true); }
|
    False { $value = new BoolValue(false); }
|
    e1 = expression Equals e2 = expression { $value = BinOp.intRelOp($Equals.text, $e1.value, $e2.value, Object::equals); }
|
    e1 = expression LessThen e2 = expression { $value = BinOp.intRelOp($LessThen.text, $e1.value, $e2.value, (i1,i2) -> i1 < i2); }
|
    Not e = expression { $value = UnOp.boolOp($Not.text, $e.value, b -> !b); }
|
    e1 = expression And e2 = expression  { $value = BinOp.boolOp($And.text, $e1.value, $e2.value, Boolean::logicalAnd); }
;

If: 'if' ;

Then: 'then' ;

Else: 'else' ;

While: 'while' ;

Do: 'do' ;

Assign: ':=' ;

SeqSeparator: ';' ;

Skip: 'SKIP' ;

True: 'tt' ;

False: 'ff' ;

Plus: '+' ;

Minus: '-';

Equals: '=' ;

LessThen: '<' ;

Not: '!' ;

And: '&&' ;

Int: '0' | ('-'? ('1'..'9') ('0'..'9')*) ;

Identifier: ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')* ;

WS: [ \t|\r\n] -> channel(HIDDEN);