-- 1. Create the Schema
CREATE SCHEMA IF NOT EXISTS identity;

-- 2. Create Identity Users Table
CREATE TABLE identity.identity_users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

-- 3. Create Identity Roles Table
CREATE TABLE identity.identity_roles (
    id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id)
);

-- 4. Create Identity User Roles (Join Table)
CREATE TABLE identity.identity_user_roles (
    identity_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identity_id, role_id),
    CONSTRAINT fk_identity_user_roles_identity_id
        FOREIGN KEY (identity_id) REFERENCES identity.identity_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_identity_user_roles_role_id
        FOREIGN KEY (role_id) REFERENCES identity.identity_roles(id) ON DELETE CASCADE
);

-- 5. Add Unique Constraints
ALTER TABLE identity.identity_users ADD CONSTRAINT uk_identity_users_email UNIQUE (email);
ALTER TABLE identity.identity_roles ADD CONSTRAINT uk_identity_roles_role UNIQUE (role);

-- 6. Create Indexes
CREATE INDEX idx_identity_users_email ON identity.identity_users(email);
CREATE INDEX idx_identity_user_roles_identity_id ON identity.identity_user_roles(identity_id);
CREATE INDEX idx_identity_user_roles_role_id ON identity.identity_user_roles(role_id);