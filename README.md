# NOM035 Backend

Spring Boot backend for NOM-035 compliance, with MySQL, dynamic survey generation, advanced scoring, and reporting.

## Prerequisites

- Docker & Docker Compose
 - Java 21 (JDK) for local builds (the project targets Java 21)

## Quick Start

1. **Clone the repository**

   ```bash
   git clone https://github.com/dbracamontes/nom035-backend.git
   cd nom035-backend
   ```

2. **Build and Run with Docker Compose**
   At project root folder run the commands below. First copy `.env.example` to `.env` and fill secrets:

   ```powershell
   copy .env.example .env
   # edit .env and set secure passwords
   mvn clean install
   docker compose up --build
   ```

   This will:
   - Start MySQL with the database defined in `.env` and the provided credentials.
   - Build and run the Spring Boot backend app on port **8080**.

3. **Access API**

   The backend will be available at:
   ```
   http://localhost:8080
   ```
   Example endpoints:
   - `/api/employees`
   - `/api/surveys`
   - `/api/surveys/generate`
   - `/api/survey-responses`
   - `/api/reports`

## Database Initialization

- On first run, the app will use `src/main/resources/data.sql` to seed the database with fake data (employees, surveys, questions, responses).

## Configuration & Secrets

- Credentials and runtime variables are provided via environment variables. For local development create a `.env` based on `.env.example` and never commit real secrets.
- The `Dockerfile` intentionally does not embed sensitive passwords. You should set:
   - `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, etc. in `.env` or use Docker secrets for production.
   - Spring datasource properties can be overridden with `SPRING_DATASOURCE_*` environment variables.

If you need a production-ready secrets approach, use Docker secrets or a secret manager (HashiCorp Vault, AWS Secrets Manager, etc.).
- To add surveys/questions, use `/api/surveys/generate` endpoint.

## Stopping the stack

```bash
docker-compose down
```

## Troubleshooting

- If MySQL is not ready when app starts, the container includes a wait-for-db script so the backend will wait for the DB to accept TCP connections before starting. If you still see problems, check logs:
   ```powershell
   docker compose logs -f
   ```

-- To restart services:
   ```powershell
   docker compose restart
   ```

---

**Enjoy your NOM-035 backend!**