[🇺🇸 English](#english) | [🇧🇷 Português](#português)

---

<br/>

<h1 id="english">🇺🇸 English</h1>

# 📦 Catalog API - High-Performance Microservices Architecture

This project is the distributed **Catalog API** built with Java 21 and Spring Boot 3. It showcases modern distributed architecture patterns, designed to handle high throughput, heavy read loads, and complex data aggregations alongside the `inventory-api-java`.

---

## 🛠️ Tech Stack & Infrastructure

- **Language:** Java 21, Spring Boot 3.2.4
- **Database:** MongoDB (Replica Set)
- **Cross-Service Communication:** Spring Cloud OpenFeign
- **Message Broker:** Apache Kafka & Zookeeper
- **Cache:** Redis
- **Resilience:** Resilience4j
- **Observability:** Micrometer, Prometheus & Grafana
- **Containers:** Docker & Docker Compose

To spin up the ecosystem locally:
```bash
docker-compose up -d
```
*(Requires `mongosh --eval "rs.initiate()"` post-boot for ACID transactions)*

---

## 🧠 Architectural Demonstrations

### 1. Structured Concurrency (Java 21 Virtual Threads)
Replaces manual asynchronous thread pools with Java 21's `StructuredTaskScope`, splitting and joining aggregation scopes via virtual threads.

### 2. Async I/O & Parallel CPU Processing
Combines custom thread pools (`@Async` & `CompletableFuture`) for concurrent database interactions, and `ForkJoinPool` (`.parallelStream()`) to maximize local CPU utilization across all physical cores.

### 3. Synchronous Fetching (Spring Cloud OpenFeign)
Bypasses slow REST templates. Dynamically injects an `InventoryClient` directly into the Java layer to instantly pull synchronous stock values from the external inventory cluster over HTTP.

### 4. Event-Driven "Outbox" Pattern (Kafka)
Broadcasts non-blocking `ProductEvent` messages to downstream queues. Allows sidecar APIs (like the Inventory service) to react in real-time without bottlenecking the main REST tunnel.

### 5. Circuit Breakers (Resilience4j)
Automatically "opens" the circuit and reroutes traffic directly to clustered Redis caches if the persistence layer becomes highly latent or unavailable.

---

## 🚀 Running & Testing

1. **Launch Infrastructure:**
   ```bash
   docker-compose up -d
   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
   ```
2. **Launch Application:** Run via IDE. Binds to `8090`.
3. **Swagger UI:** `http://localhost:8090/swagger-ui.html`

### Featured Endpoints to Test:
- `GET /catalog/analysis/structured` → Demonstrates **Virtual Threads**.
- `GET /products/{id}/stock` → Demonstrates **OpenFeign** polling remote clusters.
- `GET /catalog/analysis` → Compares **Parallel vs Sequential** data streams.
- `POST /add` → Pushes data to **Kafka** and updates **Redis**.

<br/><br/>

<hr/>

<br/>

<h1 id="português">🇧🇷 Português</h1>

# 📦 Catalog API - Microsserviços de Alta Performance

Este projeto é a **Catalog API** distribuída em Java 21 e Spring Boot 3. Ele demonstra configurações avançadas de arquitetura voltada a alto volume de leitura, agregamento rápido e comunicação remota juntamente com o `inventory-api-java`.

---

## 🛠️ Stack Tecnológico

- **Linguagem:** Java 21, Spring Boot 3.2.4
- **Banco de Dados:** MongoDB (Replica Set)
- **Comunicação Síncrona:** Spring Cloud OpenFeign
- **Mensageria:** Apache Kafka & Zookeeper
- **Cache:** Redis
- **Resiliência:** Resilience4j
- **Observabilidade:** Micrometer, Prometheus & Grafana
- **Containers:** Docker e Docker Compose

Para iniciar localmente:
```bash
docker-compose up -d
```
*(Requer `mongosh --eval "rs.initiate()"` para transações ACID)*

---

## 🧠 Demonstrações Arquiteturais

### 1. Concorrência Estruturada (Java 21 Virtual Threads)
Substitui pools de threads assíncronas padrão pelo `StructuredTaskScope` do Java 21, utilizando threads virtuais para orquestrar dados de forma limpa.

### 2. I/O Assíncrono e Processamento de CPU Paralelo
Combina pools de thread customizados (`@Async` e `CompletableFuture`) e o uso de processadores lógicos em massa através de `ForkJoinPool` (`.parallelStream()`) para executar matemática de alta performance.

### 3. Chamadas Síncronas Diretas (Spring Cloud OpenFeign)
Introdução limpa do `InventoryClient` no código Java para extrair de forma nativa pela rede e em tempo real os valores de estoque atrelados dentro das bases de outro microsserviço.

### 4. Padrão "Outbox" Orientado a Eventos (Kafka)
Distribui mensagens `ProductEvent` não-bloqueantes. Permite que serviços downstream (Inventário) se provisionem organicamente baseado nos disparos efetuados pelo núcleo.

### 5. Circuit Breakers (Resilience4j)
Previne timeouts em cascata através de redirecionamento cirúrgico de rotas para caches Redis isolados caso o banco de dados desmonte.

---

## 🚀 Executando

1. **Infraestrutura:**
   ```bash
   docker-compose up -d
   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
   ```
2. **Aplicação:** Rode pela IDE na porta `8090`.
3. **Swagger UI:** `http://localhost:8090/swagger-ui.html`

### Endpoints em Destaque:
- `GET /catalog/analysis/structured` → Roda engine com **Virtual Threads**.
- `GET /products/{id}/stock` → Verifica o estoque via rede no microsserviço externo através de **OpenFeign**.
- `GET /catalog/analysis` → Testa limites de memória usando processamento **Paralelo e Sequencial**.
- `POST /add` → Insere produtos ativando a rede do **Kafka** e inserção do **Redis**.