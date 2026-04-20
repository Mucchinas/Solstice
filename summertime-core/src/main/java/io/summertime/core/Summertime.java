package io.summertime.core;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.summertime.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Summertime {

    private static final Map<Class<?>, Object> context = new HashMap<>();

    public static void run(Class<?> mainClass) {
        System.out.println("Summertime is rising...");
        Observatory.load(); // summertime.properties

        try {
            // 1. Scan @Star @Constellation
            scanAndInstantiate(mainClass);

            // 2. @Starsign @Constellation
            processConstellations();

            // 3. @Orbit @Stardust
            performWiring();

            System.out.println("Summertime started! Celestial bodies in orbit: " + context.size());

            // 4. Charter (Server)
            launchCharter();

        } catch (Exception e) {
            throw new RuntimeException("Summertime failed to reach orbit", e);
        }
    }

    private static void scanAndInstantiate(Class<?> mainClass) throws Exception {
        String packageToScan = mainClass.getPackage().getName();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageToScan).scan()) {
            List<Class<?>> componentClasses = scanResult.getClassesWithAnnotation(Star.class.getName()).loadClasses();

            for (Class<?> clazz : componentClasses) {
                if (clazz.isAnnotation() || clazz.isInterface()) continue;

                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                context.put(clazz, constructor.newInstance());
            }
        }
    }

    private static void processConstellations() {

        for (Object bean : new ArrayList<>(context.values())) {
            if (bean.getClass().isAnnotationPresent(Constellation.class)) {
                for (Method method : bean.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Starsign.class)) {
                        try {
                            method.setAccessible(true);
                            Object produced = method.invoke(bean);

                            context.put(method.getReturnType(), produced);
                            System.out.println("Starsign registered: " + method.getReturnType().getSimpleName());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to produce Star from @Starsign: " + method.getName(), e);
                        }
                    }
                }
            }
        }
    }

    private static void performWiring() {
        for (Object bean : context.values()) {
            inject(bean);
        }
    }

    /**
     * Inietta dipendenze in un'istanza (gestita o esterna).
     * Gestisce sia @Orbit che @Stardust con conversione dei tipi.
     */
    public static void inject(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            try {
                // @Orbit (Dependency Injection)
                if (field.isAnnotationPresent(Orbit.class)) {
                    Object dependency = context.get(field.getType());
                    if (dependency != null) {
                        field.setAccessible(true);
                        field.set(instance, dependency);
                    } else {
                        System.err.println("Warning: Could not orbit " + field.getType().getSimpleName() + " into " + instance.getClass().getSimpleName());
                    }
                }

                // @Stardust (Properties Injection con conversione)
                if (field.isAnnotationPresent(Stardust.class)) {
                    String key = field.getAnnotation(Stardust.class).value();
                    String rawValue = Observatory.get(key, null);

                    if (rawValue != null) {
                        Object convertedValue = SpaceConverter.convert(field.getType(), rawValue);
                        field.setAccessible(true);
                        field.set(instance, convertedValue);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject into field: " + field.getName(), e);
            }
        }
    }

    private static void launchCharter() {
        try {
            int port = Observatory.getInt("server.port", 8080);
            Charter.start(port, context);
        } catch (Exception e) {
            throw new RuntimeException("Charter collision: failed to start server", e);
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return clazz.cast(context.get(clazz));
    }
}