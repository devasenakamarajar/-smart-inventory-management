package com.inventory.service;

import com.inventory.dto.DashboardSummaryDto;
import com.inventory.entity.Product;
import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.Sale;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CustomerRepository customerRepository;

    public DashboardService(ProductRepository productRepository,
                           SaleRepository saleRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.customerRepository = customerRepository;
    }

    public DashboardSummaryDto getDashboardSummary() {
        List<Product> allProducts = productRepository.findAllActiveProducts();
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        BigDecimal todaysSales = BigDecimal.ZERO;
        BigDecimal monthlySales = BigDecimal.ZERO;
        int totalStock = 0;

        for (Product product : allProducts) {
            if (product.getCurrentStock() != null) {
                totalStock += product.getCurrentStock();
            }
        }

        LocalDate today = LocalDate.now();
        for (Sale sale : saleRepository.findAll()) {
            if (sale.getSaleDate() != null && sale.getSaleDate().equals(today)) {
                todaysSales = todaysSales.add(sale.getTotalAmount());
            }

            if (sale.getSaleDate() != null && sale.getSaleDate().getMonth().equals(today.getMonth())) {
                monthlySales = monthlySales.add(sale.getTotalAmount());
            }
        }

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalProducts((long) allProducts.size());
        summary.setTotalStock(totalStock);
        summary.setLowStockProducts((long) lowStockProducts.size());
        summary.setTodaysSales(todaysSales);
        summary.setMonthlySales(monthlySales);
        summary.setPendingPurchaseOrders(purchaseOrderRepository.findAll().stream()
                .filter(order -> "PENDING".equalsIgnoreCase(order.getStatus()) || "CONFIRMED".equalsIgnoreCase(order.getStatus()))
                .count());
        summary.setTotalCustomers(customerRepository.count());
        return summary;
    }
}
