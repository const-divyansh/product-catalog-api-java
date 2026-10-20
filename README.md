# Product Catalog Backend API

A high-throughput, production-grade Spring Boot 3 REST API for an e-commerce product catalog designed for read-heavy workloads handling hundreds of thousands of products.

Built with **Java 21**, **Spring Boot 3.4**, **Spring Data JPA**, **Hibernate**, and **PostgreSQL** (with **H2 in-memory** fallback for zero-dependency local runs).

---

## Table of Contents
1. [Architecture & SOLID Principles](#architecture--solid-principles)
2. [Folder Structure](#folder-structure)
3. [Database & Docker Credentials](#database--docker-credentials)
4. [Getting Started (Run Locally)](#getting-started-run-locally)
5. [Run with Docker Compose (PostgreSQL)](#run-with-docker-compose-postgresql)
6. [API Specification & Examples](#api-specification--examples)
7. [Running Tests](#running-tests)
8. [Design Decisions for Scale](#design-decisions-for-scale)

---

## Architecture & SOLID Principles

The project adheres to clean architecture and SOLID design principles:

- **Single Responsibility Principle (SRP)**:
  - Controllers only handle HTTP translation and validation responses.
  - Business logic is encapsulated in `ProductServiceImpl`.
  - Dynamic criteria generation is decoupled into `ProductSpecification`.
  - Global error translation is isolated in `ApiExceptionHandler`.
- **Open-Closed Principle (OCP)**:
  - Filtering specifications can be chained and extended without modifying existing service code.
- **Liskov Substitution Principle (LSP)**:
  - `ProductServiceImpl` cleanly satisfies the contract defined by the `ProductService` interface.
- **Interface Segregation Principle (ISP)**:
  - Repository interfaces cleanly separate basic CRUD (`JpaRepository`) and query specifications (`JpaSpecificationExecutor`).
- **Dependency Inversion Principle (DIP)**:
  - Controllers depend on the `ProductService` abstraction rather than concrete implementations.

---

## Folder Structure

```text
product-catalog-api-java/
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/catalog/
    │   │   ├── ProductCatalogApplication.java       # Spring Boot main entrypoint
    │   │   ├── common/
    │   │   │   └── ApiExceptionHandler.java         # Global RFC 7807 error handler
    │   │   └── product/
    │   │       ├── Product.java                     # JPA Entity with composite indexes
    │   │       ├── ProductController.java           # REST Controller
    │   │       ├── ProductRequests.java             # Request DTOs (records + validation)
    │   │       ├── ProductResponse.java             # Response DTO (immutability)
    │   │       ├── ProductRepository.java           # Spring Data JPA Repository
    │   │       ├── ProductService.java              # Service Interface (DIP)
    │   │       ├── ProductServiceImpl.java          # Service Implementation
    │   │       ├── exception/
    │   │       │   └── ProductNotFoundException.java# Domain-specific exception
    │   │       └── specification/
    │   │           └── ProductSpecification.java    # Dynamic query predicate builder
    │   └── resources/
    │       └── application.yml                      # Configuration
    └── test/
        └── java/com/example/catalog/
            ├── common/
            │   └── ApiExceptionHandlerTest.java     # Unit tests for global exceptions
            └── product/
                ├── ProductControllerTest.java       # MockMvc integration tests
                ├── ProductRepositoryTest.java       # DataJpaTest with TestEntityManager
                └── ProductServiceTest.java          # Unit tests with Mockito mocks
```

---

## Database & Docker Credentials

The PostgreSQL configuration defined in [`docker-compose.yml`](docker-compose.yml) is:

| Parameter | Value |
| :--- | :--- |
| **Host** | `localhost` (external) / `db` (internal docker network) |
| **Port** | `5432` |
| **Database Name** | `catalog` |
| **Username** | `catalog` |
| **Password** | **`catalog`** |
| **JDBC URL** | `jdbc:postgresql://localhost:5432/catalog` |

---

## Getting Started (Run Locally)

### Prerequisites
- **Java 21+** (`java -version`)
- *(Optional)* PostgreSQL (defaults to in-memory H2 if no DB is specified)

### 1. Clone & Enter Directory
```bash
cd product-catalog-api-java
```

### 2. Run the Application
Run using the included Maven Wrapper:
```bash
./mvnw spring-boot:run
```
The service will start on port `8080`.

---

## Run with Docker Compose (PostgreSQL)

To spin up the service alongside a dedicated PostgreSQL 16 container:

```bash
docker compose up --build
```

To stop containers:
```bash
docker compose down
```

---

## API Specification & Examples

### Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/products` | Create a new product |
| `GET` | `/api/v1/products/{id}` | Retrieve single product by ID |
| `GET` | `/api/v1/products` | List, filter, sort, and paginate products |
| `PUT` | `/api/v1/products/{id}` | Replace product (idempotent full update) |
| `PATCH` | `/api/v1/products/{id}` | Partial update of specific fields |
| `DELETE` | `/api/v1/products/{id}` | Delete product |

### Example cURL Commands

#### 1. Create Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Wireless Noise-Canceling Headphones",
    "description": "High-fidelity sound with 40-hour battery life",
    "category": "electronics",
    "price": 199.99,
    "currency": "USD"
  }'
```

#### 2. Get Product by ID
```bash
curl http://localhost:8080/api/v1/products/1
```

#### 3. Filter and Paginate Products
Supports filtering by `category`, `minPrice`, `maxPrice`, case-insensitive keyword `q`, with `page`, `size`, `sortBy`, and `sortOrder`:
```bash
curl 'http://localhost:8080/api/v1/products?category=electronics&minPrice=100&maxPrice=300&page=0&size=20&sortBy=price&sortOrder=asc'
```

#### 4. Partial Update (PATCH)
```bash
curl -X PATCH http://localhost:8080/api/v1/products/1 \
  -H 'Content-Type: application/json' \
  -d '{"price": 179.99}'
```

#### 5. Delete Product
```bash
curl -X DELETE http://localhost:8080/api/v1/products/1
```

---

## Running Tests

The test suite provides multi-layer test pyramid coverage:
- **Controller Layer** (`ProductControllerTest`): Integration tests using `@SpringBootTest` and `MockMvc` verifying HTTP status codes, request bodies, and JSON responses.
- **Service Layer** (`ProductServiceTest`): Fast, isolated unit tests using Mockito verifying business rules and input validations.
- **Repository Layer** (`ProductRepositoryTest`): Sliced integration tests using `@DataJpaTest` and `TestEntityManager` validating SQL indexes, Specifications, and database queries.
- **Exception Handler Layer** (`ApiExceptionHandlerTest`): Unit tests verifying RFC 7807 `ProblemDetail` structures.

Run all tests:
```bash
./mvnw test
```

---

## Design Decisions for Scale

1. **Datastore Choice (PostgreSQL)**:
   - For a catalog holding a few hundred thousand products, PostgreSQL provides transactional consistency (ACID), strong type safety for decimal pricing, and excellent B-Tree indexing performance that fits comfortably within standard memory buffers.
2. **Indexing Strategy**:
   - Composite Index `(category, price, id)`: Enables index scans for the most common category browse and price range filtering workloads.
   - Composite Index `(created_at, id)`: Supports fast pagination for recent additions.
   - Tied-break sorting: Queries automatically sort by `id` secondary key to ensure deterministic pagination across page boundaries.
3. **Read-Heavy Optimization**:
   - `@Transactional(readOnly = true)` applied to read paths for connection pooling efficiency.
   - Targeted caching (e.g. Redis) can easily be placed on top of `ProductService.get(id)` and popular category queries.
