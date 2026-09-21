package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReportDto {

    private Long purchaseId;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String status;
}
