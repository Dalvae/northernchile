-- backend/src/main/resources/db/migration/V1__Create_initial_schema.sql
-- Tabla: users (Almacena Clientes y Administradores)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- NULLABLE para usuarios de Google
    full_name VARCHAR(255) NOT NULL,
    nationality VARCHAR(100),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ROLE_CLIENT', 'ROLE_PARTNER_ADMIN', 'ROLE_SUPER_ADMIN')),
    auth_provider VARCHAR(50) DEFAULT 'LOCAL' CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    provider_id VARCHAR(255), -- ID único del proveedor OAuth2
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: tours (El catálogo padre con reglas de recurrencia)
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id), -- Creador del tour (Alex o David)
    name VARCHAR(200) NOT NULL,
    description TEXT,
    content_key VARCHAR(100) UNIQUE, -- NULLABLE. Clave para contenido "codeado"
    category VARCHAR(50) NOT NULL CHECK (category IN ('ASTRONOMICAL', 'REGULAR', 'SPECIAL', 'PRIVATE')),
    price_adult DECIMAL(10,2) NOT NULL,
    price_child DECIMAL(10,2),
    default_max_participants INTEGER NOT NULL,
    duration_hours INTEGER NOT NULL,
    is_wind_sensitive BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(100), -- Regla CRON si es recurrente
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tours_owner_id ON tours(owner_id);
CREATE INDEX idx_tours_status ON tours(status);

-- Tabla: tour_schedules (Instancias específicas de tours programados)
CREATE TABLE tour_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL REFERENCES tours(id),
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED')),
    assigned_guide_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tour_id, start_datetime)
);

CREATE INDEX idx_tour_schedules_datetime ON tour_schedules(start_datetime);
CREATE INDEX idx_tour_schedules_tour_status ON tour_schedules(tour_id, status);

-- Tabla: bookings (Reservas realizadas por usuarios)
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    tour_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED_BY_CLIENT', 'CANCELLED_BY_ADMIN', 'AWAITING_REFUND', 'REFUNDED')),
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    language_code VARCHAR(5) NOT NULL DEFAULT 'es',
    special_requests TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_tour_date ON bookings(tour_date);

-- Tabla: participants (Personas que asisten a los tours)
CREATE TABLE participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('ADULT', 'CHILD')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_participants_booking_id ON participants(booking_id);

-- Tabla: carts (Carritos de compra persistentes)
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id), -- NULLABLE para carritos de invitado
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED')),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_expires_at ON carts(expires_at);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    schedule_id UUID NOT NULL REFERENCES tour_schedules(id),
    num_adults INTEGER NOT NULL,
    num_children INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: private_tour_requests (Solicitudes de tours privados)
CREATE TABLE private_tour_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    requested_tour_type VARCHAR(100) NOT NULL,
    requested_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    num_adults INTEGER NOT NULL,
    num_children INTEGER DEFAULT 0,
    special_requests TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'QUOTED', 'CONFIRMED', 'REJECTED')),
    quoted_price DECIMAL(10,2),
    payment_link_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
