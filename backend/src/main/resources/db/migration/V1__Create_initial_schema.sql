-- backend/src/main/resources/db/migration/V1__Create_initial_schema.sql

-- Tabla: users (Sin cambios)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL, password_hash VARCHAR(255), full_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(100), role VARCHAR(50) NOT NULL, auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255), created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    phone_number VARCHAR(20),
    date_of_birth DATE
);
CREATE INDEX idx_users_phone_number ON users(phone_number);


-- Tabla: tours (MODIFICADA para soportar JSONB y nuevas reglas)
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id),
    
    -- CAMPOS MULTILINGÜES CON JSONB
    name_translations JSONB NOT NULL,
    description_translations JSONB,
    
    -- REGLAS DE NEGOCIO
    is_wind_sensitive BOOLEAN DEFAULT FALSE,
    is_moon_sensitive BOOLEAN DEFAULT FALSE,
    is_cloud_sensitive BOOLEAN DEFAULT FALSE, -- NUEVO CAMPO
    
    -- CAMPOS ESTÁNDAR (sin cambios)
    content_key VARCHAR(100) UNIQUE,
    category VARCHAR(50) NOT NULL, price_adult DECIMAL(10,2) NOT NULL, price_child DECIMAL(10,2),
    default_max_participants INTEGER NOT NULL, duration_hours INTEGER NOT NULL,
    default_start_time TIME DEFAULT '20:00:00', -- Horario por defecto para generar schedules
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(100), status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tours_owner_id ON tours(owner_id);

-- NUEVA TABLA: tour_images (Para la galería de fotos)
CREATE TABLE tour_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    alt_text_translations JSONB, -- Texto alternativo para SEO en varios idiomas
    is_hero_image BOOLEAN DEFAULT FALSE, -- Para marcar la imagen principal
    display_order INT DEFAULT 0, -- Para ordenar la galería
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tour_images_tour_id ON tour_images(tour_id);

-- EL RESTO DE TABLAS SE MANTIENEN IGUAL (schedules, bookings, etc.)
CREATE TABLE tour_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tour_id UUID NOT NULL REFERENCES tours(id),
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL, max_participants INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN', assigned_guide_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, UNIQUE(tour_id, start_datetime)
);
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID NOT NULL REFERENCES users(id),
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id), tour_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING', subtotal DECIMAL(10,2) NOT NULL, tax_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL, language_code VARCHAR(5) NOT NULL DEFAULT 'es',
    special_requests TEXT, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    document_id VARCHAR(100),
    nationality VARCHAR(100),
    age INTEGER,
    pickup_address VARCHAR(500),
    special_requirements TEXT
);
CREATE INDEX idx_participants_document_id ON participants(document_id);
COMMENT ON TABLE participants IS 'Todos los participantes son tratados igual, sin distinción de edad';

CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), user_id UUID REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'ACTIVE', expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    num_adults INTEGER NOT NULL,
    num_children INTEGER DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE private_tour_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL, customer_phone VARCHAR(50), requested_tour_type VARCHAR(100) NOT NULL,
    requested_datetime TIMESTAMP WITH TIME ZONE NOT NULL, num_adults INTEGER NOT NULL, num_children INTEGER DEFAULT 0,
    special_requests TEXT, status VARCHAR(20) DEFAULT 'PENDING', quoted_price DECIMAL(10,2),
    payment_link_id VARCHAR(255), created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: weather_alerts (Para alertas de cambios climáticos)
CREATE TABLE weather_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_schedule_id UUID NOT NULL REFERENCES tour_schedules(id) ON DELETE CASCADE,
    alert_type VARCHAR(20) NOT NULL, -- 'WIND', 'CLOUDS', 'MOON'
    severity VARCHAR(20) NOT NULL, -- 'WARNING', 'CRITICAL'
    message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'REVIEWED', 'RESOLVED'
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(255), -- UUID del admin
    resolution VARCHAR(50), -- 'CANCELLED', 'KEPT', 'RESCHEDULED'
    wind_speed DOUBLE PRECISION, -- m/s
    cloud_coverage INTEGER, -- %
    moon_phase DOUBLE PRECISION -- 0.0-1.0
);
CREATE INDEX idx_weather_alerts_schedule_id ON weather_alerts(tour_schedule_id);
CREATE INDEX idx_weather_alerts_status ON weather_alerts(status);
