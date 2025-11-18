CREATE TABLE IF NOT EXISTS aicareer.weeks (
    id BIGSERIAL PRIMARY KEY,
    roadmap_zone_id BIGINT NOT NULL,
    week_number INTEGER NOT NULL,
    goal TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_weeks_roadmap_zone FOREIGN KEY (roadmap_zone_id) REFERENCES roadmap_zones(id) ON DELETE CASCADE
);