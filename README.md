# Authentication Service Backend

Spring Boot REST API for authentication and tenant-scoped user, role, and permission management. Each tenant has its own users and roles. The `SYSTEM` tenant is the platform scope used by the `SUDO_ADMIN` account to manage tenants and platform administration.

## Stack

- Java 21 and Spring Boot
- Gradle (wrapper included)
- MongoDB
- Spring Security, JWT, and optional OAuth2 login
- Swagger UI / OpenAPI

## Run locally

1. Install a Java 21 JDK and make a MongoDB database available.
2. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`.
3. Set the MongoDB connection values, `app.sudo-admin.email`, and `app.sudo-admin.password`. Choose a strong JWT secret and set `security.jwt.cookie-secure=false` for local HTTP development. Keep secrets out of version control.
4. Start the application:

   ```bash
   ./gradlew bootRun
   ```

The default local server address is `http://localhost:8091/authapp`. On first startup, the application creates the `SYSTEM` tenant, the system `SUDO_ADMIN` role, and the configured sudo-admin account. Creating a tenant seeds its `SYS_ADMIN`, `ADMIN`, and `USER` roles. Create the tenant's first SYS_ADMIN later through user management.

The properties example contains optional Google and GitHub OAuth client settings. Supply provider credentials if you intend to use those sign-in buttons. The application also reads JWT settings from the corresponding `JWT_*` environment variables when provided.

## API and tenant context

REST endpoints are under `/api/v1`. Authentication, user, role, and tenant operations are grouped under `/auth`, `/users`, `/role`, and `/tenants`. Tenant-scoped requests use the `X-G-APP-Tenant-Id` header with the tenant code; the platform administrator operates in `SYSTEM` scope. Authorization is enforced by role permissions on the backend.

OpenAPI documentation is available at:

- Swagger UI: `http://localhost:8091/authapp/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8091/authapp/api-docs`

## Configuration

Use `src/main/resources/application.properties.example` as the configuration reference. The key settings cover the server port and context path, frontend CORS and OAuth redirects, MongoDB, JWT and refresh-cookie behavior, sudo-admin bootstrap credentials, and optional OAuth providers. Replace all sample or blank values for the environment where the service runs.

## Project layout

- `modules/auth`: login, registration, logout, and token refresh
- `modules/user`: user lookup and account administration
- `modules/role`: roles, identifiers, and permissions
- `modules/tenant`: tenant registration and tenant lifecycle
- `security`: JWT, tenant context, OAuth callbacks, and authorization support
- `common`: shared API errors and exception handling
