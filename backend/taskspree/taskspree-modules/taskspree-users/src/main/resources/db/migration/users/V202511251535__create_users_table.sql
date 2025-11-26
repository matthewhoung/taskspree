-- 1. Create the Schema
CREATE SCHEMA IF NOT EXISTS users;

-- 2. Create the App Users Table
CREATE TABLE users.app_users (
    id UUID NOT NULL,
    identity_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

-- 3. Add Unique Constraints
ALTER TABLE users.app_users ADD CONSTRAINT uk_app_users_identity_id UNIQUE (identity_id);
ALTER TABLE users.app_users ADD CONSTRAINT uk_app_users_email UNIQUE (email);

-- 4. Create Indexes
CREATE INDEX idx_app_users_identity_id ON users.app_users(identity_id);
CREATE INDEX idx_app_users_email ON users.app_users(email);