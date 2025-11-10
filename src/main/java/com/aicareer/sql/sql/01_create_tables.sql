DROP TABLE IF EXISTS learning_state;
DROP TABLE IF EXISTS cv_data;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),

    cv_information TEXT,  -- может быть NULL при регистрации

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cv_data (
    id SERIAL PRIMARY KEY,
    user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE learning_state (
    user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    current_week_number INTEGER DEFAULT 1,
    roadmap_id INTEGER,
    last_ai_dialog_summary TEXT,
    updated_at TIMESTAMP DEFAULT NOW()
);