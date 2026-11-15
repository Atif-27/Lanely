# FreightFox – Transporter Assignment on Lanes

A Spring Boot service that assigns transporters to lanes so that every lane is covered at the lowest total cost, using at most a given number of transporters.

- **Live URL:** _add after deployment_
- **Swagger UI:** `<live-url>/swagger-ui.html` (the root URL redirects there)

## Tech stack

Java 21, Spring Boot 4, Spring Data JPA, H2 (in-memory), springdoc-openapi, JUnit 5.

## Run locally

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080/swagger-ui.html. Both endpoints have prefilled Test Case 1 and Test Case 2 request bodies.

With Docker:

```bash
docker build -t freightfox .
docker run -p 8080:8080 freightfox
```

Run the tests:

```bash
./mvnw test
```

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/transporters/input` | Submit lanes and transporter quotes. Replaces any earlier submission. |
| POST | `/api/v1/transporters/assignment` | Get the optimized assignment for `{"maxTransporters": n}`. |

Every error uses the same shape, `{"status": "error", "message": "..."}`:

| Status | When |
|---|---|
| 400 | Field validation fails, duplicate ids, quotes for unknown lanes, or malformed JSON |
| 404 | An assignment is requested before any input was submitted |
| 422 | The lanes cannot all be covered within the limit (the message gives the minimum number needed) |

## How the optimizer works

1. Every combination of transporters from size 1 up to `maxTransporters` is tried.
2. For a combination, each lane goes to the cheapest quote within it. A combination that leaves any lane unquoted is skipped.
3. Plans are ranked by:
   1. lowest total cost
   2. more transporters used (only breaks cost ties)
   3. lowest transporter ids (keeps results deterministic)
4. If no combination covers every lane, the search repeats with larger limits to report the minimum number of transporters required. A lane that nobody quotes is reported directly.

The search is exact, so the result is always the true optimum. Its cost grows with the number of combinations, which is fine for the small and medium inputs in this assignment.

## Assumptions

- Each lane is assigned to exactly one transporter, and total cost is the sum of the assigned quotes.
- A transporter counts as used only if it wins at least one lane, so fewer than `maxTransporters` may be returned when more would not lower the cost.
- When two selected transporters quote the same price for a lane, the lower transporter id wins it.
- Data is kept in an in-memory H2 database. Each submission replaces the previous one, and data is lost on restart, so submit input again after the service restarts.
- Concurrent submissions run one at a time, and an assignment request always sees one complete submission.

## About the sample response in the brief

For Test Case 1 with `maxTransporters = 3`, the brief shows a total of 75000 using transporters [4, 6, 3]. That example is illustrative:

- its own lane quotes add up to 69001, not 75000
- it assigns lane 3 to transporter 2, who is not in its selected list, so it actually uses 4 transporters

This service returns the true optimum, **60139** using transporters [1, 4, 5]:

| Lane | Transporter | Quote |
|---|---|---|
| 1 | 5 | 11601 |
| 2 | 1 | 10512 |
| 3 | 4 | 14316 |
| 4 | 4 | 10678 |
| 5 | 4 | 13032 |

For Test Case 2, the result is 7500 with limit 3, 7450 with limit 4, and a 422 with limit 2 because at least 3 transporters are required.

## Project structure

```
controller   REST endpoints and the root redirect
service      Input handling, locking and transactions
validation   Cross-field input checks
optimizer    Assignment algorithm
entity       JPA entities: Lane, Transporter, LaneQuote
repository   Spring Data repositories
dto          Request and response models
exception    Custom exceptions and global error handler
config       OpenAPI / Swagger configuration
```
