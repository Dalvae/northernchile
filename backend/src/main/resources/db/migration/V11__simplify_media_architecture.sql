-- V11: Simplify Media Architecture
-- Remove join tables (tour_media, schedule_media) and use direct FK relationships
-- This reduces query complexity and database load

-- Step 1: Add new columns to media table for gallery metadata
ALTER TABLE media ADD COLUMN IF NOT EXISTS display_order INT DEFAULT 0;
ALTER TABLE media ADD COLUMN IF NOT EXISTS is_hero BOOLEAN DEFAULT FALSE;
ALTER TABLE media ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;

-- Step 2: Migrate data from tour_media to media table
-- Update tour_id and gallery fields for media that are in tour_media
UPDATE media m
SET 
    tour_id = tm.tour_id,
    display_order = tm.display_order,
    is_hero = tm.is_hero,
    is_featured = tm.is_featured
FROM tour_media tm
WHERE m.id = tm.media_id
  AND m.tour_id IS NULL;  -- Only update if not already set

-- For media that already have tour_id set, just copy the gallery metadata
UPDATE media m
SET 
    display_order = COALESCE(tm.display_order, m.display_order),
    is_hero = COALESCE(tm.is_hero, m.is_hero),
    is_featured = COALESCE(tm.is_featured, m.is_featured)
FROM tour_media tm
WHERE m.id = tm.media_id
  AND m.tour_id IS NOT NULL;

-- Step 3: Migrate data from schedule_media to media table
-- Update schedule_id and display_order for media that are in schedule_media
UPDATE media m
SET 
    schedule_id = sm.schedule_id,
    display_order = sm.display_order
FROM schedule_media sm
WHERE m.id = sm.media_id
  AND m.schedule_id IS NULL;  -- Only update if not already set

-- For media that already have schedule_id set, just copy display_order
UPDATE media m
SET display_order = COALESCE(sm.display_order, m.display_order)
FROM schedule_media sm
WHERE m.id = sm.media_id
  AND m.schedule_id IS NOT NULL;

-- Step 4: Drop the join tables (they are no longer needed)
DROP TABLE IF EXISTS tour_media;
DROP TABLE IF EXISTS schedule_media;

-- Step 5: Add indexes for the new columns
CREATE INDEX IF NOT EXISTS idx_media_display_order ON media(tour_id, display_order) WHERE tour_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_media_schedule_display_order ON media(schedule_id, display_order) WHERE schedule_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_media_hero ON media(tour_id) WHERE is_hero = TRUE;

-- Step 6: Add constraint to ensure only one hero per tour
-- (The unique index above handles this)

-- Step 7: Update comments
COMMENT ON COLUMN media.display_order IS 'Order of media in tour/schedule gallery';
COMMENT ON COLUMN media.is_hero IS 'Whether this is the hero image for the tour';
COMMENT ON COLUMN media.is_featured IS 'Whether this media is featured in listings';
