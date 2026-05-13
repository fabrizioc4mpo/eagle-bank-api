# Eagle Bank API

---

#### A secure full-stack banking application built with Spring Boot (Java) and Angular, implementing a REST API for users, bank accounts, and transactions with JWT authentication.

#### The backend strictly follows the OpenAPI specification located in docs/openapi.yaml

# Tech Stack

---

### Backend
| Technology | Purpose                        |
|---|--------------------------------|
| Spring Boot | Java Backend framework         |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication       |
| PostgreSQL | Database                       |
| Spring Data JPA | ORM                            |
| Maven | Dependency management          |

### Frontend
| Technology | Purpose                       |
|---|-------------------------------|
| Angular | TypeScript Frontend framework |
| Bootstrap | CSS framework                 |

### Testing
| Technology | Purpose                        |
|---|--------------------------------|
| JUnit 5 | Unit testing                   |
| Mockito | Mocking                        |

### Tools
| Technology    | Purpose       |
|---------------|---------------|
| Postman       | API testing   |
| pgAdmin       | Database management |


# Features

---

### User Management

- Create, fetch, update, delete users
- Prevent deletion if user has associated accounts (409 Conflict)
- User registration
- JWT authentication
---
### Bank Account Management
- Create, fetch, list, delete bank accounts
- Fixed rules: 
  - Currency: GBP only
  - Account type: personal
  - Sort code: 10-10-10
----

### Transaction Management
- Deposit and withdrawal transactions
- Transaction List
- Balance rules:
  - Must never be negative
  - Max balance: 10000.00
- Insufficient funds: 422 Unprocessable Entity
----

### Authentication & Security

- JWT-based stateless authentication
- Protected endpoints require `Authorization: Bearer <token>`
- Passwords are securely hashed using BCrypt
- User/account ownership validation enforced at service layer
- Users can only access their own accounts and transactions
----

### Authorization Responses

| Status Code | Meaning |
|---|---|
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Authenticated user attempting to access another user's resource |

---------------------------

# Error handling
All errors follow OpenAPI specification:


| Code | Meaning                        |
|------|--------------------------------|
| 400  | Validation Error - Bad Request |
| 401  | Missing, Invalid token         |
| 403  | Forbidden                      |
| 404  | Resource not found             |
| 409  | Conflict(business rule)        |
| 422  | Unprocessable                  |
| 500  | Unexpected error               |
---

## Running the Project

---

## Requirements

- Java 21
- Maven
- PostgreSQL
- Node.js (for frontend if included)
---

## Clone repository

git clone <repo-url>

---

## Configure database

Create a PostgreSQL database:

CREATE DATABASE eagle_bank;

Update `application.properties`:

spring.datasource.url=jdbc:postgresql://localhost:5432/eagle_bank
spring.datasource.username=postgres
spring.datasource.password=password 
---

## Run backend
Install dependencies and run:
- ./mvnw spring-boot:run
### or
- run BackendApplication.java directly from IDE
-------------------------------------------------------
# Authentication

Authentication is implemented using JWT Bearer tokens.

---

## Login

POST /v1/auth/login

Example request:

{
"email": "user@example.com",
"password": "SecretPass123!"
}

Example response:

{
"token": "eyJhbGciOi..."
}

---

# API Endpoints


| Category | Method | Endpoint | Description | Auth |
|---|---|---|---|---|
| Auth | POST | `/v1/auth/login` | Authenticate and obtain JWT | ❌ |
| Users | POST | `/v1/users` | Create user | ❌ |
| Users | GET | `/v1/users/{userId}` | Fetch user by ID | ✅ Bearer |
| Users | PATCH | `/v1/users/{userId}` | Update user by ID | ✅ Bearer |
| Users | DELETE | `/v1/users/{userId}` | Delete user by ID | ✅ Bearer |
| Accounts | GET | `/v1/accounts` | List accounts | ✅ Bearer |
| Accounts | POST | `/v1/accounts` | Create account | ✅ Bearer |
| Accounts | GET | `/v1/accounts/{accountNumber}` | Fetch account | ✅ Bearer |
| Accounts | PATCH | `/v1/accounts/{accountNumber}` | Update account | ✅ Bearer |
| Accounts | DELETE | `/v1/accounts/{accountNumber}` | Delete account | ✅ Bearer |
| Transactions | POST | `/v1/accounts/{accountNumber}/transactions` | Create deposit/withdrawal transaction | ✅ Bearer |
| Transactions | GET | `/v1/accounts/{accountNumber}/transactions` | List account transactions | ✅ Bearer |
| Transactions | GET | `/v1/accounts/{accountNumber}/transactions/{transactionId}` | Fetch transaction by ID | ✅ Bearer |

---

## Path Parameter Validation


| Parameter | Pattern | Example |
|---|---|---|
| `accountNumber` | `^01\d{6}$` | `01234567` |
| `transactionId` | `^tan-[A-Za-z0-9]{6}$` | `tan-123abc` |
| `userId` | `^usr-[A-Za-z0-9]+$` | `usr-abc123` |

---

# Architecture

---

The project follows a layered architecture:

- ### Controller layer → handles HTTP requests and REST endpoints
- ### Service layer → business logic and ownership validation
- ### Repository layer → Spring Data JPA persistence and database access PostgreSQL
- ### DTO layer → request/response mapping between API and domain models
- ### Security layer → JWT authentication & authorization using Spring Security
- ### Frontend layer → Angular client application consuming the REST API

---

# Assumptions

---

- Only GBP currency is supported
- Only personal account type is supported
- Account numbers are randomly generated following a pattern
- Transactions are immutable once created

---

# Testing

---

- Partial Unit tests for business logic and best practices

---

# Future Improvements

---

- Refresh tokens
- Full test coverage
- Additional type of users (e.g., Premium users, Admins, etc.)
- Additional type of accounts (e.g., Joint accounts, Savings accounts, etc.)
- Assign debit card to each account
- Additional type of cards (e.g., Credit cards, Business cards, etc.)
- Additional type of transactions (e.g., Send money to another account etc.
- New functionalities: Apply for a loan, mortgage, investments, etc.)
- Improved UI/UX