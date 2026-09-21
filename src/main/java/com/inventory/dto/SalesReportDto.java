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
public class SalesReportDto {

    private Long saleId;
    private String customerName;
    private LocalDate saleDate;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
}
