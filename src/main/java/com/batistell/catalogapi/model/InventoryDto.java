package com.batistell.catalogapi.model;

import lombok.Data;

@Data
public class InventoryDto {
    private String productId;
    private Integer quantity;
    private String status;
}
