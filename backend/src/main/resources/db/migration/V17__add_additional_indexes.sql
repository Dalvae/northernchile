-- V17: Additional performance indexes from code audit
-- Composite index for schedule generator batch query
-- Optimizes: findExistingScheduleDatesByTourIds(tourIds, start, end)
CREATE INDEX IF NOT EXISTS idx_tour_schedules_tour_start ON tour_schedules(tour_id, start_datetime);

-- Index for OAuth provider lookups
-- Optimizes: findByProviderAndProviderId(provider, providerId)
CREATE INDEX IF NOT EXISTS idx_users_provider_id ON users(provider_id) WHERE provider_id IS NOT NULL;
