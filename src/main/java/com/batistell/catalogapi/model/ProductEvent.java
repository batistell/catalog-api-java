package com.batistell.catalogapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    
    private String eventId;
    private String action; // CREATE, UPDATE, DELETE
    private String productId;
    private Product productPayload; // Full product object, or null for DELETE
    private LocalDateTime timestamp;

}
