-- Add structured content fields to tours table
-- These fields enable rich content management directly from the admin panel

-- Simple guide name field (optional, no translation needed)
ALTER TABLE tours
ADD COLUMN guide_name VARCHAR(255);

-- JSONB fields for multilingual structured content
ALTER TABLE tours
ADD COLUMN itinerary_translations JSONB,
ADD COLUMN equipment_translations JSONB,
ADD COLUMN additional_info_translations JSONB;

-- Add comments for documentation
COMMENT ON COLUMN tours.guide_name IS 'Optional guide name (e.g., "Alex", "David")';
COMMENT ON COLUMN tours.itinerary_translations IS 'Tour itinerary per language: { "es": [{ "time": "19:30", "description": "..." }], "en": [...], "pt": [...] }';
COMMENT ON COLUMN tours.equipment_translations IS 'Equipment list per language: { "es": ["item1", "item2"], "en": [...], "pt": [...] }';
COMMENT ON COLUMN tours.additional_info_translations IS 'Additional info/recommendations per language: { "es": ["info1", "info2"], "en": [...], "pt": [...] }';

-- Create GIN indexes for efficient JSONB queries
CREATE INDEX idx_tours_itinerary_translations ON tours USING GIN (itinerary_translations);
CREATE INDEX idx_tours_equipment_translations ON tours USING GIN (equipment_translations);
CREATE INDEX idx_tours_additional_info_translations ON tours USING GIN (additional_info_translations);
