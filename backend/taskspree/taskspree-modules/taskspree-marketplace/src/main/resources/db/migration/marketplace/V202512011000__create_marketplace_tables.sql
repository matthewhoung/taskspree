-- 1. Create the Schema
CREATE SCHEMA IF NOT EXISTS marketplace;

-- 2. Create Marketplaces Table
CREATE TABLE marketplace.marketplaces (
    id UUID NOT NULL,
    owner_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) NOT NULL,
    logo_file_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Settings
    default_task_duration_days INTEGER NOT NULL DEFAULT 7,
    auto_close_slots_percentage INTEGER NOT NULL DEFAULT 80,
    reservation_timeout_days INTEGER NOT NULL DEFAULT 3,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    PRIMARY KEY (id)
);

-- 3. Create Marketplace Roles Table (3 per marketplace: OWNER, MANAGER, MEMBER)
CREATE TABLE marketplace.marketplace_roles (
    id UUID NOT NULL,
    marketplace_id UUID NOT NULL,
    role_type VARCHAR(20) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_marketplace_roles_marketplace_id
        FOREIGN KEY (marketplace_id) REFERENCES marketplace.marketplaces(id) ON DELETE CASCADE,
    CONSTRAINT uk_marketplace_roles_type
        UNIQUE (marketplace_id, role_type)
);

-- 4. Create Marketplace Members Table
CREATE TABLE marketplace.marketplace_members (
    id UUID NOT NULL,
    marketplace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_marketplace_members_marketplace_id
        FOREIGN KEY (marketplace_id) REFERENCES marketplace.marketplaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_marketplace_members_role_id
        FOREIGN KEY (role_id) REFERENCES marketplace.marketplace_roles(id),
    CONSTRAINT uk_marketplace_members_user
        UNIQUE (marketplace_id, user_id)
);

-- 5. Create Marketplace Invites Table
CREATE TABLE marketplace.marketplace_invites (
    id UUID NOT NULL,
    marketplace_id UUID NOT NULL,
    invitee_user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    invited_by_user_id UUID NOT NULL,
    token VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,

    PRIMARY KEY (id),
    CONSTRAINT fk_marketplace_invites_marketplace_id
        FOREIGN KEY (marketplace_id) REFERENCES marketplace.marketplaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_marketplace_invites_role_id
        FOREIGN KEY (role_id) REFERENCES marketplace.marketplace_roles(id),
    CONSTRAINT uk_marketplace_invites_token
        UNIQUE (token)
);

-- 6. Add Unique Constraints
ALTER TABLE marketplace.marketplaces ADD CONSTRAINT uk_marketplaces_slug UNIQUE (slug);

-- 7. Create Indexes
CREATE INDEX idx_marketplaces_owner_id ON marketplace.marketplaces(owner_id);
CREATE INDEX idx_marketplaces_slug ON marketplace.marketplaces(slug);
CREATE INDEX idx_marketplaces_status ON marketplace.marketplaces(status);

CREATE INDEX idx_marketplace_roles_marketplace_id ON marketplace.marketplace_roles(marketplace_id);

CREATE INDEX idx_marketplace_members_marketplace_id ON marketplace.marketplace_members(marketplace_id);
CREATE INDEX idx_marketplace_members_user_id ON marketplace.marketplace_members(user_id);
CREATE INDEX idx_marketplace_members_status ON marketplace.marketplace_members(status);

CREATE INDEX idx_marketplace_invites_marketplace_id ON marketplace.marketplace_invites(marketplace_id);
CREATE INDEX idx_marketplace_invites_invitee_user_id ON marketplace.marketplace_invites(invitee_user_id);
CREATE INDEX idx_marketplace_invites_token ON marketplace.marketplace_invites(token);
CREATE INDEX idx_marketplace_invites_status ON marketplace.marketplace_invites(status);