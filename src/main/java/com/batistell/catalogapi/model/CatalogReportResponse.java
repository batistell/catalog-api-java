package com.batistell.catalogapi.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CatalogReportResponse {

    private LocalDateTime generatedAt;
    private long totalProducts;
    private long productsWithDiscounts;
    private long productsWithAttributes;
    private long productsWithParents;
    private long productsWithChildren;
    private Product mostExpensiveProduct;
    private Product cheapestProduct;
    private double averagePrice;
    private Map<String, Long> productCountByCategory;
    private List<String> distinctCategories;
    private List<String> distinctDiscounts;
    private long buildTimeMs;
}
