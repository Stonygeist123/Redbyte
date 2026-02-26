package net.stonygeist.interpreter.analysis.nodes;

public enum TokenKind {
    Plus, Minus, Star, Slash,
    LParen, RParen, Comma, LBrace, RBrace,
    Equals,
    Number, Identifier,

    If, Else, Loop,

    Whitespace, Bad, Eof
}
