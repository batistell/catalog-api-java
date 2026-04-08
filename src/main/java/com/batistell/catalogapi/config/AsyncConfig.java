package com.batistell.catalogapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables @Async support and configures a dedicated thread pool
 * for catalog async operations.
 *
 * Why a custom pool?
 * - Separates async catalog work from Spring's default task executor.
 * - Gives control over concurrency (corePoolSize, maxPoolSize) and queue depth.
 * - Thread name prefix makes async threads easy to spot in logs.
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "catalogTaskExecutor")
    public Executor catalogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // always-alive threads
        executor.setMaxPoolSize(8);         // max threads when queue is full
        executor.setQueueCapacity(100);     // tasks buffered before growing threads
        executor.setThreadNamePrefix("catalog-async-");
        executor.initialize();
        return executor;
    }
}
