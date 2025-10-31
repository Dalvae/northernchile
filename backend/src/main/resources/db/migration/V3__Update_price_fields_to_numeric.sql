-- V3: Update all price-related fields to use NUMERIC(19,4) for proper money handling
-- This ensures precision and avoids floating-point errors
-- NUMERIC(19,4) allows for:
--   - Up to 15 digits before the decimal point (enough for CLP and future currencies)
--   - Up to 4 decimal places (for future multi-currency support, though CLP uses 0)

-- Update tours table
ALTER TABLE tours
    ALTER COLUMN price TYPE NUMERIC(19, 4);

-- Update bookings table
ALTER TABLE bookings
    ALTER COLUMN subtotal TYPE NUMERIC(19, 4),
    ALTER COLUMN tax_amount TYPE NUMERIC(19, 4),
    ALTER COLUMN total_amount TYPE NUMERIC(19, 4);

-- Update private_tour_requests table
ALTER TABLE private_tour_requests
    ALTER COLUMN quoted_price TYPE NUMERIC(19, 4);

-- Add comment explaining the precision choice
COMMENT ON COLUMN tours.price IS 'Price stored as NUMERIC(19,4) - minor units compatible with payment gateways. CLP uses 0 decimals.';
COMMENT ON COLUMN bookings.subtotal IS 'Stored as NUMERIC(19,4) for precision. All monetary calculations must use this type.';
COMMENT ON COLUMN bookings.tax_amount IS 'IVA amount stored as NUMERIC(19,4) for precision.';
COMMENT ON COLUMN bookings.total_amount IS 'Total amount stored as NUMERIC(19,4) for precision.';
