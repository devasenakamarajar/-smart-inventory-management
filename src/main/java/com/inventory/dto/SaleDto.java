package com.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleDto {

    private Long id;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private String customerAddress;

    private LocalDate saleDate;

    private BigDecimal totalAmount;

    private String paymentMethod;

    private String status;

    private String cashierUsername;

    @Valid
    @NotNull(message = "Sale items are required")
    private List<SaleItemDto> items;
}
