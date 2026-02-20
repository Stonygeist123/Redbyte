package net.stonygeist.redbyte.screen.robo_terminal;

import java.util.ArrayList;
import java.util.List;

public class TerminalText {
    private final List<String> lines = new ArrayList<>();

    public TerminalText() {
        lines.add("");
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }

    public void setText(String text, int line) {
        lines.set(line, text);
    }

    public void addText(String text, int line, int cursorPos) {
        String lineText = lines.get(line);
        if (cursorPos >= lineText.length())
            lines.set(line, lineText + text);
        else
            lines.set(line, lineText.substring(0, cursorPos) + text + lineText.substring(cursorPos));
    }

    public String[] getLines() {
        return lines.toArray(new String[0]);
    }

    public void newLine() {
        lines.add("");
    }
}
