package io.summertime.core.routing;

import java.util.Map;

public class RouteMatch {

    private final Route route;
    private final Map<String, String> pathVariables;

    public RouteMatch(Route route, Map<String, String> pathVariables) {
        this.route = route;
        this.pathVariables = pathVariables;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }
}
