package com.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProductDto {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    private String productName;

    @NotBlank(message = "SKU is required")
    @Size(min = 2, max = 100, message = "SKU must be between 2 and 100 characters")
    private String sku;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private BigDecimal sellingPrice;

    @NotNull(message = "Current stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer currentStock;

    @NotNull(message = "Minimum stock is required")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    @NotNull(message = "Maximum stock is required")
    @Min(value = 0, message = "Maximum stock cannot be negative")
    private Integer maximumStock;

    private LocalDate expiryDate;

    private Boolean status = true;
}
