package com.inventory.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySchemaMigration {

    public LegacySchemaMigration(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE purchase_orders MODIFY COLUMN supplier_id BIGINT NULL");
        } catch (Exception e) {
            // Ignore error on clean databases where column doesn't exist yet
            System.out.println("Schema migration skipped: " + e.getMessage());
        }
    }
}
