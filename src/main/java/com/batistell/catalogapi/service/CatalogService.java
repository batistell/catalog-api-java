package com.batistell.catalogapi.service;

import com.batistell.catalogapi.model.Product;
import com.batistell.catalogapi.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final KafkaProducerService kafkaProducerService;

    public void addProducts(String messageId, List<Product> products) {
        for (Product product : products) {
            try {
                Product saved = catalogRepository.save(product);
                kafkaProducerService.publishEvent("CREATE", saved.getId(), saved, messageId);
            } catch (Exception e) {
                log.error("messageId={} error saving product: {}", messageId, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    @CircuitBreaker(name = "catalogDB", fallbackMethod = "getAllProductsFallback")
    @Retry(name = "catalogDB", fallbackMethod = "getAllProductsFallback")
    public List<Product> getAllProducts() {
        return catalogRepository.findAll();
    }

    public List<Product> getAllProductsFallback(Exception e) {
        log.warn("Database unavailable or rate limited. Returning empty cached product list. Error: {}", e.getMessage());
        // In a real scenario, this could hit a secondary Redis cache or static fallback data.
        return List.of();
    }

    @Cacheable(value = "products", key = "#id")
    @CircuitBreaker(name = "catalogDB")
    public Product getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "product ID cannot be empty");
        }
        return catalogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(String id, Product updatedProduct) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "product ID cannot be empty");
        }
        if (!catalogRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
        updatedProduct.setId(id);
        try {
            Product saved = catalogRepository.save(updatedProduct);
            kafkaProducerService.publishEvent("UPDATE", id, saved, UUID.randomUUID().toString());
            return saved;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "product ID cannot be empty");
        }
        try {
            catalogRepository.deleteById(id);
            kafkaProducerService.publishEvent("DELETE", id, null, UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
