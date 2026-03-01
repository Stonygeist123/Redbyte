package net.stonygeist.redbyte.interpreter.diagnostics;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;

public record Diagnostic(Component message, TextSpan span) {
    public boolean isAtEof() {
        return span.start() == span.end();
    }
}
