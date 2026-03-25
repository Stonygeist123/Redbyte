package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public abstract class DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("any", Component.translatable("interpreter.redbyte.types.any"));
    @NotNull
    private final TypeSymbol type;

    public DataType(@NotNull TypeSymbol type) {
        this.type = type;
    }

    public @NotNull TypeSymbol getType() {
        return type;
    }

    public static final Map<VariableSymbol, Function<DataType, DataType>> properties = new Hashtable<>();

    public static Optional<Map.Entry<VariableSymbol, Function<DataType, DataType>>> getProperty(Class<? extends DataType> type, String name) {
        try {
            Field proprtiesField = type.getField("properties");
            Map<VariableSymbol, Function<DataType, DataType>> properties = (Map<VariableSymbol, Function<DataType, DataType>>) proprtiesField.get(null);

            Class<?> superType = type.getSuperclass();
            while (!superType.equals(Object.class)) {
                Field superPropertiesField = superType.getField("properties");
                Map<VariableSymbol, Function<DataType, DataType>> superProperties = (Map<VariableSymbol, Function<DataType, DataType>>) superPropertiesField.get(null);
                properties.putAll(superProperties);
                superType = superType.getSuperclass();
            }

            return properties.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().name.equalsIgnoreCase(name))
                    .findFirst();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<VariableSymbol> getPropertySymbols(Class<? extends DataType> type) {
        try {
            Field proprtiesField = type.getField("properties");
            Map<VariableSymbol, Function<DataType, DataType>> properties = (Map<VariableSymbol, Function<DataType, DataType>>) proprtiesField.get(null);

            Class<?> superType = type.getSuperclass();
            while (!superType.equals(Object.class)) {
                Field superPropertiesField = superType.getField("properties");
                Map<VariableSymbol, Function<DataType, DataType>> superProperties = (Map<VariableSymbol, Function<DataType, DataType>>) superPropertiesField.get(null);
                properties.putAll(superProperties);
                superType = superType.getSuperclass();
            }

            return properties.keySet();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static final List<MethodSymbol> methods = List.of();

    public static List<MethodSymbol> getMethods(Class<? extends DataType> type) {
        try {
            Field methodsField = type.getField("methods");
            List<MethodSymbol> methods = (List<MethodSymbol>) methodsField.get(null);
            List<MethodSymbol> methodsResult = new ArrayList<>(methods);

            Class<?> superType = type.getSuperclass();
            while (!superType.equals(Object.class)) {
                Field superMethodsField = superType.getField("methods");
                List<MethodSymbol> superMethods = (List<MethodSymbol>) superMethodsField.get(null);
                methodsResult.addAll(superMethods);
                superType = superType.getSuperclass();
            }

            return methodsResult;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataType d ? d.type.equals(type) : super.equals(obj);
    }

    public static final List<Class<? extends DataType>> dataTypes = List.of(DataType.class, PrimitiveType.class, NumberType.class, TextType.class, BooleanType.class, BlockDataType.class, CreatureDataType.class, EntityDataType.class, MonsterDataType.class, PlayerDataType.class, RoboDataType.class, VectorDataType.class);
}
