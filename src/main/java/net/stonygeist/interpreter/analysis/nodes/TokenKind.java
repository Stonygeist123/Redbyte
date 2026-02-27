package net.stonygeist.interpreter.analysis.nodes;

public enum TokenKind {
    Plus, Minus, Star, Slash,
    Bang, EqualsEquals, NotEquals, Greater, GreaterEquals, Less, LessEquals,
    LParen, RParen, Comma, LBrace, RBrace,
    Equals,

    Number, String, Identifier,

    If, Else, Loop, While, Till, Or, And,

    Whitespace, Bad, Eof
}
