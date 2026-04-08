package com.batistell.catalogapi.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report returned by GET /catalog/report.
 * All fields are populated concurrently via @Async queries.
 */
@Data
@Builder
public class CatalogReportResponse {

    /** Timestamp of when the report was generated. */
    private LocalDateTime generatedAt;

    /** Total number of products in the catalog. */
    private long totalProducts;

    /** Number of products that have at least one discount applied. */
    private long productsWithDiscounts;

    /** Number of products that have at least one attribute defined. */
    private long productsWithAttributes;

    /** Number of products that have parent products (sub-products). */
    private long productsWithParents;

    /** Number of products that have child products. */
    private long productsWithChildren;

    /** Most expensive product in the catalog. */
    private Product mostExpensiveProduct;

    /** Cheapest product in the catalog. */
    private Product cheapestProduct;

    /** Average price across all products. */
    private double averagePrice;

    /** Number of products grouped by category name. */
    private Map<String, Long> productCountByCategory;

    /** All distinct category names found across the catalog. */
    private List<String> distinctCategories;

    /** All distinct discount names found across the catalog. */
    private List<String> distinctDiscounts;

    /** How many milliseconds the report took to build (wall-clock). */
    private long buildTimeMs;
}
