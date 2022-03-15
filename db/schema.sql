SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: visa; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA visa;


SET default_tablespace = '';

--
-- Name: configuration; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.configuration (
    id bigint NOT NULL,
    key character varying(256) NOT NULL,
    value character varying(8192) NOT NULL
);


--
-- Name: configuration_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.configuration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.configuration_id_seq OWNED BY visa.configuration.id;


--
-- Name: employer; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.employer (
    id bigint NOT NULL,
    country_code character varying(10),
    name character varying(200),
    town character varying(100)
);


--
-- Name: experiment; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.experiment (
    id character varying(32) NOT NULL,
    instrument_id bigint NOT NULL,
    proposal_id bigint NOT NULL,
    start_date timestamp without time zone,
    end_date timestamp without time zone
);


--
-- Name: experiment_user; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.experiment_user (
    experiment_id character varying(32) NOT NULL,
    user_id character varying(250) NOT NULL
);


--
-- Name: flavour_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.flavour_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: flavour; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.flavour (
    id bigint DEFAULT nextval('visa.flavour_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    compute_id character varying(250) NOT NULL,
    cpu real NOT NULL,
    memory integer NOT NULL,
    name character varying(250) NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


--
-- Name: flavour_limit_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.flavour_limit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: flavour_limit; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.flavour_limit (
    id bigint DEFAULT nextval('visa.flavour_limit_id_seq'::regclass) NOT NULL,
    object_id bigint NOT NULL,
    object_type character varying(255) NOT NULL,
    flavour_id bigint NOT NULL
);


--
-- Name: image_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: image; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.image (
    id bigint DEFAULT nextval('visa.image_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    compute_id character varying(250) NOT NULL,
    description character varying(2500),
    icon character varying(100) NOT NULL,
    name character varying(250) NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    visible boolean DEFAULT false NOT NULL,
    version character varying(100),
    boot_command text,
    autologin character varying
);


--
-- Name: image_protocol; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.image_protocol (
    image_id bigint NOT NULL,
    protocol_id bigint NOT NULL
);


--
-- Name: instance_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance (
    id bigint DEFAULT nextval('visa.instance_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    comments character varying(2500),
    compute_id character varying(250),
    name character varying(250) NOT NULL,
    screen_height integer NOT NULL,
    screen_width integer NOT NULL,
    state character varying(50) NOT NULL,
    last_seen_at timestamp without time zone,
    termination_date timestamp without time zone,
    plan_id bigint,
    username character varying(100),
    delete_requested boolean DEFAULT false NOT NULL,
    last_interaction_at timestamp without time zone,
    ip_address character varying(255),
    keyboard_layout character varying(100) DEFAULT 'en-gb-qwerty'::character varying NOT NULL,
    deleted_at timestamp without time zone,
    security_groups text,
    uid character varying(16) NOT NULL
);


--
-- Name: instance_attribute_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_attribute_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_attribute; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_attribute (
    id bigint DEFAULT nextval('visa.instance_attribute_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    instance_id bigint NOT NULL
);


--
-- Name: instance_authentication_token_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_authentication_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_authentication_token; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_authentication_token (
    id bigint DEFAULT nextval('visa.instance_authentication_token_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    token character varying(250) NOT NULL,
    instance_id bigint NOT NULL,
    user_id character varying(250) NOT NULL
);


--
-- Name: instance_command_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_command_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_command; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_command (
    id bigint DEFAULT nextval('visa.instance_command_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    action_type character varying(50) NOT NULL,
    message character varying(255),
    state character varying(50) NOT NULL,
    instance_id bigint NOT NULL,
    user_id character varying(250)
);


--
-- Name: instance_experiment; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_experiment (
    experiment_id character varying(32) NOT NULL,
    instance_id bigint NOT NULL
);


--
-- Name: instance_expiration; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_expiration (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    expiration_date timestamp without time zone NOT NULL,
    instance_id bigint NOT NULL
);


--
-- Name: instance_expiration_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_expiration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_expiration_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.instance_expiration_id_seq OWNED BY visa.instance_expiration.id;


--
-- Name: instance_jupyter_session; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_jupyter_session (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    kernel_id character varying(150) NOT NULL,
    session_id character varying(150) NOT NULL,
    instance_id bigint NOT NULL,
    user_id character varying(250) NOT NULL
);


--
-- Name: instance_jupyter_session_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_jupyter_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_jupyter_session_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.instance_jupyter_session_id_seq OWNED BY visa.instance_jupyter_session.id;


--
-- Name: instance_member_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_member_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_member; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_member (
    id bigint DEFAULT nextval('visa.instance_member_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    role character varying(255) NOT NULL,
    user_id character varying(250) NOT NULL,
    instance_id bigint NOT NULL
);


--
-- Name: instance_session_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_session; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_session (
    id bigint DEFAULT nextval('visa.instance_session_id_seq'::regclass) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    connection_id character varying(150) NOT NULL,
    instance_id bigint NOT NULL,
    current boolean NOT NULL
);


--
-- Name: instance_session_member; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_session_member (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    active boolean NOT NULL,
    role character varying(150) NOT NULL,
    session_id character varying(150) NOT NULL,
    instance_session_id bigint NOT NULL,
    user_id character varying(250) NOT NULL,
    last_seen_at timestamp without time zone,
    last_interaction_at timestamp without time zone
);


--
-- Name: instance_session_member_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_session_member_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_session_member_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.instance_session_member_id_seq OWNED BY visa.instance_session_member.id;


--
-- Name: instance_thumbnail; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instance_thumbnail (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    instance_id bigint NOT NULL,
    data text NOT NULL
);


--
-- Name: instance_thumbnail_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.instance_thumbnail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: instance_thumbnail_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.instance_thumbnail_id_seq OWNED BY visa.instance_thumbnail.id;


--
-- Name: instrument; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instrument (
    id bigint NOT NULL,
    name character varying(250) NOT NULL
);


--
-- Name: instrument_scientist; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.instrument_scientist (
    instrument_id bigint NOT NULL,
    user_id character varying(250) NOT NULL
);


--
-- Name: plan; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.plan (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    flavour_id bigint,
    image_id bigint,
    preset boolean DEFAULT false NOT NULL
);


--
-- Name: plan_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: plan_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.plan_id_seq OWNED BY visa.plan.id;


--
-- Name: proposal; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.proposal (
    id bigint NOT NULL,
    identifier character varying(100) NOT NULL,
    title character varying(2000),
    public_at timestamp without time zone,
    summary character varying(5000)
);


--
-- Name: protocol_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.protocol_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: protocol; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.protocol (
    id bigint DEFAULT nextval('visa.protocol_id_seq'::regclass) NOT NULL,
    name character varying(100) NOT NULL,
    port integer NOT NULL
);


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.role (
    id bigint DEFAULT nextval('visa.role_id_seq'::regclass) NOT NULL,
    description character varying(250),
    name character varying(100) NOT NULL
);


--
-- Name: schema_migrations; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.schema_migrations (
    version character varying(255) NOT NULL
);


--
-- Name: security_group; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.security_group (
    id bigint NOT NULL,
    name character varying(250) NOT NULL
);


--
-- Name: security_group_filter; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.security_group_filter (
    id bigint NOT NULL,
    object_id bigint NOT NULL,
    object_type character varying(255) NOT NULL,
    security_group_id bigint NOT NULL
);


--
-- Name: security_group_filter_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.security_group_filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: security_group_filter_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.security_group_filter_id_seq OWNED BY visa.security_group_filter.id;


--
-- Name: security_group_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.security_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: security_group_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.security_group_id_seq OWNED BY visa.security_group.id;


--
-- Name: system_notification; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.system_notification (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    level character varying(50) NOT NULL,
    message character varying(4096) NOT NULL
);


--
-- Name: system_notification_id_seq; Type: SEQUENCE; Schema: visa; Owner: -
--

CREATE SEQUENCE visa.system_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: system_notification_id_seq; Type: SEQUENCE OWNED BY; Schema: visa; Owner: -
--

ALTER SEQUENCE visa.system_notification_id_seq OWNED BY visa.system_notification.id;


--
-- Name: user_role; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.user_role (
    user_id character varying(250) NOT NULL,
    role_id bigint NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: visa; Owner: -
--

CREATE TABLE visa.users (
    id character varying(250) NOT NULL,
    affiliation character varying(255),
    email character varying(100),
    first_name character varying(100),
    last_name character varying(100) NOT NULL,
    last_seen_at timestamp without time zone,
    instance_quota integer DEFAULT 2 NOT NULL,
    affiliation_id bigint,
    activated_at timestamp without time zone
);


--
-- Name: configuration id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.configuration ALTER COLUMN id SET DEFAULT nextval('visa.configuration_id_seq'::regclass);


--
-- Name: instance_expiration id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_expiration ALTER COLUMN id SET DEFAULT nextval('visa.instance_expiration_id_seq'::regclass);


--
-- Name: instance_jupyter_session id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_jupyter_session ALTER COLUMN id SET DEFAULT nextval('visa.instance_jupyter_session_id_seq'::regclass);


--
-- Name: instance_session_member id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session_member ALTER COLUMN id SET DEFAULT nextval('visa.instance_session_member_id_seq'::regclass);


--
-- Name: instance_thumbnail id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_thumbnail ALTER COLUMN id SET DEFAULT nextval('visa.instance_thumbnail_id_seq'::regclass);


--
-- Name: plan id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.plan ALTER COLUMN id SET DEFAULT nextval('visa.plan_id_seq'::regclass);


--
-- Name: security_group id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.security_group ALTER COLUMN id SET DEFAULT nextval('visa.security_group_id_seq'::regclass);


--
-- Name: security_group_filter id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.security_group_filter ALTER COLUMN id SET DEFAULT nextval('visa.security_group_filter_id_seq'::regclass);


--
-- Name: system_notification id; Type: DEFAULT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.system_notification ALTER COLUMN id SET DEFAULT nextval('visa.system_notification_id_seq'::regclass);


--
-- Name: configuration configuration_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.configuration
    ADD CONSTRAINT configuration_pkey PRIMARY KEY (id);


--
-- Name: employer employer_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.employer
    ADD CONSTRAINT employer_pkey PRIMARY KEY (id);


--
-- Name: experiment experiment_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment
    ADD CONSTRAINT experiment_pkey PRIMARY KEY (id);


--
-- Name: experiment_user experiment_user_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment_user
    ADD CONSTRAINT experiment_user_pkey PRIMARY KEY (experiment_id, user_id);


--
-- Name: flavour_limit flavour_limit_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.flavour_limit
    ADD CONSTRAINT flavour_limit_pkey PRIMARY KEY (id);


--
-- Name: flavour flavour_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.flavour
    ADD CONSTRAINT flavour_pkey PRIMARY KEY (id);


--
-- Name: image image_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);


--
-- Name: instance_attribute instance_attribute_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_attribute
    ADD CONSTRAINT instance_attribute_pkey PRIMARY KEY (id);


--
-- Name: instance_authentication_token instance_authentication_token_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_authentication_token
    ADD CONSTRAINT instance_authentication_token_pkey PRIMARY KEY (id);


--
-- Name: instance_command instance_command_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_command
    ADD CONSTRAINT instance_command_pkey PRIMARY KEY (id);


--
-- Name: instance_experiment instance_experiment_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_experiment
    ADD CONSTRAINT instance_experiment_pkey PRIMARY KEY (experiment_id, instance_id);


--
-- Name: instance_expiration instance_expiration_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_expiration
    ADD CONSTRAINT instance_expiration_pkey PRIMARY KEY (id);


--
-- Name: instance_jupyter_session instance_jupyter_session_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_jupyter_session
    ADD CONSTRAINT instance_jupyter_session_pkey PRIMARY KEY (id);


--
-- Name: instance_member instance_member_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_member
    ADD CONSTRAINT instance_member_pkey PRIMARY KEY (id);


--
-- Name: instance instance_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance
    ADD CONSTRAINT instance_pkey PRIMARY KEY (id);


--
-- Name: instance_session_member instance_session_member_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session_member
    ADD CONSTRAINT instance_session_member_pkey PRIMARY KEY (id);


--
-- Name: instance_session instance_session_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session
    ADD CONSTRAINT instance_session_pkey PRIMARY KEY (id);


--
-- Name: instance_thumbnail instance_thumbnail_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_thumbnail
    ADD CONSTRAINT instance_thumbnail_pkey PRIMARY KEY (id);


--
-- Name: instrument instrument_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instrument
    ADD CONSTRAINT instrument_pkey PRIMARY KEY (id);


--
-- Name: instrument_scientist instrument_responsible_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instrument_scientist
    ADD CONSTRAINT instrument_responsible_pkey PRIMARY KEY (instrument_id, user_id);


--
-- Name: plan plan_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.plan
    ADD CONSTRAINT plan_pkey PRIMARY KEY (id);


--
-- Name: proposal proposal_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.proposal
    ADD CONSTRAINT proposal_pkey PRIMARY KEY (id);


--
-- Name: protocol protocol_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.protocol
    ADD CONSTRAINT protocol_pkey PRIMARY KEY (id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: schema_migrations schema_migrations_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (version);


--
-- Name: security_group_filter security_group_filter_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.security_group_filter
    ADD CONSTRAINT security_group_filter_pkey PRIMARY KEY (id);


--
-- Name: security_group security_group_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.security_group
    ADD CONSTRAINT security_group_pkey PRIMARY KEY (id);


--
-- Name: system_notification system_notification_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.system_notification
    ADD CONSTRAINT system_notification_pkey PRIMARY KEY (id);


--
-- Name: instance_expiration uk_instance_expiration_instance_id; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_expiration
    ADD CONSTRAINT uk_instance_expiration_instance_id UNIQUE (instance_id);


--
-- Name: instance_thumbnail uk_instance_thumbnail_instance_id; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_thumbnail
    ADD CONSTRAINT uk_instance_thumbnail_instance_id UNIQUE (instance_id);


--
-- Name: role uk_role_name; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.role
    ADD CONSTRAINT uk_role_name UNIQUE (name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users fk_employer_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.users
    ADD CONSTRAINT fk_employer_id FOREIGN KEY (affiliation_id) REFERENCES visa.employer(id);


--
-- Name: experiment_user fk_experiment_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment_user
    ADD CONSTRAINT fk_experiment_id FOREIGN KEY (experiment_id) REFERENCES visa.experiment(id);


--
-- Name: instance_experiment fk_experiment_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_experiment
    ADD CONSTRAINT fk_experiment_id FOREIGN KEY (experiment_id) REFERENCES visa.experiment(id);


--
-- Name: flavour_limit fk_flavour_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.flavour_limit
    ADD CONSTRAINT fk_flavour_id FOREIGN KEY (flavour_id) REFERENCES visa.flavour(id);


--
-- Name: plan fk_flavour_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.plan
    ADD CONSTRAINT fk_flavour_id FOREIGN KEY (flavour_id) REFERENCES visa.flavour(id);


--
-- Name: plan fk_image_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.plan
    ADD CONSTRAINT fk_image_id FOREIGN KEY (image_id) REFERENCES visa.image(id);


--
-- Name: image_protocol fk_image_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.image_protocol
    ADD CONSTRAINT fk_image_id FOREIGN KEY (image_id) REFERENCES visa.image(id);


--
-- Name: instance_command fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_command
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_expiration fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_expiration
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_thumbnail fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_thumbnail
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_experiment fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_experiment
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_member fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_member
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_authentication_token fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_authentication_token
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_session fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_jupyter_session fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_jupyter_session
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_attribute fk_instance_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_attribute
    ADD CONSTRAINT fk_instance_id FOREIGN KEY (instance_id) REFERENCES visa.instance(id);


--
-- Name: instance_session_member fk_instance_session_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session_member
    ADD CONSTRAINT fk_instance_session_id FOREIGN KEY (instance_session_id) REFERENCES visa.instance_session(id);


--
-- Name: instrument_scientist fk_instrument_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instrument_scientist
    ADD CONSTRAINT fk_instrument_id FOREIGN KEY (instrument_id) REFERENCES visa.instrument(id);


--
-- Name: experiment fk_instrument_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment
    ADD CONSTRAINT fk_instrument_id FOREIGN KEY (instrument_id) REFERENCES visa.instrument(id);


--
-- Name: instance fk_plan_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance
    ADD CONSTRAINT fk_plan_id FOREIGN KEY (plan_id) REFERENCES visa.plan(id);


--
-- Name: experiment fk_proposal_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment
    ADD CONSTRAINT fk_proposal_id FOREIGN KEY (proposal_id) REFERENCES visa.proposal(id);


--
-- Name: image_protocol fk_protocol_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.image_protocol
    ADD CONSTRAINT fk_protocol_id FOREIGN KEY (protocol_id) REFERENCES visa.protocol(id);


--
-- Name: user_role fk_role_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.user_role
    ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES visa.role(id);


--
-- Name: security_group_filter fk_security_group_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.security_group_filter
    ADD CONSTRAINT fk_security_group_id FOREIGN KEY (security_group_id) REFERENCES visa.security_group(id);


--
-- Name: experiment_user fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.experiment_user
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instance_authentication_token fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_authentication_token
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instance_command fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_command
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instance_jupyter_session fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_jupyter_session
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instance_member fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_member
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instance_session_member fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instance_session_member
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: instrument_scientist fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.instrument_scientist
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- Name: user_role fk_users_id; Type: FK CONSTRAINT; Schema: visa; Owner: -
--

ALTER TABLE ONLY visa.user_role
    ADD CONSTRAINT fk_users_id FOREIGN KEY (user_id) REFERENCES visa.users(id);


--
-- PostgreSQL database dump complete
--


--
-- Dbmate schema migrations
--

INSERT INTO visa.schema_migrations (version) VALUES
    ('20220314151039');
