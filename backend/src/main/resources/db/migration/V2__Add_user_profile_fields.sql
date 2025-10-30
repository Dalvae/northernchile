-- V2: Agregar campos adicionales de perfil a la tabla users

-- Agregar teléfono y fecha de nacimiento
ALTER TABLE users
ADD COLUMN phone_number VARCHAR(20),
ADD COLUMN date_of_birth DATE;

-- Crear índice para optimizar búsquedas por teléfono
CREATE INDEX idx_users_phone_number ON users(phone_number);

-- Comentarios para documentación
COMMENT ON COLUMN users.phone_number IS 'Número de teléfono del usuario (formato internacional recomendado)';
COMMENT ON COLUMN users.date_of_birth IS 'Fecha de nacimiento del usuario para calcular edad y restricciones de tours';
