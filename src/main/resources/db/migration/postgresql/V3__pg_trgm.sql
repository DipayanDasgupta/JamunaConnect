-- PostgreSQL-specific: pg_trgm gives fuzzy, typo-tolerant lookup. The
-- GIN indexes keep the similarity search fast on the seeded allocation list.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_room_number_trgm ON room USING gin (room_number gin_trgm_ops);
CREATE INDEX idx_room_occupants_trgm ON room USING gin (occupants gin_trgm_ops);
CREATE INDEX idx_resident_name_trgm ON resident USING gin (name gin_trgm_ops);