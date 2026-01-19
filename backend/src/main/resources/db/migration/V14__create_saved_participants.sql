-- Create table for storing user's saved participants for reuse in future bookings
CREATE TABLE user_saved_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    document_id VARCHAR(50),
    date_of_birth DATE,
    nationality VARCHAR(100),
    phone_number VARCHAR(50),
    email VARCHAR(255),
    is_self BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for fast lookup by user
CREATE INDEX idx_saved_participants_user ON user_saved_participants(user_id);

-- Ensure only one participant can be marked as "self" per user
CREATE UNIQUE INDEX idx_saved_participants_user_self
    ON user_saved_participants(user_id)
    WHERE is_self = TRUE;
