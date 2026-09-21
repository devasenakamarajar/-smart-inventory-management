package com.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDto {

    private Long id;

    @NotNull(message = "Sale is required")
    private Long saleId;

    @NotNull(message = "Customer is required")
    private Long customerId;

    private BigDecimal totalAmount;

    private String status;

    private String processedBy;

    private String processedByName;

    private java.time.LocalDateTime returnDate;

    @Valid
    @NotNull(message = "Return items are required")
    private List<ReturnItemDto> items;
}
