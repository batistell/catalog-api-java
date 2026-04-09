package com.batistell.catalogapi.client;

import com.batistell.catalogapi.model.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-api", url = "${inventory.api.url}")
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}")
    InventoryDto getInventoryByProductId(@PathVariable("productId") String productId);
}
