package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDto {

    private Long id;
    private Long productId;
    private String productName;
    private String transactionType;
    private Integer quantity;
    private String referenceId;
    private String reason;
    private LocalDateTime createdAt;
}
