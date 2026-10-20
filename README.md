# Product Catalog API (Java)

Spring Boot 3, Java 21, Spring Data JPA, Bean Validation, and PostgreSQL. H2 is the default for a quick local run.

## Run locally
Requires Java 21+ and Maven.

```bash
mvn spring-boot:run
```

API docs can be explored with curl; base URL: `http://localhost:8080`.

## Run with PostgreSQL
```bash
docker compose up --build
```

## API
- `POST /api/v1/products` — create
- `GET /api/v1/products/{id}` — retrieve
- `GET /api/v1/products` — list/filter/paginate
- `PUT /api/v1/products/{id}` — full replacement
- `PATCH /api/v1/products/{id}` — partial update
- `DELETE /api/v1/products/{id}` — delete

List query parameters: `category`, `minPrice`, `maxPrice`, `q`, `page` (zero-based), `size` (1–100), `sortBy` (`id`, `name`, `price`, `createdAt`), `sortOrder` (`asc`, `desc`).

Example:
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Running Shoe","category":"shoes","price":89.99,"currency":"USD"}'

curl 'http://localhost:8080/api/v1/products?category=shoes&minPrice=50&maxPrice=150&page=0&size=20&sortBy=price&sortOrder=asc'
```

## Tests
```bash
mvn test
```

## Design decisions
- PostgreSQL is the source of truth. A few hundred thousand products is a reasonable starting scale for an indexed relational database.
- Composite index `(category, price, id)` supports the common category/price filter; `(created_at, id)` supports default listing order. Check production query plans with `EXPLAIN ANALYZE` before adding indexes.
- Page/offset pagination is straightforward initially. Switch to keyset pagination for deep pages or high write churn.
- Read-heavy scaling: add targeted Redis caching for hot product details/popular query pages only after measuring. Keep invalidation explicit after mutations.
- Production follow-up: replace `ddl-auto: update` with Flyway/Liquibase migrations; add authentication/authorization, rate limiting, metrics/tracing, optimistic locking, and PostgreSQL integration tests.
- Name substring filtering uses `LIKE`; for large-scale search or typo-tolerant relevance, add PostgreSQL trigram/full-text indexing or a dedicated search index based on requirements.
