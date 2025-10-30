-- V2: Add soft delete support and audit logging

-- Add deleted_at column to tours for soft delete
ALTER TABLE tours ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_tours_deleted_at ON tours(deleted_at);

-- Add deleted_at column to users for soft delete
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Add deleted_at column to bookings for soft delete
ALTER TABLE bookings ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_bookings_deleted_at ON bookings(deleted_at);

-- Create audit_logs table for tracking all admin actions
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

COMMENT ON TABLE audit_logs IS 'Audit log for tracking all admin actions in the system';
COMMENT ON COLUMN audit_logs.action IS 'Action performed: CREATE, UPDATE, DELETE, RESTORE, etc.';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity: TOUR, USER, BOOKING, SCHEDULE, etc.';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON snapshot of entity before change';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON snapshot of entity after change';
