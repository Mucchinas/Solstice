package io.summertime.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SpaceConverter {
    private static final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    static {
        converters.put(String.class, s -> s);
        converters.put(int.class, Integer::parseInt);
        converters.put(Integer.class, Integer::parseInt);
        converters.put(long.class, Long::parseLong);
        converters.put(Long.class, Long::parseLong);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Boolean.class, Boolean::parseBoolean);
        converters.put(double.class, Double::parseDouble);
        converters.put(Double.class, Double::parseDouble);
    }

    public static Object convert(Class<?> targetType, String value) {
        Function<String, Object> converter = converters.get(targetType);
        if (converter == null) {
            throw new RuntimeException("☀️ Tipo non supportato per Stardust: " + targetType.getName());
        }
        return converter.apply(value);
    }
}