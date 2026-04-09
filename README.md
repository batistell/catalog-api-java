[🇺🇸 English](#english) | [🇧🇷 Português](#português)

---

<br/>

<h1 id="english">🇺🇸 English</h1>

# 📦 Catalog API - High-Performance Microservices Architecture

This project is a high-performance, fully distributed **Catalog API** built with Java 21 and Spring Boot 3. It serves as a proof-of-concept and showcase for building robust, scalable backend systems utilizing modern distributed architecture patterns.

Instead of a basic CRUD application, this API is designed around specialized engineering paradigms to handle high throughput, heavy read loads, and complex data aggregations.

---

## 🛠️ Tech Stack & Infrastructure

- **Language & Framework:** Java 21, Spring Boot 3.2.4
- **Database:** MongoDB (Replica Set for ACID Multi-Document Transactions)
- **Message Broker:** Apache Kafka & Zookeeper (Event-Driven Architecture)
- **Distributed Cache:** Redis (Fast L1/L2 data access)
- **Resilience:** Resilience4j (Circuit Breaker & Fallbacks)
- **Observability:** Micrometer, Prometheus & Grafana
- **Infrastructure:** Docker & Docker Compose

To spin up the entire ecosystem locally:
```bash
docker-compose up -d
```
*(See the transactions section below for a necessary MongoDB initialization step)*

---

## 🧠 Architectural Choices & Demonstrations

This project is heavily engineered to demonstrate advanced backend patterns. Below are the specific choices used inside the codebase:

### 1. Modern Structured Concurrency (Java 21)
*Found in `CatalogAnalysisService.java`*
- **Implementation:** Leverages Java 21's new `StructuredTaskScope` alongside Virtual Threads.
- **Result:** Instead of manually handling scattered asynchronous thread pools, multiple aggregation tasks are cleanly forked and joined within a single deterministic block. This demonstrates state-of-the-art JVM threading paradigms. 

### 2. Concurrency (Async I/O Processing)
*Found in `CatalogReportService.java`*
- **Implementation:** The service leverages a custom thread pool (`catalogTaskExecutor`) and `@Async` combined with `CompletableFuture` (Java 8+ model).
- **Result:** Concurrently fans-out database queries and fans-in the results. Total wall-clock time is equal to the single *slowest* query, drastically minimizing I/O blocking.

### 3. Parallelism (CPU-Bound Data Processing)
*Found in `CatalogAnalysisService.java`*
- **Implementation:** Fetches the entire catalogue into JVM memory once, then utilizes the power of the `ForkJoinPool` via `.parallelStream()`. 
- **Result:** Computation is partitioned and executed simultaneously across all physical CPU cores, radically increasing execution speed of local mathematical algorithms.

### 4. Resilience Patterns (Circuit Breakers)
*Found in `CatalogService.java`*
- **Implementation:** Leverages Resilience4j (`@CircuitBreaker`, `@Retry`).
- **Result:** If the backend database becomes unresponsive, the circuit opens gracefully and redirects strictly to cached fallback mechanisms, preventing system-wide cascading thread saturation.

### 5. ACID Transactions (Distributed State)
*Found in `MongoConfig.java` & `CatalogService.java`*
- **Implementation:** `MongoTransactionManager` bean is implemented to bind Spring's `@Transactional` to MongoDB native transactions.
- **Requirement for testing locally:** MongoDB must be run as a replica set to allow rollbacks. Run the following once docker containers are up:
  ```bash
  docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
  ```

### 6. Event-Driven Architecture & Decoupling (Kafka)
*Found in `KafkaProducerService.java`*
- **Implementation:** The "Outbox Pattern" theory via Kafka. Whenever a product is mutated, the service publishes a `ProductEvent` to `catalog.product.lifecycle`.
- **Result:** Downstream systems consume the topics at their own pace without bottlenecking the main REST API.

### 7. Spring Bean Lifecycle Management
*Found in `CatalogLifecycleManager.java`*
- **Implementation:** Replaces rigid legacy EJB start-up hooks with Spring `SmartLifecycle`, `@PostConstruct`, and Graceful Shutdown configurations.
- **Result:** Guarantees critical components (like Redis/Mongo caches) are healthy before the web server begins receiving HTTP traffic, and cleanly flushes connections on destruction.

---

## 🚀 Running & Testing

1. **Launch Infrastructure:**
   ```bash
   docker-compose up -d
   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
   ```
2. **Launch Application:**
   Run the project via your IDE (ensure Java 21 is selected). The API binds to port `8090`.
3. **Observability Dashboards:**
   Navigate to **Grafana** at `http://localhost:3000` (default credentials: `admin` / `admin`).
4. **Swagger UI:**
   Navigate to HTTP Swagger documentation: `http://localhost:8090/swagger-ui.html`

### Featured Endpoints to Test:
- `GET /catalog/analysis/structured` → The new **Java 21 Virtual Threads** aggregation engine.
- `GET /catalog/report` → Watch the **Concurrency** model fetch heavy analytics instantly.
- `GET /catalog/analysis` → Compares CPU execution times for **Parallelism vs Sequential** flows live.
- `POST /add` → Watch the **Kafka** consumer logs instantly react, and future `GET` calls serve from the **Redis** Cache.

<br/><br/>

<hr/>

<br/>

<h1 id="português">🇧🇷 Português</h1>

# 📦 Catalog API - Arquitetura de Microsserviços de Alta Performance

Este projeto é uma **API de Catálogo** de alta performance e totalmente distribuída, construída com Java 21 e Spring Boot 3. Ele serve como uma prova de conceito e portfólio para a construção de sistemas backend robustos e escaláveis utilizando padrões modernos de arquitetura distribuída.

Em vez de uma aplicação CRUD básica, esta API foi arquitetada em torno de paradigmas de engenharia especializados para lidar com alto volume de requisições, cargas pesadas de leitura e agregações complexas de dados.

---

## 🛠️ Stack Tecnológico e Infraestrutura

- **Linguagem e Framework:** Java 21, Spring Boot 3.2.4
- **Banco de Dados:** MongoDB (Replica Set para Transações ACID em múltiplos documentos)
- **Mensageria:** Apache Kafka & Zookeeper (Arquitetura Orientada a Eventos)
- **Cache Distribuído:** Redis (Acesso rápido L1/L2)
- **Resiliência:** Resilience4j (Circuit Breaker e Fallbacks)
- **Observabilidade:** Micrometer, Prometheus e Grafana
- **Infraestrutura:** Docker e Docker Compose

Para rodar todo o ecossistema localmente:
```bash
docker-compose up -d
```
*(Veja a seção de transações abaixo para uma etapa necessária de inicialização do MongoDB)*

---

## 🧠 Decisões Arquiteturais e Demonstrações

Este projeto foi intensamente engenhado para demonstrar padrões avançados de backend. Abaixo estão as implementações específicas utilizadas no código:

### 1. Concorrência Estruturada Moderna (Java 21)
*Encontrado em `CatalogAnalysisService.java`*
- **Implementação:** Utiliza o novo `StructuredTaskScope` do Java 21 junto com Virtual Threads.
- **Resultado:** Em vez de lidar manualmente com pools de threads assíncronas isoladas, múltiplas tarefas são distribuídas e unidas de forma limpa dentro de um bloco determinístico. Isso demonstra o que há de mais moderno na JVM (Threads Virtuais).

### 2. Concorrência (Processamento Assíncrono de I/O)
*Encontrado em `CatalogReportService.java`*
- **Implementação:** O serviço utiliza um pool de threads customizado (`catalogTaskExecutor`) e `@Async` combinado com `CompletableFuture` (Modelo Java 8+).
- **Resultado:** Executa múltiplas queries no banco de dados de maneira concorrente. O tempo total é igual ao da query *mais lenta*, minimizando o bloqueio de I/O.

### 3. Paralelismo (Processamento de Dados na CPU)
*Encontrado em `CatalogAnalysisService.java`*
- **Implementação:** Busca todo o catálogo em memória da JVM e utiliza o poder do `ForkJoinPool` através de `.parallelStream()`.
- **Resultado:** A computação é particionada e executada simultaneamente através de todos os núcleos físicos da CPU, aumentando radicalmente a velocidade de execução em algoritmos matemáticos locais.

### 4. Padrões de Resiliência (Circuit Breakers)
*Encontrado em `CatalogService.java`*
- **Implementação:** Utilização do Resilience4j (`@CircuitBreaker`, `@Retry`).
- **Resultado:** Se o banco de dados principal instabilizar, o circuito "abre" graciosamente e redireciona estritamente para mecanismos de cache ou fallback. Isso previne o travamento em cascata do pool de threads.

### 5. Transações ACID (Estado Distribuído)
*Encontrado em `MongoConfig.java` e `CatalogService.java`*
- **Implementação:** O bean `MongoTransactionManager` foi implementado para ligar o `@Transactional` do Spring às transações nativas do MongoDB.
- **Requisito para teste local:** O MongoDB deve rodar como um replica set para permitir rollbacks. Execute o comando a seguir quando o docker estiver rodando:
  ```bash
  docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
  ```

### 6. Arquitetura Orientada a Eventos e Desacoplamento (Kafka)
*Encontrado em `KafkaProducerService.java`*
- **Implementação:** A teoria do "Outbox Pattern" via Kafka. Sempre que um produto é mutado, o serviço publica um `ProductEvent` no tópico `catalog.product.lifecycle`.
- **Resultado:** Sistemas adjacentes consomem as mensagens no próprio ritmo sem criar gargalos na API REST principal.

### 7. Gerenciamento do Ciclo de Vida Spring (Bean Lifecycle)
*Encontrado em `CatalogLifecycleManager.java`*
- **Implementação:** Substitui os antigos mecanismos de inicialização rígida do EJB pelo `SmartLifecycle` do Spring, `@PostConstruct`, e configurações de *Graceful Shutdown*.
- **Resultado:** Garante que os componentes essenciais (como as conexões Redis/Mongo) estejam saudáveis antes que o servidor comece a receber tráfego HTTP, finalizando conexões limpas ao desligar.

---

## 🚀 Como Executar e Testar

1. **Iniciando a Infraestrutura:**
   ```bash
   docker-compose up -d
   docker exec -it catalog_mongodb mongosh --eval "rs.initiate()"
   ```
2. **Iniciando a Aplicação:**
   Rode o projeto pela sua IDE (Garanta que o Java 21 esteja configurado). A API se ligará à porta `8090` por padrão.
3. **Painéis de Observabilidade:**
   Acesse o **Grafana** em `http://localhost:3000` (credenciais padrões: `admin` / `admin`).
4. **Swagger UI:**
   Acesse a documentação da API em: `http://localhost:8090/swagger-ui.html`

### Principais Endpoints para Testar:
- `GET /catalog/analysis/structured` → O novo motor de agregação via **Virtual Threads (Java 21)**.
- `GET /catalog/report` → Observe o modelo de **Concorrência** buscar métricas pesadas instantaneamente.
- `GET /catalog/analysis` → Compara ao vivo a execução **Paralela vs Sequencial**.
- `POST /add` → Veja o consumidor **Kafka** nos logs agindo instantaneamente, e chamadas subsequentes consumirem o cache do **Redis**.