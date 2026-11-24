package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

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

    static {
        TYPE.declareMembers(self -> self.addProperty(new PropertySymbol("position", VectorDataType.TYPE, Component.translatable("docs.redbyte.description.properties.entity.position")), x -> new VectorDataType(((EntityDataType<?>) x).getEntity().position())));
    }
}
