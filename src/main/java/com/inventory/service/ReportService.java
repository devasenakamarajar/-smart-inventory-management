package com.inventory.service;

import com.inventory.dto.CustomerPurchaseReportDto;
import com.inventory.dto.InventoryReportDto;
import com.inventory.dto.PurchaseReportDto;
import com.inventory.dto.SalesReportDto;
import com.inventory.entity.Customer;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public ReportService(SaleRepository saleRepository,
                        PurchaseOrderRepository purchaseOrderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public List<SalesReportDto> getSalesReport() {
        List<SalesReportDto> reports = new ArrayList<>();
        for (Sale sale : saleRepository.findAll()) {
            reports.add(toSalesReportDto(sale));
        }
        return reports;
    }

    public List<SalesReportDto> getTodaySalesReport() {
        LocalDate today = LocalDate.now();
        List<SalesReportDto> reports = new ArrayList<>();
        for (Sale sale : saleRepository.findAll()) {
            if (today.equals(sale.getSaleDate())) {
                reports.add(toSalesReportDto(sale));
            }
        }
        return reports;
    }

    private SalesReportDto toSalesReportDto(Sale sale) {
        SalesReportDto dto = new SalesReportDto();
        dto.setSaleId(sale.getId());
        dto.setCustomerName(sale.getCustomer() != null ? sale.getCustomer().getName() : "Walk-in");
        dto.setSaleDate(sale.getSaleDate());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setStatus(sale.getStatus());
        return dto;
    }

    public List<PurchaseReportDto> getPurchaseReport() {
        List<PurchaseReportDto> reports = new ArrayList<>();
        for (PurchaseOrder order : purchaseOrderRepository.findAll()) {
            PurchaseReportDto dto = new PurchaseReportDto();
            dto.setPurchaseId(order.getId());
            dto.setOrderDate(order.getOrderDate());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            reports.add(dto);
        }
        return reports;
    }

    public List<InventoryReportDto> getInventoryReport() {
        List<InventoryReportDto> reports = new ArrayList<>();
        for (Product product : productRepository.findAllActiveProducts()) {
            InventoryReportDto dto = new InventoryReportDto();
            dto.setProductId(product.getId());
            dto.setProductName(product.getProductName());
            dto.setSku(product.getSku());
            dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized");
            dto.setCurrentStock(product.getCurrentStock());
            dto.setMinimumStock(product.getMinimumStock());
            dto.setMaximumStock(product.getMaximumStock());
            reports.add(dto);
        }
        return reports;
    }

    public List<InventoryReportDto> getLowStockReport() {
        List<InventoryReportDto> reports = new ArrayList<>();
        for (Product product : productRepository.findLowStockProducts()) {
            InventoryReportDto dto = new InventoryReportDto();
            dto.setProductId(product.getId());
            dto.setProductName(product.getProductName());
            dto.setSku(product.getSku());
            dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized");
            dto.setCurrentStock(product.getCurrentStock());
            dto.setMinimumStock(product.getMinimumStock());
            dto.setMaximumStock(product.getMaximumStock());
            reports.add(dto);
        }
        return reports;
    }

    public List<CustomerPurchaseReportDto> getCustomerPurchaseReport() {
        List<CustomerPurchaseReportDto> reports = new ArrayList<>();
        for (Customer customer : customerRepository.findAll()) {
            Long totalSales = 0L;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Sale sale : saleRepository.findAll()) {
                if (sale.getCustomer() != null && sale.getCustomer().getId().equals(customer.getId())) {
                    totalSales++;
                    totalAmount = totalAmount.add(sale.getTotalAmount());
                }
            }

            CustomerPurchaseReportDto dto = new CustomerPurchaseReportDto();
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getName());
            dto.setTotalSales(totalSales);
            dto.setTotalAmount(totalAmount);
            reports.add(dto);
        }
        return reports;
    }
}
