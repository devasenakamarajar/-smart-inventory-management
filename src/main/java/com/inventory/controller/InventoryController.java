package com.inventory.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.inventory.dto.InventoryTransactionDto;
import com.inventory.entity.Product;
import com.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<Product>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }

    @GetMapping("/inventory/transactions")
    public ResponseEntity<List<InventoryTransactionDto>> getInventoryTransactions() {
        return ResponseEntity.ok(inventoryService.getAllTransactions());
    }

    @GetMapping("/inventory/transactions/product/{productId}")
    public ResponseEntity<List<InventoryTransactionDto>> getProductTransactions(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getTransactionsByProduct(productId));
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }
}
