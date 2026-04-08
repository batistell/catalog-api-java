package com.batistell.catalogapi.service;

import com.batistell.catalogapi.model.CatalogAnalysisResponse;
import com.batistell.catalogapi.model.Product;
import com.batistell.catalogapi.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogAnalysisService {

    private final CatalogRepository catalogRepository;

    public CatalogAnalysisResponse analyze(String messageId) {
        log.info("messageId={} Analysis started — cores available: {}", messageId, Runtime.getRuntime().availableProcessors());
        List<Product> products = catalogRepository.findAll();
        if (products.isEmpty()) {
            return emptyResult();
        }
        long seqStart = System.currentTimeMillis();
        ProductStats seqStats = computeSequential(products);
        long seqMs = System.currentTimeMillis() - seqStart;
        log.info("messageId={} Sequential processing done in {}ms", messageId, seqMs);
        long parStart = System.currentTimeMillis();
        ProductStats parStats = computeParallel(products);
        long parMs = System.currentTimeMillis() - parStart;
        log.info("messageId={} Parallel processing done in {}ms", messageId, parMs);
        int cores = Runtime.getRuntime().availableProcessors();
        double speedup = parMs > 0 ? (double) seqMs / parMs : 1.0;
        return CatalogAnalysisResponse.builder()
                .totalProducts(products.size())
                .minPrice(parStats.min)
                .maxPrice(parStats.max)
                .averagePrice(parStats.avg)
                .totalRevenuePotential(parStats.sum)
                .priceStdDeviation(parStats.stdDev)
                .priceRangeDistribution(parStats.priceRanges)
                .avgPriceByCategory(parStats.avgByCategory)
                .sequentialProcessingMs(seqMs)
                .parallelProcessingMs(parMs)
                .availableCpuCores(cores)
                .speedupFactor(Math.round(speedup * 100.0) / 100.0)
                .explanation(buildExplanation(seqMs, parMs, cores, products.size()))
                .build();
    }

    private ProductStats computeSequential(List<Product> products) {
        return buildStats(products, false);
    }

    private ProductStats computeParallel(List<Product> products) {
        return buildStats(products, true);
    }

    @Async("catalogTaskExecutor")
    public CompletableFuture<Map<String, Double>> computeAvgPricePerCategoryAsync(String messageId) {
        log.info("messageId={} [CONCURRENT] avgPriceByCategory started — thread={}", messageId, Thread.currentThread().getName());
        List<Product> products = catalogRepository.findAll();
        Map<String, Double> result = products.parallelStream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null && p.getPrice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.averagingDouble(Product::getPrice)
                ));
        log.info("messageId={} [CONCURRENT] avgPriceByCategory done — categories={}", messageId, result.size());
        return CompletableFuture.completedFuture(result);
    }

    @Async("catalogTaskExecutor")
    public CompletableFuture<Map<String, Long>> computeCountPerRangeAsync(String messageId) {
        log.info("messageId={} [CONCURRENT] countPerRange started — thread={}", messageId, Thread.currentThread().getName());
        List<Product> products = catalogRepository.findAll();
        Map<String, Long> result = products.parallelStream()
                .filter(p -> p.getPrice() != null)
                .collect(Collectors.groupingBy(
                        p -> {
                            double price = p.getPrice();
                            if (price < 50) return "budget (< $50)";
                            else if (price < 200) return "mid-range ($50–$200)";
                            else if (price < 500) return "premium ($200–$500)";
                            else return "luxury (> $500)";
                        },
                        Collectors.counting()
                ));
        log.info("messageId={} [CONCURRENT] countPerRange done — ranges={}", messageId, result.size());
        return CompletableFuture.completedFuture(result);
    }

    private ProductStats buildStats(List<Product> products, boolean parallel) {
        var stream = parallel ? products.parallelStream() : products.stream();
        List<Double> prices = stream
                .filter(p -> p.getPrice() != null)
                .map(Product::getPrice)
                .collect(Collectors.toList());
        if (prices.isEmpty()) return new ProductStats();
        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double sum = prices.stream().mapToDouble(Double::doubleValue).sum();
        double avg = sum / prices.size();
        double variance = prices.stream()
                .mapToDouble(p -> Math.pow(p - avg, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        Map<String, Long> ranges = products.stream()
                .filter(p -> p.getPrice() != null)
                .collect(Collectors.groupingBy(p -> {
                    double price = p.getPrice();
                    if (price < 50) return "budget (< $50)";
                    else if (price < 200) return "mid-range ($50–$200)";
                    else if (price < 500) return "premium ($200–$500)";
                    else return "luxury (> $500)";
                }, Collectors.counting()));
        Map<String, Double> avgByCategory = products.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null && p.getPrice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.averagingDouble(Product::getPrice)
                ));
        ProductStats stats = new ProductStats();
        stats.min = min;
        stats.max = max;
        stats.sum = sum;
        stats.avg = avg;
        stats.stdDev = Math.round(stdDev * 100.0) / 100.0;
        stats.priceRanges = ranges;
        stats.avgByCategory = avgByCategory;
        return stats;
    }

    private String buildExplanation(long seqMs, long parMs, int cores, int productCount) {
        String faster = parMs < seqMs ? "parallel" : "sequential";
        long diff = Math.abs(seqMs - parMs);
        return String.format(
                "Processed %d products on a %d-core machine. " +
                "Sequential stream: %dms (1 thread, 1 core). " +
                "Parallel stream: %dms (%d cores via ForkJoinPool). " +
                "%s was faster by %dms. " +
                "Note: with small datasets, parallel can be SLOWER due to thread coordination overhead.",
                productCount, cores, seqMs, parMs, cores, faster, diff
        );
    }

    private CatalogAnalysisResponse emptyResult() {
        return CatalogAnalysisResponse.builder()
                .totalProducts(0)
                .explanation("No products in the catalog. Add some products and try again.")
                .build();
    }

    private static class ProductStats {
        double min, max, sum, avg, stdDev;
        Map<String, Long> priceRanges = Map.of();
        Map<String, Double> avgByCategory = Map.of();
    }
}
