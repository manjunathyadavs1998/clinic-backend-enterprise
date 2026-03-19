
# Clinic Backend Enterprise — CLAUDE.md

## Project Overview

Spring Boot 3.2.5 / Java 17 REST API for clinic management with JWT authentication, role-based access control, and full CRUD operations across five domains.

- **Base URL:** `http://localhost:8080/api/v1`
- **Database:** MySQL `localhost:3306/clinic`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## High-Level Architecture

```
┌─────────────────────────────────────────────┐
│            Frontend Client                  │
│         http://localhost:3000               │
└──────────────────┬──────────────────────────┘
                   │  HTTP + JWT Bearer Token
                   │  CORS: localhost:3000, 127.0.0.1:3000
                   │        15.207.107.248:3000
┌──────────────────▼──────────────────────────┐
│         Spring Boot Application             │
│               Port 8080                     │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │         Security Layer                │  │
│  │  CORS → JWT Filter → Authorization   │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │         Controller Layer              │  │
│  │  Auth | Doctor | Consultation        │  │
│  │  Billing | LabTest | User            │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │         Service Layer                 │  │
│  │  Business logic + validation          │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │         Repository Layer              │  │
│  │  Spring Data JPA (8 repositories)    │  │
│  └───────────────────────────────────────┘  │
└──────────────────┬──────────────────────────┘
                   │  JDBC / Hibernate
┌──────────────────▼──────────────────────────┐
│           MySQL Database                    │
│         localhost:3306/clinic               │
│                                             │
│  users · roles · user_roles                 │
│  doctors · consultations                    │
│  consultation_tests · lab_tests             │
│  billing · refresh_tokens                   │
└─────────────────────────────────────────────┘
```

### Request Lifecycle

```
Client Request
  │
  ├─► CorsFilter          — validates Origin header
  ├─► JwtAuthFilter       — extracts & validates Bearer token
  │     ├─ Blacklist check (TokenBlacklistService)
  │     ├─ Signature & expiry validation (JwtService)
  │     └─ Sets SecurityContext authentication
  ├─► AuthorizationFilter — checks roles via @PreAuthorize
  ├─► Controller          — maps HTTP → service call
  ├─► Service             — business logic, throws custom exceptions
  ├─► Repository          — Spring Data JPA → SQL
  └─► GlobalExceptionHandler — maps exceptions → HTTP error response
```

---

## Low-Level Architecture

### Package Structure

```
com.clinic.app
├── config/
│   ├── CorsConfig.java              # CorsConfigurationSource bean
│   ├── SecurityConfig.java          # SecurityFilterChain, PasswordEncoder, AuthenticationManager
│   ├── OpenApiConfig.java           # Swagger / OpenAPI 3.0 setup
│   └── DataSeeder.java              # CommandLineRunner — seeds roles, users, doctors, lab tests
│
├── controller/
│   ├── AuthController.java          # /api/v1/auth
│   ├── DoctorController.java        # /api/v1/doctors
│   ├── ConsultationController.java  # /api/v1/consultations
│   ├── BillingController.java       # /api/v1/billing
│   ├── LabTestController.java       # /api/v1/lab-tests
│   └── UserController.java          # /api/v1/users
│
├── dto/
│   ├── auth/        LoginRequest, AuthResponse, RefreshTokenRequest, LogoutRequest
│   ├── billing/     BillResponse, UpdatePaymentRequest
│   ├── consultation/ CreateConsultationRequest, ConsultationResponse,
│   │                UpdateConsultationStatusRequest, UpdateDoctorNotesRequest,
│   │                AddConsultationTestsRequest
│   ├── doctor/      CreateDoctorRequest, DoctorResponse, UpdateDoctorAvailabilityRequest
│   ├── labtest/     CreateLabTestRequest, LabTestResponse, UpdateLabTestRequest
│   ├── user/        UserResponse, UpdateUserRequest, ChangePasswordRequest
│   └── common/      ApiErrorResponse
│
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Doctor.java
│   ├── Consultation.java
│   ├── ConsultationTest.java
│   ├── LabTest.java
│   ├── Billing.java
│   └── RefreshToken.java
│
├── enums/
│   ├── RoleName.java                # ROLE_ADMIN, ROLE_DOCTOR, ROLE_RECEPTIONIST
│   ├── ConsultationStatus.java      # SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
│   ├── ConsultationTestStatus.java  # ORDERED, IN_PROGRESS, COMPLETED, CANCELLED
│   ├── PaymentStatus.java           # PENDING, PARTIALLY_PAID, PAID, CANCELLED
│   └── PaymentMode.java             # CASH, CARD, UPI
│
├── exception/
│   ├── BadRequestException.java         # 400
│   ├── ResourceNotFoundException.java   # 404
│   ├── UnauthorizedOperationException.java # 403
│   └── GlobalExceptionHandler.java      # @RestControllerAdvice
│
├── repository/
│   ├── UserRepository               findByUsername
│   ├── RoleRepository               findByName
│   ├── DoctorRepository             findByUser, findByAvailableTrue
│   ├── ConsultationRepository       findByDoctor, findByStatus, findByScheduledAtBetween
│   ├── ConsultationTestRepository   findByConsultation
│   ├── LabTestRepository            (default CRUD)
│   ├── BillingRepository            findByConsultation
│   └── RefreshTokenRepository       findByToken, findByUserAndRevokedFalse
│
├── security/
│   ├── JwtService.java              # generate/validate/extract tokens (HS256)
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│   ├── CustomUserDetailsService.java # UserDetailsService impl
│   ├── CustomUserDetails.java        # UserDetails impl wrapping User entity
│   └── TokenBlacklistService.java   # In-memory logout blacklist
│
└── service/
    ├── AuthService.java  +  impl/AuthServiceImpl.java
    ├── BillingService.java  +  impl/BillingServiceImpl.java
    ├── ConsultationService.java  +  impl/ConsultationServiceImpl.java
    ├── DoctorService.java  +  impl/DoctorServiceImpl.java
    ├── LabTestService.java  +  impl/LabTestServiceImpl.java
    └── UserService.java  +  impl/UserServiceImpl.java
```

---

### Database Schema

```
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│    users     │  M:M   │  user_roles  │  M:M   │    roles     │
├──────────────┤◄──────►├──────────────┤◄──────►├──────────────┤
│ id (PK)      │        │ user_id (FK) │        │ id (PK)      │
│ username     │        │ role_id (FK) │        │ name (ENUM)  │
│ password     │        └──────────────┘        │ description  │
│ fullName     │                                 └──────────────┘
│ phone        │
│ email        │  1:1   ┌──────────────┐
│ active       │◄──────►│   doctors    │
│ createdAt    │        ├──────────────┤
└──────────────┘        │ id (PK)      │
                        │ user_id (FK) │  1:N   ┌─────────────────────┐
                        │ specialization│◄──────►│   consultations     │
                        │ consultationFee│       ├─────────────────────┤
                        │ available    │        │ id (PK)             │
                        │ roomNumber   │        │ patientName         │
                        │ createdAt    │        │ patientPhone        │
                        └──────────────┘        │ symptoms            │
                                                │ doctor_id (FK)      │
                        ┌──────────────┐        │ consultationFee     │
                        │  lab_tests   │  N:M   │ scheduledAt         │
                        ├──────────────┤        │ status (ENUM)       │
                        │ id (PK)      │◄──────►│ doctorNotes         │
                        │ name         │        │ created_by (FK)     │
                        │ cost         │        │ createdAt/updatedAt │
                        │ active       │        └──────────┬──────────┘
                        └──────────────┘                   │ 1:N
                               ▲                           ▼
                               │            ┌──────────────────────┐
                               │            │  consultation_tests   │
                               │            ├──────────────────────┤
                               └────────────│ id (PK)              │
                                            │ consultation_id (FK) │
                                            │ lab_test_id (FK)     │
                                            │ status (ENUM)        │
                                            │ remarks              │
                                            │ createdAt            │
                                            └──────────────────────┘

┌──────────────────┐  1:1   ┌──────────────┐
│   consultations  │◄──────►│   billing    │
│   (see above)    │        ├──────────────┤
└──────────────────┘        │ id (PK)      │
                            │ consultation_id (FK, UNIQUE) │
                            │ consultationFee │
                            │ testTotal    │
                            │ totalAmount  │
                            │ paymentStatus (ENUM) │
                            │ paymentMode (ENUM)   │
                            │ generatedAt  │
                            │ updatedAt    │
                            └──────────────┘

┌──────────────┐  1:N   ┌──────────────────┐
│    users     │◄──────►│  refresh_tokens  │
└──────────────┘        ├──────────────────┤
                        │ id (PK)          │
                        │ token (UNIQUE)   │
                        │ user_id (FK)     │
                        │ expiryDate       │
                        │ revoked          │
                        │ createdAt        │
                        └──────────────────┘
```

---

### API Endpoints

#### Auth — `/api/v1/auth` (public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/login` | Authenticate, returns access + refresh tokens |
| POST | `/refresh` | Exchange refresh token for new access token |
| POST | `/logout` | Revoke tokens |

#### Doctors — `/api/v1/doctors`
| Method | Path | Roles |
|--------|------|-------|
| GET | `/` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/available` | ADMIN, RECEPTIONIST |
| GET | `/{id}` | ADMIN, DOCTOR, RECEPTIONIST |
| POST | `/` | ADMIN |
| PATCH | `/{id}/availability` | ADMIN, DOCTOR |
| DELETE | `/{id}` | ADMIN |

#### Consultations — `/api/v1/consultations`
| Method | Path | Roles |
|--------|------|-------|
| POST | `/` | ADMIN, RECEPTIONIST |
| GET | `/` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/{id}` | ADMIN, DOCTOR, RECEPTIONIST |
| PATCH | `/{id}/status` | ADMIN, DOCTOR, RECEPTIONIST |
| PATCH | `/{id}/doctor-notes` | ADMIN, DOCTOR |
| POST | `/{id}/tests` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/{id}/tests` | ADMIN, DOCTOR, RECEPTIONIST |
| DELETE | `/{id}` | ADMIN |

#### Billing — `/api/v1/billing`
| Method | Path | Roles |
|--------|------|-------|
| POST | `/consultations/{consultationId}/generate` | ADMIN, RECEPTIONIST |
| GET | `/` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/{billId}` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/consultations/{consultationId}` | ADMIN, DOCTOR, RECEPTIONIST |
| PATCH | `/{billId}/payment` | ADMIN, RECEPTIONIST |
| DELETE | `/{billId}` | ADMIN |

#### Lab Tests — `/api/v1/lab-tests`
| Method | Path | Roles |
|--------|------|-------|
| GET | `/` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/{id}` | ADMIN, DOCTOR, RECEPTIONIST |
| POST | `/` | ADMIN |
| PATCH | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

#### Users — `/api/v1/users`
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/me` | ALL | Get logged-in user's profile |
| GET | `/` | ADMIN | List all users |
| GET | `/{id}` | ADMIN | Get user by id |
| PATCH | `/{id}` | ADMIN | Update fullName, phone, email |
| PATCH | `/{id}/password` | ADMIN | Change password (min 6 chars) |
| PATCH | `/{id}/toggle-active` | ADMIN | Enable / disable user account |
| DELETE | `/{id}` | ADMIN | Delete user (blocked if doctor profile exists) |

---

### JWT Authentication Flow

```
POST /api/v1/auth/login
  │  { username, password }
  ▼
AuthService.login()
  ├─ AuthenticationManager.authenticate()
  ├─ JwtService.generateAccessToken()   → 24h HS256 JWT
  ├─ JwtService.generateRefreshToken()  → 7d  HS256 JWT
  └─ Persist RefreshToken entity in DB
  │
  └─► { accessToken, refreshToken }

Subsequent requests:
  Authorization: Bearer <accessToken>
  │
  ▼
JwtAuthenticationFilter
  ├─ TokenBlacklistService.isBlacklisted(token) → 401 if true
  ├─ JwtService.isTokenValid(token)             → 401 if false
  ├─ JwtService.extractUsername(token)
  ├─ CustomUserDetailsService.loadUserByUsername()
  └─ SecurityContextHolder.setAuthentication()

POST /api/v1/auth/logout
  ├─ TokenBlacklistService.blacklist(accessToken)
  └─ RefreshToken.revoked = true (DB)
```

---

### Role-Based Access Control

```
ROLE_ADMIN        — full access to all endpoints including DELETE
ROLE_DOCTOR       — read consultations, update own notes/status, update availability
ROLE_RECEPTIONIST — create consultations, read data, generate & update billing
```

Doctor ownership validation: when a DOCTOR updates consultation status or notes,
`ConsultationServiceImpl` verifies the authenticated user's doctor profile matches
the consultation's assigned doctor.

---

### Default Seed Data

| Username | Password | Role |
|----------|----------|------|
| admin | admin@123 | ADMIN |
| doctor1 | doctor@123 | DOCTOR |
| doctor2 | doctor@123 | DOCTOR |
| doctor3 | doctor@123 | DOCTOR |
| staff | staff@123 | RECEPTIONIST |

---

### Key Configuration (`application.yml`)

```yaml
server.port: 8080
spring.datasource.url: jdbc:mysql://localhost:3306/clinic
spring.jpa.hibernate.ddl-auto: update
app.jwt.access-token-expiration-ms: 86400000    # 24h
app.jwt.refresh-token-expiration-ms: 604800000  # 7 days
```

---

### Clinic Workflow

```
1. RECEPTIONIST creates Consultation (status: SCHEDULED)
        │
2. DOCTOR updates status → IN_PROGRESS
   DOCTOR adds notes
   DOCTOR prescribes Lab Tests → ConsultationTest records (status: ORDERED)
        │
3. Status updated → COMPLETED
        │
4. RECEPTIONIST generates Bill
   (consultationFee + sum of lab test costs = totalAmount)
        │
5. RECEPTIONIST updates payment (CASH/CARD/UPI → status: PAID)
```
