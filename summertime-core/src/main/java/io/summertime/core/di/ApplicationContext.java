package io.summertime.core.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    public void registerBean(Class<?> type, Object instance) {
        beans.put(type, instance);
        // Also register against implemented interfaces
        for (Class<?> iface : type.getInterfaces()) {
            beans.putIfAbsent(iface, instance);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
    
    public Map<Class<?>, Object> getAllBeans() {
        return beans;
    }
}
