package io.summertime.core.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import io.summertime.core.common.Logger;
import io.summertime.core.routing.Route;
import io.summertime.core.routing.RouteMatch;
import io.summertime.core.routing.Router;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    private final Router router;
    private final ArgumentResolver argumentResolver;
    private final Gson gson = new Gson();

    public RequestHandler(Router router, ArgumentResolver argumentResolver) {
        this.router = router;
        this.argumentResolver = argumentResolver;
    }

    public void handle(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            router.findRoute(path, method)
                    .ifPresentOrElse(routeMatch -> {
                        try {
                            Route route = routeMatch.getRoute();
                            Object[] args = argumentResolver.resolveArguments(exchange, route.getMethod(), routeMatch);
                            Object result = route.getMethod().invoke(route.getBean(), args);
                            sendResponse(exchange, result);
                        } catch (Exception e) {
                            Logger.error("Error invoking route: " + path, e);
                            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
                        }
                    }, () -> sendError(exchange, 404, "Not Found"));

        } catch (Exception e) {
            Logger.error("Unhandled exception during request handling", e);
            sendError(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    private void sendResponse(HttpExchange exchange, Object result) throws IOException {
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

    private void sendError(HttpExchange exchange, int code, String message) {
        try {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            Logger.error("Failed to send error response", e);
        }
    }
}
