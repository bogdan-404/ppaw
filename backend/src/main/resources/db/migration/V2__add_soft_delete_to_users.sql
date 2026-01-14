-- Add is_deleted column to users table for soft delete support
ALTER TABLE users ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_users_is_deleted ON users(is_deleted);
