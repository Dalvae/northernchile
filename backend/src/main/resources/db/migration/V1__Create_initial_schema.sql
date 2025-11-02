-- backend/src/main/resources/db/migration/V1__Create_initial_schema.sql
-- Initial database schema for Northern Chile tour booking platform
-- All TIMESTAMP columns store UTC time without timezone info
-- Application layer handles Chile timezone conversion (America/Santiago with DST)

-- Tabla: users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
COMMENT ON COLUMN users.created_at IS 'UTC timestamp - application handles timezone conversion';

-- Tabla: tours (Soporte multilingüe con JSONB)
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id),

    -- Campos multilingües con JSONB
    name_translations JSONB NOT NULL,
    description_translations JSONB,

    -- Reglas de negocio para sensibilidad climática
    wind_sensitive BOOLEAN DEFAULT FALSE,
    moon_sensitive BOOLEAN DEFAULT FALSE,
    cloud_sensitive BOOLEAN DEFAULT FALSE,

    -- Campos estándar
    content_key VARCHAR(100) UNIQUE,
    slug VARCHAR(255) UNIQUE,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(19,4) NOT NULL,
    default_max_participants INTEGER NOT NULL,
    duration_hours INTEGER NOT NULL,
    default_start_time TIME,
    recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(100),
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_tours_owner_id ON tours(owner_id);
CREATE INDEX idx_tours_status ON tours(status);
CREATE INDEX idx_tours_category ON tours(category);
CREATE INDEX idx_tours_slug ON tours(slug);
CREATE INDEX idx_tours_deleted_at ON tours(deleted_at);
COMMENT ON COLUMN tours.name_translations IS 'JSON object with language codes as keys: {"es": "Tour name", "en": "Tour name", "pt": "Nome do tour"}';
COMMENT ON COLUMN tours.slug IS 'SEO-friendly URL slug generated from tour name (e.g., "tour-astronomico")';
COMMENT ON COLUMN tours.price IS 'Price stored as NUMERIC(19,4) - minor units compatible with payment gateways. CLP uses 0 decimals.';
COMMENT ON COLUMN tours.created_at IS 'UTC timestamp - application handles timezone conversion';

-- Tabla: tour_images (Galería de fotos de tours)
CREATE TABLE tour_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    alt_text_translations JSONB,
    is_hero_image BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tour_images_tour_id ON tour_images(tour_id);
CREATE INDEX idx_tour_images_hero ON tour_images(is_hero_image) WHERE is_hero_image = TRUE;

-- Tabla: tour_schedules (Instancias específicas de tours)
CREATE TABLE tour_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL REFERENCES tours(id),
    start_datetime TIMESTAMP NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_guide_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tour_id, start_datetime)
);
CREATE INDEX idx_tour_schedules_tour_id ON tour_schedules(tour_id);
CREATE INDEX idx_tour_schedules_start_datetime ON tour_schedules(start_datetime);
CREATE INDEX idx_tour_schedules_status ON tour_schedules(status);
COMMENT ON COLUMN tour_schedules.start_datetime IS 'UTC timestamp - represents exact moment in time. Application converts to/from Chile timezone (America/Santiago).';

-- Tabla: bookings
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    tour_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    subtotal DECIMAL(19,4) NOT NULL,
    tax_amount DECIMAL(19,4) NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    language_code VARCHAR(5) NOT NULL DEFAULT 'es',
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_schedule_id ON bookings(schedule_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_tour_date ON bookings(tour_date);
CREATE INDEX idx_bookings_deleted_at ON bookings(deleted_at);
COMMENT ON COLUMN bookings.subtotal IS 'Stored as NUMERIC(19,4) for precision. All monetary calculations must use this type.';
COMMENT ON COLUMN bookings.tax_amount IS 'IVA amount stored as NUMERIC(19,4) for precision.';
COMMENT ON COLUMN bookings.total_amount IS 'Total amount stored as NUMERIC(19,4) for precision.';

-- Tabla: participants
CREATE TABLE participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    document_id VARCHAR(100) NOT NULL,
    nationality VARCHAR(100),
    age INTEGER,
    pickup_address VARCHAR(500),
    special_requirements TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_participants_booking_id ON participants(booking_id);
CREATE INDEX idx_participants_document_id ON participants(document_id);
COMMENT ON TABLE participants IS 'All participants are treated equally, no age-based pricing distinction';

-- Tabla: carts (Persistent shopping carts)
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_expires_at ON carts(expires_at);

-- Tabla: cart_items
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    num_participants INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_schedule_id ON cart_items(schedule_id);

-- Tabla: private_tour_requests
CREATE TABLE private_tour_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    requested_tour_type VARCHAR(100) NOT NULL,
    requested_start_datetime TIMESTAMP NOT NULL,
    num_participants INTEGER NOT NULL DEFAULT 1,
    special_requests TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    quoted_price DECIMAL(19,4),
    payment_link_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_private_requests_status ON private_tour_requests(status);
CREATE INDEX idx_private_requests_email ON private_tour_requests(customer_email);
COMMENT ON COLUMN private_tour_requests.requested_start_datetime IS 'UTC timestamp - client requested date/time';

-- Tabla: weather_alerts (Alertas de cambios climáticos)
CREATE TABLE weather_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_schedule_id UUID NOT NULL REFERENCES tour_schedules(id) ON DELETE CASCADE,
    alert_type VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    resolution VARCHAR(50),
    wind_speed DOUBLE PRECISION,
    cloud_coverage INTEGER,
    moon_phase DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_weather_alerts_schedule_id ON weather_alerts(tour_schedule_id);
CREATE INDEX idx_weather_alerts_status ON weather_alerts(status);
CREATE INDEX idx_weather_alerts_created_at ON weather_alerts(created_at);
COMMENT ON TABLE weather_alerts IS 'Automated alerts for weather conditions affecting tours';
COMMENT ON COLUMN weather_alerts.alert_type IS 'WIND, CLOUDS, or MOON';
COMMENT ON COLUMN weather_alerts.severity IS 'WARNING or CRITICAL';
COMMENT ON COLUMN weather_alerts.resolution IS 'CANCELLED, KEPT, or RESCHEDULED';

-- Tabla: audit_logs (Registro de auditoría completo)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    user_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    entity_description TEXT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
COMMENT ON TABLE audit_logs IS 'Audit trail for all critical operations (CRUD on tours, bookings, users, etc.)';
COMMENT ON COLUMN audit_logs.action IS 'CREATE, UPDATE, DELETE, PASSWORD_CHANGE, ADMIN_PASSWORD_RESET, etc.';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity: TOUR, USER, BOOKING, SCHEDULE, etc.';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON snapshot of entity before change';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON snapshot of entity after change';
