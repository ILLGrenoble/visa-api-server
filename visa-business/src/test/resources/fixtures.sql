INSERT INTO flavour (id, name, memory, cpu, compute_id, created_at, updated_at, deleted)
VALUES (1000, 'Flavour 1', 1024, 2, '24e7437a-eae5-48c4-1234-778c42a6acf9', '2019-01-01', '2019-01-01', false);
INSERT INTO flavour (id, name, memory, cpu, compute_id, created_at, updated_at, deleted)
VALUES (1001, 'Flavour 2', 1024, 2, '24e7437a-eae5-48c4-1234-778c42a6acf9', '2019-01-01', '2019-01-01', false);
INSERT INTO flavour (id, name, memory, cpu, compute_id, created_at, updated_at, deleted)
VALUES (1002, 'Flavour 3', 1024, 2, '24e7437a-eae5-48c4-1234-778c42a6acf9', '2019-01-01', '2019-01-01', false);


INSERT INTO image (id, name, description, icon, compute_id, deleted, visible, created_at, updated_at)
VALUES (1000, 'Image 1', 'Image 1 description', 'icon1', '24e7437a-eae5-48c4-abcd-778c42a6acf9', false, true, '2019-01-01', '2019-01-01');
INSERT INTO image (id, name, description, icon, compute_id, deleted, visible, created_at, updated_at)
VALUES (1001, 'Image 2', 'Image 2 description', 'icon2', '24e7437a-eae5-48c4-efab-778c42a6acf9', false, true, '2019-01-01', '2019-01-01');
INSERT INTO image (id, name, description, icon, compute_id, deleted, visible, created_at, updated_at)
VALUES (1002, 'Image 3', 'Image 3 description', 'icon3', '24e7437a-eae5-48c4-5432-778c42a6acf9', false, true, '2019-01-01', '2019-01-01');
INSERT INTO image (id, name, description, icon, compute_id, deleted, visible, created_at, updated_at)
VALUES (1003, 'Image 4', 'Image 4 description', 'icon3', '24e7437a-eae5-48c4-5432-778c42a6acf9', false, true, '2019-01-01', '2019-01-01');

INSERT INTO plan (id, flavour_id, image_id, created_at, updated_at, preset)
VALUES (1000, 1001, 1000, '2019-01-01', '2019-01-01', true);
INSERT INTO plan (id, flavour_id, image_id, created_at, updated_at, preset)
VALUES (1001, 1001, 1001, '2019-01-01', '2019-01-01', false);
INSERT INTO plan (id, flavour_id, image_id, created_at, updated_at, preset)
VALUES (1002, 1001, 1002, '2019-01-01', '2019-01-01', false);
INSERT INTO plan (id, flavour_id, image_id, created_at, updated_at, preset)
VALUES (1003, 1000, 1000, '2019-01-01', '2019-01-01', false);

INSERT INTO protocol (id, name, port) VALUES (1000, 'GUACD', 4822);
INSERT INTO protocol (id, name, port) VALUES (1001, 'RDP', 3389);

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1000, '1000abcd', '24e7437a-eae5-48c4-923e-778c42a6acf8', 'Instance 1', 'This is an instance', 1001, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', '2019-01-01', '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1001, '1001abcd', '24e7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 2', 'This is another instance', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', '2019-01-01', '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1002, '1002abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 3', 'This is an instance unlinked to any actionType', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1003, '1003abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 3', 'This is an instance unlinked to any actionType', 1000, 1280, 1024, 'DELETED', '2019-01-01', '2019-01-01', '2019-01-01', '2019-01-01', '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1004, '1004abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 4', 'Instance 4', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1005, '1005abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 5', 'Instance 5', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1006, '1006abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 6', 'Instance 6', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1007, '1007abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 1', 'Instance 7', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1008, '1008abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 8', 'Instance 8', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1009, '1009abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 9', 'Instance 9', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1010, '1010abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 10', 'Instance 10', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO instance (id, uid, compute_id, name, comments, plan_id, screen_width, screen_height, state, deleted_at, created_at, updated_at, last_seen_at, termination_date, delete_requested, keyboard_layout)
VALUES (1011, '1011abcd', '34f7437a-eae5-48c4-923e-778c42a6acf9', 'Instance 11', 'Instance 11', 1000, 1280, 1024, 'ACTIVE', null, '2019-01-01', '2019-01-01', now(), '2099-01-01', false, 'en-gb-qwerty');

INSERT INTO employer (id, name, town, country_code)
VALUES (1001, 'Company 1', 'Grenoble', 'FR');
INSERT INTO employer (id, name, town, country_code)
VALUES (1002, 'Company 2', 'Grenoble', 'FR');
INSERT INTO employer (id, name, town, country_code)
VALUES (1003, 'Company 3', 'Grenoble', 'FR');
INSERT INTO employer (id, name, town, country_code)
VALUES (1004, 'Company 4', 'Grenoble', 'FR');
INSERT INTO employer (id, name, town, country_code)
VALUES (1005, 'Company 5', 'Grenoble', 'FR');
INSERT INTO employer (id, name, town, country_code)
VALUES (1006, 'Company 6', 'Grenoble', 'FR');

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('1', 'bloggs@example.com', 'Joe', 'Bloggs', 1001, '2019-01-01', -1);

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('2',  'clayton@example.com', 'Kim', 'Clayton', 1002, '2019-01-01', -1);

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('3', 'hall@example.com', 'Jamie', 'Hall', 1003, '2019-01-01', -1);

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('4', 'zimmerman@example.com', 'Zimmerman', 'Mcfarland', 1004, '2019-01-01', -1);

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('5', 'wilkinson@example.com', 'Mark', 'Wilkinson', 1005, '2019-01-01', -1);

INSERT INTO users (id, email, first_name, last_name, affiliation_id, activated_at, instance_quota)
VALUES ('6', 'clarkson@example.com', 'Tim', 'Clarkson', 1006, '2019-01-01', -1);

INSERT INTO role (id, name, description) VALUES (1000, 'ADMIN', 'Admin role');
INSERT INTO role (id, name, description) VALUES (1001, 'STAFF', 'Staff role');
INSERT INTO role (id, name, description) VALUES (1002, 'INSTRUMENT_CONTROL', 'Instrument Control role');

INSERT INTO user_role (user_id, role_id) VALUES ('1', 1000);
INSERT INTO user_role (user_id, role_id) VALUES ('1', 1001);
INSERT INTO user_role (user_id, role_id) VALUES ('2', 1000);
INSERT INTO user_role (user_id, role_id) VALUES ('3', 1001);

INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1000, '1', 1000, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1001, '2', 1000, 'USER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1002, '3', 1000, 'GUEST',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1003, '1', 1001, 'USER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1004, '1', 1003, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1104, '1', 1004, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1105, '2', 1005, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1106, '3', 1006, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1107, '4', 1007, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1108, '5', 1008, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1109, '6', 1009, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1110, '3', 1010, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1111, '4', 1011, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1112, '6', 1001, 'OWNER',  '2019-01-01', '2019-01-01');
INSERT INTO instance_member (id, user_id, instance_id, role, created_at, updated_at)
VALUES (1113, '6', 1002, 'OWNER',  '2019-01-01', '2019-01-01');

INSERT INTO instance_command (id, user_id, instance_id, action_type, state, message, created_at, updated_at)
VALUES (1000, '1', 1000, 'START', 'RUNNING', '', '2019-01-01', '2019-01-01');

INSERT INTO instance_command (id, user_id, instance_id, action_type, state, message, created_at, updated_at)
VALUES (1001, '1', 1001, 'SHUTDOWN', 'PENDING', '', '2019-01-01', '2019-01-01');

INSERT INTO instrument (id, name)
VALUES (1, 'I1');
INSERT INTO instrument (id, name)
VALUES (2, 'I2');
INSERT INTO instrument (id, name)
VALUES (3, 'I3');
INSERT INTO instrument (id, name)
VALUES (4, 'I4');
INSERT INTO instrument (id, name)
VALUES (5, 'I5');


INSERT INTO proposal (id, identifier, title, summary)
VALUES (1, 'PRO-1', 'Proposal 1 title', 'Proposal 1 summary');
INSERT INTO proposal (id, identifier, title, summary)
VALUES (2, 'PRO-2', 'Proposal 2 title', 'Proposal 2 summary');
INSERT INTO proposal (id, identifier, title, summary)
VALUES (3, 'PRO-3', 'Proposal 3 title', 'Proposal 3 summary');
INSERT INTO proposal (id, identifier, title, summary)
VALUES (4, 'PRO-4', 'Proposal 4 title', 'Proposal 4 summary');
INSERT INTO proposal (id, identifier, title, summary)
VALUES (5, 'PRO-5', 'Proposal 5 title', 'Proposal 5 summary');

INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0001-0001-000001', 1, 1, '2016-01-01', '2016-01-02');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0001-0002-000001', 2, 1, '2016-01-01', '2016-01-02');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0001-0002-000002', 2, 2, '2016-01-01', '2016-01-02');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0001-0003-000002', 3, 2, '2016-01-01', '2016-03-07');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0001-0005-000002', 3, 2, '2016-01-01', '2016-03-07');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0002-0003-000003', 3, 3, '2016-04-01', '2016-04-07');
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0006-0001-000001', 1, 1, CURRENT_DATE, CURRENT_DATE + 1);
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0006-0002-000001', 2, 1, CURRENT_DATE, CURRENT_DATE + 1);
INSERT INTO experiment (id, instrument_id, proposal_id, start_date, end_date)
VALUES ('0006-0005-000001', 5, 1, CURRENT_DATE, CURRENT_DATE + 1);

INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0001-0001-000001', '1');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0001-0002-000001', '1');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0001-0002-000002', '2');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0001-0003-000002', '2');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0002-0003-000003', '3');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '1');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '2');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '3');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '4');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '5');
INSERT INTO experiment_user (experiment_id, user_id)
VALUES ('0006-0001-000001', '6');

INSERT INTO instance_authentication_token (id, token, instance_id, user_id, created_at, updated_at)
VALUES (1000, '24e7437a-eae5-48c4-923e-778c42a6acf8', 1000, '1', '2019-01-01', '2019-01-01');
INSERT INTO instance_authentication_token (id, token, instance_id, user_id, created_at, updated_at)
VALUES (1001, '24e7437b-eae5-48c4-923e-778c42a6acf8', 1000, '2', '2019-01-01', '2019-01-01');

INSERT INTO instance_session (id, instance_id, connection_id, created_at, updated_at, current)
VALUES (1000, 1000, '24e7437a-eae5-48c4-923e-778c42a6acf8', '2019-01-01', '2019-01-01', true);
INSERT INTO instance_session (id, instance_id, connection_id, created_at, updated_at, current)
VALUES (1001, 1001, '24e7437a-eae5-48c4-923e-778c42a6acf8', '2019-01-01', '2019-01-01', true);
INSERT INTO instance_session (id, instance_id, connection_id, created_at, updated_at, current)
VALUES (1002, 1002, '24e7437a-eae5-48c4-923e-778c42a6acf8', '2019-01-01', '2019-01-01', true);

INSERT INTO instance_session_member (id, session_id, role, active, instance_session_id, user_id, created_at, updated_at)
VALUES (1000, '24e7437a-eae5-48c4-923e-778c42a6acf8', 'OWNER', true, 1000, '1', '2019-01-01', '2019-01-01');
INSERT INTO instance_session_member (id, session_id, role, active, instance_session_id, user_id, created_at, updated_at)
VALUES (1001, '24e7437a-eae5-48c4-823e-778c42a6acf8', 'USER', true, 1000, '2', '2019-01-01', '2019-01-01');

INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1000, '0001-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1003, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1004, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1005, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1006, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1007, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1008, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1009, '0001-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1010, '0006-0001-000001');
INSERT INTO instance_experiment (instance_id, experiment_id) VALUES (1010, '0001-0002-000002');

INSERT INTO instrument_scientist (user_id, instrument_id) VALUES ('6', 1);
INSERT INTO instrument_scientist (user_id, instrument_id) VALUES ('5', 2);

INSERT INTO flavour_limit (id, flavour_id, object_id, object_type) VALUES (1000, 1000, 1, 'INSTRUMENT');
INSERT INTO flavour_limit (id, flavour_id, object_id, object_type) VALUES (1001, 1000, 2, 'INSTRUMENT');

INSERT INTO instance_expiration (id, instance_id, expiration_date, created_at, updated_at)
VALUES (1000, 1000, '2019-01-02', '2019-01-01', '2019-01-01');
INSERT INTO instance_expiration (id, instance_id, expiration_date, created_at, updated_at)
VALUES (1001, 1001, '2099-01-02', '2019-01-01', '2019-01-01');


INSERT INTO security_group (id, name) VALUES (1000, 'COMMON_SG');
INSERT INTO security_group (id, name) VALUES (1001, 'I1_SG');
INSERT INTO security_group (id, name) VALUES (1002, 'I2_SG');
INSERT INTO security_group (id, name) VALUES (1003, 'I3_SG');
INSERT INTO security_group (id, name) VALUES (1004, 'I4_SG');
INSERT INTO security_group (id, name) VALUES (1005, 'ADMIN_SG');
INSERT INTO security_group (id, name) VALUES (1006, 'STAFF_SG');

INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1001, 1001, 1, 'INSTRUMENT');
INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1002, 1002, 2, 'INSTRUMENT');
INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1003, 1003, 3, 'INSTRUMENT');
INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1004, 1004, 4, 'INSTRUMENT');
INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1005, 1005, 1000, 'ROLE');
INSERT INTO security_group_filter (id, security_group_id, object_id, object_type) VALUES (1006, 1006, 1001, 'ROLE');

INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1001, null, 0, null, '2026-08-20 08:12:00', 2.33, 3, 0.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1002, null, 0, null, '2026-08-20 08:13:00', 2.5, 4, 1, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1003, null, 0, null, '2026-08-20 08:14:00', 1.75, 4, 0.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1004, 3.5, 2, 1.1, '2026-08-20 08:15:00', 1.75, 4, 0.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1005, 4.5, 3, 1.5, '2026-08-20 08:16:00', 1.5, 4, 0.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1006, 4.5, 3, 1.5, '2026-08-20 09:00:00', 1.5, 3, 0.2, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1007, 3.5, 5, 1.0, '2026-08-20 09:01:00', 1.6, 5, 1.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1008, 5.5, 3, 2.5, '2026-08-20 09:02:00', 1.75, 8, 0.5, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1009, 3.5, 4, 0.5, '2026-08-20 09:03:00', 2.0, 3, 0.6, 11700, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1010, 6.5, 6, 1.2, '2026-08-20 09:04:00', 2.5, 6, 1.0, 11700, 1);

INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1011, 14.5, 3, 0.5, '2026-08-20 09:00:00', 2.5, 3, 0.7, 11701, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1012, 13.5, 5, 2.0, '2026-08-20 09:01:00', 2.6, 5, 1.8, 11701, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1013, 15.5, 3, 2.5, '2026-08-20 09:02:00', 1.75, 8, 0.9, 11701, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1014, 13.5, 4, 1.5, '2026-08-20 09:03:00', 1.0, 3, 0.1, 11701, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1015, 16.5, 6, 2.2, '2026-08-20 09:04:00', 0.5, 6, 1.5, 11701, 1);

INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1021, 14.5, 3, 0.5, '2026-08-20 09:00:00', 2.5, 3, 0.7, 11702, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1022, 13.5, 5, 2.0, '2026-08-20 09:01:00', 2.6, 5, 1.8, 11702, 1);

INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1031, null, 0, null, '2026-08-20 09:00:00', 2.5, 3, 0.7, 11703, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1032, 13.5, 5, 2.0, '2026-08-20 09:01:00', 2.6, 5, 1.8, 11703, 1);

INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1101, null, 0, null, CURRENT_TIMESTAMP - INTERVAL '6 days 5 minutes', 2.33, 3, 0.5, 11800, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1102, null, 0, null, CURRENT_TIMESTAMP - INTERVAL '6 days 4 minutes', 2.5, 4, 1, 11800, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1103, null, 0, null, CURRENT_TIMESTAMP - INTERVAL '6 days 3 minutes', 1.75, 4, 0.5, 11800, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1104, 3.5, 2, 1.1, CURRENT_TIMESTAMP - INTERVAL '6 days 2 minutes', 1.75, 4, 0.5, 11800, 1);
INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
VALUES (1105, 4.5, 3, 1.5, CURRENT_TIMESTAMP - INTERVAL '6 days 1 minutes', 1.5, 4, 0.5, 11800, 1);

-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1101, 61.5, 4, 22.64950330581225, '2026-07-16 15:17:01.004000', 1.5, 4, 0.5773502691896257, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1102, 73.75, 4, 54.56112779870543, '2026-07-16 15:18:01.003000', 1.5, 4, 0.5773502691896257, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1103, 89.25, 4, 53.38148867663147, '2026-07-16 15:19:01.003000', 1.25, 4, 0.5, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1104, 59.75, 4, 18.997806890972093, '2026-07-16 15:20:01.018000', 1.25, 4, 0.5, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1105, 119.5, 4, 70.8072500995955, '2026-07-16 15:21:01.003000', 1, 4, 0, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1106, 88.25, 4, 66.90976510694584, '2026-07-16 15:22:01.002000', 1.5, 4, 0.5773502691896257, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1107, 63, 4, 19.165942015286735, '2026-07-16 15:23:01.003000', 1.25, 4, 0.5, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1108, 52, 4, 22.135943621178654, '2026-07-16 15:24:01.003000', 1.25, 4, 0.5, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1109, 75.75, 4, 47.800801945853024, '2026-07-16 15:25:01.004000', 1.25, 4, 0.5, 11703, 1);
-- INSERT INTO desktop_session_rtt_sample (id, client_mean_rtt_ms, client_rtt_sample_count, client_sd_rtt_ms, date, instance_mean_rtt_ms, instance_rtt_sample_count, instance_sd_rtt_ms, instance_session_member_id, sample_period_minutes)
-- VALUES (1110, 58.666666666666664, 3, 26.102362600602522, '2026-07-16 15:16:01.004000', 1, 2, 0, 11703, 1);

