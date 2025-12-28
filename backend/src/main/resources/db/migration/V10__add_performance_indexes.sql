-- V10: Performance indexes for frequently queried columns
-- These indexes improve query performance for scheduled jobs and reports

-- Bookings indexes
CREATE INDEX IF NOT EXISTS idx_bookings_schedule_status ON bookings(schedule_id, status);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

-- Payment sessions indexes (for cleanup jobs and availability checks)
CREATE INDEX IF NOT EXISTS idx_payment_sessions_status_expires ON payment_sessions(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_payment_sessions_token ON payment_sessions(token);

-- Cart items index (for availability validation)
CREATE INDEX IF NOT EXISTS idx_cart_items_schedule_id ON cart_items(schedule_id);

-- Carts index (for cleanup job)
CREATE INDEX IF NOT EXISTS idx_carts_expires_at ON carts(expires_at);

-- Tour schedules indexes (for auto-close job and calendar queries)
CREATE INDEX IF NOT EXISTS idx_tour_schedules_status_start ON tour_schedules(status, start_datetime);
