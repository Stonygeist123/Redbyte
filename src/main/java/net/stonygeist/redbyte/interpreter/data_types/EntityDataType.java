package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class EntityDataType<T extends Entity> extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("entity", Component.translatable("interpreter.redbyte.types.entity"));
    private final T entity;

    public EntityDataType(@NotNull TypeSymbol type, T entity) {
        super(type);
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }

    public static final Map<PropertySymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>(Map.of(
            new PropertySymbol("position", VectorDataType.class, Component.translatable("docs.redbyte.description.properties.entity.position")), x -> new VectorDataType(x.entity.position())
    ));
    public static final List<MethodSymbol> methods = List.of();
}
