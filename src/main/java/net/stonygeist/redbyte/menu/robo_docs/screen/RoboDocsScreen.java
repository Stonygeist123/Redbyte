package net.stonygeist.redbyte.menu.robo_docs.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.stonygeist.redbyte.interpreter.Miscellaneous;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Node;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;
import net.stonygeist.redbyte.interpreter.analysis.nodes.stmt.Stmt;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.menu.robo_docs.RoboDocs;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class RoboDocsScreen extends AbstractContainerScreen<RoboDocs> {
    private static final int BORDER_COLOR = 0xff808080;
    private static final int SCREEN_COLOR = 0xff000000;

    private static final int TERMINAL_WIDTH = 800;
    private static final int TERMINAL_HEIGHT = 400;
    private static final int NAV_BAR_HEIGHT = 40;
    private static final int BORDER_WIDTH = 4;

    private static final int TEXT_PADDING_X = 14;
    private static final int TEXT_PADDING_Y = 0;

    // Scrollbar
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int MIN_SCROLLBAR_THUMB_HEIGHT = 14;
    private static final int SCROLLBAR_TRACK_COLOR = 0xff2d2d2d;
    private static final int SCROLLBAR_THUMB_COLOR = 0xff8a8a8a;

    private int verticalScrollOffset;

    private Category category = Category.None;

    public enum Category {
        None, Statements, Expressions, Library
    }

    public RoboDocsScreen(RoboDocs menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new BackButton(
                (width - TERMINAL_WIDTH) / 2 + 25, (height - TERMINAL_HEIGHT) / 2, 80, 20,
                getMenu().getRedbyteID())
        );
        addRenderableWidget(new CategoryButton(
                width - TERMINAL_WIDTH / 4 - 240, (height - TERMINAL_HEIGHT) / 2, 80, 20,
                Component.translatable("menu.redbyte.docs.statements"), Category.Statements, category -> this.category = category
        ));
        addRenderableWidget(new CategoryButton(
                width - TERMINAL_WIDTH / 4 - 120, (height - TERMINAL_HEIGHT) / 2, 80, 20,
                Component.translatable("menu.redbyte.docs.expressions"), Category.Expressions, category -> this.category = category
        ));
        addRenderableWidget(new CategoryButton(
                width - TERMINAL_WIDTH / 4, (height - TERMINAL_HEIGHT) / 2, 80, 20,
                Component.translatable("menu.redbyte.docs.library"), Category.Library, category -> this.category = category
        ));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - TERMINAL_WIDTH) / 2;
        int y = (height - TERMINAL_HEIGHT) / 2;
        guiGraphics.drawString(font, getTitle(), x + 6, y + NAV_BAR_HEIGHT, 0x00ff00);
        guiGraphics.fill(x - BORDER_WIDTH, y - BORDER_WIDTH, x + TERMINAL_WIDTH + BORDER_WIDTH, y + TERMINAL_HEIGHT + BORDER_WIDTH, BORDER_COLOR);
        guiGraphics.fill(x, y, x + TERMINAL_WIDTH, y + TERMINAL_HEIGHT, SCREEN_COLOR);
        guiGraphics.hLine(x, x + TERMINAL_WIDTH, y + 20, 0xff7c7c7c);

        int visibleTextHeight = getVisibleTextHeight();
        int totalContentHeight = getTotalContentHeight();
        int maxScrollY = Math.max(0, totalContentHeight - visibleTextHeight);
        verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);
        renderCategoryContent(guiGraphics, x, y, visibleTextHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (category != Category.None) {
            int visibleTextHeight = getVisibleTextHeight();
            int totalContentHeight = getTotalContentHeight();
            if (totalContentHeight > visibleTextHeight) {
                int scrollAmount = (int) (font.lineHeight * -scrollY);
                int maxScrollY = Math.max(0, totalContentHeight - visibleTextHeight);
                verticalScrollOffset += scrollAmount;
                verticalScrollOffset = Mth.clamp(verticalScrollOffset, 0, maxScrollY);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int getVisibleTextHeight() {
        return TERMINAL_HEIGHT - NAV_BAR_HEIGHT - TEXT_PADDING_Y * 2 - BORDER_WIDTH * 2;
    }

    private int getTotalContentHeight() {
        return switch (category) {
            case Statements -> calculateStatementsHeight();
            case Expressions -> calculateExpressionsHeight();
            case Library -> calculateLibraryHeight();
            default -> 0;
        };
    }

    private int calculateStatementsHeight() {
        int totalHeight = 0;
        for (Class<? extends Stmt> stmt : Node.allStatements) {
            int textY = font.lineHeight * 4;
            try {
                Method docsMethod = stmt.getMethod("docs");
                Component docs = (Component) docsMethod.invoke(null);
                List<FormattedCharSequence> wrappedDocs = font.split(docs, (int) (TERMINAL_WIDTH / 2f));
                textY += font.lineHeight * (2 + wrappedDocs.size());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }

            totalHeight += textY;
        }

        return totalHeight;
    }

    private int calculateExpressionsHeight() {
        return Node.allExpressions.size() * font.lineHeight * 4;
    }

    private int calculateLibraryHeight() {
        int totalHeight = 3 * font.lineHeight;
        for (FunctionSymbol function : Miscellaneous.functions) {
            List<FormattedCharSequence> wrappedDescriptionLines = font.split(
                    FormattedText.of(function.description.getString()),
                    (int) (TERMINAL_WIDTH - (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.75f)
            );
            totalHeight += (wrappedDescriptionLines.size() + 1) * font.lineHeight;
        }

        for (Class<? extends DataType> type : DataType.dataTypes) {
            List<MethodSymbol> methods = DataType.getMethods(type);
            Set<PropertySymbol> properties = DataType.getPropertySymbols(type);
            if (methods.isEmpty() && properties.isEmpty())
                continue;
            totalHeight += 6 * font.lineHeight;
            totalHeight += (methods.stream().mapToInt(m -> {
                List<FormattedCharSequence> wrappedDescriptionLines = font.split(
                        FormattedText.of(m.description.getString()),
                        (int) (TERMINAL_WIDTH - (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.75f)
                );
                return wrappedDescriptionLines.size();
            }).sum() + 1) * font.lineHeight;
            totalHeight += (properties.stream().mapToInt(p -> {
                List<FormattedCharSequence> wrappedDescriptionLines = font.split(FormattedText.of(p.description.getString()), (int) (TERMINAL_WIDTH - (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.75f));
                return wrappedDescriptionLines.size();
            }).sum() + 1) * font.lineHeight;
        }

        return totalHeight;
    }

    private void renderCategoryContent(@NotNull GuiGraphics guiGraphics, int x, int y, int visibleTextHeight) {
        guiGraphics.enableScissor(x, y + NAV_BAR_HEIGHT, x + TERMINAL_WIDTH, y + TERMINAL_HEIGHT - BORDER_WIDTH);
        switch (category) {
            case Statements -> renderStatementsContent(guiGraphics, x, y);
            case Expressions -> renderExpressionsContent(guiGraphics, x, y);
            case Library -> renderLibraryContent(guiGraphics, x, y);
            case None -> renderDefaultContent(guiGraphics, x);
        }

        guiGraphics.disableScissor();
        drawVerticalScrollbar(guiGraphics, x + TERMINAL_WIDTH - 10, y + TEXT_PADDING_Y + NAV_BAR_HEIGHT, visibleTextHeight);
    }

    private void renderStatementsContent(@NotNull GuiGraphics guiGraphics, int x, int y) {
        int startX = x + TEXT_PADDING_X;
        int startY = y + NAV_BAR_HEIGHT + TEXT_PADDING_Y;
        int textY = startY - verticalScrollOffset;
        for (Class<? extends Stmt> stmt : Node.allStatements)
            textY += drawStatement(guiGraphics, startX, textY, stmt);
    }

    private void renderExpressionsContent(@NotNull GuiGraphics guiGraphics, int x, int y) {
        int startX = x + TEXT_PADDING_X;
        int startY = y + NAV_BAR_HEIGHT + TEXT_PADDING_Y;
        int textY = startY - verticalScrollOffset;
        for (Class<? extends Expr> expr : Node.allExpressions)
            textY += drawExpression(guiGraphics, startX, textY, expr);
    }

    private void renderLibraryContent(@NotNull GuiGraphics guiGraphics, int x, int y) {
        int startX = x + TEXT_PADDING_X;
        int startY = y + NAV_BAR_HEIGHT + TEXT_PADDING_Y;
        int textY = startY - verticalScrollOffset;
        guiGraphics.drawString(font, Component.translatable("docs.redbyte.title.global_functions"), startX, textY, 0xfffc0060);
        textY += font.lineHeight * 3;
        for (FunctionSymbol function : Miscellaneous.functions)
            textY += drawLibraryFunction(guiGraphics, startX, textY, function);

        for (Class<? extends DataType> type : DataType.dataTypes) {
            try {
                List<MethodSymbol> methods = DataType.getMethods(type);
                Set<PropertySymbol> properties = DataType.getPropertySymbols(type);
                if (methods.isEmpty() && properties.isEmpty())
                    continue;
                textY += font.lineHeight * 3;
                Field typeNameField = type.getField("TYPE");
                Component typeName = ((TypeSymbol) typeNameField.get(null)).getDocsName();
                guiGraphics.drawString(font, typeName, startX, textY, 0xfffc0060);
                textY += font.lineHeight * 3;
                for (MethodSymbol method : methods)
                    textY += drawLibraryFunction(guiGraphics, startX, textY, method);
                for (PropertySymbol property : properties)
                    textY += drawLibraryProperty(guiGraphics, startX, textY, property);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void renderDefaultContent(@NotNull GuiGraphics guiGraphics, int x) {
        MutableComponent title = Component.translatable("docs.redbyte.title.docs1").withStyle(style -> style.withBold(true));
        MutableComponent subtitle = Component.translatable("docs.redbyte.title.docs2");
        guiGraphics.drawString(font, title, x + TERMINAL_WIDTH / 2 - font.width(title) / 2, height / 2, 0xffffffff);
        guiGraphics.drawString(font, subtitle, x + TERMINAL_WIDTH / 2 - font.width(subtitle) / 2, height / 2 + font.lineHeight, 0xffffffff);
    }

    public static final int GENERAL_COLOR = 0xffffff;
    public static final int PUNCTUATION_COLOR = 0xa9a9a9;
    public static final int PARAM_TYPE_COLOR = 0x0834eb;
    public static final int RETURN_TYPE_COLOR = 0x4c00b0;
    public static final int NAME_COLOR = 0xfc0060;
    public static final int VALUE_COLOR = 0x0834eb;

    private int drawStatement(@NotNull GuiGraphics guiGraphics, int x, int y, Class<? extends Stmt> stmt) {
        int textY = 0;
        try {
            Method titleMethod = stmt.getMethod("title");
            Method syntaxMethod = stmt.getMethod("syntax");
            Method docsMethod = stmt.getMethod("docs");
            Method exampleMethod = stmt.getMethod("example");
            Component title = ((Component) titleMethod.invoke(null)).copy().append(Component.literal(": "));
            Component syntax = (Component) syntaxMethod.invoke(null);
            Component docs = (Component) docsMethod.invoke(null);
            MutableComponent example = ((Component) exampleMethod.invoke(null)).copy();
            List<FormattedCharSequence> exampleLines = font.split(example, TERMINAL_WIDTH / 3);
            guiGraphics.drawString(font, title, x, y, GENERAL_COLOR);
            textY += font.lineHeight * 2;
            guiGraphics.drawString(font, syntax, x, y + textY, GENERAL_COLOR);
            int exampleY = 0;
            for (FormattedCharSequence exampleLine : exampleLines) {
                guiGraphics.drawString(font, exampleLine, x + (int) (TERMINAL_WIDTH / 1.5f), y + textY + exampleY * font.lineHeight, GENERAL_COLOR);
                ++exampleY;
            }

            textY += font.lineHeight * 2;
            List<FormattedCharSequence> wrappedDocs = font.split(docs, (int) (TERMINAL_WIDTH / 2f));
            int docY = 0;
            for (FormattedCharSequence docLine : wrappedDocs) {
                guiGraphics.drawString(font, docLine, x + 80, y + textY + docY * font.lineHeight, GENERAL_COLOR);
                ++docY;
            }

            textY += font.lineHeight * (2 + docY);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return textY;
    }

    private int drawExpression(@NotNull GuiGraphics guiGraphics, int x, int y, Class<? extends Expr> expr) {
        try {
            Method titleMethod = expr.getMethod("title");
            Method docsMethod = expr.getMethod("docs");
            Method exampleMethod = expr.getMethod("example");
            Component title = ((Component) titleMethod.invoke(null)).copy().append(Component.literal(": "));
            Component docs = (Component) docsMethod.invoke(null);
            Component example = (Component) exampleMethod.invoke(null);
            guiGraphics.drawString(font, title, x, y, GENERAL_COLOR);
            guiGraphics.drawString(font, docs, x + 80, y + font.lineHeight * 2, GENERAL_COLOR);
            guiGraphics.drawString(font, example, x + (int) (TERMINAL_WIDTH / 1.5f), y + font.lineHeight * 2, GENERAL_COLOR);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return font.lineHeight * 4;
    }

    public int drawLibraryFunction(@NotNull GuiGraphics guiGraphics, int x, int y, FunctionSymbol function) {
        int textX = 0;
        guiGraphics.drawString(font, function.name, x, y, GENERAL_COLOR);
        textX += font.width(function.name);
        guiGraphics.drawString(font, "(", x + textX, y, PUNCTUATION_COLOR);
        textX += font.width("(");
        for (int i = 0; i < function.parameters.size(); i++) {
            try {
                Component paramTypeName = ((TypeSymbol) function.parameters.get(i).getField("TYPE").get(null)).getDocsName();
                guiGraphics.drawString(font, paramTypeName, x + textX, y, PARAM_TYPE_COLOR);
                textX += font.width(paramTypeName);
                if (i < function.parameters.size() - 1) {
                    guiGraphics.drawString(font, ", ", x + textX, y, PUNCTUATION_COLOR);
                    textX += font.width(", ");
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        guiGraphics.drawString(font, ")", x + textX, y, PUNCTUATION_COLOR);
        textX += font.width(")");
        try {
            TypeSymbol type = ((TypeSymbol) function.type.getField("TYPE").get(null));
            Component typeName = type.getDocsName();
            if (!type.equals(NothingDataType.TYPE)) {
                guiGraphics.drawString(font, " -> ", x + textX, y, PUNCTUATION_COLOR);
                textX += font.width(" -> ");
                guiGraphics.drawString(font, typeName, x + textX, y, RETURN_TYPE_COLOR);
            }

            List<FormattedCharSequence> wrappedDescriptionLines = font.split(FormattedText.of(function.description.getString()), (int) (TERMINAL_WIDTH - (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.75f));
            for (int i = 0; i < wrappedDescriptionLines.size(); ++i) {
                FormattedCharSequence line = wrappedDescriptionLines.get(i);
                guiGraphics.drawString(font, line, (int) (x + (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.5f), y + i * font.lineHeight, GENERAL_COLOR);
            }

            return (wrappedDescriptionLines.size() + 1) * font.lineHeight;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int drawLibraryProperty(@NotNull GuiGraphics guiGraphics, int x, int y, PropertySymbol property) {
        int textX = 0;
        guiGraphics.drawString(font, property.name, x, y, GENERAL_COLOR);
        textX += font.width(property.name);

        guiGraphics.drawString(font, " -> ", x + textX, y, PUNCTUATION_COLOR);
        textX += font.width(" -> ");
        try {
            Component typeName = ((TypeSymbol) property.type.getField("TYPE").get(null)).getDocsName();
            guiGraphics.drawString(font, typeName, x + textX, y, RETURN_TYPE_COLOR);
            List<FormattedCharSequence> wrappedDescriptionLines = font.split(FormattedText.of(property.description.getString()), (int) (TERMINAL_WIDTH - (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.75f));
            for (int i = 0; i < wrappedDescriptionLines.size(); ++i) {
                FormattedCharSequence line = wrappedDescriptionLines.get(i);
                guiGraphics.drawString(font, line, (int) (x + (TERMINAL_WIDTH - TEXT_PADDING_X) / 2.5f), y + i * font.lineHeight, GENERAL_COLOR);
            }

            return (wrappedDescriptionLines.size() + 1) * font.lineHeight;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawVerticalScrollbar(GuiGraphics guiGraphics, int x, int y, int visibleTextHeight) {
        int totalContentHeight = getTotalContentHeight();
        if (totalContentHeight <= visibleTextHeight)
            return;

        // Draw scrollbar track
        guiGraphics.fill(x, y, x + SCROLLBAR_WIDTH, y + visibleTextHeight, SCROLLBAR_TRACK_COLOR);

        // Calculate thumb size based on content vs visible ratio
        float viewRatio = (float) visibleTextHeight / totalContentHeight;
        int thumbHeight = Math.max(MIN_SCROLLBAR_THUMB_HEIGHT, (int) (visibleTextHeight * viewRatio));

        // Calculate thumb position based on current scroll offset
        int maxScrollOffset = Math.max(0, totalContentHeight - visibleTextHeight);
        float scrollRatio = maxScrollOffset > 0 ? (float) verticalScrollOffset / maxScrollOffset : 0f;
        scrollRatio = Mth.clamp(scrollRatio, 0, 1);

        int maxThumbTravel = visibleTextHeight - thumbHeight;
        int thumbY = y + (int) (scrollRatio * maxThumbTravel);
        guiGraphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, SCROLLBAR_THUMB_COLOR);
    }
}
