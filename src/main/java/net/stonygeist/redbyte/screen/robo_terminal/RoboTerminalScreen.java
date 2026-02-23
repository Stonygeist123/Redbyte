package net.stonygeist.redbyte.screen.robo_terminal;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SRoboCodePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoboTerminalScreen extends Screen {
    private static final int BORDER_COLOR = 0xff808080;
    private static final int SCREEN_COLOR = 0xff000000;

    private static final int TERMINAL_WIDTH = 750;
    private static final int TERMINAL_HEIGHT = 360;

    public static final int MAX_TEXT_LINE_WIDTH = 275;

    private int curLine;
    private int tickCounter;

    private TextFieldHelper textFieldHelper;
    private TerminalText terminalText;
    private UUID redbyteID;
    private boolean initialised;

    public RoboTerminalScreen() {
        super(Component.translatable("screen.redbyte.robo_terminal.edit"));
        terminalText = new TerminalText();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new StartButton(width - (width - TERMINAL_WIDTH) / 2 - 100, (height - TERMINAL_HEIGHT) / 2, 100, 20,
                Component.translatable("screen.redbyte.robo_terminal.start"), terminalText::toString, redbyteID));
    }

    public void setId(UUID redbyteID) {
        this.redbyteID = redbyteID;
        Redbyte.CHANNEL.send(new C2SRoboCodePacket(redbyteID, getCode()), PacketDistributor.SERVER.noArg());
    }

    public String getCode() {
        return terminalText.toString();
    }

    public void setCode(String code) {
        terminalText = new TerminalText(code);
        initialised = true;
    }

    @Override
    public void tick() {
        if (initialised) {
            ++tickCounter;
            if (textFieldHelper == null) {
                Minecraft mc = getMinecraft();
                if (mc != null && mc.level != null && mc.player != null) {
                    textFieldHelper = new TextFieldHelper(
                            this::getCode,
                            text -> terminalText.setText(text, curLine),
                            TextFieldHelper.createClipboardGetter(mc),
                            TextFieldHelper.createClipboardSetter(mc),
                            text -> mc.font.width(text) <= MAX_TEXT_LINE_WIDTH);
                }
            }
        }
    }

    @Override
    protected void renderBlurredBackground(float pPartialTick) {
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - TERMINAL_WIDTH) / 2;
        int y = (height - TERMINAL_HEIGHT) / 2;

        guiGraphics.fill(x - 4, y - 4, x + TERMINAL_WIDTH + 4, y + TERMINAL_HEIGHT + 4, BORDER_COLOR);
        guiGraphics.fill(x, y, x + TERMINAL_WIDTH, y + TERMINAL_HEIGHT, SCREEN_COLOR);

        guiGraphics.drawString(font, getTitle(), x + 6, y + 6, 0x00ff00);

        if (!initialised || textFieldHelper == null) {
            guiGraphics.drawString(font, Component.translatable("screen.redbyte.robo_terminal.loading"), width / 2, height / 2, 0xffffffff);
        } else {
            int textY = y + 20;
            String[] lines = terminalText.getLines();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                boolean isCurLine = i == curLine;
                boolean showCursor = (tickCounter / 10) % 2 == 0;
                guiGraphics.drawString(font, line, x + 6, textY, 0xffffff);
                if (isCurLine && showCursor)
                    guiGraphics.vLine(x + 5 + font.width(line.substring(0, textFieldHelper.getCursorPos())), textY - 1, textY - 1 + font.lineHeight, 0xffff00ff);
                textY += isCurLine ? 14 : 10;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    //    TODO: Fix selection, ctrl + A ...
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textFieldHelper == null) return super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == InputConstants.KEY_UP) {
            if (curLine == 0)
                textFieldHelper.setCursorPos(0);
            else {
                curLine = curLine - 1;
                if (textFieldHelper.getCursorPos() >= terminalText.getLines()[curLine].length())
                    textFieldHelper.setCursorPos(terminalText.getLines()[curLine].length());
            }

            return true;
        } else if (keyCode == InputConstants.KEY_DOWN) {
            if (curLine + 1 >= terminalText.getLines().length)
                terminalText.newLine();

            curLine = curLine + 1;
            if (textFieldHelper.getCursorPos() >= terminalText.getLines()[curLine].length())
                textFieldHelper.setCursorPos(terminalText.getLines()[curLine].length());
            return true;
        } else if (keyCode == InputConstants.KEY_LEFT) {
            if (textFieldHelper.getCursorPos() > 0) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() - 1);
                textFieldHelper.setSelectionPos(textFieldHelper.getCursorPos());
            } else if (curLine > 0) {
                --curLine;
                textFieldHelper.setCursorPos(terminalText.getLines()[curLine].length());
                textFieldHelper.setSelectionPos(textFieldHelper.getCursorPos());
            }

            return true;
        } else if (keyCode == InputConstants.KEY_RIGHT) {
            if (textFieldHelper.getCursorPos() < terminalText.getLines()[curLine].length()) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() + 1);
                textFieldHelper.setSelectionPos(textFieldHelper.getCursorPos());
            } else if (curLine + 1 < terminalText.getLines().length) {
                ++curLine;
                textFieldHelper.setCursorPos(0);
                textFieldHelper.setSelectionPos(textFieldHelper.getCursorPos());
            }

            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            terminalText.newLine();
            curLine = curLine + 1;
            textFieldHelper.setCursorPos(0);
            return true;
        } else return textFieldHelper.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textFieldHelper == null) return false;
        if (Character.isISOControl(codePoint)) return false;

        String text = String.valueOf(codePoint);
        terminalText.addText(text, curLine, textFieldHelper.getCursorPos());

        int cursorPos = textFieldHelper.getCursorPos();
        textFieldHelper.setCursorPos(cursorPos + 1);
        textFieldHelper.setSelectionPos(textFieldHelper.getCursorPos());
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        Redbyte.CHANNEL.send(new C2SRoboCodePacket(redbyteID, getCode()), PacketDistributor.SERVER.noArg());
    }
}
