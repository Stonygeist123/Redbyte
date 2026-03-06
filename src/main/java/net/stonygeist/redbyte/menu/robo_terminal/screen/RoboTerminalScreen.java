package net.stonygeist.redbyte.menu.robo_terminal.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.menu.robo_terminal.RoboTerminal;
import net.stonygeist.redbyte.server.C2SStoreRoboCodePacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class RoboTerminalScreen extends AbstractContainerScreen<RoboTerminal> {
    private static final int BORDER_COLOR = 0xff808080;
    private static final int SCREEN_COLOR = 0xff000000;

    private static final int TERMINAL_WIDTH = 800;
    private static final int TERMINAL_HEIGHT = 400;
    private static final int NAV_BAR_HEIGHT = 40;

    private static final int TEXT_PADDING_X = 14;
    private static final int TEXT_PADDING_Y = 20;

    private static final int RESULT_PANEL_GAP = 20;
    private static final int DIAGNOSTIC_HIGHLIGHT_COLOR = 0x66ff0000;

    // Scrollbar
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

    private TextFieldHelper textFieldHelper;
    private boolean followCursorOnRender = true;
    private boolean errorHighlightingDisabled;
    private boolean runDisabledUntilBuild = true;
    private CompoundTag lastDiagnosticsTag = new CompoundTag();

    // TODO: Add documentations in-game

    public RoboTerminalScreen(RoboTerminal menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (getMenu().getRoboEntity() != null && getMenu().getTerminalText() != null) {
            addRenderableWidget(new BuildButton(
                    width - (width - TERMINAL_WIDTH) / 2 - 250, (height - TERMINAL_HEIGHT) / 2, 100, 20,
                    () -> getMenu().getTerminalText().toString(),
                    getMenu().getRoboEntity().getRedbyteID().orElse(null))
            );
            addRenderableWidget(new RunButton(
                    width - (width - TERMINAL_WIDTH) / 2 - 125, (height - TERMINAL_HEIGHT) / 2, 100, 20,
                    getMenu().getRoboEntity(),
                    this::runIsDisabled)
            );
        }
    }

    @Override
    public void containerTick() {
        if (getMenu().getTerminalText() != null) {
            ++tickCounter;
            if (textFieldHelper == null) {
                Minecraft mc = getMinecraft();
                if (mc != null && mc.level != null && mc.player != null) {
                    var x = TextFieldHelper.createClipboardGetter(mc);
                    var y = TextFieldHelper.createClipboardSetter(mc);
                    textFieldHelper = new TextFieldHelper(
                            () -> getMenu().getTerminalText().getLines()[curLine],
                            this::setText, x, y,
                            text -> true);
                    clampEditorState();
                }
            }
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - TERMINAL_WIDTH) / 2;
        int y = (height - TERMINAL_HEIGHT) / 2;
        guiGraphics.drawString(font, getTitle(), x + 6, y + NAV_BAR_HEIGHT, 0x00ff00);
        guiGraphics.fill(x - 4, y - 4, x + TERMINAL_WIDTH + 4, y + TERMINAL_HEIGHT + 4, BORDER_COLOR);
        guiGraphics.fill(x, y, x + TERMINAL_WIDTH, y + TERMINAL_HEIGHT, SCREEN_COLOR);

        if (getMenu().getTerminalText() == null || textFieldHelper == null) {
            guiGraphics.drawString(font, Component.translatable("screen.redbyte.robo_terminal.loading"), width / 2, height / 2, 0xffffffff);
        } else {
            if (getMenu().getRoboEntity() != null) {
                CompoundTag currentDiagnosticsTag = getMenu().getRoboEntity().getDiagnosticsTag();
                if (!currentDiagnosticsTag.equals(lastDiagnosticsTag)) {
                    errorHighlightingDisabled = false;
                    runDisabledUntilBuild = false;
                    lastDiagnosticsTag = currentDiagnosticsTag.copy();
                } else if (lastDiagnosticsTag.isEmpty() && currentDiagnosticsTag.isEmpty())
                    runDisabledUntilBuild = false;
            }

            clampEditorState();
            int textX = x + TEXT_PADDING_X;
            int textY = y + TEXT_PADDING_Y + NAV_BAR_HEIGHT;
            int visibleTextWidth = Math.min(MAX_TEXT_LINE_WIDTH, TERMINAL_WIDTH - (TEXT_PADDING_X * 2));
            int visibleTextHeight = getvisibleTextHeight();
            String[] lines = getMenu().getTerminalText().getLines();
            String currentLine = lines[curLine];
            adjustHorizontalScroll(currentLine, Mth.clamp(textFieldHelper.getCursorPos(), 0, currentLine.length()), visibleTextWidth);
            adjustVerticalScroll(curLine, visibleTextHeight, followCursorOnRender);

            int firstVisibleLine = Math.max(0, verticalScrollOffset / font.lineHeight);
            int lastVisibleLine = Math.min(lines.length - 1, firstVisibleLine + (visibleTextHeight / font.lineHeight) + 1);

            for (int i = firstVisibleLine; i <= lastVisibleLine && i < lines.length; ++i) {
                String line = lines[i];
                boolean isCurLine = i == curLine;
                boolean showCursor = (tickCounter / 10) % 2 == 0;

                // Calculate vertical position for this lineStart
                int lineY = textY + (i - firstVisibleLine) * font.lineHeight;

                // Skip if lineStart is above visible area
                if (lineY < textY) continue;
                // Skip if lineStart is below visible area
                if (lineY > textY + visibleTextHeight) break;

                int visibleStart = firstVisibleIndex(line, horizontalScrollOffset);
                int visibleEnd = firstIndexPastVisibleWidth(line, visibleStart, visibleTextWidth + 1);

                int safeCursorPos = Mth.clamp(textFieldHelper.getCursorPos(), 0, line.length());
                int safeSelectionPos = Mth.clamp(textFieldHelper.getSelectionPos(), 0, line.length());

                // Check if this lineStart is part of our custom selection
                boolean isLineInSelection = hasSelection &&
                        ((i >= selectionStartLine && i <= selectionEndLine) ||
                                (i >= selectionEndLine && i <= selectionStartLine));

                drawErrorHighlightsForLine(
                        guiGraphics, line, i, lineY, textX,
                        visibleStart, visibleEnd + 1
                );

                if (isLineInSelection) {
                    // Calculate selection bounds for this lineStart
                    int lineSelStart;
                    int lineSelEnd;

                    if (selectionStartLine < selectionEndLine) {
                        if (i == selectionStartLine) {
                            // Start lineStart of multi-lineStart selection
                            lineSelStart = selectionStartChar;
                            lineSelEnd = line.length();
                        } else if (i == selectionEndLine) {
                            // End lineStart of multi-lineStart selection
                            lineSelStart = 0;
                            lineSelEnd = selectionEndChar;
                        } else {
                            // Middle lineStart of multi-lineStart selection
                            lineSelStart = 0;
                            lineSelEnd = line.length();
                        }
                    } else {
                        // Reverse selection (endColumn before startColumn)
                        if (selectionEndLine == selectionStartLine) {
                            // Selection is within single lineStart
                            lineSelStart = Math.min(selectionStartChar, selectionEndChar);
                            lineSelEnd = Math.max(selectionStartChar, selectionEndChar);
                        } else if (i == selectionEndLine) {
                            // Start lineStart of multi-lineStart selection (reversed)
                            lineSelStart = selectionEndChar;
                            lineSelEnd = line.length();
                        } else if (i == selectionStartLine) {
                            // End lineStart of multi-lineStart selection (reversed)
                            lineSelStart = 0;
                            lineSelEnd = selectionStartChar;
                        } else {
                            // Middle lineStart of multi-lineStart selection (reversed)
                            lineSelStart = 0;
                            lineSelEnd = line.length();
                        }
                    }

                    // Draw the lineStart with selection highlighting
                    drawClippedRange(guiGraphics, line, 0, lineSelStart, visibleStart, visibleEnd, textX, lineY, 0xffffff);
                    drawClippedRange(guiGraphics, line, lineSelStart, lineSelEnd, visibleStart, visibleEnd, textX, lineY, 0x0000ff);
                    drawClippedRange(guiGraphics, line, lineSelEnd, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);
                } else if (textFieldHelper.isSelecting() && isCurLine) {
                    // Fall back to TextFieldHelper selection for current lineStart
                    int selStart = Math.min(safeSelectionPos, safeCursorPos);
                    int selEnd = Math.max(safeSelectionPos, safeCursorPos);
                    drawClippedRange(guiGraphics, line, 0, selStart, visibleStart, visibleEnd, textX, lineY, 0xffffff);
                    drawClippedRange(guiGraphics, line, selStart, selEnd, visibleStart, visibleEnd, textX, lineY, 0x0000ff);
                    drawClippedRange(guiGraphics, line, selEnd, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);
                } else
                    drawClippedRange(guiGraphics, line, 0, line.length(), visibleStart, visibleEnd + 1, textX, lineY, 0xffffff);

                guiGraphics.drawString(font, (i + 1) + ": ", x + 2, lineY, 0xa9a9a9);
                if (isCurLine && showCursor) {
                    int cursorX = textX + font.width(line.substring(visibleStart, safeCursorPos));
                    guiGraphics.vLine(cursorX, lineY - 1, lineY - 1 + font.lineHeight, 0xffff00ff);
                }
            }

            drawHorizontalScrollbar(guiGraphics, currentLine, textX, y + TERMINAL_HEIGHT - SCROLLBAR_BOTTOM_PADDING, visibleTextWidth);
            drawVerticalScrollbar(guiGraphics, x + MAX_TEXT_LINE_WIDTH + RESULT_PANEL_GAP, y + TEXT_PADDING_Y + NAV_BAR_HEIGHT, visibleTextHeight);
        }

        drawResultPanel(guiGraphics, x, y);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBlurredBackground(float pPartialTick) {
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
            if (curLine + 1 >= getMenu().getTerminalText().getLines().length)
                getMenu().getTerminalText().newLine(curLine, textFieldHelper.getCursorPos());
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
            if (textFieldHelper.getCursorPos() < getMenu().getTerminalText().getLines()[curLine].length()) {
                textFieldHelper.setCursorPos(textFieldHelper.getCursorPos() + 1, false);
            } else if (curLine + 1 < getMenu().getTerminalText().getLines().length) {
                ++curLine;
                textFieldHelper.setCursorToStart();
            }

            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            getMenu().getTerminalText().newLine(curLine, textFieldHelper.getCursorPos());
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
                textFieldHelper.insertText(getMenu().getTerminalText().getLines()[curLine + 1]);
                getMenu().getTerminalText().removeLine(curLine + 1);
            } else
                textFieldHelper.removeFromCursor(-1, cursorStep);
            return true;
        } else if (keyCode == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }

        return textFieldHelper.keyPressed(keyCode);
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
        int visibleTextHeight = getvisibleTextHeight();
        int totalContentHeight = getMenu().getTerminalText().getLines().length * font.lineHeight;

        // Check if Shift is held for horizontal scrolling
        if (hasShiftDown()) {
            // Horizontal scrolling with Shift + mouse wheel
            String currentLine = getMenu().getTerminalText().getLines()[curLine];
            int lineWidth = font.width(currentLine);
            int maxLineWidth = Math.min(MAX_TEXT_LINE_WIDTH, TERMINAL_WIDTH - (TEXT_PADDING_X * 2));
            int maxScrollX = Math.max(0, lineWidth - maxLineWidth);

            if (maxScrollX > 0) {
                // Calculate scroll amount based on font width and scrollY (using scrollY for horizontal since scrollX is usually 0)
                int scrollAmount = (int) (font.width("W") * -scrollY);
                horizontalScrollOffset += scrollAmount;
                // Clamp scroll offset
                horizontalScrollOffset = Mth.clamp(horizontalScrollOffset, 0, maxScrollX);
                return true;
            }
        } else {
            if (totalContentHeight > visibleTextHeight) {
                followCursorOnRender = false;
                int scrollAmount = (int) (font.lineHeight * -scrollY);
                int maxScrollY = Math.max(0, totalContentHeight - visibleTextHeight);
                verticalScrollOffset += scrollAmount;
                verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (getMenu().getRoboEntity() != null)
            Redbyte.CHANNEL.send(new C2SStoreRoboCodePacket(getMenu().getRoboEntity().getRedbyteID().orElse(null), getMenu().getTerminalText().toString()), PacketDistributor.SERVER.noArg());
    }

    private int getvisibleTextHeight() {
        return TERMINAL_HEIGHT - NAV_BAR_HEIGHT - TEXT_PADDING_Y * 2;
    }

    private void setText(String text) {
        getMenu().getTerminalText().setText(text, curLine);
        errorHighlightingDisabled = true;
        runDisabledUntilBuild = true;
    }

    private boolean runIsDisabled() {
        if (getMenu().getRoboEntity() == null)
            return true;
        if (runDisabledUntilBuild)
            return true;
        return !getMenu().getRoboEntity().getBuildDone() || !getMenu().getRoboEntity().getDiagnostics().isEmpty();
    }

    private void drawResultPanel(GuiGraphics guiGraphics, int screenX, int screenY) {
        int panelX = screenX + TEXT_PADDING_X + MAX_TEXT_LINE_WIDTH + RESULT_PANEL_GAP;
        int panelY = screenY + NAV_BAR_HEIGHT;
        int panelEndX = screenX + TERMINAL_WIDTH - TEXT_PADDING_X - SCROLLBAR_HEIGHT - 2;
        int panelWidth = panelEndX - panelX;
        guiGraphics.drawString(font, Component.translatable("screen.redbyte.robo_terminal.result"), panelX, panelY, 0x00ff00);
        guiGraphics.vLine(panelX - RESULT_PANEL_GAP / 2, screenY - 2, screenY + TERMINAL_HEIGHT, 0xff7c7c7c);

        RoboEntity roboEntity = getMenu().getRoboEntity();
        if (roboEntity == null)
            return;

        int contentY = panelY + font.lineHeight + 4;
        int maxBottom = screenY + TERMINAL_HEIGHT - SCROLLBAR_BOTTOM_PADDING;
        boolean runtime = roboEntity.getRuntimeError() != null || !roboEntity.getPrintOutput().isEmpty() || roboEntity.getIsRuntime();
        DiagnosticBag diagnostics = roboEntity.getDiagnostics();
        if (roboEntity.getRuntimeError() != null)
            diagnostics.add(roboEntity.getRuntimeError());

        if (diagnostics.isEmpty() && roboEntity.getBuildDone() && !runtime) {
            guiGraphics.drawString(font, Component.translatable("screen.redbyte.robo_terminal.no_errors"), panelX, contentY, 0xffaaaaaa);
            return;
        }

        if (!roboEntity.getPrintOutput().isEmpty()) {
            for (String output : roboEntity.getPrintOutput()) {
                List<FormattedCharSequence> wrappedLines = font.split(FormattedText.of(output), panelWidth);
                for (FormattedCharSequence wrappedLine : wrappedLines) {
                    if (contentY + font.lineHeight > maxBottom) return;
                    guiGraphics.drawString(font, wrappedLine, panelX, contentY, 0xffffffff);
                    contentY += font.lineHeight;
                }
            }
        }

        for (Diagnostic diagnostic : diagnostics) {
            List<FormattedCharSequence> locationLines = font.split(FormattedText.of(formatDiagnosticLocation(diagnostic.span())), panelWidth);
            for (FormattedCharSequence locationLine : locationLines) {
                if (contentY + font.lineHeight > maxBottom) return;
                guiGraphics.drawString(font, locationLine, panelX, contentY, 0xffaaaaaa);
                contentY += font.lineHeight;
            }

            List<FormattedCharSequence> wrappedLines = font.split(FormattedText.of((roboEntity.getRuntimeError() != null ? Component.translatable("runtime.redbyte.error.runtime").getString() + ": " : "") + diagnostic.message()), panelWidth);
            for (FormattedCharSequence wrappedLine : wrappedLines) {
                if (contentY + font.lineHeight > maxBottom) return;
                guiGraphics.drawString(font, wrappedLine, panelX, contentY, 0xffff5555);
                contentY += font.lineHeight;
            }

            contentY += font.lineHeight * 2;
        }
    }

    private String formatDiagnosticLocation(TextSpan span) {
        if (span.lineStart() == span.lineEnd()) {
            if (span.startColumn() == span.endColumn())
                return "Line " + span.lineStart() + ": " + span.endColumn();
            return "Line " + span.lineStart() + ": " + span.startColumn() + "-" + span.endColumn();
        }

        return "Lines " + span.lineStart() + "-" + span.lineEnd() + ": " + span.startColumn() + "-" + span.endColumn();
    }

    private void drawErrorHighlightsForLine(
            GuiGraphics guiGraphics,
            String line,
            int lineIndex,
            int lineY,
            int textX,
            int clipStart,
            int clipEnd
    ) {
        if (errorHighlightingDisabled)
            return;

        if (getMenu().getRoboEntity() == null || getMenu().getRoboEntity().getDiagnostics().isEmpty() || !getMenu().getRoboEntity().getBuildDone())
            return;

        int lineNumber = lineIndex + 1;
        for (Diagnostic diagnostic : getMenu().getRoboEntity().getDiagnostics()) {
            TextSpan span = diagnostic.span();
            if (lineNumber < span.lineStart() || lineNumber > span.lineEnd())
                continue;

            int localStart;
            int localEnd;

            if (span.lineStart() == span.lineEnd()) {
                localStart = Mth.clamp(span.startColumn(), 0, line.length());
                localEnd = Mth.clamp(span.endColumn(), 0, line.length());
            } else if (lineNumber == span.lineStart()) {
                localStart = Mth.clamp(span.startColumn(), 0, line.length());
                localEnd = line.length();
            } else if (lineNumber == span.lineEnd()) {
                localStart = 0;
                localEnd = Mth.clamp(span.endColumn(), 0, line.length());
            } else {
                localStart = 0;
                localEnd = line.length();
            }

            if (localStart == localEnd && diagnostic.isAtEof()) {
                if (localStart > 0) localStart--;
                else if (!line.isEmpty()) localEnd = 1;
                else continue;
            }

            int drawStart = Math.max(localStart, clipStart);
            int drawEnd = Math.min(localEnd, clipEnd);
            if (drawStart >= drawEnd)
                continue;

            int x1 = textX + font.width(line.substring(clipStart, drawStart));
            int x2 = textX + font.width(line.substring(clipStart, drawEnd));
            guiGraphics.fill(x1, lineY - 1, x2, lineY - 1 + font.lineHeight, DIAGNOSTIC_HIGHLIGHT_COLOR);
        }
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
        int totalContentHeight = getMenu().getTerminalText().getLines().length * font.lineHeight;
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
        int lineCount = getMenu().getTerminalText().getLines().length;
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

    private void clampEditorState() {
        String[] lines = getMenu().getTerminalText().getLines();
        if (lines.length == 0) {
            getMenu().getTerminalText().newLine(curLine, textFieldHelper.getCursorPos());
            lines = getMenu().getTerminalText().getLines();
        }

        curLine = Mth.clamp(curLine, 0, lines.length - 1);
        if (textFieldHelper.getCursorPos() > getMenu().getTerminalText().getLines()[curLine].length())
            textFieldHelper.setCursorToEnd();
    }

    private void selectAll() {
        // If already have a selection, select all text
        if (hasSelection) {
            selectionStartLine = 0;
            selectionStartChar = 0;
            selectionEndLine = getMenu().getTerminalText().getLines().length - 1;
            selectionEndChar = getMenu().getTerminalText().getLines()[selectionEndLine].length();
        } else {
            // First Ctrl+A: select current lineStart
            selectionStartLine = curLine;
            selectionStartChar = 0;
            selectionEndLine = curLine;
            selectionEndChar = getMenu().getTerminalText().getLines()[curLine].length();
        }

        hasSelection = true;
    }

    private boolean handleShiftArrowKeys(int keyCode) {
        String[] lines = getMenu().getTerminalText().getLines();

        // Initialize selection if not already started
        if (!hasSelection) {
            hasSelection = true;
            selectionStartLine = curLine;
            selectionStartChar = textFieldHelper.getCursorPos();
            selectionEndLine = curLine;
            selectionEndChar = textFieldHelper.getCursorPos();
        }

        // Update selection endColumn point based on arrow key
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

        String[] lines = getMenu().getTerminalText().getLines();

        // Determine the actual selection bounds (normalize startColumn/endColumn)
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

        // Handle single lineStart selection
        if (actualStartLine == actualEndLine) {
            String line = lines[actualStartLine];
            String before = line.substring(0, actualStartChar);
            String after = line.substring(actualEndChar);
            getMenu().getTerminalText().setText(before + after, actualStartLine);
            curLine = actualStartLine;
            textFieldHelper.setCursorPos(actualStartChar, false);
        } else {
            // Handle multi-lineStart selection
            String firstLine = lines[actualStartLine];
            String lastLine = lines[actualEndLine];

            // Combine the parts before the selection startColumn and after the selection endColumn
            String combined = firstLine.substring(0, actualStartChar) + lastLine.substring(actualEndChar);

            // Replace the first lineStart with the combined text
            getMenu().getTerminalText().setText(combined, actualStartLine);

            // Remove all the lines in between
            for (int i = actualEndLine; i > actualStartLine; i--) {
                getMenu().getTerminalText().removeLine(i);
            }

            // Update cursor position
            curLine = actualStartLine;
            textFieldHelper.setCursorPos(actualStartChar, false);
        }

        // Clear selection
        hasSelection = false;
    }
}
