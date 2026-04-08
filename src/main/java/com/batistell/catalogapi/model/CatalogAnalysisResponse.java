package com.batistell.catalogapi.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Result of GET /catalog/analysis.
 * Contains the same computed statistics produced three different ways,
 * along with timing so you can compare the approaches.
 */
@Data
@Builder
public class CatalogAnalysisResponse {

    // ── Computed statistics (identical regardless of approach) ────────────────

    /** Total number of products processed. */
    private long totalProducts;

    /** Lowest price found. */
    private double minPrice;

    /** Highest price found. */
    private double maxPrice;

    /** Average (mean) price across all products. */
    private double averagePrice;

    /** Sum of all product prices. */
    private double totalRevenuePotential;

    /** Standard deviation of prices (measures how spread out prices are). */
    private double priceStdDeviation;

    /** Count of products per price bucket. */
    private Map<String, Long> priceRangeDistribution;

    /** Average price per category name. */
    private Map<String, Double> avgPriceByCategory;

    // ── Timing comparison — the interesting part ──────────────────────────────

    /**
     * Time to process the dataset using a regular sequential Stream.
     * All work happens on one thread, one element at a time.
     */
    private long sequentialProcessingMs;

    /**
     * Time to process the SAME dataset using parallelStream().
     * The JVM's ForkJoinPool splits the list across all available CPU cores
     * and processes chunks simultaneously (true PARALLELISM).
     */
    private long parallelProcessingMs;

    /**
     * Number of CPU cores available to the JVM.
     * More cores = greater potential speedup from parallelStream().
     */
    private int availableCpuCores;

    /**
     * How many times faster parallel was vs sequential.
     * e.g. 3.5 means parallel was 3.5x faster.
     */
    private double speedupFactor;

    /**
     * Human-readable explanation of what each approach did.
     */
    private String explanation;
}
