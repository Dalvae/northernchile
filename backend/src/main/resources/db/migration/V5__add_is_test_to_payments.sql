-- Add is_test column to payments table for test mode segregation
-- This allows marking test transactions to exclude them from financial reports

ALTER TABLE payments
ADD COLUMN is_test BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for filtering test payments
CREATE INDEX idx_payments_is_test ON payments(is_test);

-- Comment for documentation
COMMENT ON COLUMN payments.is_test IS 'Flag indicating if this is a test payment (excluded from financial reports). Automatically set based on PAYMENT_TEST_MODE configuration.';
