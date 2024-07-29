-- migrate:up

CREATE SEQUENCE client_authentication_token_id_seq;
CREATE TABLE IF NOT EXISTS client_authentication_token (
     id             BIGINT NOT NULL CONSTRAINT client_authentication_token_pk PRIMARY KEY DEFAULT nextval('client_authentication_token_id_seq'),
     created_at     TIMESTAMP NOT NULL,
     updated_at     TIMESTAMP NOT NULL,
     client_id      VARCHAR(250) NOT NULL,
     token          VARCHAR(250) NOT NULL,
     user_id        VARCHAR(255) NOT NULL constraint fk_users_id references users
);

-- migrate:down

