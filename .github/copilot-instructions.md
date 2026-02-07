Repository: nom035-backend (Spring Boot, Java 21, Maven)

Purpose
- Backend service for NOM-035 compliance: surveys, scoring, reporting (PDFs), MySQL persistence.

Primary languages & frameworks
- Java 21, Spring Boot, Maven
- SQL / MySQL
- Docker & Docker Compose

What I want from GitHub Copilot (instructions)
- Keep changes small and focused. Prefer adding new classes/services over large refactors unless required.
- Follow existing code conventions (Java idioms, package layout under src/main/java, use Lombok only where already used).
- Maintain compatibility with Java 21 and Maven build lifecycle (mvn clean install must succeed).
- Do not hardcode secrets or credentials. Use environment variables or application.properties profiles.
- Prefer using existing utility classes, services and DTOs. Reuse REST patterns already present in controllers.
- When adding endpoints, include proper validation, request/response DTOs, and unit tests when practical.
- When changing database schema, add migration scripts (prefer src/main/resources/schema.sql or explain why a migration tool is needed).
- For PDF/report changes, preserve the current branding query parameters and accept optional overrides as described in README.

Testing and verification
- Run mvn -DskipTests=false test when adding functionality. New code should include unit tests for core logic.
- When adding integration work, prefer lightweight tests using H2 profiles (application-h2.properties exists in resources).

Commit message style
- Use short imperative verb phrase: "Add X", "Fix Y", "Refactor Z". Mention related issue/PR if applicable.

When unsure, ask for context
- If a change touches business rules (scoring, legal text, dictamen logic), stop and request clarification.

Files & locations to inspect first
- src/main/java: backend code (controllers, services, repositories)
- src/main/resources: data.sql, schema.sql, application-*.properties, branding/
- Dockerfile, docker-compose.yml, wait-for-db.sh

Unsafe actions (do not perform)
- Do not commit credentials or .env files with real secrets.
- Do not change project Java target or major build tooling without explicit instruction.

If making edits
- Keep changes localized and include tests. Update README or add short comments in code when behavior is not obvious.

Thanks—make conservative, test-backed improvements that preserve existing behavior unless the user asks for breaking changes.