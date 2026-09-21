package com.inventory.controller;

import com.inventory.dto.SaleDto;
import com.inventory.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping("/sales")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SaleDto> createSale(@Valid @RequestBody SaleDto saleDto) {
        SaleDto savedSale = saleService.createSale(saleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSale);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<SaleDto>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/sales/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SaleDto> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }
}
