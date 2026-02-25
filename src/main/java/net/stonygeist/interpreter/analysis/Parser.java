package net.stonygeist.interpreter.analysis;

import net.stonygeist.interpreter.analysis.nodes.*;
import net.stonygeist.interpreter.miscellaneous.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final Token[] tokens;
    private int current;

    public Parser(Token[] tokens) {
        this.tokens = Arrays.stream(tokens).filter(t -> t.kind != TokenKind.Whitespace).toArray(Token[]::new);
    }

    public Expr[] parse() {
        List<Expr> exprs = new ArrayList<>();
        while (!isAtEnd())
            exprs.add(parseExpr(0));
        return exprs.toArray(new Expr[0]);
    }

    private Expr parseExpr(int parentPrecedence) {
        Token token = getCurrent();
        ++current;
        if (token == null) throw new RuntimeException();
        return checkExtension(switch (token.kind) {
            case Number -> new LiteralExpr(token);
            case Identifier -> new NameExpr(token);
            case LParen -> new GroupExpr(token, parseExpr(0), match(TokenKind.RParen));
            case Minus -> new UnaryExpr(parseExpr(Config.unaryPrecedence), token);
            default -> throw new RuntimeException();
        }, parentPrecedence);
    }

    private Token match(TokenKind kind) {
        Token token = getCurrent();
        ++current;
        if (token == null || token.kind != kind)
            throw new RuntimeException();
        return token;
    }

    private Expr checkExtension(Expr expr, int parentPrecedence) {
        Token token = getCurrent();
        if (token == null) return expr;
        else if (Config.getBinaryPrecedence(token.kind) > parentPrecedence) {
            int precedence = Config.getBinaryPrecedence(token.kind);
            while (precedence > parentPrecedence && !isAtEnd()) {
                ++current;
                expr = new BinaryExpr(expr, token, parseExpr(precedence));
                if (!isAtEnd()) {
                    token = getCurrent();
                    precedence = Config.getBinaryPrecedence(token.kind);
                }
            }
        } else if (expr instanceof NameExpr name) {
            if (token.kind == TokenKind.Equals)
                return new AssignExpr(name.name, token, parseExpr(0));
            else if (token.kind == TokenKind.LParen) {
                if (!Config.functions.containsKey(name.name.lexeme.toLowerCase())) throw new RuntimeException();
                Token lParen = match(TokenKind.LParen);
                List<Expr> args = new ArrayList<>();
                while (!isAtEnd() && getCurrent() != null && getCurrent().kind != TokenKind.RParen) {
                    args.add(parseExpr(0));
                    if (getCurrent().kind != TokenKind.RParen && !isAtEnd())
                        match(TokenKind.Comma);
                }

                Token rParen = match(TokenKind.RParen);
                return new CallExpr(name.name, lParen, args.toArray(new Expr[0]), rParen);
            }
        }

        return expr;
    }

    private Token getCurrent() {
        return isAtEnd() ? null : tokens[current];
    }

    private boolean isAtEnd() {
        return current >= tokens.length;
    }
}
