package io.summertime.core.routing;

import io.summertime.annotations.GetChart;
import io.summertime.annotations.PostChart;
import io.summertime.annotations.Quasar;
import io.summertime.core.routing.Route;
import io.summertime.core.common.Logger;
import io.summertime.core.di.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private void addRoute(String path, String httpMethod, Method method, Object bean) {
        Route route = new Route(path, httpMethod, method, bean);
        routes.add(route);
        Logger.info("Mapped route: [" + httpMethod + "] " + path);
    }

    public Optional<Route> findRoute(String path, String httpMethod) {
        return routes.stream()
                .filter(r -> r.getPath().equals(path) && r.getHttpMethod().equalsIgnoreCase(httpMethod))
                .findFirst();
    }
}
