-- V4: Add contact_messages table and performance indexes

-- Create contact_messages table
CREATE TABLE contact_messages (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL,
    replied_at TIMESTAMP,
    CONSTRAINT chk_contact_status CHECK (status IN ('NEW', 'READ', 'REPLIED', 'ARCHIVED'))
);

-- Create indexes for contact_messages
CREATE INDEX idx_contact_messages_status ON contact_messages(status);
CREATE INDEX idx_contact_messages_created_at ON contact_messages(created_at DESC);

-- Performance indexes for existing tables

-- Bookings table
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_schedule_id ON bookings(schedule_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at DESC);

-- Tour schedules table
CREATE INDEX IF NOT EXISTS idx_tour_schedules_tour_id ON tour_schedules(tour_id);
CREATE INDEX IF NOT EXISTS idx_tour_schedules_start_datetime ON tour_schedules(start_datetime);
CREATE INDEX IF NOT EXISTS idx_tour_schedules_status ON tour_schedules(status);

-- Tours table
CREATE INDEX IF NOT EXISTS idx_tours_owner_id ON tours(owner_id);
CREATE INDEX IF NOT EXISTS idx_tours_status ON tours(status);
CREATE INDEX IF NOT EXISTS idx_tours_slug ON tours(slug);

-- Media table indexes already exist in V2 migration (idx_media_owner, idx_media_tour, idx_media_schedule)

-- Weather alerts table
CREATE INDEX IF NOT EXISTS idx_weather_alerts_schedule_id ON weather_alerts(schedule_id);
CREATE INDEX IF NOT EXISTS idx_weather_alerts_status ON weather_alerts(status);
CREATE INDEX IF NOT EXISTS idx_weather_alerts_created_at ON weather_alerts(created_at DESC);

-- Cart items (for cart retrieval)
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);

-- Participants (for booking queries)
CREATE INDEX IF NOT EXISTS idx_participants_booking_id ON participants(booking_id);

-- Private tour requests
CREATE INDEX IF NOT EXISTS idx_private_tour_requests_status ON private_tour_requests(status);
CREATE INDEX IF NOT EXISTS idx_private_tour_requests_created_at ON private_tour_requests(created_at DESC);
