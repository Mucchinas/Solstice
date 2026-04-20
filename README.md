# Summertime Framework

**Summertime** is a minimalist, space-themed web framework for Java, designed for simplicity, speed, and **full retrocompatibility with Java 8**. It provides a lightweight alternative to heavy frameworks, utilizing a unique celestial nomenclature for its components.

## Key Features

- **Java 8+ Compatible**: Works on older environments while supporting modern development patterns.
- **Minimalist Footprint**: Built on top of the native `com.sun.net.httpserver` and `Gson`.
- **Annotation-Driven**: Clean and intuitive API using space-inspired decorators.
- **Dependency Injection**: Automated "Orbiting" (DI) for singleton components.
- **JSON Ready**: Built-in support for JSON request/response serialization.

---

## The Celestial Metaphors (Core Concepts)

Summertime uses a space-themed vocabulary to describe its components:

| Term | Annotation | Description |
| :--- | :--- | :--- |
| **Star** | `@Star` | A standard component or service (Singleton Bean). |
| **Quasar** | `@Quasar` | A web controller that exposes endpoints to the "galaxy" (network). |
| **Orbit** | `@Orbit` | Marks a field for Dependency Injection. The framework "orbits" the dependency into the star. |
| **Chart** | `@GetChart` / `@PostChart` | A route definition mapping a path to a specific method. |
| **Traveler** | `@ChartTraveler` | The request body payload traveling through the network (parsed from JSON). |
| **Spec** | `@ChartSpec` | A specific parameter (query parameter) required for the chart. |
| **Charter** | — | The internal engine that manages routes and navigates HTTP traffic. |

---

## Getting Started

### 1. Initialize the Universe
To start your application, simply call `Summertime.run()` passing your main class. This triggers classpath scanning and starts the server on port `8080`.

```java
public class Main {
    public static void main(String[] args) {
        Summertime.run(Main.class);
    }
}
```

### 2. Create a Star (Service)
Define your business logic in a class annotated with `@Star`.

```java
@Star
public class ExplorationService {
    public String getStatus() {
        return "All systems nominal.";
    }
}
```

### 3. Create a Quasar (Controller)
Expose endpoints by annotating a class with `@Quasar`. Use `@Orbit` to inject your stars.

```java
@Quasar
public class MissionController {

    @Orbit
    private ExplorationService service;

    // GET Request with Query Parameter
    @GetChart(path = "/mission/status")
    public String checkStatus(@ChartSpec("id") String missionId) {
        return "Mission " + missionId + ": " + service.getStatus();
    }

    // POST Request with JSON Body
    @PostChart(path = "/mission/launch")
    public LaunchResult launch(@ChartTraveler LaunchData data) {
        // data is automatically parsed from JSON
        return new LaunchResult("Launched " + data.getRocketName());
    }
}
```

---

## Networking & Parameters

- **JSON Support**: Methods returning objects (other than `String`) are automatically serialized to JSON.
- **Query Parameters**: Use `@ChartSpec("name")` to bind query string values to method arguments.
- **Request Body**: Use `@ChartTraveler` to bind the incoming JSON body to a POJO.
- **Customization**: The server currently orbits on port `8080` by default.

---

## Technical Architecture

- **Classpath Scanning**: Powered by [ClassGraph](https://github.com/classgraph/classgraph) for efficient discovery of celestial bodies.
- **JSON Parsing**: Powered by [Gson](https://github.com/google/gson).
- **HTTP Engine**: Utilizes the robust, native `HttpServer` included in the JDK.

## Requirements

- **Java 8** or higher.
- **Maven** (for dependency management).

---

Enjoy the warmth of a simpler development experience with Summertime.