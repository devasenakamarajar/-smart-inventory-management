package com.inventory.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySchemaMigration {

    public LegacySchemaMigration(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("ALTER TABLE purchase_orders MODIFY COLUMN supplier_id BIGINT NULL");
    }
}
