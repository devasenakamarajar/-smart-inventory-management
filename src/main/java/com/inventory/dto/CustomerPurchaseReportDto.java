package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPurchaseReportDto {

    private Long customerId;
    private String customerName;
    private Long totalSales;
    private BigDecimal totalAmount;
}
