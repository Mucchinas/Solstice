# Summertime Framework Documentation

Summertime is a minimalist web framework for Java 8+ designed with a celestial metaphor. It provides automated dependency injection, configuration management, and a lightweight web server.

## Core Architecture

The framework operates on a singleton-based container. It scans the base package of the application to identify and manage celestial bodies.

### Component Management

- **@Star**: Defines a managed singleton component. Any class marked with this annotation is instantiated during startup.
- **@Quasar**: A specialized Star that acts as a web controller. Only methods within a Quasar can be mapped to web routes.
- **@Constellation**: A configuration class. It is itself a Star and can contain factory methods to produce other stars.

```java
@Star
class ScienceModule {} // Managed singleton
```

### Dependency Injection (Orbiting)

The framework performs automated wiring of dependencies using the **@Orbit** annotation. It supports injection into both managed beans and external instances via `Summertime.inject(Object)`.

- **Field Injection**: Injecting a managed bean into another.
- **External Injection**: Injecting managed beans into instances created outside the framework.

```java
@Star
class NavigationSystem {
    @Orbit
    private ScienceModule module; // Wired automatically
}
```

### Configuration and Properties (Stardust)

Summertime includes a property management system that automatically loads `summertime.properties` from the classpath.

- **@Stardust**: Injects values from properties files into fields.
- **Type Conversion**: The framework has a built-in conversion service that automatically converts string properties into primitive types (int, long, boolean, double) and their wrappers.

```java
@Star
class Engine {
    @Stardust("engine.thrust")
    private int thrust; // Converted from string to int
}
```

### Factory Methods (Starsigns)

When a component cannot be instantiated via a default constructor or requires complex setup, use a **Constellation**.

- **@Starsign**: Marks a method within a Constellation as a factory. The return type of the method is registered as a managed Star.

```java
@Constellation
class SystemConfig {
    @Starsign
    public HttpClient getClient() {
        return HttpClient.newBuilder().build(); // Registered as a Star
    }
}
```

## Web Engine

The web server is built on the native JDK HttpServer and runs by default on port 8080 (configurable via `server.port`).

### Route Mapping

Endpoints are defined using method-level annotations within a Quasar.

- **@GetChart(path)**: Maps an HTTP GET request to the method.
- **@PostChart(path)**: Maps an HTTP POST request to the method.

### Parameter Resolution

Method arguments are resolved dynamically using specialized annotations:

- **@ChartParam(name)**: Binds a path variable from the URL. Supports automatic type conversion.
- **@ChartSpec(name)**: Binds a query parameter from the URL. Supports automatic type conversion.
- **@ChartTraveler**: Binds the request body. The framework expects JSON and deserializes it into the parameter type using GSON.

```java
@Quasar
class CommunicationBridge {
    @GetChart(path = "/ping/{id}")
    public String ping(@ChartParam("id") int id) {
        return "Pong: " + id;
    }

    @GetChart(path = "/status")
    public String getStatus(@ChartSpec("code") int code) {
        return "Status Code: " + code;
    }

    @PostChart(path = "/message")
    public Response send(@ChartTraveler Message msg) {
        return new Response("Received: " + msg.content); // Serialized to JSON
    }
}
```

### Response Handling

- **String**: Returned as `text/plain`.
- **Objects**: Automatically serialized to JSON with `application/json` content type.

## Technical Initialization

To start the framework, invoke the `run` method from the main entry point. This method bootstraps the application and returns a `Summertime` instance, which can be used for programmatic access to beans or to shut down the application.

```java
public class Application {
    public static void main(String[] args) {
        Summertime app = Summertime.run(Application.class);

        // The application is now running.
        // To stop it gracefully:
        // app.stop();
    }
}
```

## Configuration (summertime.properties)

Standard configuration keys:
- `server.port`: The port for the web server (default: 8080).
- Custom keys for use with `@Stardust`.

```properties
server.port=9000
galaxy.name=Andromeda
number.int=42
```
