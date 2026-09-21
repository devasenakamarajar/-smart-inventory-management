package com.inventory.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.inventory.dto.PurchaseOrderDto;
import com.inventory.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/purchases")
    public ResponseEntity<PurchaseOrderDto> createPurchase(@Valid @RequestBody PurchaseOrderDto purchaseOrderDto) {
        PurchaseOrderDto savedPurchase = purchaseService.createPurchase(purchaseOrderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPurchase);
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseOrderDto>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @GetMapping("/purchases/{id}")
    public ResponseEntity<PurchaseOrderDto> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }
}
