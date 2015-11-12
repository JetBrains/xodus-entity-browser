package com.lehvolk.xodus.repo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

/**
 * Supported UI property types. All other types will be shown in readonly mode.
 * @author Alexey Volkov
 * @since 12.11.2015
 */
public final class UIPropertyTypes {
    private static final Map<Class<? extends Comparable<?>>, UIPropertyType<?>> BY_CLASS = new ConcurrentHashMap<>();
    private static final Map<String, UIPropertyType<?>> BY_NAME = new ConcurrentHashMap<>();

    private static final UIPropertyType<String> STRING =
            newType(String.class, x -> x);
    private static final UIPropertyType<Byte> BYTE = newType(Byte.class, Byte::valueOf);
    private static final UIPropertyType<Short> SHORT = newType(Short.class, Short::valueOf);
    private static final UIPropertyType<Integer> INT = newType(Integer.class, Integer::valueOf);
    private static final UIPropertyType<Long> LONG = newType(Long.class, Long::valueOf);
    private static final UIPropertyType<Float> FLOAT = newType(Float.class, Float::valueOf);
    private static final UIPropertyType<Double> DOUBLE = newType(Double.class, Double::valueOf);


    public static class UIPropertyType<T extends Comparable<?>> {
        private final Class<T> clazz;
        private final Function<String, T> function;

        private UIPropertyType(Class<T> clazz, Function<String, T> function) {
            this.clazz = clazz;
            this.function = function;
        }

        @Nullable
        public String toString(T value) {
            if (value == null) {
                return null;
            }
            return value.toString();
        }

        @Nullable
        public T toValue(String value) {
            if (value == null) {
                return null;
            }
            return function.apply(value);
        }

        public boolean isValid(String value) {
            try {
                function.apply(value);
                return true;
            } catch (RuntimeException e) {
                // ignore result if conversion failed
                return false;
            }
        }
    }

    private UIPropertyTypes() {
    }

    private static <T extends Comparable<?>> UIPropertyType<T> newType(Class<T> clazz, Function<String, T> function) {
        UIPropertyType<T> type = new UIPropertyType<>(clazz, function);
        BY_CLASS.put(clazz, type);
        BY_NAME.put(clazz.getName(), type);
        return type;
    }

    public static boolean isSupported(Class<?> clazz) {
        return BY_CLASS.containsKey(clazz);
    }

    public static boolean isSupported(String clazz) {
        return BY_NAME.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<?>> UIPropertyType<T> uiTypeOf(Class<?> clazz) {
        return (UIPropertyType<T>) BY_CLASS.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<?>> UIPropertyType<T> uiTypeOf(String clazz) {
        return (UIPropertyType<T>) BY_NAME.get(clazz);
    }
}
