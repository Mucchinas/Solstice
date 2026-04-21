package io.solstice.core;

import io.solstice.core.common.Logger;
import io.solstice.core.convert.ConversionService;
import io.solstice.core.di.ApplicationContext;
import io.solstice.core.di.ComponentScanner;
import io.solstice.core.di.DependencyInjector;
import io.solstice.core.props.PropertyResolver;
import io.solstice.core.routing.Router;
import io.solstice.core.server.ArgumentResolver;
import io.solstice.core.server.RequestHandler;
import io.solstice.core.server.WebServer;

public class Solstice {

    private final ApplicationContext context;
    private final DependencyInjector injector;
    private final WebServer webServer;

    private Solstice(Class<?> mainClass) {
        Logger.info("Solstice is rising...");

        // 1. Core Services Initialization
        this.context = new ApplicationContext();
        PropertyResolver propertyResolver = new PropertyResolver();
        ConversionService conversionService = new ConversionService();
        this.context.registerBean(PropertyResolver.class, propertyResolver);
        this.context.registerBean(ConversionService.class, conversionService);
        this.context.registerBean(ApplicationContext.class, context);

        // 2. Component Scanning
        ComponentScanner scanner = new ComponentScanner(context);
        scanner.scanAndInstantiate(mainClass.getPackage().getName());

        // 3. Dependency Injection
        this.injector = new DependencyInjector(context, propertyResolver, conversionService);
        this.injector.processConstellations();
        this.injector.performWiring();

        Logger.info("Solstice genesis completed! Celestial Bodies in orbit: " + context.getAllBeans().size());

        // 4. Launch Web Server
        this.webServer = launchWebServer();
        this.webServer.start();
    }

    public static Solstice run(Class<?> mainClass) {
        try {
            return new Solstice(mainClass);
        } catch (Exception e) {
            Logger.error("Solstice failed to reach orbit", e);
            throw new RuntimeException("Solstice failed to reach orbit", e);
        }
    }

    private WebServer launchWebServer() {
        try {
            PropertyResolver props = context.getBean(PropertyResolver.class);
            ConversionService conversionService = context.getBean(ConversionService.class);

            Router router = new Router(context);
            ArgumentResolver argumentResolver = new ArgumentResolver(conversionService);
            RequestHandler requestHandler = new RequestHandler(router, argumentResolver);

            int port = props.getIntProperty("server.port", 8080);
            return new WebServer(port, requestHandler);
        } catch (Exception e) {
            Logger.error("Web server failed to start", e);
            throw new RuntimeException("Web server failed to start", e);
        }
    }

    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public void inject(Object instance) {
        injector.inject(instance);
    }

    public void stop() {
        if (webServer != null) {
            webServer.stop();
        }
        Logger.info("Solstice dissolved.");
    }
}