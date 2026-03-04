package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.world.entity.Entity;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public abstract class EntityDataType<T extends Entity> extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("entity");
    private final T entity;

    public EntityDataType(@NotNull TypeSymbol type, T entity) {
        super(type);
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }

    public boolean isNull() {
        return entity == null;
    }
}
