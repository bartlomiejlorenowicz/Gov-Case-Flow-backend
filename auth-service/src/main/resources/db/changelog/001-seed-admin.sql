--liquibase formatted sql

--changeset Bartek:001-seed-admin-user
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM users WHERE username='admin@wp.pl';
INSERT INTO users (id, username, password_hash, enabled, created_at)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'admin@wp.pl',
  '$2a$10$p.pKFVLsIBoDkgB7vRF8zuORXQfEDt9tOwVcE1hLGWK01Fio2Zh7G',
  true,
  now()
);

--changeset Bartek:002-seed-admin-role-admin
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM user_roles WHERE user_id='11111111-1111-1111-1111-111111111111' AND role='ADMIN';
INSERT INTO user_roles (user_id, role)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'ADMIN'
);

--changeset Bartek:003-seed-admin-role-user
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM user_roles WHERE user_id='11111111-1111-1111-1111-111111111111' AND role='USER';
INSERT INTO user_roles (user_id, role)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'USER'
);
