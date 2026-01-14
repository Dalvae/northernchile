-- V12: Add composite index for booking reminder queries
-- This index optimizes the scheduled reminder job that finds confirmed bookings
-- with upcoming schedules that haven't received reminders yet

-- Composite index for reminder query: status + reminder_sent_at
-- Covers: findByStatusAndStartDateTimeBetweenAndReminderNotSent
CREATE INDEX IF NOT EXISTS idx_bookings_status_reminder ON bookings(status, reminder_sent_at);

-- Partial index for pending bookings cleanup (only indexes pending bookings)
-- More efficient than full index since we only query pending status
CREATE INDEX IF NOT EXISTS idx_bookings_stale_pending ON bookings(created_at) WHERE status = 'PENDING';
