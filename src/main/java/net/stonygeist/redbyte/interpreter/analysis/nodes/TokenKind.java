package net.stonygeist.redbyte.interpreter.analysis.nodes;

public enum TokenKind {
    Plus, Minus, Star, Slash,
    Bang, EqualsEquals, NotEquals, Greater, GreaterEquals, Less, LessEquals,
    LParen, RParen, LBrace, RBrace,
    Equals, Comma, Dot,

    Number, String, Identifier, Robo,

    True, False, If, Else, Loop, Once, While, Always, Or, And,

    Whitespace, Bad, Eof
}
