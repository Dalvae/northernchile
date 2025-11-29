-- Add reminder_sent_at column to track when tour reminder emails were sent
-- This prevents duplicate reminder emails from being sent

ALTER TABLE bookings ADD COLUMN reminder_sent_at TIMESTAMPTZ;

-- Index for efficient querying of bookings that haven't received reminders
CREATE INDEX idx_bookings_reminder_sent_at ON bookings(reminder_sent_at) WHERE reminder_sent_at IS NULL;
