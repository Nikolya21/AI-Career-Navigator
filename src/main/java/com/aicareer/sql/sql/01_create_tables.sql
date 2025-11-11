CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    vacancy_now VARCHAR(255),
    roadmap_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cv_data (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    information TEXT,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_compliance_percentage INTEGER,
    skills_json TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);