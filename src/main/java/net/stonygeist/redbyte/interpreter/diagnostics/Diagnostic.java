package net.stonygeist.redbyte.interpreter.diagnostics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import org.jetbrains.annotations.NotNull;

public record Diagnostic(@NotNull String message, @NotNull TextSpan span) {
    public Diagnostic(Component message, TextSpan span) {
        this(message.getString(), span);
    }

    public static Diagnostic deserializeNBT(CompoundTag tag, int index) {
        String message = tag.getString("message_" + index);
        TextSpan span = readTextSpanFromTag(tag, "span_" + index);
        return new Diagnostic(message, span);
    }

    public CompoundTag serializeNBT(int index) {
        CompoundTag tag = new CompoundTag();
        tag.putString("message_" + index, message());
        writeTextSpanToTag(tag, "span_" + index, span());
        return tag;
    }

    public static TextSpan readTextSpanFromTag(CompoundTag tag, String key) {
        int startColumn = tag.getInt(key + "StartCol");
        int endColumn = tag.getInt(key + "EndCol");
        int lineStart = tag.getInt(key + "LineStart");
        int lineEnd = tag.getInt(key + "LineEnd");
        return new TextSpan(startColumn, endColumn, lineStart, lineEnd);
    }

    public static void writeTextSpanToTag(CompoundTag tag, String key, TextSpan span) {
        tag.putInt(key + "StartCol", span.startColumn());
        tag.putInt(key + "EndCol", span.endColumn());
        tag.putInt(key + "LineStart", span.lineStart());
        tag.putInt(key + "LineEnd", span.lineEnd());
    }

    public boolean isAtEof() {
        return span.startColumn() == span.endColumn();
    }
}
