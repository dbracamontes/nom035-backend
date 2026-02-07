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
   mvn clean install -DskipTests
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

## Document AI / OCR configuration

- Copia `.env.example` a `.env` y agrega tus secretos locales (no los subas al repo). Variables clave:
   - `OPENAI_API_KEY` y `OPENAI_MODEL` (p.ej. gpt-4.1-mini)
   - `OCR_PROVIDER` (`local` si usas Tesseract)
   - `DOC_AI_MAX_PAGES`, `DOC_AI_MAX_FILE_MB`, `DOC_AI_STORAGE_BASE_PATH`
- Si usas OCR local, instala Tesseract y apunta `TESSDATA_PREFIX` al directorio `tessdata`.
- En Docker ya viene Tesseract con `spa` y `TESSDATA_PREFIX=/usr/share/tessdata`; no necesitas instalarlo en el host.
- Endpoints del módulo (roles ADMIN/COMPANY):
   - `POST /api/documents/interpret` (multipart PDF)
   - `GET /api/documents/{jobId}/status`
   - `GET /api/documents/{jobId}/preview`
   - `GET /api/documents/{jobId}/download`

## PDF Reports

Two PDF endpoints are available (authentication required):

1. Individual application dictamen
```
GET /api/reports/application/{applicationId}/dictamen.pdf
Roles: ADMIN, COMPANY, EMPLOYEE
```
2. Company summary dictamen
```
GET /api/reports/company/{companyId}/dictamen-summary.pdf
Roles: ADMIN, COMPANY
```
Optional query parameters for branding (any combination):
- `title`
- `subtitle`
- `companyName`
- `footerText`
- `primaryHex` (e.g. %232196F3 for #2196F3)
- `secondaryHex`
- `logoClasspath` (classpath resource path, e.g. `/branding/logo.png` placed under `src/main/resources/branding/`)

Example curl (replace TOKEN and IDs):
```bash
curl -H "Authorization: Bearer TOKEN" \
     -H "Accept: application/pdf" \
     "http://localhost:8080/api/reports/application/42/dictamen.pdf?title=Dictamen&companyName=Mi%20Empresa&primaryHex=%232196F3" \
     --output dictamen-42.pdf
```
```bash
curl -H "Authorization: Bearer TOKEN" \
     -H "Accept: application/pdf" \
     "http://localhost:8080/api/reports/company/7/dictamen-summary.pdf?title=Resumen&companyName=Mi%20Empresa" \
     --output dictamen-summary-company-7.pdf
```
If no branding params are provided a default header/footer style is used.

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