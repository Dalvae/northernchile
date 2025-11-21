-- V3: Add Featured Images + Remove Old System
-- Adds is_featured column for highlighting tour images
-- Removes old tour_images table (data will be lost, test photos only)

-- Step 1: Drop old table from dual system
DROP TABLE IF EXISTS tour_images;

-- Step 2: Add is_featured column to tour_media (idempotent)
ALTER TABLE tour_media
ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE;

-- Step 3: Add index for efficient featured queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_tour_media_featured ON tour_media(tour_id) WHERE is_featured = TRUE;

-- Comments for documentation
COMMENT ON COLUMN tour_media.is_featured IS 'Whether this image is featured/highlighted (non-exclusive, 3-5 recommended per tour)';
