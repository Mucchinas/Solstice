package io.summertime.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import io.summertime.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Charter {
    private static final Gson gson = new Gson();
    private static final List<Route> routes = new ArrayList<>();

    public static void start(int port, Map<Class<?>, Object> context) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        buildRouteTable(context);

        server.createContext("/", exchange -> {
            try {
                handleRequest(exchange);
            } catch (Exception e) {
                sendError(exchange, 500, "Collisione spaziale: " + e.getMessage());
            } finally {
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Charter orbitante sulla porta " + port);
    }

    private static void buildRouteTable(Map<Class<?>, Object> context) {
        for (Object bean : context.values()) {
            if (!bean.getClass().isAnnotationPresent(Quasar.class)) continue;

            for (Method m : bean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(GetChart.class)) {
                    routes.add(new Route(m.getAnnotation(GetChart.class).path(), "GET", m, bean));
                } else if (m.isAnnotationPresent(PostChart.class)) {
                    routes.add(new Route(m.getAnnotation(PostChart.class).path(), "POST", m, bean));
                }
            }
        }
        routes.forEach(r -> System.out.println("Rotta tracciata: [" + r.getHttpMethod() + "] " + r.getPath()));
    }

    private static void handleRequest(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Route route = routes.stream()
                .filter(r -> r.getPath().equals(path) && r.getHttpMethod().equalsIgnoreCase(method))
                .findFirst()
                .orElse(null);

        if (route == null) {
            sendError(exchange, 404, "Rotta non trovata nella galassia");
            return;
        }

        Object[] args = resolveArguments(exchange, route.getMethod());

        Object result = route.getMethod().invoke(route.getBean(), args);

        sendResponse(exchange, result);
    }

    private static Object[] resolveArguments(HttpExchange exchange, Method method) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];

            if (p.isAnnotationPresent(ChartSpec.class)) {
                String rawValue = queryParams.get(p.getAnnotation(ChartSpec.class).value());
                if (rawValue != null) {
                    // Utilizzo dello SpaceConverter per supportare int, boolean, etc. nell'URL
                    args[i] = SpaceConverter.convert(p.getType(), rawValue);
                }
            }
            else if (p.isAnnotationPresent(ChartTraveler.class)) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                args[i] = gson.fromJson(reader, p.getType());
            }
        }
        return args;
    }

    private static void sendResponse(HttpExchange exchange, Object result) throws IOException {
        String response;
        String contentType;

        if (result instanceof String) {
            response = (String) result;
            contentType = "text/plain";
        } else {
            response = gson.toJson(result);
            contentType = "application/json";
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length > 1) params.put(kv[0], kv[1]);
        }
        return params;
    }
}