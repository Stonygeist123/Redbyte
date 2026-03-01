package net.stonygeist.redbyte.screen.robo_terminal;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SStoreRoboCodePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoboTerminalScreen extends Screen {
    private static final int BORDER_COLOR = 0xff808080;
    private static final int SCREEN_COLOR = 0xff000000;

    private static final int TERMINAL_WIDTH = 750;
    private static final int TERMINAL_HEIGHT = 360;

    // Scrollbar
    private static final int TEXT_PADDING_X = 6;
    private static final int TEXT_PADDING_Y = 20;
    private static final int SCROLLBAR_HEIGHT = 4;
    private static final int SCROLLBAR_BOTTOM_PADDING = 8;
    private static final int SCROLLBAR_TRACK_COLOR = 0xff2d2d2d;
    private static final int SCROLLBAR_THUMB_COLOR = 0xff8a8a8a;
    private static final int MIN_SCROLLBAR_THUMB_WIDTH = 14;
    private static final int MAX_TEXT_LINE_WIDTH = 500;

    private int horizontalScrollOffset;
    private int verticalScrollOffset;

    private int curLine;
    private int tickCounter;

    // Selection tracking
    private int selectionStartLine;
    private int selectionStartChar;
    private int selectionEndLine;
    private int selectionEndChar;
    private boolean hasSelection;

    private final RoboEntity roboEntity;
    private TextFieldHelper textFieldHelper;
    private TerminalText terminalText;
    private UUID redbyteID;
    private boolean initialised;
    private boolean followCursorOnRender = true;

    // TODO: Add documentations in-game

    public RoboTerminalScreen(RoboEntity roboEntity) {
        super(Component.translatable("screen.redbyte.robo_terminal.edit"));
        this.roboEntity = roboEntity;
        terminalText = new TerminalText();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new StartButton(width - (width - TERMINAL_WIDTH) / 2 - 100, (height - TERMINAL_HEIGHT) / 2, 100, 20,
                Component.translatable("screen.redbyte.robo_terminal.start"), terminalText::toString, roboEntity));
    }

    @Override
    public void tick() {
        if (initialised) {
            ++tickCounter;
            if (textFieldHelper == null) {
                Minecraft mc = getMinecraft();
                if (mc != null && mc.level != null && mc.player != null) {
                    var x = TextFieldHelper.createClipboardGetter(mc);
                    var y = TextFieldHelper.createClipboardSetter(mc);
                    textFieldHelper = new TextFieldHelper(
                            () -> terminalText.getLines()[curLine],
                            text -> terminalText.setText(text, curLine), x, y,
                            text -> true);
                    clampEditorState();
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
            clampEditorState();

            int textX = x + TEXT_PADDING_X;
            int textY = y + TEXT_PADDING_Y - 6;
            int visibleTextWidth = Math.min(MAX_TEXT_LINE_WIDTH, TERMINAL_WIDTH - (TEXT_PADDING_X * 2));
            int visibleTextHeight = TERMINAL_HEIGHT - TEXT_PADDING_Y - 4;
            String[] lines = terminalText.getLines();
            String currentLine = lines[curLine];
            adjustHorizontalScroll(currentLine, Mth.clamp(textFieldHelper.getCursorPos(), 0, currentLine.length()), visibleTextWidth);
            adjustVerticalScroll(curLine, visibleTextHeight, followCursorOnRender);

            int firstVisibleLine = Math.max(0, verticalScrollOffset / font.lineHeight);
            int lastVisibleLine = Math.min(lines.length - 1, firstVisibleLine + (visibleTextHeight / font.lineHeight) + 1);

            for (int i = firstVisibleLine; i <= lastVisibleLine && i < lines.length; i++) {
                String line = lines[i];
                boolean isCurLine = i == curLine;
                boolean showCursor = (tickCounter / 10) % 2 == 0;

                // Calculate vertical position for this line
                int lineY = textY + (i - firstVisibleLine) * font.lineHeight;

                // Skip if line is above visible area
                if (lineY < textY) continue;
                // Skip if line is below visible area
                if (lineY > textY + visibleTextHeight) break;

                int visibleStart = firstVisibleIndex(line, horizontalScrollOffset);
                int visibleEnd = firstIndexPastVisibleWidth(line, visibleStart, visibleTextWidth + 1);

                int safeCursorPos = Mth.clamp(textFieldHelper.getCursorPos(), 0, line.length());
                int safeSelectionPos = Mth.clamp(textFieldHelper.getSelectionPos(), 0, line.length());

                // Check if this line is part of our custom selection
                boolean isLineInSelection = hasSelection &&
                        ((i >= selectionStartLine && i <= selectionEndLine) ||
                                (i >= selectionEndLine && i <= selectionStartLine));

                if (isLineInSelection) {
                    // Calculate selection bounds for this line
                    int lineSelStart;
                    int lineSelEnd;

                    if (selectionStartLine < selectionEndLine) {
                        if (i == selectionStartLine) {
                            // Start line of multi-line selection
                            lineSelStart = selectionStartChar;
                            lineSelEnd = line.length();
                        } else if (i == selectionEndLine) {
                            // End line of multi-line selection
                            lineSelStart = 0;
                            lineSelEnd = selectionEndChar;
                        } else {
                            // Middle line of multi-line selection
                            lineSelStart = 0;
                            lineSelEnd = line.length();
                        }
                    } else {
                        // Reverse selection (end before start)
                        if (selectionEndLine == selectionStartLine) {
                            // Selection is within single line
                            lineSelStart = Math.min(selectionStartChar, selectionEndChar);
                            lineSelEnd = Math.max(selectionStartChar, selectionEndChar);
                        } else if (i == selectionEndLine) {
                            // Start line of multi-line selection (reversed)
                            lineSelStart = selectionEndChar;
                            lineSelEnd = line.length();
                        } else if (i == selectionStartLine) {
                            // End line of multi-line selection (reversed)
                            lineSelStart = 0;
                            lineSelEnd = selectionStartChar;
                        } else {
                            // Middle line of multi-line selection (reversed)
                            lineSelStart = 0;
                            lineSelEnd = line.length();
                        }
                    }

                    // Draw the line with selection highlighting
                    drawClippedRange(guiGraphics, line, 0, lineSelStart, visibleStart, visibleEnd, textX, lineY, 0xffffff);
                    drawClippedRange(guiGraphics, line, lineSelStart, lineSelEnd, visibleStart, visibleEnd, textX, lineY, 0x0000ff);
                    drawClippedRange(guiGraphics, line, lineSelEnd, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);
                } else if (textFieldHelper.isSelecting() && isCurLine) {
                    // Fall back to TextFieldHelper selection for current line
                    int selStart = Math.min(safeSelectionPos, safeCursorPos);
                    int selEnd = Math.max(safeSelectionPos, safeCursorPos);
                    drawClippedRange(guiGraphics, line, 0, selStart, visibleStart, visibleEnd, textX, lineY, 0xffffff);
                    drawClippedRange(guiGraphics, line, selStart, selEnd, visibleStart, visibleEnd, textX, lineY, 0x0000ff);
                    drawClippedRange(guiGraphics, line, selEnd, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);
                } else {
                    drawClippedRange(guiGraphics, line, 0, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);
                }

                if (isCurLine && showCursor) {
                    int cursorX = textX + font.width(line.substring(visibleStart, safeCursorPos));
                    guiGraphics.vLine(cursorX, lineY - 1, lineY - 1 + font.lineHeight, 0xffff00ff);
                }
            }

            drawHorizontalScrollbar(guiGraphics, currentLine, textX, y + TERMINAL_HEIGHT - SCROLLBAR_BOTTOM_PADDING, visibleTextWidth);
            drawVerticalScrollbar(guiGraphics, x + TERMINAL_WIDTH - TEXT_PADDING_X, y + TEXT_PADDING_Y, visibleTextHeight);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textFieldHelper == null) return super.keyPressed(keyCode, scanCode, modifiers);
        followCursorOnRender = true;
        clampEditorState();

        boolean hasShift = hasShiftDown();
        boolean hasCtrl = hasControlDown();

        // Handle Ctrl+A for select all
        if (keyCode == InputConstants.KEY_A && hasCtrl) {
            selectAll();
            return true;
        }

        // Handle Shift + Arrow keys for selection
        if (hasShift)
            return handleShiftArrowKeys(keyCode);

        // Reset selection when moving without Shift
        if (keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_DOWN ||
                keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_RIGHT) {
            hasSelection = false;
        }

        if (keyCode == InputConstants.KEY_UP) {
            if (curLine == 0)
                textFieldHelper.setCursorToStart();
            else
                --curLine;
            return true;
        } else if (keyCode == InputConstants.KEY_DOWN) {
            if (curLine + 1 >= terminalText.getLines().length)
                terminalText.newLine(curLine, textFieldHelper.getCursorPos());
            ++curLine;
            return true;
        } else if (keyCode == InputConstants.KEY_LEFT) {
            if (textFieldHelper.getCursorPos() > 0)
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() - 1, false);
            else if (curLine > 0) {
                --curLine;
                textFieldHelper.setCursorToEnd();
            }

            return true;
        } else if (keyCode == InputConstants.KEY_RIGHT) {
            if (textFieldHelper.getCursorPos() < terminalText.getLines()[curLine].length()) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() + 1, false);
            } else if (curLine + 1 < terminalText.getLines().length) {
                ++curLine;
                textFieldHelper.setCursorToStart();
            }

            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            terminalText.newLine(curLine, textFieldHelper.getCursorPos());
            ++curLine;
            return true;
        } else if (keyCode == InputConstants.KEY_BACKSPACE) {
            // Handle selection deletion
            if (hasSelection) {
                deleteSelection();
                return true;
            }

            TextFieldHelper.CursorStep cursorStep = hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
            if (textFieldHelper.getCursorPos() == 0 && curLine > 0) {
                --curLine;
                textFieldHelper.insertText(terminalText.getLines()[curLine + 1]);
                terminalText.removeLine(curLine + 1);
            } else
                textFieldHelper.removeFromCursor(-1, cursorStep);
            return true;
        } else
            return textFieldHelper.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textFieldHelper == null) return false;
        if (Character.isISOControl(codePoint)) return false;
        followCursorOnRender = true;
        clampEditorState();

        // Handle text replacement when there's a selection
        if (hasSelection)
            deleteSelection();

        String text = String.valueOf(codePoint);
        textFieldHelper.insertText(text);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (textFieldHelper == null) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        int visibleTextHeight = TERMINAL_HEIGHT - TEXT_PADDING_Y - 4;
        int totalContentHeight = terminalText.getLines().length * font.lineHeight;

        // Check if Shift is held for horizontal scrolling
        if (hasShiftDown()) {
            // Horizontal scrolling with Shift + mouse wheel
            String currentLine = terminalText.getLines()[curLine];
            int lineWidth = font.width(currentLine);
            int maxLineWidth = Math.min(MAX_TEXT_LINE_WIDTH, TERMINAL_WIDTH - (TEXT_PADDING_X * 2));
            int maxScrollX = Math.max(0, lineWidth - maxLineWidth);

            if (maxScrollX > 0) {
                // Calculate scroll amount based on font width and scrollY (using scrollY for horizontal since scrollX is usually 0)
                int scrollAmount = (int) (font.width("W") * -scrollY);
                horizontalScrollOffset += scrollAmount;

                // Clamp scroll offset
                horizontalScrollOffset = Mth.clamp(horizontalScrollOffset, 0, maxScrollX);

                // Move cursor position based on scroll amount
                int scrollChars = (int) Math.abs(scrollY) * 4; // Move 4 characters per scroll unit
                if (scrollY > 0) {
                    // Scrolling right - move cursor left
                    int newCursorPos = Math.max(0, textFieldHelper.getCursorPos() - scrollChars);
                    textFieldHelper.setCursorPos(newCursorPos, false);
                } else if (scrollY < 0) {
                    // Scrolling left - move cursor right
                    int newCursorPos = Math.min(currentLine.length(), textFieldHelper.getCursorPos() + scrollChars);
                    textFieldHelper.setCursorPos(newCursorPos, false);
                }

                return true;
            }
        } else {
            if (totalContentHeight > visibleTextHeight) {
                followCursorOnRender = false;

                int scrollAmount = (int) (font.lineHeight * -scrollY);
                verticalScrollOffset += scrollAmount;

                int maxScrollY = Math.max(0, totalContentHeight - visibleTextHeight);
                verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);

                int firstVisibleLine = Math.max(0, verticalScrollOffset / font.lineHeight);
                int lastVisibleLine = Math.min(terminalText.getLines().length - 1, firstVisibleLine + (visibleTextHeight / font.lineHeight));

                if (scrollY > 0) {
                    if (curLine <= firstVisibleLine && curLine > 0) {
                        --curLine;
                        textFieldHelper.setCursorToEnd();
                    }
                } else if (scrollY < 0)
                    if (curLine >= lastVisibleLine && curLine + 1 < terminalText.getLines().length) {
                        ++curLine;
                        textFieldHelper.setCursorToStart();
                    }

                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        super.onClose();
        Redbyte.CHANNEL.send(new C2SStoreRoboCodePacket(redbyteID, terminalText.toString()), PacketDistributor.SERVER.noArg());
    }

    private void drawClippedRange(GuiGraphics guiGraphics, String line, int rangeStart, int rangeEnd,
                                  int clipStart, int clipEnd, int x, int y, int color) {
        int drawStart = Math.max(rangeStart, clipStart);
        int drawEnd = Math.min(rangeEnd, clipEnd);
        if (drawStart >= drawEnd)
            return;

        int drawX = x + font.width(line.substring(clipStart, drawStart));
        guiGraphics.drawString(font, line.substring(drawStart, drawEnd), drawX, y, color);
    }

    private int firstVisibleIndex(String line, int scrollWidth) {
        int i = 0;
        int width = 0;
        while (i < line.length()) {
            int charWidth = font.width(line.substring(i, i + 1));
            if (width + charWidth > scrollWidth)
                break;

            width += charWidth;
            ++i;
        }

        return i;
    }

    private int firstIndexPastVisibleWidth(String line, int startIndex, int maxWidth) {
        int i = startIndex;
        int width = 0;
        while (i < line.length()) {
            int charWidth = font.width(line.substring(i, i + 1));
            if (width + charWidth > maxWidth)
                break;

            width += charWidth;
            ++i;
        }

        return i;
    }

    private void adjustHorizontalScroll(String line, int cursorPos, int visibleWidth) {
        int lineWidth = font.width(line);
        int maxScrollX = Math.max(0, lineWidth - visibleWidth);
        horizontalScrollOffset = Mth.clamp(horizontalScrollOffset, 0, maxScrollX);

        int cursorWidth = font.width(line.substring(0, cursorPos));
        if (cursorWidth < horizontalScrollOffset)
            horizontalScrollOffset = cursorWidth;
        else if (cursorWidth > horizontalScrollOffset + visibleWidth - 1)
            horizontalScrollOffset = cursorWidth - visibleWidth + 1;
        horizontalScrollOffset = Mth.clamp(horizontalScrollOffset, 0, maxScrollX);
    }

    private void adjustVerticalScroll(int curLine, int visibleHeight, boolean followCursor) {
        int totalContentHeight = terminalText.getLines().length * font.lineHeight;
        int maxScrollY = Math.max(0, totalContentHeight - visibleHeight);
        verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);

        if (!followCursor)
            return;

        int currentLineTop = curLine * font.lineHeight;
        int currentLineBottom = currentLineTop + font.lineHeight;

        if (currentLineTop < verticalScrollOffset)
            verticalScrollOffset = currentLineTop;
        else if (currentLineBottom > verticalScrollOffset + visibleHeight)
            verticalScrollOffset = currentLineBottom - visibleHeight;

        verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);
    }

    private void drawHorizontalScrollbar(GuiGraphics guiGraphics, String line, int x, int y, int visibleTextWidth) {
        int lineWidth = font.width(line);
        if (lineWidth <= visibleTextWidth)
            return;

        int trackX2 = x + visibleTextWidth;
        guiGraphics.fill(x, y, trackX2, y + SCROLLBAR_HEIGHT, SCROLLBAR_TRACK_COLOR);

        int cursorPx = font.width(line.substring(0, Mth.clamp(textFieldHelper.getCursorPos(), 0, line.length())));
        int maxThumbTravel = Math.max(0, visibleTextWidth - MIN_SCROLLBAR_THUMB_WIDTH);
        int thumbOffset = (int) ((cursorPx / (double) lineWidth) * maxThumbTravel);
        int thumbX = x + Mth.clamp(thumbOffset, 0, maxThumbTravel);

        guiGraphics.fill(thumbX, y, thumbX + MIN_SCROLLBAR_THUMB_WIDTH, y + SCROLLBAR_HEIGHT, SCROLLBAR_THUMB_COLOR);
    }

    private void drawVerticalScrollbar(GuiGraphics guiGraphics, int x, int y, int visibleTextHeight) {
        int lineCount = terminalText.getLines().length;
        int totalContentHeight = lineCount * font.lineHeight;
        if (totalContentHeight <= visibleTextHeight)
            return;

        // Draw scrollbar track
        guiGraphics.fill(x, y, x + SCROLLBAR_HEIGHT, y + visibleTextHeight, SCROLLBAR_TRACK_COLOR);

        // Calculate thumb size based on content vs visible ratio
        float viewRatio = (float) visibleTextHeight / totalContentHeight;
        int thumbHeight = Math.max(MIN_SCROLLBAR_THUMB_WIDTH, (int) (visibleTextHeight * viewRatio));

        // Calculate thumb position based on current scroll offset
        int maxScrollOffset = Math.max(0, totalContentHeight - visibleTextHeight);
        float scrollRatio = maxScrollOffset > 0 ? (float) verticalScrollOffset / maxScrollOffset : 0f;
        scrollRatio = Mth.clamp(scrollRatio, 0, 1);

        int maxThumbTravel = visibleTextHeight - thumbHeight;
        int thumbY = y + (int) (scrollRatio * maxThumbTravel);

        guiGraphics.fill(x, thumbY, x + SCROLLBAR_HEIGHT, thumbY + thumbHeight, SCROLLBAR_THUMB_COLOR);
    }

    public void setId(UUID redbyteID) {
        this.redbyteID = redbyteID;
    }

    public void saveCode(String code) {
        terminalText = new TerminalText(code == null ? "" : code);
        curLine = 0;
        tickCounter = 0;
        textFieldHelper = null;
        horizontalScrollOffset = 0;
        verticalScrollOffset = 0;
        followCursorOnRender = true;
        initialised = true;

        // Initialize selection variables
        selectionStartLine = 0;
        selectionStartChar = 0;
        selectionEndLine = 0;
        selectionEndChar = 0;
        hasSelection = false;
    }

    private void clampEditorState() {
        String[] lines = terminalText.getLines();
        if (lines.length == 0) {
            terminalText.newLine(curLine, textFieldHelper.getCursorPos());
            lines = terminalText.getLines();
        }

        curLine = Mth.clamp(curLine, 0, lines.length - 1);
        if (textFieldHelper.getCursorPos() > terminalText.getLines()[curLine].length())
            textFieldHelper.setCursorToEnd();
    }

    private void selectAll() {
        // If already have a selection, select all text
        if (hasSelection) {
            selectionStartLine = 0;
            selectionStartChar = 0;
            selectionEndLine = terminalText.getLines().length - 1;
            selectionEndChar = terminalText.getLines()[selectionEndLine].length();
        } else {
            // First Ctrl+A: select current line
            selectionStartLine = curLine;
            selectionStartChar = 0;
            selectionEndLine = curLine;
            selectionEndChar = terminalText.getLines()[curLine].length();
        }

        hasSelection = true;
    }

    private boolean handleShiftArrowKeys(int keyCode) {
        String[] lines = terminalText.getLines();

        // Initialize selection if not already started
        if (!hasSelection) {
            hasSelection = true;
            selectionStartLine = curLine;
            selectionStartChar = textFieldHelper.getCursorPos();
            selectionEndLine = curLine;
            selectionEndChar = textFieldHelper.getCursorPos();
        }

        // Update selection end point based on arrow key
        if (keyCode == InputConstants.KEY_UP) {
            if (curLine > 0) {
                --curLine;
                if (textFieldHelper.getCursorPos() > lines[curLine].length()) {
                    textFieldHelper.setCursorToEnd();
                    selectionEndChar = lines[curLine].length();
                } else {
                    selectionEndChar = textFieldHelper.getCursorPos();
                }
                selectionEndLine = curLine;
            }
        } else if (keyCode == InputConstants.KEY_DOWN) {
            if (curLine + 1 < lines.length) {
                ++curLine;
                if (textFieldHelper.getCursorPos() > lines[curLine].length()) {
                    textFieldHelper.setCursorToEnd();
                    selectionEndChar = lines[curLine].length();
                } else {
                    selectionEndChar = textFieldHelper.getCursorPos();
                }
                selectionEndLine = curLine;
            }
        } else if (keyCode == InputConstants.KEY_LEFT) {
            if (textFieldHelper.getCursorPos() > 0) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() - 1, false);
                selectionEndChar = textFieldHelper.getCursorPos();
                selectionEndLine = curLine;
            } else if (curLine > 0) {
                --curLine;
                textFieldHelper.setCursorToEnd();
                selectionEndChar = lines[curLine].length();
                selectionEndLine = curLine;
            }
        } else if (keyCode == InputConstants.KEY_RIGHT) {
            if (textFieldHelper.getCursorPos() < lines[curLine].length()) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() + 1, false);
                selectionEndChar = textFieldHelper.getCursorPos();
                selectionEndLine = curLine;
            } else if (curLine + 1 < lines.length) {
                ++curLine;
                textFieldHelper.setCursorToStart();
                selectionEndChar = 0;
                selectionEndLine = curLine;
            }
        }

        return true;
    }

    private void deleteSelection() {
        if (!hasSelection) return;

        String[] lines = terminalText.getLines();

        // Determine the actual selection bounds (normalize start/end)
        int actualStartLine = Math.min(selectionStartLine, selectionEndLine);
        int actualEndLine = Math.max(selectionStartLine, selectionEndLine);
        int actualStartChar, actualEndChar;

        if (selectionStartLine <= selectionEndLine) {
            actualStartChar = selectionStartChar;
            actualEndChar = selectionEndChar;
        } else {
            actualStartChar = selectionEndChar;
            actualEndChar = selectionStartChar;
        }

        // Handle single line selection
        if (actualStartLine == actualEndLine) {
            String line = lines[actualStartLine];
            String before = line.substring(0, actualStartChar);
            String after = line.substring(actualEndChar);
            terminalText.setText(before + after, actualStartLine);
            curLine = actualStartLine;
            textFieldHelper.setCursorPos(actualStartChar, false);
        } else {
            // Handle multi-line selection
            String firstLine = lines[actualStartLine];
            String lastLine = lines[actualEndLine];

            // Combine the parts before the selection start and after the selection end
            String combined = firstLine.substring(0, actualStartChar) + lastLine.substring(actualEndChar);

            // Replace the first line with the combined text
            terminalText.setText(combined, actualStartLine);

            // Remove all the lines in between
            for (int i = actualEndLine; i > actualStartLine; i--) {
                terminalText.removeLine(i);
            }

            // Update cursor position
            curLine = actualStartLine;
            textFieldHelper.setCursorPos(actualStartChar, false);
        }

        // Clear selection
        hasSelection = false;
    }
}
