# Clinic Backend - Spring Boot 3 / Java 17

Production-style backend for a small clinic workflow:

Patient Visit -> Consultation Created -> Doctor Consultation -> Optional Lab Tests -> Bill Generated

## Features

- JWT authentication
- Refresh tokens
- Logout with access-token revocation (in-memory blacklist for demo)
- Role-based access control
- Method-level security with `@PreAuthorize`
- BCrypt password hashing
- Spring Data JPA + MySQL
- OpenAPI / Swagger
- Seed data on startup

## Roles

- `ADMIN` - full access
- `DOCTOR` - read doctor data, read all consultations, update own consultation notes/status, add tests for own consultations
- `RECEPTIONIST` - create consultations, read doctors/tests/consultations/billing, generate bill, update payment

## Main Modules

- Auth
- Users / Roles
- Doctors
- Consultations
- Lab Tests
- Billing

## Default Seed Users

These are created automatically on startup if the database is empty:

- `admin / admin123`
- `doctor1 / doctor123`
- `reception1 / recep123`

## Run

1. Create database:
   ```sql
   CREATE DATABASE clinic;
   ```

2. Update DB credentials in `src/main/resources/application.yml`

3. Run:
   ```bash
   mvn clean spring-boot:run
   ```

## Swagger

- `http://localhost:8080/swagger-ui/index.html`

## Important Notes

- Logout blacklist is implemented in-memory for simplicity. For production, move it to Redis or DB.
- Refresh tokens are stored in DB.
- For the current requirements, patient data is stored inside `consultations` instead of a separate `patients` table.
- This project is intentionally compact but structured like a real enterprise backend.

## Main API Summary

### Auth
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### Doctors
- `GET /api/v1/doctors`
- `GET /api/v1/doctors/available`
- `POST /api/v1/doctors`
- `PATCH /api/v1/doctors/{id}/availability`

### Consultations
- `POST /api/v1/consultations`
- `GET /api/v1/consultations`
- `GET /api/v1/consultations/{id}`
- `PATCH /api/v1/consultations/{id}/status`
- `PATCH /api/v1/consultations/{id}/doctor-notes`
- `POST /api/v1/consultations/{id}/tests`
- `GET /api/v1/consultations/{id}/tests`

### Lab Tests
- `GET /api/v1/lab-tests`
- `POST /api/v1/lab-tests`

### Billing
- `POST /api/v1/billing/consultations/{consultationId}/generate`
- `GET /api/v1/billing/consultations/{consultationId}`
- `PATCH /api/v1/billing/{billId}/payment`
