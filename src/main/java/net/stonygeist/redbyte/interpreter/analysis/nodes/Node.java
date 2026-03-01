package net.stonygeist.redbyte.interpreter.analysis.nodes;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;

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
            if (Node.class.isAssignableFrom(f.getType())) {
                try {
                    Node child = (Node) f.get(this);
                    if (child != null)
                        result.add(child);
                } catch (IllegalAccessException ignored) {
                }
            } else if (f.getType().isArray() && Node.class.isAssignableFrom(f.getType().arrayType())) {
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
