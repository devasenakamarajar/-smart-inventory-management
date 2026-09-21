package com.inventory.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.inventory.dto.ReturnDto;
import com.inventory.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping("/returns")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ReturnDto> createReturn(@Valid @RequestBody ReturnDto returnDto) {
        ReturnDto savedReturn = returnService.createReturn(returnDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReturn);
    }

    @GetMapping("/returns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReturnDto>> getAllReturns() {
        return ResponseEntity.ok(returnService.getAllReturns());
    }
}
