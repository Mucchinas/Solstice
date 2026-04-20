package io.summertime.core.di;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.summertime.annotations.Constellation;
import io.summertime.annotations.Star;
import io.summertime.core.common.Logger;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {

    private final ApplicationContext context;

    public ComponentScanner(ApplicationContext context) {
        this.context = context;
    }

    public void scanAndInstantiate(String basePackage) {
        Logger.info("Scanning for components in package: " + basePackage);
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            Set<Class<?>> componentClasses = new HashSet<>();
            componentClasses.addAll(scanResult.getClassesWithAnnotation(Star.class.getName()).loadClasses());
            componentClasses.addAll(scanResult.getClassesWithAnnotation(Constellation.class.getName()).loadClasses());

            for (Class<?> clazz : componentClasses) {
                if (clazz.isAnnotation() || clazz.isInterface() || context.getBean(clazz) != null) {
                    continue;
                }
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance();
                    context.registerBean(clazz, instance);
                    Logger.info("Registered Star: " + clazz.getSimpleName());
                } catch (Exception e) {
                    Logger.error("Failed to instantiate Star: " + clazz.getName(), e);
                    throw new RuntimeException("Failed to instantiate Star: " + clazz.getName(), e);
                }
            }
        }
    }
}
