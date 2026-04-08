package com.batistell.catalogapi.service;

import com.batistell.catalogapi.model.CatalogReportResponse;
import com.batistell.catalogapi.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogReportService {

    private final MongoTemplate mongoTemplate;

    public CatalogReportResponse buildReport(String messageId) {
        long start = System.currentTimeMillis();
        log.info("messageId={} Report build started", messageId);
        CompletableFuture<Long> totalCf = countTotal(messageId);
        CompletableFuture<Long> withDiscountsCf = countWithDiscounts(messageId);
        CompletableFuture<Long> withAttributesCf = countWithAttributes(messageId);
        CompletableFuture<Long> withParentsCf = countWithParents(messageId);
        CompletableFuture<Long> withChildrenCf = countWithChildren(messageId);
        CompletableFuture<Product> mostExpensiveCf = findMostExpensive(messageId);
        CompletableFuture<Product> cheapestCf = findCheapest(messageId);
        CompletableFuture<Double> avgPriceCf = computeAveragePrice(messageId);
        CompletableFuture<Map<String, Long>> byCategoryCf = groupByCategory(messageId);
        CompletableFuture<List<String>> distinctCatsCf = findDistinctCategories(messageId);
        CompletableFuture<List<String>> distinctDiscsCf = findDistinctDiscounts(messageId);
        CompletableFuture.allOf(
                totalCf, withDiscountsCf, withAttributesCf,
                withParentsCf, withChildrenCf,
                mostExpensiveCf, cheapestCf, avgPriceCf,
                byCategoryCf, distinctCatsCf, distinctDiscsCf
        ).join();
        long elapsed = System.currentTimeMillis() - start;
        log.info("messageId={} Report build finished in {}ms", messageId, elapsed);
        return CatalogReportResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalProducts(getValue(totalCf, 0L))
                .productsWithDiscounts(getValue(withDiscountsCf, 0L))
                .productsWithAttributes(getValue(withAttributesCf, 0L))
                .productsWithParents(getValue(withParentsCf, 0L))
                .productsWithChildren(getValue(withChildrenCf, 0L))
                .mostExpensiveProduct(getValue(mostExpensiveCf, null))
                .cheapestProduct(getValue(cheapestCf, null))
                .averagePrice(getValue(avgPriceCf, 0.0))
                .productCountByCategory(getValue(byCategoryCf, Map.of()))
                .distinctCategories(getValue(distinctCatsCf, List.of()))
                .distinctDiscounts(getValue(distinctDiscsCf, List.of()))
                .buildTimeMs(elapsed)
                .build();
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Long> countTotal(String messageId) {
        long count = mongoTemplate.count(new Query(), Product.class);
        return CompletableFuture.completedFuture(count);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Long> countWithDiscounts(String messageId) {
        Query q = new Query(Criteria.where("discounts").exists(true).not().size(0));
        return CompletableFuture.completedFuture(mongoTemplate.count(q, Product.class));
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Long> countWithAttributes(String messageId) {
        Query q = new Query(Criteria.where("attributes").exists(true).not().size(0));
        return CompletableFuture.completedFuture(mongoTemplate.count(q, Product.class));
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Long> countWithParents(String messageId) {
        Query q = new Query(Criteria.where("parents").exists(true).not().size(0));
        return CompletableFuture.completedFuture(mongoTemplate.count(q, Product.class));
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Long> countWithChildren(String messageId) {
        Query q = new Query(Criteria.where("children").exists(true).not().size(0));
        return CompletableFuture.completedFuture(mongoTemplate.count(q, Product.class));
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Product> findMostExpensive(String messageId) {
        Query q = new Query().with(Sort.by(Sort.Direction.DESC, "price")).limit(1);
        Product product = mongoTemplate.findOne(q, Product.class);
        return CompletableFuture.completedFuture(product);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Product> findCheapest(String messageId) {
        Query q = new Query().with(Sort.by(Sort.Direction.ASC, "price")).limit(1);
        Product product = mongoTemplate.findOne(q, Product.class);
        return CompletableFuture.completedFuture(product);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Double> computeAveragePrice(String messageId) {
        List<Product> all = mongoTemplate.findAll(Product.class);
        double avg = all.stream()
                .filter(p -> p.getPrice() != null)
                .mapToDouble(Product::getPrice)
                .average()
                .orElse(0.0);
        return CompletableFuture.completedFuture(avg);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<Map<String, Long>> groupByCategory(String messageId) {
        List<Product> all = mongoTemplate.findAll(Product.class);
        Map<String, Long> grouped = all.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.counting()
                ));
        return CompletableFuture.completedFuture(grouped);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<List<String>> findDistinctCategories(String messageId) {
        List<String> categories = mongoTemplate.findDistinct("category.name", Product.class, String.class);
        return CompletableFuture.completedFuture(categories);
    }

    @Async("catalogTaskExecutor")
    CompletableFuture<List<String>> findDistinctDiscounts(String messageId) {
        List<String> discounts = mongoTemplate.findDistinct("discounts.name", Product.class, String.class);
        return CompletableFuture.completedFuture(discounts);
    }

    private <T> T getValue(CompletableFuture<T> future, T fallback) {
        try {
            return future.get();
        } catch (Exception e) {
            log.warn("Failed to get async result, using fallback: {}", e.getMessage());
            return fallback;
        }
    }
}
