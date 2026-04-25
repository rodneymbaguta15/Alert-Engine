-- =====================================================================
-- V1__init_schema.sql
-- Initial schema for the price alert engine.
-- Includes users table (stub for Phase 1, fully used in Phase 7 w/ JWT).
-- =====================================================================

-- ---------------------------------------------------------------------
-- users: single seeded user for Phase 1, ready for multi-tenancy later.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),                        -- nullable for now; required when JWT lands
    display_name    VARCHAR(100),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- Seed a default trader so Phase 1 CRUD has a valid user_id to attach alerts to.
INSERT INTO users (email, display_name)
VALUES ('default@local.dev', 'Default Trader');


-- ---------------------------------------------------------------------
-- alert_configs: trader-defined thresholds per ticker.
-- Each row is one rule: "notify me when AAPL goes above 200".
-- ---------------------------------------------------------------------
CREATE TABLE alert_configs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticker              VARCHAR(10)    NOT NULL,
    threshold_price     NUMERIC(18, 4) NOT NULL CHECK (threshold_price > 0),
    direction           VARCHAR(10)    NOT NULL CHECK (direction IN ('ABOVE', 'BELOW')),
    cooldown_seconds    INTEGER        NOT NULL DEFAULT 900 CHECK (cooldown_seconds >= 0),
    channels            VARCHAR(100)   NOT NULL DEFAULT 'IN_APP',  -- CSV: 'IN_APP,EMAIL'
    enabled             BOOLEAN        NOT NULL DEFAULT TRUE,
    -- Tracks whether the threshold is currently "armed" — used for
    -- once-per-crossing semantics (only fire on the transition).
    is_armed            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alert_configs_user_id      ON alert_configs(user_id);
CREATE INDEX idx_alert_configs_ticker       ON alert_configs(ticker);
CREATE INDEX idx_alert_configs_user_enabled ON alert_configs(user_id, enabled);


-- ---------------------------------------------------------------------
-- alert_history: record of every alert attempt (one row per channel).
-- This is also the source of truth for cooldown checks.
-- ---------------------------------------------------------------------
CREATE TABLE alert_history (
    id                  BIGSERIAL PRIMARY KEY,
    alert_config_id     BIGINT         NOT NULL REFERENCES alert_configs(id) ON DELETE CASCADE,
    user_id             BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticker              VARCHAR(10)    NOT NULL,
    triggered_price     NUMERIC(18, 4) NOT NULL,
    threshold_price     NUMERIC(18, 4) NOT NULL,
    direction           VARCHAR(10)    NOT NULL,
    channel             VARCHAR(20)    NOT NULL CHECK (channel IN ('IN_APP', 'EMAIL')),
    delivery_status     VARCHAR(30)    NOT NULL CHECK (delivery_status IN ('SENT', 'FAILED', 'SUPPRESSED_COOLDOWN')),
    error_message       TEXT,
    triggered_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Lookups are dominated by: "latest SENT history row for this config"
-- (cooldown check) and "user's history paginated by time" (UI).
CREATE INDEX idx_alert_history_config_triggered ON alert_history(alert_config_id, triggered_at DESC);
CREATE INDEX idx_alert_history_user_triggered   ON alert_history(user_id, triggered_at DESC);
CREATE INDEX idx_alert_history_status           ON alert_history(delivery_status);


-- ---------------------------------------------------------------------
-- price_cache: latest known quote per ticker. One row per ticker, upserted
-- on each poll. Keeps hot reads off Finnhub and gives the UI something
-- to display without calling the API on every page load.
-- ---------------------------------------------------------------------
CREATE TABLE price_cache (
    ticker          VARCHAR(10)    PRIMARY KEY,
    current_price   NUMERIC(18, 4) NOT NULL,
    previous_close  NUMERIC(18, 4),
    high_price      NUMERIC(18, 4),
    low_price       NUMERIC(18, 4),
    open_price      NUMERIC(18, 4),
    quote_timestamp TIMESTAMPTZ,                         -- timestamp from Finnhub
    fetched_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()-- when we fetched it
);


-- ---------------------------------------------------------------------
-- updated_at auto-update trigger (standard PG pattern).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_alert_configs_updated_at
    BEFORE UPDATE ON alert_configs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
