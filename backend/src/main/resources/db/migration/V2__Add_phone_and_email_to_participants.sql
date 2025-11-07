-- Add phone_number and email columns to participants table
ALTER TABLE participants
    ADD COLUMN phone_number VARCHAR(20),
    ADD COLUMN email VARCHAR(255);

-- Add indexes for performance
CREATE INDEX idx_participants_phone ON participants(phone_number);
CREATE INDEX idx_participants_email ON participants(email);
