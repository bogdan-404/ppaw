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

-- Jobs table
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'DONE', 'FAILED')),
    progress INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    error_message TEXT NULL
);

CREATE INDEX idx_jobs_status_created ON jobs(status, created_at);
CREATE INDEX idx_jobs_user_created ON jobs(user_id, created_at);

-- Usage logs table
CREATE TABLE usage_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('SUMMARIZE', 'REWRITE', 'BATCH')),
    chars_in INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    job_id UUID NULL REFERENCES jobs(id) ON DELETE SET NULL
);

CREATE INDEX idx_usage_logs_user_created ON usage_logs(user_id, created_at);

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

-- Job payloads table
CREATE TABLE job_payloads (
    job_id UUID PRIMARY KEY REFERENCES jobs(id) ON DELETE CASCADE,
    payload_json TEXT NOT NULL
);

-- Job results table
CREATE TABLE job_results (
    job_id UUID PRIMARY KEY REFERENCES jobs(id) ON DELETE CASCADE,
    result_text TEXT NOT NULL,
    metrics_json TEXT NULL,
    export_type VARCHAR(50) NOT NULL DEFAULT 'NONE' CHECK (export_type IN ('NONE', 'CSV', 'JSON')),
    export_path VARCHAR(500) NULL
);

-- Saved works table
CREATE TABLE saved_works (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    work_type VARCHAR(50) NOT NULL CHECK (work_type IN ('SUMMARIZE', 'REWRITE', 'BATCH')),
    input_text TEXT NOT NULL,
    output_text TEXT NOT NULL,
    style VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    job_id UUID NULL REFERENCES jobs(id) ON DELETE SET NULL
);

CREATE INDEX idx_saved_works_user_created ON saved_works(user_id, created_at);



