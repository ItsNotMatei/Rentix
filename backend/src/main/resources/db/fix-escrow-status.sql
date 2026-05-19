-- Run once if buy-now fails with "Data truncated for column 'escrow_status'".
-- Cause: MySQL ENUM missing newer values (e.g. CANCELLED) added in EscrowStatus.java.
ALTER TABLE marketplace_orders
    MODIFY COLUMN escrow_status VARCHAR(32) NOT NULL;
