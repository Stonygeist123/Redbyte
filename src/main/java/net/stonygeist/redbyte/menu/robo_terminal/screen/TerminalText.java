package net.stonygeist.redbyte.menu.robo_terminal.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerminalText {
    private final List<String> lines = new ArrayList<>();

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

    public String[] getLines() {
        return lines.toArray(new String[0]);
    }

    public void removeLine(int line) {
        lines.remove(line);
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

    public void setLines(String[] newLines) {
        lines.clear();
        lines.addAll(Arrays.asList(newLines));
    }

    public record Pos(int line, int col) {
    }

    public Pos insert(int line, int col, String text) {
        String[] parts = text.replace("\r\n", "\n").replace('\r', '\n').replace("\t", "    ").split("\n", -1);
        String cur = lines.get(line);
        String before = cur.substring(0, col);
        String after = cur.substring(col);

        if (parts.length == 1) {
            lines.set(line, before + parts[0] + after);
            return new Pos(line, col + parts[0].length());
        }

        lines.set(line, before + parts[0]);
        for (int i = 1; i < parts.length; ++i)
            lines.add(line + i, parts[i]);

        int last = line + parts.length - 1;
        lines.set(last, parts[parts.length - 1] + after);
        return new Pos(last, parts[parts.length - 1].length());
    }

}
