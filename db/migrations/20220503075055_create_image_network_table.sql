-- migrate:up

CREATE SEQUENCE instance_network_id_seq;

CREATE TABLE image_network
(
    id BIGINT DEFAULT nextval('image_network_id_seq'::regclass) NOT NULL CONSTRAINT image_network_pkey PRIMARY KEY,
    network_id VARCHAR(255) NOT NULL
);


-- migrate:down

DROP TABLE image_network;

DROP SEQUENCE  instance_network_id_seq;