-- Add email fields to marketplace_invites for display purposes
-- This allows showing invitee/inviter info without additional lookups

-- 1. Add invitee_email column (the email used to send the invite)
ALTER TABLE marketplace.marketplace_invites
    ADD COLUMN invitee_email VARCHAR(255);

-- 2. Backfill existing invites (if any) - set to empty string as placeholder
UPDATE marketplace.marketplace_invites
SET invitee_email = ''
WHERE invitee_email IS NULL;

-- 3. Make invitee_email NOT NULL after backfill
ALTER TABLE marketplace.marketplace_invites
    ALTER COLUMN invitee_email SET NOT NULL;

-- 4. Create index for looking up invites by email
CREATE INDEX idx_marketplace_invites_invitee_email
    ON marketplace.marketplace_invites(invitee_email);
