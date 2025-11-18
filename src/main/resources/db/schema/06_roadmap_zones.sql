CREATE TABLE IF NOT EXISTS aicareer.roadmap_zones (
    id BIGSERIAL PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    learning_goal TEXT,
    complexity_level VARCHAR(100),
    zone_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmap_zones_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE
);