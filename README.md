# LoadBalancer

This repository is a Spring Boot-based load balancer implemented in Java. The README below only states facts found in the code and build files (no assumptions).

Summary
- Spring Boot application with main class: `org.example.loadbalancer.LoadBalancerApplication`.
- Uses Spring scheduling and async support (@EnableScheduling, @EnableAsync).
- Build tool: Gradle (wrapper included). Java toolchain is configured in `build.gradle` with languageVersion = 26.

What the code does (exact behavior)
- HTTP entrypoint: `src/main/java/org/example/loadbalancer/controllers/MainController.java` exposes a GET endpoint at `/` and extracts Host, User-Agent, and Accept headers.
- Forwarding: `ForwardService` performs asynchronous forwarding of requests. It selects targets using a simple rotating index (AtomicInteger), uses `org.springframework.web.client.RestClient` for HTTP calls, and retries across the list of healthy servers.
- Health checks: `HealthCheckService` reads the property `forward.urls` (injected via `@Value("${forward.urls}")`) and runs a scheduled health check every 10000 ms (10 seconds) using a virtual-thread-per-task executor. A server is considered healthy when a GET to its URL returns HTTP 200.
- Failure handling: when forwarding fails for a backend, `ForwardService` calls `HealthCheckService.removeServer(...)`. If no healthy servers exist or all attempts fail, a runtime `BadGatewayException` (defined in `src/main/java/org/example/loadbalancer/Exception/BadGatewayException.java`) is thrown.

Configuration (exact keys used in code)
- forward.urls — defined in code as a String[] via `@Value("${forward.urls}")`. Example invocation:
  ./gradlew bootRun --args='--forward.urls=http://localhost:8081,http://localhost:8082'
  or
  java -jar build/libs/<artifact>.jar --forward.urls=http://localhost:8081,http://localhost:8082

Build and run
- Build: `./gradlew clean build`
- Run with Gradle: `./gradlew bootRun`
- Run JAR (after build): `java -jar build/libs/<artifact>.jar`
- Tests: `./gradlew test` (there is a basic Spring context test at `src/test/java/org/example/loadbalancer/LoadBalancerApplicationTests.java`).

Notes on dependencies (from build.gradle)
- spring-boot-starter-webmvc — application runs on Spring Web MVC.
- springdoc-openapi-starter-webmvc-ui — OpenAPI / Swagger UI dependency is present in the build file.

Developer details (precise)
- Main classes:
  - `org.example.loadbalancer.LoadBalancerApplication` — Spring Boot entry point
  - `org.example.loadbalancer.controllers.MainController` — GET "/" handler
  - `org.example.loadbalancer.services.ForwardService` — forwarding logic
  - `org.example.loadbalancer.services.HealthCheckService` — health check logic
  - `org.example.loadbalancer.Exception.BadGatewayException` — runtime exception used on failures
- Health check schedule: `@Scheduled(fixedRate = 10000)` (10 seconds)
- Forwarding strategy implemented: simple rotating index (round-robin style) using `AtomicInteger` in `ForwardService`.
