package io.summertime.core.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ConversionService {

    private final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public ConversionService() {
        registerDefaultConverters();
    }

    private void registerDefaultConverters() {
        register(String.class, s -> s);
        register(int.class, Integer::parseInt);
        register(Integer.class, Integer::parseInt);
        register(long.class, Long::parseLong);
        register(Long.class, Long::parseLong);
        register(boolean.class, Boolean::parseBoolean);
        register(Boolean.class, Boolean::parseBoolean);
        register(double.class, Double::parseDouble);
        register(Double.class, Double::parseDouble);
    }

    public <T> void register(Class<T> type, Function<String, T> converter) {
        converters.put(type, (Function<String, Object>) converter);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> convert(String value, Class<T> targetType) {
        if (value == null) {
            return Optional.empty();
        }
        Function<String, Object> converter = converters.get(targetType);
        if (converter == null) {
            return Optional.empty();
        }
        try {
            return Optional.of((T) converter.apply(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
