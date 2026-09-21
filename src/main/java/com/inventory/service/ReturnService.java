package com.inventory.service;

import com.inventory.dto.ReturnDto;
import com.inventory.dto.ReturnItemDto;
import com.inventory.entity.Customer;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.ReturnEntity;
import com.inventory.entity.ReturnItem;
import com.inventory.entity.Sale;
import com.inventory.entity.SaleItem;
import com.inventory.entity.User;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.ReturnItemRepository;
import com.inventory.repository.ReturnRepository;
import com.inventory.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final com.inventory.repository.UserRepository userRepository;

    public ReturnService(ReturnRepository returnRepository,
                        ReturnItemRepository returnItemRepository,
                        SaleRepository saleRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        InventoryTransactionRepository inventoryTransactionRepository,
                        com.inventory.repository.UserRepository userRepository) {
        this.returnRepository = returnRepository;
        this.returnItemRepository = returnItemRepository;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReturnDto createReturn(ReturnDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Return items are required");
        }

        Sale sale = saleRepository.findById(dto.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + dto.getSaleId()));

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        User staff = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow(() -> new ResourceNotFoundException("Staff account not found"));

        Map<Long, Integer> soldQuantitiesByProduct = sale.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        SaleItem::getQuantity,
                        Integer::sum
                ));

        ReturnEntity returnEntity = new ReturnEntity();
        returnEntity.setSale(sale);
        returnEntity.setCustomer(customer);
        returnEntity.setCreatedBy(staff);
        returnEntity.setStatus("PENDING");
        returnEntity.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ReturnItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            Integer soldQuantity = soldQuantitiesByProduct.getOrDefault(product.getId(), 0);
            if (itemDto.getQuantity() > soldQuantity) {
                throw new IllegalArgumentException("Return quantity cannot exceed sold quantity for product: " + product.getProductName());
            }

            if (itemDto.getUnitPrice() == null || itemDto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be greater than zero for product: " + product.getProductName());
            }

            BigDecimal subtotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            ReturnItem returnItem = new ReturnItem();
            returnItem.setReturnEntity(returnEntity);
            returnItem.setProduct(product);
            returnItem.setQuantity(itemDto.getQuantity());
            returnItem.setUnitPrice(itemDto.getUnitPrice());
            returnItem.setSubtotal(subtotal);
            returnItem.setDamaged(itemDto.isDamaged());
            returnEntity.getItems().add(returnItem);

            if (!itemDto.isDamaged()) {
                int updatedStock = product.getCurrentStock() == null ? 0 : product.getCurrentStock();
                product.setCurrentStock(updatedStock + itemDto.getQuantity());
                productRepository.save(product);

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setTransactionType("RETURN");
                transaction.setQuantity(itemDto.getQuantity());
                transaction.setReferenceId("RET-" + returnEntity.getId());
                transaction.setReason("Customer return accepted in good condition");
                inventoryTransactionRepository.save(transaction);
            } else {
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setTransactionType("RETURN");
                transaction.setQuantity(itemDto.getQuantity());
                transaction.setReferenceId("RET-" + returnEntity.getId());
                transaction.setReason("Damaged return recorded separately");
                inventoryTransactionRepository.save(transaction);
            }
        }

        returnEntity.setTotalAmount(totalAmount);
        returnEntity.setStatus("COMPLETED");

        ReturnEntity savedReturn = returnRepository.save(returnEntity);

        for (ReturnItem returnItem : returnEntity.getItems()) {
            returnItem.setReturnEntity(savedReturn);
            returnItemRepository.save(returnItem);
        }

        return convertToDto(savedReturn);
    }

    public List<ReturnDto> getAllReturns() {
        List<ReturnDto> result = new ArrayList<>();
        for (ReturnEntity returnEntity : returnRepository.findAll()) {
            result.add(convertToDto(returnEntity));
        }
        return result;
    }

    private ReturnDto convertToDto(ReturnEntity returnEntity) {
        ReturnDto dto = new ReturnDto();
        dto.setId(returnEntity.getId());
        dto.setSaleId(returnEntity.getSale().getId());
        dto.setCustomerId(returnEntity.getCustomer().getId());
        dto.setTotalAmount(returnEntity.getTotalAmount());
        dto.setStatus(returnEntity.getStatus());
        dto.setReturnDate(returnEntity.getReturnDate());
        dto.setProcessedBy(returnEntity.getCreatedBy() != null ? returnEntity.getCreatedBy().getUsername() : null);
        dto.setProcessedByName(returnEntity.getCreatedBy() != null ? returnEntity.getCreatedBy().getFullName() : null);

        List<ReturnItemDto> itemDtos = new ArrayList<>();
        for (ReturnItem item : returnEntity.getItems()) {
            ReturnItemDto itemDto = new ReturnItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setSubtotal(item.getSubtotal());
            itemDto.setDamaged(item.isDamaged());
            itemDtos.add(itemDto);
        }

        dto.setItems(itemDtos);
        return dto;
    }
}
