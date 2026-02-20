package net.stonygeist.interpreter.analysis;

public record Token(String lexeme, TokenKind kind, TextSpan position) {
}
