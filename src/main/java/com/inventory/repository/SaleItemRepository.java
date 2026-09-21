package com.inventory.repository;

import com.inventory.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT si.sale.saleDate, SUM(si.quantity) FROM SaleItem si " +
            "WHERE si.product.id = :productId " +
            "AND si.sale.saleDate BETWEEN :startDate AND :endDate " +
            "GROUP BY si.sale.saleDate " +
            "ORDER BY si.sale.saleDate ASC")
    List<Object[]> findDailySalesForProduct(@Param("productId") Long productId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
