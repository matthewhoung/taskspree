-- 1. Add Account Status Fields to Identity Users
ALTER TABLE identity.identity_users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN disabled_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN disabled_reason VARCHAR(255);

-- 2. Create Identity Sessions Table (for Refresh Tokens)
CREATE TABLE identity.identity_sessions (
    id UUID NOT NULL,
    identity_id UUID NOT NULL,
    refresh_token VARCHAR(512) NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_reason VARCHAR(255),
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_identity_sessions_identity_id
        FOREIGN KEY (identity_id) REFERENCES identity.identity_users(id) ON DELETE CASCADE
);

-- 3. Add Unique Constraint on Refresh Token
ALTER TABLE identity.identity_sessions
    ADD CONSTRAINT uk_identity_sessions_refresh_token UNIQUE (refresh_token);

-- 4. Create Indexes for Performance
CREATE INDEX idx_identity_sessions_identity_id ON identity.identity_sessions(identity_id);
CREATE INDEX idx_identity_sessions_refresh_token ON identity.identity_sessions(refresh_token);
CREATE INDEX idx_identity_sessions_expires_at ON identity.identity_sessions(expires_at);
CREATE INDEX idx_identity_users_enabled ON identity.identity_users(enabled);