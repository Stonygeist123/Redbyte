package net.stonygeist.interpreter.miscellaneous;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerPlayer;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.symbols.FunctionSymbol;
import net.stonygeist.interpreter.symbols.TypeSymbol;

import javax.annotation.Nullable;
import java.util.List;

public enum Config {
    ;
    public final static int unaryPrecedence = 7;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 6;
            case Plus, Minus -> 5;
            case EqualsEquals, NotEquals -> 4;
            case Greater, GreaterEquals, Less, LessEquals -> 3;
            case And -> 2;
            case Or -> 1;
            default -> 0;
        };
    }

    @Nullable
    public static FunctionSymbol getFunction(String name) {
        return functions.stream().filter(f -> f.name.toLowerCase().equals(name)).findFirst().orElse(null);
    }

    public static final ImmutableList<FunctionSymbol> functions = new ImmutableList.Builder<FunctionSymbol>()
            .add(new FunctionSymbol("walk", ImmutableList.of(TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> null))
            .add(new FunctionSymbol("walkto", ImmutableList.of(TypeSymbol.Number, TypeSymbol.Number, TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> null))
            .add(new FunctionSymbol("jump", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> null))
            .add(new FunctionSymbol("position_x", ImmutableList.of(), TypeSymbol.Number, (ev, robo, args) -> (float) ev.getRoboEntity().position().x))
            .add(new FunctionSymbol("position_y", ImmutableList.of(), TypeSymbol.Number, (ev, robo, args) -> (float) ev.getRoboEntity().position().y))
            .add(new FunctionSymbol("position_z", ImmutableList.of(), TypeSymbol.Number, (ev, robo, args) -> (float) ev.getRoboEntity().position().z))
            .add(new FunctionSymbol("follow", ImmutableList.of(TypeSymbol.Text), TypeSymbol.Void, (ev, robo, args) -> {
                String address = (String) args[0];
                List<ServerPlayer> players = robo.serverLevel.getPlayers(p -> p.getName().getString().equals(address));
                if (players.isEmpty())
                    throw new RuntimeException();
                robo.setTargetPlayer(players.getFirst());
                return null;
            }))
            .add(new FunctionSymbol("stop_follow", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> {
                robo.setTargetPlayer(null);
                return null;
            }))
            .add(new FunctionSymbol("attack", ImmutableList.of(TypeSymbol.Text), TypeSymbol.Void, (ev, robo, args) -> null))
            .add(new FunctionSymbol("can_attack", ImmutableList.of(TypeSymbol.Text), TypeSymbol.Boolean, (ev, robo, args) -> null))
            .build();

    public static final ImmutableMap<String, TokenKind> keywords = new ImmutableMap.Builder<String, TokenKind>()
            .put("if", TokenKind.If)
            .put("else", TokenKind.Else)
            .put("loop", TokenKind.Loop)
            .put("while", TokenKind.While)
            .put("till", TokenKind.Till)
            .put("or", TokenKind.Or)
            .put("and", TokenKind.And)
            .build();
}
