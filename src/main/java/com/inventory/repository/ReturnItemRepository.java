package com.inventory.repository;

import com.inventory.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
}
