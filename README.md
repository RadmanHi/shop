# 🛒 Shop API

A backend service simulating a real-world e-commerce cart system, built with Spring Boot and PostgreSQL.

> ### 🔗 Full source code available on the [`develop`](https://github.com/RadmanHi/shop/tree/develop) branch

---

## ⚡ Quick Start

**Prerequisites:** Docker and Docker Compose.

```bash
git clone -b develop https://github.com/RadmanHi/shop.git
cd shop
docker compose up --build
```

| | |
|---|---|
| 🌐 API | http://localhost:8080 |
| 📖 Swagger | http://localhost:8080/swagger-ui/index.html |
| ❤️ Health | http://localhost:8080/actuator/health |

> 🌱 Runs with the `dev` profile by default — sample products and a test user are seeded automatically.
> Use `X-User-Id: user-1` in any cart request to get started immediately.

---

## ✏️ Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 4.0.6 |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Migrations | Flyway |
| Mapping | MapStruct |
| Docs | SpringDoc OpenAPI |
| Testing | JUnit 5, Testcontainers, Mockito |

---

## ⚙️ Configuration

All values have sensible defaults for local development.

| Property | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5431/shop` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `shop` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `password` | Database password |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active profile |

---

## 🗺️ API Overview

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | List all products (paginated, max size 250) |
| GET | `/api/v1/products/{id}` | Get a single product |

### Cart
> All cart endpoints require an `X-User-Id` header. Use `user-1` locally.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/carts` | Get current cart |
| POST | `/api/v1/carts/items` | Add item |
| PATCH | `/api/v1/carts/items/{productId}` | Update quantity |
| DELETE | `/api/v1/carts/items/{productId}` | Remove item |
| POST | `/api/v1/carts/checkout` | Initiate checkout |
| POST | `/api/v1/carts/payment-result` | Submit payment result ⚠️ internal use only |

---

## 🔄 Checkout Flow

```
Add Items → Initiate Checkout → [Payment Service — out of scope] → PURCHASED / CANCELLED / TIMEOUT
```

> Payment integration is intentionally out of scope. `/payment-result` is designed to receive callbacks from an external payment service.

| # | What happens | Why it matters |
|---|---|---|
| 🔒 | Prices are snapshotted at checkout | Cart prices are frozen at the moment of checkout — admin price changes don't affect an in-progress session |
| 📦 | Stock is reserved at checkout | No two users can claim the same last item |
| 🔁 | Cancellation or timeout releases stock | Nothing stays reserved indefinitely |
| 🛡️ | Payment results honored even on state mismatch | Stock never leaks due to unexpected edge cases |
| ⏱️ | Expired checkouts self-recover | Users are never permanently stuck — a background job runs every minute to clean up, with inline recovery as a safety net |
| 🔀 | Concurrent requests are safe | Multiple requests hitting the same cart or stock simultaneously are handled gracefully — no data corruption, no overselling |

---

## 🚨 Error Handling

All errors — validation, business rules, or unexpected failures — return the same consistent response shape. No surprises for the client.

---

## 🧪 Tests

Tests run automatically as part of the build. A real PostgreSQL instance is spun up via Testcontainers — no manual setup needed.

```bash
mvn clean install
```
