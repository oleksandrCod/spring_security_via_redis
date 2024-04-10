CREATE TABLE IF NOT EXISTS users
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    email              VARCHAR(255) UNIQUE               NOT NULL,
    password           VARCHAR(255)                      NOT NULL,
    first_name         VARCHAR(255)                      NOT NULL,
    last_name          VARCHAR(255)                      NOT NULL,
    private_solana_key VARCHAR(255),
    is_enabled         BOOLEAN,
    is_deleted         BOOLEAN DEFAULT FALSE             NOT NULL
);
CREATE TABLE IF NOT EXISTS roles
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name       ENUM ('ROLE_USER', 'ROLE_ADMIN')  NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE             NOT NULL
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
