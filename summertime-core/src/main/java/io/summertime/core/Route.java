package io.summertime.core;

import java.lang.reflect.Method;

public class Route {
    private final String path;
    private final String httpMethod;
    private final Method method;
    private final Object bean;

    public Route(String path, String httpMethod, Method method, Object bean) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.method = method;
        this.bean = bean;
        this.method.setAccessible(true);
    }

    // Getters
    public String getPath() { return path; }
    public String getHttpMethod() { return httpMethod; }
    public Method getMethod() { return method; }
    public Object getBean() { return bean; }
}