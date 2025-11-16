-- Northern Chile Database Schema - Initial Migration
-- Generated from JPA entities
-- PostgreSQL 15+

-- Table: users
-- Core user table with authentication and profile data
CREATE TABLE users (
    id UUID NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(255),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    role VARCHAR(50) NOT NULL,
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    deleted_at TIMESTAMP(6) WITH TIME ZONE
);

-- Index for soft delete queries
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Table: tours
-- Tour catalog with multilingual content and recurrence rules
CREATE TABLE tours (
    id UUID NOT NULL PRIMARY KEY,
    owner_id UUID NOT NULL,
    name_translations JSONB NOT NULL,
    description_blocks_translations JSONB,
    itinerary_translations JSONB,
    equipment_translations JSONB,
    additional_info_translations JSONB,
    wind_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    moon_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    cloud_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    content_key VARCHAR(100) UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    guide_name VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    price NUMERIC(19,4) NOT NULL,
    default_max_participants INTEGER NOT NULL,
    duration_hours INTEGER NOT NULL,
    default_start_time TIME(6),
    recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP(6) WITH TIME ZONE,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    deleted_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_tours_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Indexes for tours
CREATE INDEX idx_tours_owner_id ON tours(owner_id);
CREATE INDEX idx_tours_status ON tours(status);
CREATE INDEX idx_tours_category ON tours(category);
CREATE INDEX idx_tours_deleted_at ON tours(deleted_at);

-- Table: tour_schedules
-- Specific date/time instances for bookable tours
CREATE TABLE tour_schedules (
    id UUID NOT NULL PRIMARY KEY,
    tour_id UUID NOT NULL,
    start_datetime TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_guide_id UUID,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_tour_schedules_tour FOREIGN KEY (tour_id) REFERENCES tours(id),
    CONSTRAINT fk_tour_schedules_guide FOREIGN KEY (assigned_guide_id) REFERENCES users(id),
    CONSTRAINT uk_tour_schedule_datetime UNIQUE (tour_id, start_datetime)
);

-- Indexes for tour_schedules
CREATE INDEX idx_tour_schedules_tour_id ON tour_schedules(tour_id);
CREATE INDEX idx_tour_schedules_start_datetime ON tour_schedules(start_datetime);
CREATE INDEX idx_tour_schedules_status ON tour_schedules(status);

-- Table: tour_images
-- Gallery images for tours with display order
CREATE TABLE tour_images (
    id UUID NOT NULL PRIMARY KEY,
    tour_id UUID,
    image_url TEXT NOT NULL,
    alt_text_translations JSONB,
    display_order INTEGER NOT NULL DEFAULT 0,
    hero_image BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_tour_images_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
);

-- Index for tour_images
CREATE INDEX idx_tour_images_tour_id ON tour_images(tour_id);

-- Table: bookings
-- Customer reservations for tour schedules
CREATE TABLE bookings (
    id UUID NOT NULL PRIMARY KEY,
    schedule_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tour_date DATE NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    subtotal NUMERIC(19,4) NOT NULL,
    tax_amount NUMERIC(19,4) NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL,
    status VARCHAR(30),
    special_requests TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    deleted_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_bookings_schedule FOREIGN KEY (schedule_id) REFERENCES tour_schedules(id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes for bookings
CREATE INDEX idx_bookings_schedule_id ON bookings(schedule_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_tour_date ON bookings(tour_date);

-- Table: participants
-- People attending tours (may or may not have user accounts)
CREATE TABLE participants (
    id UUID NOT NULL PRIMARY KEY,
    booking_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(20),
    document_id VARCHAR(100) NOT NULL,
    nationality VARCHAR(100),
    date_of_birth DATE,
    age INTEGER,
    pickup_address VARCHAR(500),
    special_requirements TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_participants_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Index for participants
CREATE INDEX idx_participants_booking_id ON participants(booking_id);

-- Table: payments
-- Payment transactions for bookings
CREATE TABLE payments (
    id UUID NOT NULL PRIMARY KEY,
    booking_id UUID NOT NULL,
    provider VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    external_payment_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CLP',
    payment_url TEXT,
    details_url TEXT,
    qr_code TEXT,
    pix_code TEXT,
    token VARCHAR(500),
    expires_at TIMESTAMP(6) WITH TIME ZONE,
    provider_response JSONB,
    error_message TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Indexes for payments
CREATE INDEX idx_payment_booking ON payments(booking_id);
CREATE INDEX idx_payment_external_id ON payments(external_payment_id);
CREATE INDEX idx_payment_status ON payments(status);

-- Table: carts
-- Persistent shopping carts for users
CREATE TABLE carts (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID,
    status VARCHAR(20),
    created_at TIMESTAMP(6) WITH TIME ZONE,
    expires_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index for carts
CREATE INDEX idx_carts_user_id ON carts(user_id);

-- Table: cart_items
-- Items in shopping carts
CREATE TABLE cart_items (
    id UUID NOT NULL PRIMARY KEY,
    cart_id UUID NOT NULL,
    schedule_id UUID NOT NULL,
    num_participants INTEGER NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_schedule FOREIGN KEY (schedule_id) REFERENCES tour_schedules(id)
);

-- Indexes for cart_items
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_schedule_id ON cart_items(schedule_id);

-- Table: email_verification_tokens
-- Email verification tokens (24-hour expiration)
CREATE TABLE email_verification_tokens (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for email_verification_tokens
CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);

-- Table: password_reset_tokens
-- Password reset tokens (2-hour expiration)
CREATE TABLE password_reset_tokens (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for password_reset_tokens
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- Table: private_tour_requests
-- Custom private tour inquiries
CREATE TABLE private_tour_requests (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(255),
    requested_tour_type VARCHAR(255) NOT NULL,
    requested_start_datetime TIMESTAMP(6) WITH TIME ZONE,
    num_participants INTEGER NOT NULL,
    special_requests TEXT,
    status VARCHAR(255),
    quoted_price NUMERIC(38,2),
    payment_link_id VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_private_tour_requests_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index for private_tour_requests
CREATE INDEX idx_private_tour_requests_user_id ON private_tour_requests(user_id);
CREATE INDEX idx_private_tour_requests_status ON private_tour_requests(status);

-- Table: weather_alerts
-- Automated weather and lunar phase alerts
CREATE TABLE weather_alerts (
    id UUID NOT NULL PRIMARY KEY,
    tour_schedule_id UUID NOT NULL,
    alert_type VARCHAR(255) NOT NULL,
    severity VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    message TEXT,
    wind_speed DOUBLE PRECISION,
    cloud_coverage INTEGER,
    moon_phase DOUBLE PRECISION,
    resolution VARCHAR(255),
    resolved_by VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT fk_weather_alerts_schedule FOREIGN KEY (tour_schedule_id) REFERENCES tour_schedules(id) ON DELETE CASCADE
);

-- Index for weather_alerts
CREATE INDEX idx_weather_alerts_schedule_id ON weather_alerts(tour_schedule_id);
CREATE INDEX idx_weather_alerts_status ON weather_alerts(status);

-- Table: audit_logs
-- System activity tracking and audit trail
CREATE TABLE audit_logs (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    entity_description TEXT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes for audit_logs
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
