package io.summertime.core.routing;

import io.summertime.annotations.GetChart;
import io.summertime.annotations.PostChart;
import io.summertime.annotations.Quasar;
import io.summertime.core.common.Logger;
import io.summertime.core.di.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

public class Router {

    private final List<Route> routes = new ArrayList<>();

    public Router(ApplicationContext context) {
        buildRouteTable(context);
    }

    private void buildRouteTable(ApplicationContext context) {
        for (Object bean : context.getAllBeans().values()) {
            if (!bean.getClass().isAnnotationPresent(Quasar.class)) {
                continue;
            }

            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetChart.class)) {
                    GetChart annotation = method.getAnnotation(GetChart.class);
                    addRoute(annotation.path(), "GET", method, bean);
                } else if (method.isAnnotationPresent(PostChart.class)) {
                    PostChart annotation = method.getAnnotation(PostChart.class);
                    addRoute(annotation.path(), "POST", method, bean);
                }
            }
        }
    }

    private void addRoute(String pathTemplate, String httpMethod, Method method, Object bean) {
        Route route = new Route(pathTemplate, httpMethod, method, bean);
        routes.add(route);
        Logger.info("Mapped route: [" + httpMethod + "] " + pathTemplate);
    }

    public Optional<RouteMatch> findRoute(String path, String httpMethod) {
        for (Route route : routes) {
            if (!route.getHttpMethod().equalsIgnoreCase(httpMethod)) {
                continue;
            }

            Matcher matcher = route.match(path);
            if (matcher != null) {
                Map<String, String> pathVariables = new HashMap<>();
                List<String> variableNames = route.getPathVariableNames();
                for (int i = 0; i < matcher.groupCount(); i++) {
                    pathVariables.put(variableNames.get(i), matcher.group(i + 1));
                }
                return Optional.of(new RouteMatch(route, pathVariables));
            }
        }
        return Optional.empty();
    }
}
