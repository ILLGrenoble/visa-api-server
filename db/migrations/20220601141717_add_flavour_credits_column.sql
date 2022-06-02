-- migrate:up

ALTER TABLE flavour ADD COLUMN IF NOT EXISTS credits INT DEFAULT 1;

-- migrate:down


ALTER TABLE flavour DROP COLUMN IF EXISTS credits;
