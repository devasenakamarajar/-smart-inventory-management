package com.inventory.service;

import com.inventory.dto.PurchaseItemDto;
import com.inventory.dto.PurchaseOrderDto;
import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.Product;
import com.inventory.entity.PurchaseItem;
import com.inventory.entity.PurchaseOrder;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryTransactionRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseItemRepository;
import com.inventory.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;

    public PurchaseService(PurchaseOrderRepository purchaseOrderRepository,
                          PurchaseItemRepository purchaseItemRepository,
                          InventoryTransactionRepository inventoryTransactionRepository,
                          ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public PurchaseOrderDto createPurchase(PurchaseOrderDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Purchase items are required");
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now());
        purchaseOrder.setStatus("CONFIRMED");
        purchaseOrder.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product: " + product.getProductName());
            }

            if (itemDto.getUnitPrice() == null || itemDto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be greater than zero for product: " + product.getProductName());
            }

            BigDecimal subtotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setPurchaseOrder(purchaseOrder);
            purchaseItem.setProduct(product);
            purchaseItem.setQuantity(itemDto.getQuantity());
            purchaseItem.setUnitPrice(itemDto.getUnitPrice());
            purchaseItem.setSubtotal(subtotal);
            purchaseOrder.getItems().add(purchaseItem);

            int newStock = product.getCurrentStock() == null ? 0 : product.getCurrentStock();
            product.setCurrentStock(newStock + itemDto.getQuantity());
            productRepository.save(product);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setTransactionType("PURCHASE");
            transaction.setQuantity(itemDto.getQuantity());
            transaction.setReferenceId("PO-" + purchaseOrder.getId());
            transaction.setReason("Goods received into inventory");
            inventoryTransactionRepository.save(transaction);
        }

        purchaseOrder.setTotalAmount(totalAmount);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        for (PurchaseItem item : purchaseOrder.getItems()) {
            item.setPurchaseOrder(savedPurchaseOrder);
            purchaseItemRepository.save(item);
        }

        return convertToDto(savedPurchaseOrder);
    }

    public List<PurchaseOrderDto> getAllPurchases() {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        List<PurchaseOrderDto> result = new ArrayList<>();

        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            result.add(convertToDto(purchaseOrder));
        }

        return result;
    }

    public PurchaseOrderDto getPurchaseById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return convertToDto(purchaseOrder);
    }

    private PurchaseOrderDto convertToDto(PurchaseOrder purchaseOrder) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(purchaseOrder.getId());
        dto.setOrderDate(purchaseOrder.getOrderDate());
        dto.setStatus(purchaseOrder.getStatus());
        dto.setTotalAmount(purchaseOrder.getTotalAmount());

        List<PurchaseItemDto> items = new ArrayList<>();
        for (PurchaseItem item : purchaseOrder.getItems()) {
            PurchaseItemDto itemDto = new PurchaseItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setSubtotal(item.getSubtotal());
            items.add(itemDto);
        }

        dto.setItems(items);
        return dto;
    }
}
