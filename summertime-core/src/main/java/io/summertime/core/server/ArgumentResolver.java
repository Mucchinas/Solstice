package io.summertime.core.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import io.summertime.annotations.ChartSpec;
import io.summertime.annotations.ChartTraveler;
import io.summertime.core.convert.ConversionService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArgumentResolver {

    private final ConversionService conversionService;
    private final Gson gson = new Gson();

    public ArgumentResolver(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public Object[] resolveArguments(HttpExchange exchange, Method method) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getRawQuery());

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];

            if (p.isAnnotationPresent(ChartSpec.class)) {
                String paramName = p.getAnnotation(ChartSpec.class).value();
                String rawValue = queryParams.get(paramName);
                if (rawValue != null) {
                    args[i] = conversionService.convert(rawValue, p.getType())
                            .orElseThrow(() -> new IllegalArgumentException("Cannot convert value for param " + paramName));
                }
            } else if (p.isAnnotationPresent(ChartTraveler.class)) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                args[i] = gson.fromJson(reader, p.getType());
            }
        }
        return args;
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            try {
                String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!key.isEmpty()) {
                    String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                    params.put(key, value);
                }
            } catch (UnsupportedEncodingException e) {
                // Should not happen with UTF-8
                throw new RuntimeException(e);
            }
        }
        return params;
    }
}
