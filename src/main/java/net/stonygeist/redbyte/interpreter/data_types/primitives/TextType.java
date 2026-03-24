package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class TextType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("text", Component.translatable("interpreter.redbyte.types.text"));
    private final String value;

    public TextType(String value) {
        super(TYPE);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
