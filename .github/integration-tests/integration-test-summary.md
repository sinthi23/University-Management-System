# Integration Test Summary

## Tests added

- `src/test/java/com/example/demo/integration/UserManagementL1Test.java`
  - Admin creates a student through HTTP and verifies the returned resource, persisted record, and encoded password.
  - Teacher reads student records through the secured HTTP endpoint and database.
  - Duplicate email returns HTTP 409 and does not persist another user.
- `src/test/java/com/example/demo/service/UserServiceTest.java`
  - Six unit tests cover role-filtered reads, role mismatch, normalized creation and password encoding, required-field validation, profile update/password preservation, and deletion.

## Test setup

Integration tests use the application's Spring Boot configuration and an isolated in-memory H2 database. They do not require a separate service or Docker.

## Execution

- Full suite: `.\mvnw.cmd verify`
- Layer 1: `.\mvnw.cmd verify -Dgroups=Layer1`
- Windows runner: `.\.github\integration-tests\run-layer1-tests.ps1`
- Unix runner: `bash .github/integration-tests/run-layer1-tests.sh`

## Results

- Full suite: **9 passed, 0 failed** using `.\mvnw.cmd clean verify "-Djava.version=23"`.
- Layer 1 only: **3 passed, 0 failed** using `.\mvnw.cmd "-Djava.version=23" "-Dgroups=Layer1" verify`.
- The temporary Java version override was needed because this environment has JDK 23 while the project targets Java 25. The project configuration was not changed; rerun without the override on JDK 25.
- No production-code changes were needed.
