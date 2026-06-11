-- =====================================================
-- V9: Create password_reset_tokens table
-- Description: Stores 6-digit numeric tokens for password reset
-- =====================================================

-- Drop table if exists (to ensure fresh start – optional)
-- DROP TABLE IF EXISTS password_reset_tokens CASCADE;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id         BIGSERIAL    PRIMARY KEY,
                                                     token      VARCHAR(6)   NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token
    ON password_reset_tokens(token);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user
    ON password_reset_tokens(user_id);

-- Optional: add a composite index for expiration cleanup
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires
    ON password_reset_tokens(expires_at)
    WHERE used = FALSE;

COMMENT ON TABLE password_reset_tokens IS 'Stores short-lived 6-digit tokens for password reset flow';
COMMENT ON COLUMN password_reset_tokens.token IS '6-digit numeric token (e.g., 482761)';
COMMENT ON COLUMN password_reset_tokens.user_id IS 'References the user requesting reset';
COMMENT ON COLUMN password_reset_tokens.expires_at IS 'Token becomes invalid after this timestamp';
COMMENT ON COLUMN password_reset_tokens.used IS 'Whether the token has been consumed';