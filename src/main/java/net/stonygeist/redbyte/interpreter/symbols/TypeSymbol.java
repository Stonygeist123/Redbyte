package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.ListType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TypeSymbol extends Symbol {
    private final @Nullable TypeSymbol superType;
    private final Component docsName;
    private final @Nullable TypeSymbol elementType;
    private final @NotNull Members members;

    public TypeSymbol(String name, Component docsName) {
        this(name, null, docsName, null);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName) {
        this(name, superType, docsName, null);
    }

    private TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName, @Nullable TypeSymbol elementType) {
        this(name, superType, docsName, elementType, new Members());
    }

    private TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName, @Nullable TypeSymbol elementType, @NotNull Members members) {
        super(name);
        this.superType = superType;
        this.docsName = docsName;
        this.elementType = elementType;
        this.members = members;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeSymbol t) || isList() != t.isList())
            return false;
        if (isList()) {
            assert elementType != null && t.elementType != null;
            return elementType.equals(t.elementType) || t.elementType.name.equals(NothingDataType.TYPE.name);
        }
        return name.equals(t.name) || equals(t.superType);
    }

    public int hashCode() {
        return Objects.hash(name, isList());
    }

    public boolean isAssignableFrom(TypeSymbol other) {
        if (name.equals(DataType.TYPE.name))
            return true;
        if (isList() != other.isList())
            return false;
        for (TypeSymbol type = other; type != null; type = type.superType)
            if (type.name.equals(name))
                return true;
        return false;
    }

    public void declareMembers(Consumer<TypeSymbol> declaration) {
        synchronized (members) {
            members.declarations = members.declarations == null ? declaration : members.declarations.andThen(declaration);
        }
    }

    public void addProperty(PropertySymbol property, Function<DataType, DataType> getter) {
        members.properties.put(property, getter);
    }

    public void addMethods(MethodSymbol... methods) {
        members.methods.addAll(List.of(methods));
    }


    public Map<PropertySymbol, Function<DataType, DataType>> getProperties() {
        if (isList())
            return ListType.TYPE.getProperties();

        Map<PropertySymbol, Function<DataType, DataType>> result = new LinkedHashMap<>();
        if (superType != null)
            result.putAll(superType.getProperties());

        declared().properties.forEach((property, getter) -> {
            result.keySet().removeIf(p -> p.name.equals(property.name));
            result.put(property, getter);
        });
        return result;
    }

    public List<MethodSymbol> getMethods() {
        if (isList())
            return ListType.TYPE.getMethods();
        
        List<MethodSymbol> result = new ArrayList<>();
        if (superType != null)
            result.addAll(superType.getMethods());
        for (MethodSymbol method : declared().methods) {
            result.removeIf(m -> m.name.equals(method.name) && m.parameters.equals(method.parameters));
            result.add(method);
        }
        return result;
    }

    private Members declared() {
        synchronized (members) {
            Consumer<TypeSymbol> declarations = members.declarations;
            if (declarations != null) {
                members.declarations = null;
                declarations.accept(this);
            }
        }
        return members;
    }

    private static final class Members {
        private final Map<PropertySymbol, Function<DataType, DataType>> properties = new LinkedHashMap<>();
        private final List<MethodSymbol> methods = new ArrayList<>();
        private @Nullable Consumer<TypeSymbol> declarations;
    }

    public static @NotNull TypeSymbol listOf(@NotNull TypeSymbol elementType) {
        return new TypeSymbol("list", null, elementType.docsName.copy().append("[]"), elementType);
    }

    public TypeSymbol getBase() {
        return new TypeSymbol(name, superType, docsName, null, members);
    }

    @Override
    public String toString() {
        return isList() ? elementType + "[]" : name;
    }

    public boolean isList() {
        return elementType != null;
    }

    public Component getDocsName() {
        return docsName;
    }

    public @Nullable TypeSymbol getElementType() {
        return elementType;
    }
}
