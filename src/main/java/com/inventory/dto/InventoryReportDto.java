package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportDto {

    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private Integer currentStock;
    private Integer minimumStock;
    private Integer maximumStock;
}
