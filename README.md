# 📦 Catalog API - High-Performance Microservices Architecture

This project is a high-performance, fully distributed **Catalog API** built with Java 17 and Spring Boot 3. It serves as a proof-of-concept and showcase for building robust, scalable backend systems utilizing modern distributed architecture patterns.

Instead of a basic CRUD application, this API is designed around specialized engineering paradigms to handle high throughput, heavy read loads, and complex data aggregations.

---

## 🛠️ Tech Stack & Infrastructure

- **Language & Framework:** Java 17, Spring Boot 3.2.4
- **Database:** MongoDB (Running as a Replica Set for ACID Multi-Document Transactions)
- **Message Broker:** Apache Kafka & Zookeeper (Event-Driven Architecture)
- **Distributed Cache:** Redis (Fast L1/L2 data access)
- **Infrastructure:** Docker & Docker Compose

To spin up the entire ecosystem locally:
```bash
docker-compose up -d
```
*(See the transactions section below for a necessary MongoDB initialization step)*

---

## 🧠 Architectural Choices & Demonstrations

This project is heavily engineered to demonstrate advanced backend patterns. Below are the specific choices used inside the codebase:

### 1. Concurrency (Async I/O Processing)
*Found in `CatalogReportService.java`*

When compiling a global catalog report, the application must run dozens of independent MongoDB queries (counts, mins, maxes, averages). Running these sequentially is an anti-pattern. 
- **Implementation:** The service leverages a custom thread pool (`catalogTaskExecutor`) and `@Async` combined with `CompletableFuture`.
- **Result:** We concurrently fan-out 11 independent database queries and fan-in the results using `CompletableFuture.allOf().join()`. The total wall-clock time is equal to the single *slowest* query, rather than the sum of all queries, drastically minimizing I/O blocking.

### 2. Parallelism (CPU-Bound Data Processing)
*Found in `CatalogAnalysisService.java`*

While concurrency solves waiting for I/O, parallelism solves heavy mathematical computation over massive datasets.
- **Implementation:** Fetches the entire catalogue into JVM memory once, then utilizes the power of the `ForkJoinPool` via `.parallelStream()`. 
- **Result:** The workload is automatically partitioned and computed simultaneously across all available physical CPU cores, radically increasing the speed of standard-deviation algorithms and complex category groupings. An endpoint is provided specifically to benchmark Sequential vs Parallel execution times live.

### 3. ACID Transactions (Distributed State)
*Found in `MongoConfig.java` and `CatalogService.java`*

In NoSQL systems, multi-document transactions are often skipped. This API strictly enforces consistency.
- **Implementation:** `MongoTransactionManager` bean is implemented to bind Spring's `@Transactional` to MongoDB native transactions.
- **Requirement for testing locally:** MongoDB must be run as a replica set to allow rollbacks. Run the following once the docker containers are up:
  ```bash
  docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
  ```

### 4. Event-Driven Architecture & Decoupling (Kafka)
*Found in `KafkaProducerService.java` & `SearchServiceListener.java`*

Synchronous REST calls between microservices create a brittle, tightly coupled architecture. We implement the "Outbox Pattern" theory via Kafka.
- **Implementation:** The Catalog Service is the ultimate source of truth. Whenever a product is added, updated, or removed, the service immediately publishes a `ProductEvent` payload to the `catalog.product.lifecycle` Kafka topic.
- **Result:** Zero tight coupling. Downstream systems (like Inventory management or Elasticsearch indexers) simply consume the topics at their own pace. A mock `SearchServiceListener` is actively running to demonstrate this decoupled reaction in the logs.

### 5. Distributed Caching (Redis)
*Found in `RedisConfig.java` & `CatalogService.java`*

Catalog services read ratios are notoriously high compared to writes.
- **Implementation:** A global Redis cache layer with an automated TTL (Time-To-Live). The heavy `getProductById` hits the cache first via `@Cacheable`.
- **Consitency:** When operations modify a product (`updateProduct` or `deleteProduct`), Spring utilizes `@CacheEvict` to strategically wipe the stale record from Redis, ensuring cache consistency.

---

## 🚀 Running & Testing

1. **Launch Infrastructure:**
   ```bash
   docker-compose up -d
   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
   ```
2. **Launch Application:**
   Run the project via your IDE or `./mvnw spring-boot:run`. The API binds to port `8090` by default.
3. **Swagger UI:**
   Navigate to HTTP Swagger documentation to explore and test the endpoints directly:
   `http://localhost:8090/swagger-ui.html`

### Featured Endpoints to Test:
- `GET /catalog/report` → Watch the **Concurrency** model fetch heavy analytics instantly.
- `GET /catalog/analysis` → Compares CPU execution times for **Parallelism vs Sequential** flows live.
- `POST /add` → Watch the **Kafka** consumer logs instantly react, and future `GET` calls serve from the **Redis** Cache.