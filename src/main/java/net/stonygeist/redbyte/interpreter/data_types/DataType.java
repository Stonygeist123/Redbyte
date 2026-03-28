package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
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

    public static final Map<PropertySymbol, Function<DataType, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();

    private static final Map<Class<? extends DataType>, Map<PropertySymbol, Function<DataType, DataType>>> propertiesCache = new Hashtable<>();
    private static final Map<Class<? extends DataType>, List<MethodSymbol>> methodsCache = new Hashtable<>();
    private static final Map<Class<? extends DataType>, TypeSymbol> typeSymbolsCache = new Hashtable<>();
    public static final List<Class<? extends DataType>> dataTypes = List.of(DataType.class, PrimitiveType.class, NumberType.class, TextType.class, BooleanType.class, CreatureDataType.class, EntityDataType.class, MonsterDataType.class, PlayerDataType.class, RoboDataType.class, VectorDataType.class, BlockDataType.class, ContainerBlockDataType.class);

    static {
        try {
            for (Class<? extends DataType> type : dataTypes) {
                Map<PropertySymbol, Function<DataType, DataType>> properties = getProperties(type);
                List<MethodSymbol> methods = getMethods(type);
                TypeSymbol typeSymbol = getTypeSymbol(type);
                if (typeSymbol != null)
                    typeSymbolsCache.put(type, typeSymbol);

                if (properties == null || methods == null)
                    continue;

                List<MethodSymbol> methodsCopy = new ArrayList<>(List.copyOf(methods));
                Class<?> superType = type.getSuperclass();
                while (!superType.equals(Object.class)) {
                    Field superPropertiesField = superType.getField("properties");
                    Map<PropertySymbol, Function<DataType, DataType>> superProperties = (Map<PropertySymbol, Function<DataType, DataType>>) superPropertiesField.get(null);
                    properties.putAll(superProperties);

                    Field superMethodsField = superType.getField("methods");
                    List<MethodSymbol> superMethods = (List<MethodSymbol>) superMethodsField.get(null);
                    methodsCopy.addAll(superMethods);
                    superType = superType.getSuperclass();
                }

                propertiesCache.put(type, properties);
                methodsCache.put(type, methodsCopy);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<PropertySymbol, Function<DataType, DataType>> getProperties(Class<? extends DataType> type) {
        try {
            if (!propertiesCache.containsKey(type)) {
                Field proprtiesField = type.getField("properties");
                Map<PropertySymbol, Function<DataType, DataType>> properties = (Map<PropertySymbol, Function<DataType, DataType>>) proprtiesField.get(null);
                propertiesCache.put(type, properties);
                return properties;
            }

            return propertiesCache.get(type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<PropertySymbol> getPropertySymbols(Class<? extends DataType> type) {
        try {
            if (!propertiesCache.containsKey(type)) {
                Field proprtiesField = type.getField("properties");
                Map<PropertySymbol, Function<DataType, DataType>> properties = (Map<PropertySymbol, Function<DataType, DataType>>) proprtiesField.get(null);
                propertiesCache.put(type, properties);
                return properties.keySet();
            }

            return propertiesCache.get(type).keySet();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<MethodSymbol> getMethods(Class<? extends DataType> type) {
        try {
            if (!methodsCache.containsKey(type)) {
                Field methodsField = type.getField("methods");
                List<MethodSymbol> methods = new ArrayList<>((List<MethodSymbol>) methodsField.get(null));
                methodsCache.put(type, methods);
                return methods;
            }

            return methodsCache.get(type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static TypeSymbol getTypeSymbol(Class<? extends DataType> type) {
        try {
            if (!typeSymbolsCache.containsKey(type)) {
                Field typeSymbolField = type.getField("TYPE");
                TypeSymbol typeSymbol = (TypeSymbol) typeSymbolField.get(null);
                typeSymbolsCache.put(type, typeSymbol);
                return typeSymbol;
            }

            return typeSymbolsCache.get(type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataType d ? d.type.equals(type) : super.equals(obj);
    }
}
