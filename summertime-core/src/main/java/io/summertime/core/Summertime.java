package io.summertime.core;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.summertime.annotations.Constellation;
import io.summertime.annotations.Star;
import io.summertime.annotations.Orbit;
import io.summertime.annotations.Starsign;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Summertime {

    private static final Map<Class<?>, Object> context = new HashMap<>();

    public static void run(Class<?> mainClass) {
        System.out.println("Summertime is starting...");

        try {
            // 1. SCAN
            String packageToScan = mainClass.getPackage().getName();
            try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageToScan).scan()) {
                List<Class<?>> componentClasses = scanResult.getClassesWithAnnotation(Star.class.getName()).loadClasses();

                // 2. ISTANTIATE
                for (Class<?> clazz : componentClasses) {

                    if (clazz.isAnnotation() || clazz.isInterface()) {
                        continue;
                    }

                    try {

                        java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor();


                        constructor.setAccessible(true);

                        context.put(clazz, constructor.newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException("Errore creando: " + clazz.getName(), e);
                    }
                }
            }

            for (Object bean : new ArrayList<>(context.values())) { // Copia per evitare ConcurrentModification
                if (bean.getClass().isAnnotationPresent(Constellation.class)) {
                    for (Method method : bean.getClass().getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Starsign.class)) {
                            try {
                                method.setAccessible(true);

                                Object produced = method.invoke(bean);

                                context.put(method.getReturnType(), produced);
                                System.out.println("Nuova Star prodotta dalla costellazione: " + method.getReturnType().getSimpleName());
                            } catch (Exception e) {
                                throw new RuntimeException("Errore nella produzione della Star", e);
                            }
                        }
                    }
                }
            }

            // 3. WIRING
            for (Object cb : context.values()) {
                for (Field field : cb.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Orbit.class)) {
                        Object dependency = context.get(field.getType());
                        if (dependency == null) {
                            System.err.println("Attenzione: Impossibile iniettare " + field.getType().getSimpleName() +
                                    ". Assicurati che sia annotato con @Star!");
                        } else {
                            field.setAccessible(true);
                            field.set(cb, dependency);
                        }
                    }
                }
            }

            System.out.println("Summertime started! Celestial bodies loaded: " + context.size());
            try {
                Charter.start(8080, context);
            } catch (Exception e) {
                throw new RuntimeException("Errore fatale durante l'avvio del Charter", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Summertime failed to start", e);
        }
    }

    public static void inject(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Orbit.class)) {
                Object dependency = context.get(field.getType());
                if (dependency != null) {
                    try {
                        field.setAccessible(true);
                        field.set(instance, dependency);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Errore iniezione manuale", e);
                    }
                }
            }
        }
    }


    public static <T> T getBean(Class<T> clazz) {
        return clazz.cast(context.get(clazz));
    }
}