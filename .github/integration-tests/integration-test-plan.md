# Integration Test Plan

## Coverage and approach

The application has a Spring MVC controller layer, `UserService`, Spring Data JPA, and PostgreSQL configured for normal runs. No tests existed before this change. The existing H2 test dependency allows the full application stack to be exercised with an isolated relational database, without requiring PostgreSQL or Docker.

The Layer 1 tests run as Spring Boot integration tests using `MockMvc`, the real security configuration, service and repository beans, and an in-memory H2 database. Unit tests separately exercise service behavior with mocked repository and password-encoder collaborators.

## Components and scenarios

| Component interaction | Scenario | Expected validation |
|---|---|---|
| Authenticated admin → student controller → service → H2 repository | Create a student | HTTP 201 and location, response role, persisted student data, encoded password |
| Authenticated teacher → student controller → service → H2 repository | List students | HTTP 200 and actual seeded student data |
| Authenticated admin → student controller → service → H2 repository | Create user with an existing email | HTTP 409 and no duplicate persisted user |

## Setup and execution

- JDK 25 is the configured project target.
- Maven Wrapper is the build runner.
- The integration class overrides the datasource to use in-memory H2 and creates a fresh schema.
- Tests use Spring Security's test support for authenticated principals.
- Run all tests: `.\mvnw.cmd verify`
- Run Layer 1 tests: `.\mvnw.cmd verify -Dgroups=Layer1`
- Cross-platform runners: `run-layer1-tests.ps1` and `run-layer1-tests.sh`.
