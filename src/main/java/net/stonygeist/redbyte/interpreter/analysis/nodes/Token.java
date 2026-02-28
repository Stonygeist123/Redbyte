package net.stonygeist.redbyte.interpreter.analysis.nodes;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;

import java.util.Objects;

public final class Token extends Node {
    public final String lexeme;
    public final TokenKind kind;
    public final TextSpan span;

    public Token(String lexeme, TokenKind kind, TextSpan span) {
        this.lexeme = lexeme;
        this.kind = kind;
        this.span = span;
    }

    @Override
    public TextSpan span() {
        return span;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        Token other = (Token) obj;
        return Objects.equals(lexeme, other.lexeme) &&
                Objects.equals(kind, other.kind) &&
                Objects.equals(span, other.span);
    }
}
