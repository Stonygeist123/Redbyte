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
    public static final List<MethodSymbol> methods = List.of();

    public static final Map<Class<? extends DataType>, Map<VariableSymbol, Function<DataType, DataType>>> propertiesCache = new Hashtable<>();
    public static final Map<Class<? extends DataType>, List<MethodSymbol>> methodsCache = new Hashtable<>();
    public static final List<Class<? extends DataType>> dataTypes = List.of(DataType.class, PrimitiveType.class, NumberType.class, TextType.class, BooleanType.class, BlockDataType.class, CreatureDataType.class, EntityDataType.class, MonsterDataType.class, PlayerDataType.class, RoboDataType.class, VectorDataType.class);

    static {
        try {
            for (Class<? extends DataType> type : dataTypes) {
                Field proprtiesField = type.getField("properties");
                Field methodsField = type.getField("methods");
                Map<VariableSymbol, Function<DataType, DataType>> properties = (Map<VariableSymbol, Function<DataType, DataType>>) proprtiesField.get(null);
                List<MethodSymbol> methods = new ArrayList<>((List<MethodSymbol>) methodsField.get(null));

                Class<?> superType = type.getSuperclass();
                while (!superType.equals(Object.class)) {
                    Field superPropertiesField = superType.getField("properties");
                    Map<VariableSymbol, Function<DataType, DataType>> superProperties = (Map<VariableSymbol, Function<DataType, DataType>>) superPropertiesField.get(null);
                    properties.putAll(superProperties);

                    Field superMethodsField = superType.getField("methods");
                    List<MethodSymbol> superMethods = (List<MethodSymbol>) superMethodsField.get(null);
                    methods.addAll(superMethods);
                    superType = superType.getSuperclass();
                }

                propertiesCache.put(type, properties);
                methodsCache.put(type, methods);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Map.Entry<VariableSymbol, Function<DataType, DataType>>> getProperty(Class<? extends DataType> type, String name) {
        return propertiesCache.get(type).entrySet()
                .stream()
                .filter(entry -> entry.getKey().name.equalsIgnoreCase(name))
                .findFirst();
    }

    public static Set<VariableSymbol> getPropertySymbols(Class<? extends DataType> type) {
        return propertiesCache.get(type).keySet();
    }

    public static List<MethodSymbol> getMethods(Class<? extends DataType> type) {
        return methodsCache.get(type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataType d ? d.type.equals(type) : super.equals(obj);
    }
}
