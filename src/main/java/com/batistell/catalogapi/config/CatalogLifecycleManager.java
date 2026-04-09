package com.batistell.catalogapi.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Demonstrates Spring Bean Lifecycle Management, acting as an equivalent to traditional EJB lifecycle mechanisms.
 * This class ensures certain operations happen exactly at startup (e.g. warming up caches)
 * and exactly at shutdown (e.g. gracefully closing external connections).
 */
@Slf4j
@Component
public class CatalogLifecycleManager implements SmartLifecycle {

    private boolean isRunning = false;

    @PostConstruct
    public void init() {
        log.info("[LIFECYCLE] @PostConstruct: CatalogLifecycleManager instance created. Preparing resources...");
    }

    @Override
    public void start() {
        // SmartLifecycle start() happens after ApplicationContext is refreshed.
        log.info("[LIFECYCLE] SmartLifecycle.start(): Checking dependencies (MongoDB, Redis, Kafka) and warming up metadata caches...");
        // Simulation of warm-up logic
        try {
            Thread.sleep(500); // Simulate network check
            log.info("[LIFECYCLE] Cache warmed and dependencies are reachable.");
            isRunning = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        log.info("[LIFECYCLE] SmartLifecycle.stop(): Gracefully shutting down application context...");
        log.info("[LIFECYCLE] Sending termination signals to active producers and rejecting new requests.");
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        // Defines startup/shutdown order. Lowest starts first, highest stops first.
        return Integer.MAX_VALUE; 
    }

    @PreDestroy
    public void destroy() {
        log.info("[LIFECYCLE] @PreDestroy: Cleaning up remaining object-level resources, closing local buffers...");
    }
}
