package net.stonygeist.redbyte.interpreter.analysis.nodes;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public TextSpan span() {
        List<Node> children = getChildren();
        TextSpan firstSpan = getFirstNode(children).span();
        TextSpan lastSpan = getLastNode(children).span();
        return new TextSpan(firstSpan.startColumn(), lastSpan.endColumn(), firstSpan.lineStart(), lastSpan.lineEnd());
    }

    private static Node getFirstNode(List<Node> nodes) {
        Node first = nodes.getFirst();
        if (nodes.size() == 1)
            return first;

        for (Node node : nodes.subList(1, nodes.size()))
            if (node.span().lineStart() < first.span().lineStart())
                first = node;
            else if (node.span().startColumn() < first.span().startColumn())
                first = node;
        return first;
    }

    private static Node getLastNode(List<Node> nodes) {
        Node last = nodes.getLast();
        if (nodes.size() == 1)
            return last;

        for (Node node : nodes.subList(0, nodes.size() - 1))
            if (node.span().lineEnd() > last.span().lineEnd())
                last = node;
            else if (node.span().endColumn() > last.span().endColumn())
                last = node;
        return last;
    }

    private List<Node> getChildren() {
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

    public static final List<Class<? extends Expr>> allExpressions = List.of(LiteralExpr.class, BinaryExpr.class, UnaryExpr.class, NameExpr.class, AssignExpr.class, CallExpr.class, GroupExpr.class, RoboExpr.class);
}
