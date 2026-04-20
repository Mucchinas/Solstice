package io.summertime.core.routing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)}");

    private final String httpMethod;
    private final Method method;
    private final Object bean;
    private final Pattern pathPattern;
    private final List<String> pathVariableNames = new ArrayList<>();

    public Route(String pathTemplate, String httpMethod, Method method, Object bean) {
        this.httpMethod = httpMethod;
        this.method = method;
        this.bean = bean;
        this.method.setAccessible(true);
        this.pathPattern = compilePathTemplate(pathTemplate);
    }

    private Pattern compilePathTemplate(String pathTemplate) {
        Matcher matcher = PARAM_PATTERN.matcher(pathTemplate);
        StringBuffer regex = new StringBuffer();
        while (matcher.find()) {
            pathVariableNames.add(matcher.group(1));
            matcher.appendReplacement(regex, "([^/]+)");
        }
        matcher.appendTail(regex);
        return Pattern.compile(regex.toString());
    }

    public Matcher match(String path) {
        Matcher matcher = pathPattern.matcher(path);
        return matcher.matches() ? matcher : null;
    }

    // Getters
    public String getHttpMethod() { return httpMethod; }
    public Method getMethod() { return method; }
    public Object getBean() { return bean; }
    public List<String> getPathVariableNames() { return pathVariableNames; }
}
