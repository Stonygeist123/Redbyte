package net.stonygeist.interpreter.analysis.nodes;

public enum TokenKind {
    Number, Identifier,
    Plus, Minus, Star, Slash,
    LParen, RParen, Comma,
    Equals,
    Whitespace, Bad, Eof
}
