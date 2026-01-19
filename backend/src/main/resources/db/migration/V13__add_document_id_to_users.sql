-- Add document_id column to users table for ID/passport storage
ALTER TABLE users ADD COLUMN document_id VARCHAR(50);
