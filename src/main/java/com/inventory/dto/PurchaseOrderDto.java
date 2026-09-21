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
public class PurchaseOrderDto {

    private Long id;

    private LocalDate orderDate;

    private String status;

    private BigDecimal totalAmount;

    @Valid
    @NotNull(message = "Purchase items are required")
    private List<PurchaseItemDto> items;
}
