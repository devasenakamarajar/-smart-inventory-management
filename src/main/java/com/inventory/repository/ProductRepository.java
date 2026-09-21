package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByProductNameIgnoreCase(String productName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.status = true")
    Optional<Product> findActiveByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.status = true")
    List<Product> findAllActiveProducts();

    @Query("SELECT p FROM Product p WHERE p.status = true AND p.currentStock <= p.minimumStock")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.status = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId)")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId);
}
