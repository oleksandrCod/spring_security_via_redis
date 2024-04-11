CREATE TABLE IF NOT EXISTS users
(
    id                 BIGSERIAL PRIMARY KEY,
    email              VARCHAR(255) UNIQUE   NOT NULL,
    password           VARCHAR(255)          NOT NULL,
    first_name         VARCHAR(255)          NOT NULL,
    last_name          VARCHAR(255)          NOT NULL,
    private_solana_key VARCHAR(255),
    is_enabled         BOOLEAN,
    is_deleted         BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS roles
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT,
    role_id BIGINT
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id);
