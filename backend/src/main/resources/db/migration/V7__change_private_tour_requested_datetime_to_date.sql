-- Change requested_start_datetime from TIMESTAMP WITH TIME ZONE to DATE
-- Since we only need the preferred date, not a specific time

ALTER TABLE private_tour_requests
ALTER COLUMN requested_start_datetime TYPE DATE
USING requested_start_datetime::DATE;
