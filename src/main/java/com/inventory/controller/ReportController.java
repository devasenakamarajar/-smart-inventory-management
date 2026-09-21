package com.inventory.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.inventory.dto.CustomerPurchaseReportDto;
import com.inventory.dto.InventoryReportDto;
import com.inventory.dto.PurchaseReportDto;
import com.inventory.dto.SalesReportDto;
import com.inventory.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportDto>> getSalesReport() {
        return ResponseEntity.ok(reportService.getSalesReport());
    }

    @GetMapping("/sales/today")
    public ResponseEntity<List<SalesReportDto>> getTodaySalesReport() {
        return ResponseEntity.ok(reportService.getTodaySalesReport());
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseReportDto>> getPurchaseReport() {
        return ResponseEntity.ok(reportService.getPurchaseReport());
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryReportDto>> getInventoryReport() {
        return ResponseEntity.ok(reportService.getInventoryReport());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryReportDto>> getLowStockReport() {
        return ResponseEntity.ok(reportService.getLowStockReport());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerPurchaseReportDto>> getCustomerPurchaseReport() {
        return ResponseEntity.ok(reportService.getCustomerPurchaseReport());
    }
}
