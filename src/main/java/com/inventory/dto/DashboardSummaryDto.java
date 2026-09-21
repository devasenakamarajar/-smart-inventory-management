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
public class DashboardSummaryDto {

    private Long totalProducts;
    private Integer totalStock;
    private Long lowStockProducts;
    private BigDecimal todaysSales;
    private BigDecimal monthlySales;
    private Long pendingPurchaseOrders;
    private Long totalCustomers;
}
