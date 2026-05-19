package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL ENUM columns created by Hibernate do not pick up new {@link com.example.demo.model.EscrowStatus}
 * values automatically. Converts escrow_status to VARCHAR so all enum constants persist correctly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EscrowStatusColumnMigrator {

    private final JdbcTemplate jdbcTemplate;

    @Value("${rentix.schema.fix-escrow-status:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateEscrowStatusColumn() {
        if (!enabled) {
            return;
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE marketplace_orders MODIFY COLUMN escrow_status VARCHAR(32) NOT NULL"
            );
            log.info("marketplace_orders.escrow_status ensured as VARCHAR(32)");
        } catch (Exception ex) {
            log.warn("Could not migrate escrow_status column (run SQL manually if buy-now fails): {}", ex.getMessage());
        }
    }
}
