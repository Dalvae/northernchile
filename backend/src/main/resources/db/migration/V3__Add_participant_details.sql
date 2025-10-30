-- V3: Agregar campos detallados a la tabla participants

-- Agregar campos de información del participante
ALTER TABLE participants
ADD COLUMN document_id VARCHAR(100),
ADD COLUMN nationality VARCHAR(100),
ADD COLUMN age INTEGER,
ADD COLUMN pickup_address VARCHAR(500),
ADD COLUMN special_requirements TEXT;

-- Crear índice para búsquedas por documento
CREATE INDEX idx_participants_document_id ON participants(document_id);

-- Comentarios para documentación
COMMENT ON COLUMN participants.document_id IS 'Documento de identidad del participante (pasaporte, DNI, etc.)';
COMMENT ON COLUMN participants.nationality IS 'Nacionalidad del participante';
COMMENT ON COLUMN participants.age IS 'Edad del participante al momento de la reserva';
COMMENT ON COLUMN participants.pickup_address IS 'Dirección de recogida en San Pedro de Atacama (hotel, hostal, etc.)';
COMMENT ON COLUMN participants.special_requirements IS 'Requisitos especiales (alergias, movilidad reducida, etc.)';
