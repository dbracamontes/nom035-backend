# NOM035 Backend

Spring Boot backend for NOM-035 compliance, with MySQL, dynamic survey generation, advanced scoring, and reporting.

## Prerequisites

- Docker & Docker Compose

## Quick Start

1. **Clone the repository**

   ```bash
   git clone https://github.com/dbracamontes/nom035-backend.git
   cd nom035-backend
   ```

2. **Build and Run with Docker Compose**

   ```bash
   docker-compose up --build
   ```

   This will:
   - Start MySQL with the database `nom035` and root password `your_mysql_password`.
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

## Customization

- To change DB credentials, update:
  - `docker-compose.yml` (env vars)
  - `src/main/resources/application.properties`
- To add surveys/questions, use `/api/surveys/generate` endpoint.

## Stopping the stack

```bash
docker-compose down
```

## Troubleshooting

- If MySQL is not ready when app starts, restart the app container:
  ```bash
  docker-compose restart app
  ```

---

**Enjoy your NOM-035 backend!**