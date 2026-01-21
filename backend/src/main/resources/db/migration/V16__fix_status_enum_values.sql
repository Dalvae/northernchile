-- Fix tour status values to match enum
UPDATE tours
SET status = 'DRAFT'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('DRAFT', 'PUBLISHED');

UPDATE tours
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix weather alert status values
UPDATE weather_alerts
SET status = 'PENDING'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('PENDING', 'REVIEWED', 'RESOLVED');

UPDATE weather_alerts
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix booking status values
UPDATE bookings
SET status = 'PENDING'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REFUNDED');

UPDATE bookings
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix tour schedule status values
UPDATE tour_schedules
SET status = 'OPEN'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('OPEN', 'CLOSED', 'CANCELLED', 'FULL');

UPDATE tour_schedules
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix cart status values
UPDATE carts
SET status = 'ACTIVE'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('ACTIVE', 'EXPIRED', 'CONVERTED');

UPDATE carts
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix payment session status values
UPDATE payment_sessions
SET status = 'PENDING'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('PENDING', 'COMPLETED', 'FAILED', 'EXPIRED', 'CANCELLED', 'REFUNDED');

UPDATE payment_sessions
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Fix payment status values
UPDATE payments
SET status = 'PENDING'
WHERE status IS NULL OR TRIM(status) = '' OR UPPER(status) NOT IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUND_PENDING', 'REFUNDED', 'EXPIRED');

UPDATE payments
SET status = UPPER(status)
WHERE status IS NOT NULL;
