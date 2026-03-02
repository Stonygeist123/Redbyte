package net.stonygeist.redbyte.interpreter.analysis;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Config;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.*;
import net.stonygeist.redbyte.interpreter.analysis.nodes.stmt.*;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final Token[] tokens;
    private int current;
    private final DiagnosticBag diagnostics = new DiagnosticBag();

    public Parser(Token[] tokens, List<Diagnostic> diagnostics) {
        this.tokens = Arrays.stream(tokens).filter(t -> t.kind != TokenKind.Whitespace).toArray(Token[]::new);
        this.diagnostics.addAll(diagnostics);
    }

    public Stmt[] parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (getCurrent().kind != TokenKind.Eof)
            stmts.add(parseStmt());
        return stmts.toArray(new Stmt[0]);
    }

    private Stmt parseStmt() {
        Token token = getCurrent();
        return switch (token.kind) {
            case LBrace -> parseBlockStmt(token);
            case If -> parseIfStmt(token);
            case While -> parseWhileStmt(token);
            case Once -> parseOnceStmt(token);
            case Loop -> parseLoopStmt(token);
            default -> new ExprStmt(parseExpr(0));
        };
    }

    private Stmt parseBlockStmt(Token lBrace) {
        ++current;
        List<Stmt> stmts = new ArrayList<>();
        Token rBrace = getCurrent();
        while (getCurrent().kind != TokenKind.RBrace && getCurrent().kind != TokenKind.Eof) {
            stmts.add(parseStmt());
            rBrace = getCurrent();
        }

        if (rBrace.kind != TokenKind.RBrace)
            diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_character", "'}'", "None"), rBrace.span()));
        else
            ++current;
        return new BlockStmt(lBrace, stmts.toArray(new Stmt[0]), rBrace);
    }

    private Stmt parseIfStmt(Token keyword) {
        ++current;
        Expr condition = parseExpr(0);
        Stmt stmt = parseStmt();
        Token elseToken = getCurrent();
        Stmt elseStmt = null;
        if (elseToken.kind == TokenKind.Else) {
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

    private Stmt parseOnceStmt(Token keyword) {
        ++current;
        Expr condition = parseExpr(0);
        Stmt stmt = parseStmt();
        return new OnceStmt(keyword, condition, stmt);
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
        return checkExtension(switch (token.kind) {
            case Number, String -> new LiteralExpr(token);
            case Identifier -> new NameExpr(token);
            case LParen -> {
                Expr expr = parseExpr(0);
                Token rParen = getCurrent();
                if (rParen.kind != TokenKind.RParen)
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_character", "')'", rParen.lexeme), rParen.span()));
                else
                    ++current;
                yield new GroupExpr(token, expr, rParen);
            }
            case Plus, Minus, Bang -> new UnaryExpr(parseExpr(Config.unaryPrecedence), token);
            default -> {
                diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_expression"), token.span()));
                yield new ErrorExpr(token);
            }
        }, parentPrecedence);
    }

    private Expr checkExtension(Expr expr, int parentPrecedence) {
        Token token = getCurrent();
        if (token.kind == TokenKind.Eof)
            return expr;
        else if (Config.getBinaryPrecedence(token.kind) > parentPrecedence) {
            int precedence = Config.getBinaryPrecedence(token.kind);
            while (precedence > parentPrecedence) {
                ++current;
                expr = new BinaryExpr(expr, token, parseExpr(precedence));
                token = getCurrent();
                precedence = Config.getBinaryPrecedence(token.kind);
            }

            return expr;
        } else if (expr instanceof NameExpr nameExpr) {
            if (token.kind == TokenKind.Equals)
                return new AssignExpr(nameExpr.name, token, parseExpr(0));
            else if (token.kind == TokenKind.LParen) {
                ++current;
                List<Expr> args = new ArrayList<>();
                boolean needsArg = false;
                while (getCurrent().kind != TokenKind.RParen && getCurrent().kind != TokenKind.Eof) {
                    needsArg = false;
                    args.add(parseExpr(0));
                    Token currentToken = getCurrent();
                    if (currentToken.kind != TokenKind.RParen && currentToken.kind != TokenKind.Eof && currentToken.kind != TokenKind.Comma)
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_character", "')' or ','", currentToken.lexeme), currentToken.span()));
                    else if (currentToken.kind == TokenKind.Comma) {
                        needsArg = true;
                        ++current;
                    }
                }

                if (needsArg)
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_expression"), getCurrent().span()));

                Token rParen = getCurrent();
                if (rParen.kind != TokenKind.RParen)
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_character", "')'", rParen.lexeme), rParen.span()));
                else
                    ++current;

                expr = new CallExpr(nameExpr.name, token, args.toArray(new Expr[0]), rParen);
            } else
                return expr;
        } else
            return expr;

        return checkExtension(expr, parentPrecedence);
    }

    @NotNull
    private Token getCurrent() {
        return current >= tokens.length ? tokens[tokens.length - 1] : tokens[current];
    }

    public DiagnosticBag getDiagnostics() {
        return diagnostics;
    }
}
