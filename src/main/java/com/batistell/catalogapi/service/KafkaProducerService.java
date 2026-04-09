package com.batistell.catalogapi.service;

import com.batistell.catalogapi.model.Product;
import com.batistell.catalogapi.model.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "catalog.product.lifecycle";

    @Async("catalogTaskExecutor")
    public void publishEvent(String action, String productId, Product payload, String messageId) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .action(action)
                .productId(productId)
                .productPayload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("messageId={} Sending {} event to Kafka topic {} for productId={}", messageId, action, TOPIC, productId);
        kafkaTemplate.send(TOPIC, productId, event);
    }
}
