package net.stonygeist.redbyte.interpreter.analysis.nodes;

public enum TokenKind {
    Plus, Minus, Star, Slash,
    Bang, EqualsEquals, NotEquals, Greater, GreaterEquals, Less, LessEquals,
    LParen, RParen, Comma, LBrace, RBrace,
    Equals,

    Number, String, Identifier,

    If, Else, Loop, While, Once, Or, And,

    Whitespace, Bad, Eof
}
