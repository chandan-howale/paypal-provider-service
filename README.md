# PayPal Provider Service

A microservice that handles PayPal payment processing (order creation and capture) as part of the [payment-integration-system](https://github.com/chandan-howale/payment-integration-system).

---

## Overview

This service acts as the **PayPal integration layer** in the payment-integration-system. It provides REST APIs for creating and capturing PayPal orders, handling OAuth token management, and communicating with PayPal's REST API. It is called by the **payment-processing-service** orchestrator and registered with **Netflix Eureka** for service discovery.

### System Architecture

```
                         ┌──────────────────────────┐
                         │     Netflix Eureka        │
                         │    (Service Registry)     │
                         │                           │
                         │  paypal-provider-service  │
                         │   registers on startup    │
                         └────────────▲──────────────┘
                                      │ discover
                                      │
┌──────────────────────────┐         │         ┌──────────────────────┐
│  payment-processing-     │─────────┼────────▶│   Redis              │
│  service                 │ calls   │         │  (OAuth token cache) │
│  (Orchestrator)          │         │         └──────────────────────┘
└──────────────────────────┘         │
                                      │
                         ┌────────────┴──────────────┐
                         │  paypal-provider-service   │
                         │     (This Service)         │
                         │                            │
                         │  • Creates PayPal orders    │
                         │  • Captures PayPal orders   │
                         │  • Manages OAuth tokens     │
                         └────────────┬───────────────┘
                                      │
                                      ▼
                         ┌──────────────────────────┐
                         │   PayPal Sandbox API      │
                         │  (OAuth + Orders + URLs)  │
                         └──────────────────────────┘
```

**How it works:**
1. **paypal-provider-service** registers itself with **Eureka** on startup, making it discoverable by other microservices.
2. **payment-processing-service** discovers this service via Eureka and makes REST calls to create/capture PayPal orders.
3. **Redis** is used to cache the PayPal OAuth access token (avoids requesting a new token on every API call).
4. **PayPal Sandbox API** is the external service where actual order creation and capture happen.

### Payment Flow — Step by Step

```
Step 1: Create Order
────────────────────────────────────────────────────────────────

  payment-processing-service
         │
         │  POST /payments
         │  { "amount": 10.00, "currencyCode": "USD",
         │    "returnUrl": "...", "cancelUrl": "..." }
         ▼
  paypal-provider-service
         │
         │  ┌─────────────────────────────────────┐
         │  │ 1. Validate request (amount, URLs)  │
         │  │ 2. Get OAuth token (from Redis or   │
         │  │    PayPal)                           │
         │  │ 3. Build PayPal order request        │
         │  │ 4. Call PayPal Create Order API      │
         │  │ 5. Parse response                    │
         │  └─────────────────────────────────────┘
         │
         ▼
  Response:
  {
    "orderId": "5O190127TN364715T",
    "paypalStatus": "PAYER_ACTION_REQUIRED",
    "redirectUrl": "https://www.sandbox.paypal.com/..."
  }

Step 2: User Approves on PayPal (external browser flow)

Step 3: Capture Order
────────────────────────────────────────────────────────────────

  payment-processing-service
         │
         │  POST /payments/5O190127TN364715T/capture
         ▼
  paypal-provider-service
         │
         │  ┌─────────────────────────────────────┐
         │  │ 1. Get OAuth token                   │
         │  │ 2. Call PayPal Capture Order API     │
         │  │ 3. Parse response                    │
         │  └─────────────────────────────────────┘
         │
         ▼
  Response:
  {
    "orderId": "5O190127TN364715T",
    "paypalStatus": "COMPLETED"
  }
```

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 3.4.2** | Application framework |
| **Java 17** | Language runtime |
| **Netflix Eureka** | Service discovery & registration |
| **Redis** | Caching PayPal OAuth access tokens |
| **Apache HttpClient 5** | Connection-pooled HTTP client for PayPal API calls |
| **AWS Secrets Manager** | Credential management (dev/prod profiles) |
| **Micrometer + Brave** | Distributed tracing |
| **Spring Boot Actuator** | Health checks and monitoring |
| **Lombok** | Boilerplate code reduction |
| **Jackson** | JSON serialization/deserialization |
| **Maven** | Build tool |

---

## REST API Endpoints

### PayPal Payment Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/payments` | Create a new PayPal order |
| `POST` | `/payments/{orderId}/capture` | Capture a previously created PayPal order |

### Redis Utility Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/redis/value/{key}` | Set a Redis key-value pair |
| `GET` | `/redis/value/{key}` | Get value by key |
| `POST` | `/redis/value/{key}/expiry/{timeoutInSecs}` | Set key with TTL |
| `POST` | `/redis/list/{key}` | Add value to a Redis list |
| `GET` | `/redis/list/{key}` | Get all values from a Redis list |
| `POST` | `/redis/hash/{hashName}/{key}` | Set a hash field |
| `GET` | `/redis/hash/{hashName}/{key}` | Get a hash field |
| `GET` | `/redis/hash/{hashName}` | Get all hash entries |

### Actuator

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application health check |

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Redis** running locally (for local profile)
- **PayPal Developer Account** — Get sandbox credentials from [developer.paypal.com](https://developer.paypal.com/)
- **Docker** (optional) — For running Redis locally

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/chandan-howale/paypal-provider-service.git
cd paypal-provider-service/paypal-provider-eureka-sr-impl
```

### 2. Set Up Environment Variables

Copy the example env file and add your credentials:

```bash
cp .env.example .env
```

Edit `.env` with your PayPal sandbox credentials:

```env
PAYPAL_CLIENT_ID=your_actual_client_id
PAYPAL_CLIENT_SECRET=your_actual_client_secret
```

### 3. Start Redis (Local)

```bash
# Using Docker (recommended)
docker run -d --name redis -p 6379:6379 redis:latest

# Or using Homebrew (macOS)
brew services start redis

# Or using Windows (with WSL or Docker Desktop)
```

### 4. Run the Application

#### Option A: Using IntelliJ IDEA (Recommended)

1. Open the project folder (`paypal-provider-eureka-sr-impl`) in IntelliJ IDEA.
2. IntelliJ automatically detects the Maven project — wait for dependencies to load.
3. Navigate to `src/main/java/com/chandan/payments/PaypalProviderServiceApplication.java`.
4. Right-click → **Run 'PaypalProviderServiceApplication'** (or click the green triangle next to the class).
5. The service starts on **port 8083**.

> **Note for IntelliJ users:** If Lombok annotations aren't working, enable annotation processing:
> `Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing`

#### Option B: Using VS Code

1. Install extensions: **Extension Pack for Java** (Microsoft) + **Spring Boot Extension Pack**.
2. Open the project folder in VS Code.
3. Press `F5` or go to **Run → Start Debugging**.
4. Select "Java" when prompted.
5. The service starts on **port 8083**.

#### Option C: Using Maven (Command Line)

```bash
# Build the project first
mvn clean install -DskipTests

# Run with local profile (default)
mvn spring-boot:run
```

The service starts on **port 8083**.

### 5. Verify

```bash
# Check health
curl http://localhost:8083/actuator/health
```

---

## Configuration Profiles

| Profile | PayPal Credentials | Redis | PayPal API |
|---|---|---|---|
| **local** (default) | From `.env` file | localhost:6379 | Sandbox |
| **dev** | AWS Secrets Manager | AWS ElastiCache (SSL) | Sandbox |
| **qa/prod** | Not configured | Not configured | Not configured |

Switch profiles using Maven:

```bash
mvn spring-boot:run -Pdev
```

---

## Project Structure

```
paypal-provider-eureka-sr-impl/
├── src/main/java/com/chandan/payments/
│   ├── controller/          # REST controllers
│   ├── service/             # Business logic (PaymentServiceImpl, TokenService, RedisService, PaymentValidator)
│   │   ├── helper/          # PayPal request/response builders
│   │   └── interfaces/      # Service contracts
│   ├── http/                # HTTP abstraction layer
│   ├── paypal/
│   │   ├── req/             # PayPal API request models
│   │   └── res/             # PayPal API response models
│   │       └── error/       # PayPal error response models
│   ├── pojo/                # Application DTOs
│   ├── constant/            # Constants and error codes
│   ├── exception/           # Custom exceptions and global error handler
│   ├── config/              # Spring configuration (RestClient bean)
│   └── util/                # JSON utilities, PayPal order helpers
├── src/main/resources/
│   ├── application.properties
│   ├── application-local.properties
│   ├── application-dev.properties
│   └── application-qa.properties
└── pom.xml
```

---

## Error Handling

All errors return a consistent JSON response:

```json
{
  "errorCode": "30004",
  "errorMessage": "Amount must be greater than zero"
}
```

Error codes follow the `30000`–`30009` range:

| Code | Description |
|---|---|
| 30000 | Generic error |
| 30001 | Currency code required |
| 30002 | Return URL required |
| 30003 | Invalid request payload |
| 30004 | Invalid amount |
| 30005 | Cancel URL required |
| 30006 | PayPal service unavailable |
| 30007 | PayPal API error |
| 30008 | Unknown PayPal error |
| 30009 | Resource not found |

---

## Key Design Decisions

- **OAuth token caching** — Access tokens are cached in Redis with TTL = token expiry − 5 minutes, avoiding redundant token requests to PayPal on every API call.
- **Connection pooling** — Apache HttpClient 5 connection pool (100 max connections, 10s connect timeout, 15s read timeout) for efficient HTTP communication with PayPal.
- **Idempotency** — Each PayPal API call includes a unique `Paypal-Request-Id` header (UUID) to prevent duplicate operations.
- **Separation of concerns** — Helpers (`CreateOrderHelper`, `CaptureOrderHelper`) handle PayPal-specific request building and response parsing, keeping services focused on orchestration.

---

## Testing

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=PaypalProviderServiceApplicationTests
```

> **Note:** The current test suite only includes a Spring context-load test. Redis is required even for tests to start.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

## Author

**Chandan Howale** — [GitHub](https://github.com/chandan-howale)

Built as part of the [payment-integration-system](https://github.com/chandan-howale/payment-integration-system) project.
