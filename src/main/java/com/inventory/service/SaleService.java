package com.inventory.service;

import com.inventory.dto.SaleDto;
import com.inventory.dto.SaleItemDto;
import com.inventory.entity.Customer;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.Sale;
import com.inventory.entity.SaleItem;
import com.inventory.entity.User;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleItemRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository,
                      SaleItemRepository saleItemRepository,
                      CustomerRepository customerRepository,
                      ProductRepository productRepository,
                      InventoryTransactionRepository inventoryTransactionRepository,
                      UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SaleDto createSale(SaleDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sale items are required");
        }

        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        } else if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank()
                && !"Walk-in customer".equalsIgnoreCase(dto.getCustomerName().trim())) {
            customer = new Customer();
            customer.setName(dto.getCustomerName().trim());
            customer.setPhone(blankToNull(dto.getCustomerPhone()));
            customer.setEmail(blankToNull(dto.getCustomerEmail()));
            customer.setAddress(blankToNull(dto.getCustomerAddress()));
            customer = customerRepository.save(customer);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User cashier = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Cashier account not found"));

        Sale sale = new Sale();
        sale.setCreatedBy(cashier);
        sale.setCustomer(customer);
        sale.setCustomerName(dto.getCustomerName());
        sale.setSaleDate(dto.getSaleDate() != null ? dto.getSaleDate() : LocalDate.now());
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setStatus(dto.getStatus() != null ? dto.getStatus() : "COMPLETED");
        sale.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> productIds = new HashSet<>();

        for (SaleItemDto itemDto : dto.getItems()) {
            if (!productIds.add(itemDto.getProductId())) {
                throw new IllegalArgumentException("A product can appear only once in a bill");
            }

            Product product = productRepository.findActiveByIdForUpdate(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product: " + product.getProductName());
            }

            int currentStock = product.getCurrentStock() == null ? 0 : product.getCurrentStock();
            if (itemDto.getQuantity() > currentStock) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
            }

            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDto.getQuantity());
            saleItem.setUnitPrice(unitPrice);
            saleItem.setSubtotal(subtotal);
            sale.getItems().add(saleItem);

            product.setCurrentStock(currentStock - itemDto.getQuantity());
        }

        sale.setTotalAmount(totalAmount);
        Sale savedSale = saleRepository.save(sale);

        for (SaleItem saleItem : savedSale.getItems()) {
            Product product = saleItem.getProduct();
            productRepository.save(product);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setTransactionType("SALE");
            transaction.setQuantity(saleItem.getQuantity());
            transaction.setReferenceId("SL-" + savedSale.getId());
            transaction.setReason("Product sold to customer");
            transaction.setCreatedBy(cashier);
            inventoryTransactionRepository.save(transaction);
        }

        return convertToDto(savedSale);
    }

    public List<SaleDto> getAllSales() {
        List<Sale> sales = saleRepository.findAll();
        List<SaleDto> result = new ArrayList<>();
        for (Sale sale : sales) {
            result.add(convertToDto(sale));
        }
        return result;
    }

    public SaleDto getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return convertToDto(sale);
    }

    private SaleDto convertToDto(Sale sale) {
        SaleDto dto = new SaleDto();
        dto.setId(sale.getId());
        dto.setCustomerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null);
        dto.setCustomerName(sale.getCustomerName());
        if (sale.getCustomer() != null) {
            dto.setCustomerPhone(sale.getCustomer().getPhone());
            dto.setCustomerEmail(sale.getCustomer().getEmail());
            dto.setCustomerAddress(sale.getCustomer().getAddress());
        }
        dto.setSaleDate(sale.getSaleDate());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setStatus(sale.getStatus());
        dto.setCashierUsername(sale.getCreatedBy() != null ? sale.getCreatedBy().getUsername() : null);

        List<SaleItemDto> items = new ArrayList<>();
        for (SaleItem item : sale.getItems()) {
            SaleItemDto itemDto = new SaleItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setSubtotal(item.getSubtotal());
            itemDto.setProductName(item.getProduct().getProductName());
            itemDto.setSku(item.getProduct().getSku());
            items.add(itemDto);
        }

        dto.setItems(items);
        return dto;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
