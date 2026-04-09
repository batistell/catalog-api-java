package com.batistell.catalogapi.service;

import com.batistell.catalogapi.model.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchServiceListener {

    @KafkaListener(topics = "catalog.product.lifecycle", groupId = "search-service-group", properties = {"spring.json.value.default.type=com.batistell.catalogapi.model.ProductEvent"})
    public void consumeProductEvent(ProductEvent event) {
        log.info("🔔 [MOCK SEARCH SERVICE] Received Kafka Event: Action={}, ProductID={}, EventID={}", 
                event.getAction(), event.getProductId(), event.getEventId());
        
        switch (event.getAction()) {
            case "CREATE":
            case "UPDATE":
                log.info("   -> Creating/Updating document in local Elasticsearch index for fuzzy search.");
                break;
            case "DELETE":
                log.info("   -> Removing product from Elasticsearch index.");
                break;
            default:
                log.warn("   -> Unknown action received.");
        }
    }
}
