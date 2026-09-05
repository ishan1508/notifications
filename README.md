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

## Notification API

Create a notification:

```bash
curl -i -X POST http://localhost:8080/notifications \
  -H 'Content-Type: application/json' \
  -d '{
    "recipientId": "user-123",
    "type": "PAYROLL_COMPLETED",
    "message": "Your payroll run has completed."
  }'
```

New notifications have a generated UUID, an `UNREAD` status, and a UTC creation
timestamp.

Retrieve one notification or filter the collection:

```bash
curl http://localhost:8080/notifications/{notificationId}
curl 'http://localhost:8080/notifications?recipientId=user-123&status=UNREAD'
```

Mark a notification as read:

```bash
curl -X PATCH http://localhost:8080/notifications/{notificationId} \
  -H 'Content-Type: application/json' \
  -d '{"status":"READ"}'
```

Changing the status to `READ` sets `readAt`; changing it back to `UNREAD` clears
`readAt`. Notification content cannot be changed after creation.

Delete a notification:

```bash
curl -X DELETE http://localhost:8080/notifications/{notificationId}
```

The API returns RFC 9457 Problem Details for invalid requests and missing
notifications.

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
├── controller/   HTTP request and response handling
├── domain/       Notification model and status
├── dto/          Validated API requests and responses
├── exception/    Consistent Problem Detail error handling
├── repository/   In-memory notification storage
└── service/      Notification business rules
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
