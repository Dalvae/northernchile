-- Add date_of_birth column to participants table
-- This will be the source of truth for calculating age
ALTER TABLE participants ADD COLUMN date_of_birth DATE;

-- Note: age column will remain for backward compatibility and can be calculated/updated
-- Later we can make it a computed/generated column or remove it entirely
