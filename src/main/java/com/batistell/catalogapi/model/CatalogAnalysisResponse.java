package com.batistell.catalogapi.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CatalogAnalysisResponse {

    private long totalProducts;
    private double minPrice;
    private double maxPrice;
    private double averagePrice;
    private double totalRevenuePotential;
    private double priceStdDeviation;
    private Map<String, Long> priceRangeDistribution;
    private Map<String, Double> avgPriceByCategory;
    private long sequentialProcessingMs;
    private long parallelProcessingMs;
    private int availableCpuCores;
    private double speedupFactor;
    private String explanation;
}
