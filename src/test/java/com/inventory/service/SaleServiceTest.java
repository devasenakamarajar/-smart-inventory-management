package com.inventory.service;

import com.inventory.dto.SaleDto;
import com.inventory.dto.SaleItemDto;
import com.inventory.entity.Product;
import com.inventory.entity.User;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleItemRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesDatabasePriceAndReducesStock() {
        User cashier = new User();
        cashier.setUsername("staff");
        Product product = new Product();
        product.setId(7L);
        product.setProductName("Keyboard");
        product.setSku("KEY-001");
        product.setSellingPrice(new BigDecimal("25.00"));
        product.setCurrentStock(10);
        product.setStatus(true);

        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(cashier));
        when(productRepository.findActiveByIdForUpdate(7L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("staff", "n/a"));

        SaleItemDto item = new SaleItemDto();
        item.setProductId(7L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("0.01"));
        SaleDto request = new SaleDto();
        request.setCustomerName("Walk-in customer");
        request.setItems(List.of(item));

        SaleDto result = new SaleService(saleRepository, saleItemRepository, customerRepository,
                productRepository, inventoryTransactionRepository, userRepository).createSale(request);

        assertEquals(new BigDecimal("50.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("25.00"), result.getItems().get(0).getUnitPrice());
        assertEquals(8, product.getCurrentStock());
        assertEquals("staff", result.getCashierUsername());
        verify(productRepository).findActiveByIdForUpdate(eq(7L));
        verify(inventoryTransactionRepository).save(any());
    }

    @Test
    void rejectsInsufficientStockBeforePersistingSale() {
        User cashier = new User();
        cashier.setUsername("staff");
        Product product = new Product();
        product.setId(7L);
        product.setProductName("Keyboard");
        product.setSellingPrice(new BigDecimal("25.00"));
        product.setCurrentStock(1);
        product.setStatus(true);
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(cashier));
        when(productRepository.findActiveByIdForUpdate(7L)).thenReturn(Optional.of(product));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("staff", "n/a"));

        SaleItemDto item = new SaleItemDto();
        item.setProductId(7L);
        item.setQuantity(2);
        SaleDto request = new SaleDto();
        request.setItems(List.of(item));

        SaleService service = new SaleService(saleRepository, saleItemRepository, customerRepository,
                productRepository, inventoryTransactionRepository, userRepository);

        assertThrows(IllegalArgumentException.class, () -> service.createSale(request));
        verify(saleRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(inventoryTransactionRepository, never()).save(any());
    }
}
