package net.stonygeist.interpreter.analysis.nodes;

import net.stonygeist.interpreter.miscellaneous.TextSpan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public TextSpan span() {
        List<Node> children = getChildren();
        return new TextSpan(children.getFirst().span().start(), children.getLast().span().end());
    }

    public List<Node> getChildren() {
        List<Node> result = new ArrayList<>();
        Field[] fields = getClass().getFields();
        for (Field f : fields) {
            if (f.getType().isAssignableFrom(Node.class)) {
                try {
                    Node child = (Node) f.get(this);
                    if (child != null)
                        result.add(child);
                } catch (IllegalAccessException ignored) {
                }
            } else if (f.getType().isArray() && f.getType().arrayType().isAssignableFrom(Node.class)) {
                try {
                    Node[] children = (Node[]) f.get(this);
                    if (children != null)
                        for (Node child : children)
                            if (child != null)
                                result.add(child);
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        return result;
    }
}
