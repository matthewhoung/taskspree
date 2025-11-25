-- 1. Create the Schema
CREATE SCHEMA IF NOT EXISTS users;

-- 2. Create the Table (Matches your User.java entity)
CREATE TABLE users.users (
    id uuid NOT NULL,
    email varchar(255) NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    identity_id varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

-- 3. Add Constraints (Unique Email & Identity)
ALTER TABLE users.users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users.users ADD CONSTRAINT uk_users_identity_id UNIQUE (identity_id);