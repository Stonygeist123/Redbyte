package net.stonygeist.redbyte.screen.robo_terminal;

import java.util.ArrayList;
import java.util.List;

public class TerminalText {
    private final List<String> lines = new ArrayList<>();

    public TerminalText() {
        lines.add("");
    }

    public TerminalText(String text) {
        lines.addAll(List.of(text.split("\\n", -1)));
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

    public void newLine(int curLine, int cursorPos) {
        lines.add("");
        if (curLine + 1 < lines.size()) {
            for (int i = lines.size() - 2; i >= curLine; --i)
                if (i == curLine)
                    lines.set(i + 1, lines.get(i).substring(cursorPos));
                else
                    lines.set(i + 1, lines.get(i));

            lines.set(curLine, lines.get(curLine).substring(0, cursorPos));
        }
    }
}
