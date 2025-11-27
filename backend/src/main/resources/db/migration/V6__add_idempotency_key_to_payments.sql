-- Add idempotency_key column to payments table for preventing duplicate payment attempts
ALTER TABLE payments ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

-- Create unique index for idempotency key (allows nulls)
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_idempotency ON payments (idempotency_key) WHERE idempotency_key IS NOT NULL;
