-- =====================================================================
-- V2__add_seeded_traders.sql
-- Replace the single default user with two hardcoded traders for dev.
--
-- Passwords are bcrypt hashes of:
--   trader1@local.dev -> trader1pass
--   trader2@local.dev -> trader2pass
--
-- Hashes generated with bcrypt strength 10. Never reuse these in prod.
-- =====================================================================

-- Insert the two traders.
-- Use ON CONFLICT to make this migration idempotent if rerun.
INSERT INTO users (email, password_hash, display_name, enabled)
VALUES
    (
        'trader1@local.dev',
        '$2a$10$SY/PLTHbPvR4BRn9grJpHuzkvaXbR.Ad9zoZGn/82Nb5mrPP0OawW',  -- trader1pass
        'Trader One',
        TRUE
    ),
    (
        'trader2@local.dev',
        '$2a$10$P9kxHrwJ8tEUO0kF0K81Pem8O6nJf.tmb1d4Y3UI./UfgJZ5MO/qa',  -- trader2pass
        'Trader Two',
        TRUE
    )
ON CONFLICT (email) DO UPDATE
    SET password_hash = EXCLUDED.password_hash,
        display_name = EXCLUDED.display_name,
        enabled = EXCLUDED.enabled;

-- Move any pre-existing alerts (created under the old user_id=1) to trader1.
-- Trader1 might have id=2 now (since user_id=1 was the seed from V1), so we
-- look up trader1 by email and reassign rows there.
UPDATE alert_configs
SET user_id = (SELECT id FROM users WHERE email = 'trader1@local.dev')
WHERE user_id = 1
  AND user_id <> (SELECT id FROM users WHERE email = 'trader1@local.dev');

UPDATE alert_history
SET user_id = (SELECT id FROM users WHERE email = 'trader1@local.dev')
WHERE user_id = 1
  AND user_id <> (SELECT id FROM users WHERE email = 'trader1@local.dev');

-- Now safe to delete the old default@local.dev user (it has no alerts left).
DELETE FROM users WHERE email = 'default@local.dev';
