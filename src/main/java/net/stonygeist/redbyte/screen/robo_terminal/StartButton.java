package net.stonygeist.redbyte.screen.robo_terminal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.stonygeist.interpreter.analysis.Lexer;
import net.stonygeist.interpreter.analysis.Token;

import java.util.List;
import java.util.function.Supplier;

public class StartButton extends Button {
    private final Supplier<String> getText;

    protected StartButton(int x, int y, int width, int height, MutableComponent message, Supplier<String> getText) {
        super(x, y, width, height, message, b -> {
        }, s -> message);
        this.getText = getText;
    }

    @Override
    public void onPress() {
        super.onPress();
        Lexer lexer = new Lexer(getText.get());
        List<Token> tokens = lexer.lex();
        System.out.println(tokens.toArray().length);
    }
}
