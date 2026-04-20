package io.summertime.core.di;

import io.summertime.annotations.Constellation;
import io.summertime.annotations.Orbit;
import io.summertime.annotations.Stardust;
import io.summertime.annotations.Starsign;
import io.summertime.core.common.Logger;
import io.summertime.core.convert.ConversionService;
import io.summertime.core.props.PropertyResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class DependencyInjector {

    private final ApplicationContext context;
    private final PropertyResolver propertyResolver;
    private final ConversionService conversionService;

    public DependencyInjector(ApplicationContext context, PropertyResolver propertyResolver, ConversionService conversionService) {
        this.context = context;
        this.propertyResolver = propertyResolver;
        this.conversionService = conversionService;
    }

    public void processConstellations() {
        for (Object bean : new ArrayList<>(context.getAllBeans().values())) {
            if (bean.getClass().isAnnotationPresent(Constellation.class)) {
                for (Method method : bean.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Starsign.class)) {
                        try {
                            method.setAccessible(true);
                            Object produced = method.invoke(bean);
                            context.registerBean(method.getReturnType(), produced);
                            Logger.info("Registered Starsign: " + method.getReturnType().getSimpleName());
                        } catch (Exception e) {
                            Logger.error("Failed to produce Star from @Starsign: " + method.getName(), e);
                            throw new RuntimeException("Failed to produce Star from @Starsign: " + method.getName(), e);
                        }
                    }
                }
            }
        }
    }

    public void performWiring() {
        for (Object bean : context.getAllBeans().values()) {
            inject(bean);
        }
    }

    public void inject(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Orbit.class)) {
                    injectOrbit(instance, field);
                }
                if (field.isAnnotationPresent(Stardust.class)) {
                    injectStardust(instance, field);
                }
            } catch (Exception e) {
                Logger.error("Failed to inject into field: " + field.getName(), e);
                throw new RuntimeException("Failed to inject into field: " + field.getName(), e);
            }
        }
    }

    private void injectOrbit(Object instance, Field field) throws IllegalAccessException {
        Object dependency = context.getBean(field.getType());
        if (dependency != null) {
            field.setAccessible(true);
            field.set(instance, dependency);
        } else {
            throw new RuntimeException("Could not orbit " + field.getType().getSimpleName() + " into " + instance.getClass().getSimpleName() + ". Is it a @Star?");
        }
    }

    private void injectStardust(Object instance, Field field) throws IllegalAccessException {
        String key = field.getAnnotation(Stardust.class).value();
        String rawValue = propertyResolver.getProperty(key);

        if (rawValue != null) {
            Object convertedValue = conversionService.convert(rawValue, field.getType())
                    .orElseThrow(() -> new RuntimeException("Unsupported type for @Stardust: " + field.getType().getName()));
            field.setAccessible(true);
            field.set(instance, convertedValue);
        }
    }
}
