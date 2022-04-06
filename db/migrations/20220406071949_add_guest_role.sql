-- migrate:up

INSERT INTO role (description, name) VALUES ('Guest Role', 'GUEST');

-- migrate:down

DELETE FROM role WHERE name = 'GUEST';
