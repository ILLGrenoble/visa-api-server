-- migrate:up

ALTER TABLE instance ADD uid VARCHAR(16);
UPDATE instance SET uid = id WHERE uid IS NULL;
ALTER TABLE instance ALTER COLUMN uid SET NOT NULL;

-- migrate:down

ALTER TABLE instance DROP COLUMN uid;
