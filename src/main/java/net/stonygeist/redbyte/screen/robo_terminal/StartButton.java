package net.stonygeist.redbyte.screen.robo_terminal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.interpreter.Evaluator;
import net.stonygeist.interpreter.analysis.Lexer;
import net.stonygeist.interpreter.analysis.Parser;
import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.stmt.Stmt;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SRoboCodePacket;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class StartButton extends Button {
    private final Supplier<String> getText;
    private final UUID redbyteID;

    protected StartButton(int x, int y, int width, int height, MutableComponent message, Supplier<String> getText, UUID redbyteID) {
        super(x, y, width, height, message, b -> {
        }, s -> message);
        this.getText = getText;
        this.redbyteID = redbyteID;
    }

    @Override
    public void onPress() {
        super.onPress();
        Redbyte.CHANNEL.send(new C2SRoboCodePacket(redbyteID, getText.get()), PacketDistributor.SERVER.noArg());
        try {
            Lexer lexer = new Lexer(getText.get());
            List<Token> tokens = lexer.lex();
            Parser parser = new Parser(tokens.toArray(new Token[0]));
            Stmt[] stmts = parser.parse();
            Evaluator evaluator = new Evaluator(stmts, redbyteID);
            evaluator.run();
        } catch (RuntimeException e) {
            System.out.println("Error");
        }
    }
}
