-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Plans table
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    price_cents INT NOT NULL DEFAULT 0,
    billing_period VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Plan limits table
CREATE TABLE plan_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    key VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    UNIQUE(plan_id, key)
);

-- Subscriptions table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id),
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE', 'CANCELED', 'EXPIRED')),
    start_at TIMESTAMP NOT NULL DEFAULT NOW(),
    end_at TIMESTAMP NULL
);

CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, status);

-- Usage logs table
CREATE TABLE usage_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('SUMMARIZE', 'REWRITE', 'BATCH')),
    chars_in INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    job_id UUID NULL
);

CREATE INDEX idx_usage_logs_user_created ON usage_logs(user_id, created_at);

-- Saved works table (with logical delete)
CREATE TABLE saved_works (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    work_type VARCHAR(50) NOT NULL CHECK (work_type IN ('SUMMARIZE', 'REWRITE', 'BATCH')),
    input_text TEXT NOT NULL,
    output_text TEXT NOT NULL,
    style VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_saved_works_user_created ON saved_works(user_id, created_at);
CREATE INDEX idx_saved_works_deleted_at ON saved_works(deleted_at);

-- Tone rule sets table
CREATE TABLE tone_rule_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    rules_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tone_rule_sets_user ON tone_rule_sets(user_id);

-- Insert seed data: Plans
INSERT INTO plans (code, name, price_cents, billing_period, is_active) VALUES
    ('FREE', 'Free Plan', 0, 'MONTHLY', TRUE),
    ('USUAL', 'Usual Plan', 999, 'MONTHLY', TRUE),
    ('PREMIUM', 'Premium Plan', 2999, 'MONTHLY', TRUE);

-- Insert plan limits for FREE plan
INSERT INTO plan_limits (plan_id, key, value)
SELECT id, 'daily_requests', '3' FROM plans WHERE code = 'FREE'
UNION ALL
SELECT id, 'max_chars', '2000' FROM plans WHERE code = 'FREE'
UNION ALL
SELECT id, 'allowed_styles', 'simple' FROM plans WHERE code = 'FREE'
UNION ALL
SELECT id, 'batch_enabled', 'false' FROM plans WHERE code = 'FREE'
UNION ALL
SELECT id, 'export_enabled', 'false' FROM plans WHERE code = 'FREE'
UNION ALL
SELECT id, 'tone_rules_enabled', 'false' FROM plans WHERE code = 'FREE';

-- Insert plan limits for USUAL plan
INSERT INTO plan_limits (plan_id, key, value)
SELECT id, 'monthly_requests', '200' FROM plans WHERE code = 'USUAL'
UNION ALL
SELECT id, 'max_chars', '20000' FROM plans WHERE code = 'USUAL'
UNION ALL
SELECT id, 'allowed_styles', 'simple,formal,academic,bullet_points,friendly' FROM plans WHERE code = 'USUAL'
UNION ALL
SELECT id, 'batch_enabled', 'false' FROM plans WHERE code = 'USUAL'
UNION ALL
SELECT id, 'export_enabled', 'false' FROM plans WHERE code = 'USUAL'
UNION ALL
SELECT id, 'tone_rules_enabled', 'false' FROM plans WHERE code = 'USUAL';

-- Insert plan limits for PREMIUM plan
INSERT INTO plan_limits (plan_id, key, value)
SELECT id, 'unlimited_requests', 'true' FROM plans WHERE code = 'PREMIUM'
UNION ALL
SELECT id, 'max_chars', '999999' FROM plans WHERE code = 'PREMIUM'
UNION ALL
SELECT id, 'allowed_styles', 'simple,formal,academic,bullet_points,friendly,casual,professional' FROM plans WHERE code = 'PREMIUM'
UNION ALL
SELECT id, 'batch_enabled', 'true' FROM plans WHERE code = 'PREMIUM'
UNION ALL
SELECT id, 'export_enabled', 'true' FROM plans WHERE code = 'PREMIUM'
UNION ALL
SELECT id, 'tone_rules_enabled', 'true' FROM plans WHERE code = 'PREMIUM';

-- Insert admin user (password: admin)
INSERT INTO users (email, password_hash, role) VALUES
    ('admin@local', 'admin', 'ADMIN');

-- Insert default test user (password: user)
INSERT INTO users (email, password_hash, role) VALUES
    ('user@local', 'user', 'USER');
