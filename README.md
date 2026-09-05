# Notifications

A Spring Boot foundation for notification APIs. It includes request validation,
consistent problem responses, Actuator health checks, tests, formatting checks,
and CI.

## Prerequisites

- JDK 21 or newer
- Maven 3.9 or newer

## Run the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## Check application health

```bash
curl http://localhost:8080/actuator/health
```

Only the safe `health` and `info` Actuator endpoints are exposed.

## Build and test

```bash
mvn verify
```

To apply the configured Java formatter:

```bash
mvn spotless:apply
```

## Editor setup

When Cursor opens the project, install the recommended extensions when prompted. Java
files are then formatted with Palantir Java Format and imports are organized whenever
you save.

The editor and Maven use the same pinned formatter version. `mvn verify` also runs
Spotless and strict Java compiler linting, so CI rejects unformatted code, wildcard
imports, and compiler warnings.

## Project structure

```text
src/main/java/com/ishan/notifications/
└── exception/    Consistent Problem Detail error handling
```

When extending the application:

1. Model the API contract in `dto`.
2. Keep business rules in `service`.
3. Keep controllers focused on HTTP concerns.
4. Add focused tests for happy paths and edge cases.
5. Add persistence, authentication, or external integrations only when the exercise
   requires them.

## Technology

- Java 21
- Spring Boot 4
- Maven
- JUnit 5 and MockMvc
- Spring Boot Actuator
