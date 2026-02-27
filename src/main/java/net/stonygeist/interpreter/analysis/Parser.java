package net.stonygeist.interpreter.analysis;

import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.analysis.nodes.expr.*;
import net.stonygeist.interpreter.analysis.nodes.stmt.*;
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

    public Stmt[] parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd())
            stmts.add(parseStmt());
        return stmts.toArray(new Stmt[0]);
    }

    private Stmt parseStmt() {
        Token token = getCurrent();
        if (token == null) throw new RuntimeException();
        return switch (token.kind) {
            case LBrace -> parseBlockStmt(token);
            case If -> parseIfStmt(token);
            case While -> parseWhileStmt(token);
            case Till -> parseTillStmt(token);
            case Loop -> parseLoopStmt(token);
            default -> new ExprStmt(parseExpr(0));
        };
    }

    private Stmt parseBlockStmt(Token lBrace) {
        ++current;
        List<Stmt> stmts = new ArrayList<>();
        Token rBrace = getCurrent();
        while (!isAtEnd() && rBrace != null) {
            if (rBrace.kind == TokenKind.RBrace) break;
            stmts.add(parseStmt());
            rBrace = getCurrent();
        }

        if (rBrace == null || rBrace.kind != TokenKind.RBrace) throw new RuntimeException();
        ++current;
        return new BlockStmt(lBrace, stmts.toArray(new Stmt[0]), rBrace);
    }

    private Stmt parseIfStmt(Token keyword) {
        ++current;
        Expr condition = parseExpr(0);
        Stmt stmt = parseStmt();
        Token elseToken = getCurrent();
        Stmt elseStmt = null;
        if (elseToken != null && elseToken.kind == TokenKind.Else) {
            ++current;
            elseStmt = parseStmt();
        } else
            elseToken = null;

        return new IfStmt(keyword, condition, stmt, elseToken, elseStmt);
    }

    private Stmt parseWhileStmt(Token keyword) {
        ++current;
        Expr condition = parseExpr(0);
        Stmt stmt = parseStmt();
        return new WhileStmt(keyword, condition, stmt);
    }

    private Stmt parseTillStmt(Token keyword) {
        ++current;
        Expr condition = parseExpr(0);
        Stmt stmt = parseStmt();
        return new TillStmt(keyword, condition, stmt);
    }

    private Stmt parseLoopStmt(Token keyword) {
        ++current;
        Expr count = parseExpr(0);
        Stmt stmt = parseStmt();
        return new LoopStmt(keyword, count, stmt);
    }

    private Expr parseExpr(int parentPrecedence) {
        Token token = getCurrent();
        ++current;
        if (token == null) throw new RuntimeException();
        return checkExtension(switch (token.kind) {
            case Number, String -> new LiteralExpr(token);
            case Identifier -> new NameExpr(token);
            case LParen -> new GroupExpr(token, parseExpr(0), match(TokenKind.RParen));
            case Plus, Minus, Bang -> new UnaryExpr(parseExpr(Config.unaryPrecedence), token);
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
        } else if (expr instanceof NameExpr nameExpr) {
            if (token.kind == TokenKind.Equals)
                return new AssignExpr(nameExpr.name, token, parseExpr(0));
            else if (token.kind == TokenKind.LParen) {
                Token lParen = match(TokenKind.LParen);
                List<Expr> args = new ArrayList<>();
                while (!isAtEnd() && getCurrent() != null && getCurrent().kind != TokenKind.RParen) {
                    args.add(parseExpr(0));
                    if (getCurrent().kind != TokenKind.RParen && !isAtEnd())
                        match(TokenKind.Comma);
                }

                Token rParen = match(TokenKind.RParen);
                expr = new CallExpr(nameExpr.name, lParen, args.toArray(new Expr[0]), rParen);
            }
        } else
            return expr;

        return checkExtension(expr, parentPrecedence);
    }

    private Token getCurrent() {
        return isAtEnd() ? null : tokens[current];
    }

    private boolean isAtEnd() {
        return current >= tokens.length;
    }
}
