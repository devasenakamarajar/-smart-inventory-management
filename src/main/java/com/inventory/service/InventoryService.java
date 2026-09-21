package com.inventory.service;

import com.inventory.dto.InventoryTransactionDto;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryTransactionRepository inventoryTransactionRepository,
                           ProductRepository productRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllActiveProducts();
    }

    public List<InventoryTransactionDto> getAllTransactions() {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAll();
        List<InventoryTransactionDto> result = new ArrayList<>();

        for (InventoryTransaction transaction : transactions) {
            result.add(convertToDto(transaction));
        }

        return result;
    }

    public List<InventoryTransactionDto> getTransactionsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getProduct().getId().equals(product.getId()))
                .toList();

        List<InventoryTransactionDto> result = new ArrayList<>();
        for (InventoryTransaction transaction : transactions) {
            result.add(convertToDto(transaction));
        }

        return result;
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    private InventoryTransactionDto convertToDto(InventoryTransaction transaction) {
        InventoryTransactionDto dto = new InventoryTransactionDto();
        dto.setId(transaction.getId());
        dto.setProductId(transaction.getProduct().getId());
        dto.setProductName(transaction.getProduct().getProductName());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setQuantity(transaction.getQuantity());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setReason(transaction.getReason());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
