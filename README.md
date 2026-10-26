# E-Library

A REST backend for a small library. Patrons can browse the catalogue, inspect a book's
details, borrow and return copies, and see everything they currently have on loan.

Authentication is intentionally **not** part of this service: an API gateway
authenticates callers and injects the trusted `X-User-Id` header, which the service
consumes as the patron identity (see [Assumptions & Trade-offs](#assumptions--trade-offs)).

## Features

1. **Browse books** — paginated list with optional free-text search over title, author,
   and category.
2. **Book details** — full record for a single book, including copy availability.
3. **Borrow** — take a copy of a book; rejects unavailable copies and duplicate active
   loans, and survives concurrent borrows of the last copy.
4. **Return** — give a copy back; ownership-protected so patrons cannot touch each
   other's loans.
5. **Current loans** — the caller's active loans, soonest due date first, with overdue
   loans flagged.

## Tech Stack

- **Java 21**, **Spring Boot 4.1**, **Spring Data JPA** (Hibernate), **Spring Security**
  (permit-all; the gateway owns authentication)
- **H2** in-memory database + **Flyway** migrations (validate mode — entities must match
  the schema)
- **Lombok**, **Bean Validation**
- **JUnit 5**, **Mockito**, **MockMvc** — 42 tests: unit, slice (`@DataJpaTest`,
  `@WebMvcTest`), and end-to-end

## Quick Start

Prerequisites: **JDK 21**. No other setup — Maven and the database are included
(the Maven wrapper downloads everything it needs).

```bash
./mvnw spring-boot:run
```

The API is served at `http://localhost:8080/api/v1`. To inspect the data, open the
H2 console at `http://localhost:8080/h2-console` with:

- JDBC URL: `jdbc:h2:mem:elibrary`
- User name: `sa` (empty password)

Flyway migrates and seeds the database on startup: **3 patrons** (ids 1–3: Alice Chen,
Bob Wang, Carol Lin) and **10 books** (book 1 *Clean Code* has 2 copies; book 3
*Designing Data-Intensive Applications* has 1).

## API Reference

All endpoints live under `/api/v1`. Every mutating or patron-scoped endpoint requires
the `X-User-Id` header, injected by the gateway after authentication.

| # | Method & path | Description | Auth |
|---|---------------|-------------|------|
| 1 | `GET /books` | Browse/search books (paginated) | — |
| 2 | `GET /books/{id}` | Book details | — |
| 3 | `POST /loans` | Borrow a copy | `X-User-Id` |
| 4 | `POST /loans/{loanId}/return` | Return a loan | `X-User-Id` |
| 5 | `GET /me/loans` | Current loans of the caller | `X-User-Id` |

### 1. Browse books

Query params: `q` (optional free-text, matched case-insensitively against title,
author, category), `page` (0-based, default 0), `size` (default 20, max 100).

```bash
curl 'http://localhost:8080/api/v1/books?q=java&page=0&size=5'
```

```json
{
  "content": [
    {
      "id": 6,
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "category": "Java",
      "availableCopies": 2
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 2,
  "totalPages": 1
}
```

### 2. Book details

```bash
curl 'http://localhost:8080/api/v1/books/1'
```

```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "publisher": "Prentice Hall",
  "publishedDate": "2008-08-11",
  "category": "Software Engineering",
  "description": "A handbook of agile software craftsmanship.",
  "totalCopies": 2,
  "availableCopies": 2
}
```

### 3. Borrow a copy

```bash
curl -X POST 'http://localhost:8080/api/v1/loans' \
  -H 'Content-Type: application/json' \
  -H 'X-User-Id: 1' \
  -d '{"bookId": 1}'
```

`201 Created`:

```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "patronId": 1,
  "borrowedAt": "2026-10-26T10:00:00Z",
  "dueAt": "2026-11-09T10:00:00Z",
  "returnedAt": null,
  "status": "ACTIVE"
}
```

### 4. Return a loan

```bash
curl -X POST 'http://localhost:8080/api/v1/loans/1/return' \
  -H 'X-User-Id: 1'
```

`200 OK` — the same loan shape with `returnedAt` set and `status: "RETURNED"`.

### 5. Current loans

```bash
curl 'http://localhost:8080/api/v1/me/loans' \
  -H 'X-User-Id: 1'
```

`200 OK` — a JSON array of loans (`status` is `ACTIVE` or `OVERDUE`), ordered by due
date ascending.

## Error Format

Every failure is an [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
response (`application/problem+json`) with a machine-readable `code`:

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Book not found",
  "code": "BOOK_NOT_FOUND"
}
```

| HTTP | `code` | When |
|------|--------|------|
| 400 | `USER_ID_MISSING` | `X-User-Id` header absent or not a number on a protected endpoint |
| 400 | `VALIDATION_FAILED` | Bean Validation, malformed JSON body, or a non-numeric path variable (detail is always English) |
| 404 | `BOOK_NOT_FOUND` | Unknown book id |
| 404 | `PATRON_NOT_FOUND` | Unknown patron id |
| 404 | `LOAN_NOT_FOUND` | Unknown loan id — **or** the loan belongs to another patron (ownership is never revealed) |
| 404 | `NOT_FOUND` | Unknown route |
| 409 | `NO_COPIES_AVAILABLE` | All copies are on loan |
| 409 | `BOOK_ALREADY_BORROWED` | The caller already has an active loan of this book |
| 409 | `LOAN_ALREADY_RETURNED` | Returning a loan that was already returned |
| 409 | `CONCURRENT_UPDATE` | Two requests raced on the last copy; retry |
| 405 | `METHOD_NOT_ALLOWED` | HTTP method not supported by the endpoint |
| 415 | `MEDIA_TYPE_NOT_SUPPORTED` | Request `Content-Type` not supported |
| 500 | `INTERNAL_ERROR` | Anything unexpected |

## Assumptions & Trade-offs

- **Authentication lives at the gateway.** The service trusts `X-User-Id` after the
  gateway authenticates the caller; there is no login or session here. This keeps the
  service small and composable, at the cost of relying on the gateway to strip or set
  the header correctly.
- **Concurrency is handled with optimistic locking.** Borrowing the last copy races:
  two requests can both pass the availability check. `@Version` on `Book` makes the
  loser's commit fail, which surfaces as `409 CONCURRENT_UPDATE` instead of silently
  overselling the copy. Simpler and safer than pessimistic row locks for a
  read-mostly catalogue, and the conflict window is tiny.
- **Loan status is derived, never stored.** `ACTIVE`/`OVERDUE`/`RETURNED` is computed
  from `returnedAt` and `dueAt` against the current time, so there is no denormalized
  state to drift out of sync and no batch job to flip `OVERDUE`.
- **Loan period is configuration.** `elibrary.loan-period-days=14` in
  `application.properties`, injectable and testable via a `Clock` bean.
- **No fines, reservations, or renewals.** The assignment's scope is
  browse/detail/borrow/return/current loans; those features are deliberately absent.
- **Duplicate-borrow check is service-level, not a DB constraint.** A unique index on
  `(patron_id, book_id) WHERE returned_at IS NULL` would need per-database partial-index
  syntax, complicates H2→PostgreSQL portability, and cannot express "borrow again after
  return" portably either. The service check is atomic within the borrow transaction;
  the residual race (same patron, two simultaneous borrows) is a rare double-borrow,
  accepted here — a DB-level guard could be added per target database if required.
- **H2 + Flyway for zero-setup delivery; PostgreSQL is a swap away.** Hibernate
  validates entities against the Flyway-managed schema (`ddl-auto=validate`), and the
  migrations use portable SQL (the only H2-specific bit is in-memory mode). To move to
  PostgreSQL: add the `postgresql` driver, point `spring.datasource.*` at the database,
  and restart — Flyway runs the same migrations.

## Project Structure

```
src/main/java/com/dmx/elibrary/
├── ELibraryApplication.java
├── config/          # SecurityConfig (permit-all), AppConfig (Clock bean)
├── controller/      # BookController, LoanController (REST + X-User-Id parsing)
├── domain/          # Book, Patron, Loan, LoanStatus (entities + domain rules)
├── dto/             # Request/response records, decoupled from entities
├── exception/       # ErrorCode, BusinessException, GlobalExceptionHandler (RFC 9457)
├── repository/      # Spring Data JPA repositories
└── service/         # BookService, LoanService (+ impl/ implementations)
src/main/resources/
├── application.properties
└── db/migration/    # V1__create_tables.sql, V2__seed_data.sql
src/test/java/com/dmx/elibrary/
├── ELibraryEndToEndTest.java          # full-stack smoke tests (Flyway + security + API)
├── controller/                        # @WebMvcTest slices
├── service/impl/                      # Mockito unit tests
├── repository/                        # @DataJpaTest slices (incl. optimistic-lock race)
├── exception/                         # handler unit tests
└── PersistenceSetupTest.java          # migration + seed sanity check
```

## Testing

```bash
./mvnw test
```

42 tests across five layers. Highlights:

- **`BookRepositoryTest`** reproduces a real concurrent borrow of the last copy on two
  entity managers and proves the optimistic lock fires.
- **`LoanServiceImplTest`** covers borrow/return rules (availability, duplicates,
  already-returned, cross-patron access) against a mutable `Clock`, including the
  overdue-status derivation.
- **`ELibraryEndToEndTest`** drives the five features through the full stack — Flyway
  migrations, seed data, security config, and error handling included.
