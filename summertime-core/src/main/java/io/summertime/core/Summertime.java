package io.summertime.core;

import io.summertime.core.common.Logger;
import io.summertime.core.convert.ConversionService;
import io.summertime.core.di.ApplicationContext;
import io.summertime.core.di.ComponentScanner;
import io.summertime.core.di.DependencyInjector;
import io.summertime.core.props.PropertyResolver;
import io.summertime.core.routing.Router;
import io.summertime.core.server.ArgumentResolver;
import io.summertime.core.server.RequestHandler;
import io.summertime.core.server.WebServer;

public class Summertime {

    private final ApplicationContext context;
    private final DependencyInjector injector;
    private final WebServer webServer;

    private Summertime(Class<?> mainClass) {
        Logger.info("Summertime is condensing...");

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

        Logger.info("Summertime genesis completed! Celestial Bodies in orbit: " + context.getAllBeans().size());

        // 4. Launch Web Server
        this.webServer = launchWebServer();
        this.webServer.start();
    }

    public static Summertime run(Class<?> mainClass) {
        try {
            return new Summertime(mainClass);
        } catch (Exception e) {
            Logger.error("Summertime failed to reach orbit", e);
            throw new RuntimeException("Summertime failed to reach orbit", e);
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
        Logger.info("Summertime dissolved.");
    }
}