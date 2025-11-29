-- Payment Sessions: Stores checkout data temporarily until payment is completed
-- Bookings are only created after successful payment

CREATE TABLE payment_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    items JSONB NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CLP',
    language_code VARCHAR(5) NOT NULL DEFAULT 'es',
    user_email VARCHAR(255),
    provider VARCHAR(20),
    payment_method VARCHAR(20),
    external_payment_id VARCHAR(255),
    token VARCHAR(500),
    payment_url TEXT,
    qr_code TEXT,
    pix_code TEXT,
    return_url TEXT,
    cancel_url TEXT,
    provider_response JSONB,
    error_message TEXT,
    is_test BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for efficient queries
CREATE INDEX idx_payment_session_user ON payment_sessions(user_id);
CREATE INDEX idx_payment_session_status ON payment_sessions(status);
CREATE INDEX idx_payment_session_token ON payment_sessions(token);
CREATE INDEX idx_payment_session_expires ON payment_sessions(expires_at);
CREATE INDEX idx_payment_session_external_id ON payment_sessions(external_payment_id);

-- Make booking_id nullable in payments table (payment can exist before booking)
ALTER TABLE payments ALTER COLUMN booking_id DROP NOT NULL;

-- Add payment_session_id to payments table
ALTER TABLE payments ADD COLUMN payment_session_id UUID REFERENCES payment_sessions(id);
CREATE INDEX idx_payment_session_id ON payments(payment_session_id);

-- Comment explaining the new flow
COMMENT ON TABLE payment_sessions IS 'Stores checkout data temporarily. Bookings are created only after successful payment confirmation.';
