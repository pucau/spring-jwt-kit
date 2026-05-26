# spring-boot-jwt-starter

A Spring Boot auto-configuration starter that provides JWT authentication and a standard API response wrapper out of the box. Add it as a Maven dependency and JWT security is wired up automatically.

---

## Features

- **JWT authentication** — generate, validate, and parse JWT tokens via `JwtService`
- **Auto-configured Spring Security** — stateless filter chain with configurable excluded paths
- **Standard API response envelope** — `ApiResponse<T>` wraps every response in a consistent JSON shape
- **Global exception handling** — maps common exceptions to HTTP status codes in the same `ApiResponse` format
- **Zero boilerplate** — drop in the dependency, set `jwt.secret-key`, done

---

## Maven dependency

```xml
<dependency>
    <groupId>com.library</groupId>
    <artifactId>spring-boot-jwt-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Install to local Maven repository:

```bash
mvn install
```

---

## Quick start

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.library</groupId>
    <artifactId>spring-boot-jwt-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure the secret key

`application.yml`:

```yaml
jwt:
  secret-key: dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2
```

The secret key must be a Base64-encoded string long enough for the HMAC-SHA algorithm (at least 32 bytes decoded).

### 3. Provide a `UserDetailsService` bean

The starter requires a `UserDetailsService` bean to load users by username during token validation.

```java
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // load from database, etc.
        return User.builder()
                .username(username)
                .password("{noop}password")
                .roles("USER")
                .build();
    }
}
```

### 4. Use `JwtService` in your auth endpoint

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(user);
        return ApiResponse.success(token, "Login successful");
    }
}
```

Clients include the token in subsequent requests:

```
Authorization: Bearer <token>
```

---

## Configuration properties

All properties are bound to the `jwt` prefix.

| Property | Type | Default | Required | Description |
|---|---|---|---|---|
| `jwt.secret-key` | `String` | — | **Yes** | Base64-encoded HMAC secret key |
| `jwt.expiration-ms` | `long` | `86400000` (24 h) | No | Token validity in milliseconds |
| `jwt.excluded-paths` | `List<String>` | `/auth/**`, `/public/**` | No | Ant-style paths that bypass JWT authentication |

### Full example

```yaml
jwt:
  secret-key: dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2
  expiration-ms: 3600000
  excluded-paths:
    - /auth/**
    - /public/**
    - /actuator/health
```

---

## ApiResponse format

Every response from a consuming application that uses `ApiResponse<T>` follows this shape:

**Success:**

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "name": "Alice"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

**Error:**

```json
{
  "success": false,
  "message": "Access denied",
  "data": null,
  "timestamp": "2024-01-01T12:00:01"
}
```

### Creating responses

```java
// success with default "OK" message
return ResponseEntity.ok(ApiResponse.success(myData));

// success with custom message
return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(myData, "User created"));

// error
return ResponseEntity.badRequest()
        .body(ApiResponse.error("Email already registered"));
```

---

## Exception mapping

The `GlobalExceptionHandler` (`@RestControllerAdvice`) catches the following exceptions automatically:

| Exception | HTTP Status | Message |
|---|---|---|
| `AccessDeniedException` | 403 Forbidden | `Access denied` |
| `BadCredentialsException` | 401 Unauthorized | `Invalid credentials` |
| `NoResourceFoundException` | 404 Not Found | `Resource not found: <path>` |
| `BadRequestException` | 400 Bad Request | Exception message |
| `IllegalArgumentException` | 400 Bad Request | Exception message |
| `Exception` (catch-all) | 500 Internal Server Error | `Internal server error` |

Throw `BadRequestException` from your own code to produce a 400 with a custom message:

```java
throw new BadRequestException("Email address is invalid");
```

---

## Overriding defaults

Every bean registered by the auto-configuration is annotated with `@ConditionalOnMissingBean`. Declare your own bean of the same type and the starter's default is skipped.

**Custom security filter chain:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                JwtAuthenticationFilter jwtFilter) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/my-public/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

---

## Project structure

```
src/
  main/
    java/
      com/library/jwtautostarter/
        config/
          JwtAutoConfiguration.java      — Spring Boot auto-configuration entry point
          JwtSecurityConfiguration.java  — SecurityFilterChain builder
        filter/
          JwtAuthenticationFilter.java   — OncePerRequestFilter for token validation
        service/
          JwtService.java                — token generation, validation, claim extraction
        response/
          ApiResponse.java               — generic response envelope
        exception/
          GlobalExceptionHandler.java    — @RestControllerAdvice for HTTP error mapping
          BadRequestException.java       — domain exception for 400 Bad Request
        properties/
          JwtProperties.java             — @ConfigurationProperties bound to jwt.*
    resources/
      META-INF/spring/
        org.springframework.boot.autoconfigure.AutoConfiguration.imports
  test/
    java/
      com/library/jwtautostarter/
        config/    — JwtAutoConfigurationTest
        filter/    — JwtAuthenticationFilterTest
        service/   — JwtServiceTest
        response/  — ApiResponseTest
        exception/ — GlobalExceptionHandlerTest
        properties/— JwtPropertiesTest
```

---

## Building

```bash
mvn clean install
```

Running tests only:

```bash
mvn test
```

---

## Requirements

- Java 17+
- Spring Boot 3.x
