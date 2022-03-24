-- migrate:up

ALTER TABLE system_notification ADD COLUMN tmp_do_not_touch boolean DEFAULT FALSE;
UPDATE system_notification SET tmp_do_not_touch = (
    select column_name = 'activated_at'
    from information_schema.columns
    where table_name = 'system_notification'
    and column_name = 'activated_at'
);

ALTER TABLE system_notification ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP DEFAULT NULL;
ALTER TABLE system_notification ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Only set activated notifications that have the new table structure (ignore if activate_at existed already)
UPDATE system_notification SET activated_at = CURRENT_TIMESTAMP WHERE tmp_do_not_touch IS NULL;

ALTER TABLE system_notification DROP COLUMN tmp_do_not_touch;

-- migrate:down

ALTER TABLE system_notification DROP COLUMN activated_at;
ALTER TABLE system_notification DROP COLUMN deleted_at;
