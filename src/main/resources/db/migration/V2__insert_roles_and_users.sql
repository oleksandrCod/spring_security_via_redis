INSERT INTO roles (name)
VALUES ('ROLE_USER');
INSERT INTO roles (name)
VALUES ('ROLE_ADMIN');

INSERT INTO users (email, password, first_name, last_name, private_solana_key, is_enabled, is_deleted)
VALUES ('john.doe@example.com', '$2a$10$fXK1jneVEQtuwTcUiGzBIudqHi5v6bNC2YCB.NycjSxv4GKzF3E4a', 'John', 'Doe',
        '2WGcYYau2gLu2DUq68SxxXQmCgi77n8hFqqLNbNyg6Xfh2m3tvg8LF5Lgh69CFDux41LUKV1ak1ERHUqiBZnyshz', '1', '0'),
       ('jane.doe@example.com', '$2a$10$fXK1jneVEQtuwTcUiGzBIudqHi5v6bNC2YCB.NycjSxv4GKzF3E4a', 'Jane', 'Doe', NULL,
        '1', '0'),
       ('alice.smith@example.com', '$2a$10$fXK1jneVEQtuwTcUiGzBIudqHi5v6bNC2YCB.NycjSxv4GKzF3E4a', 'Alice', 'Smith',
        NULL, '1', '0'),
       ('bob.johnson@example.com', '$2a$10$fXK1jneVEQtuwTcUiGzBIudqHi5v6bNC2YCB.NycjSxv4GKzF3E4a', 'Bob', 'Johnson',
        NULL, '1', '0'),
       ('charlie.brown@example.com', '$2a$10$fXK1jneVEQtuwTcUiGzBIudqHi5v6bNC2YCB.NycjSxv4GKzF3E4a', 'Charlie', 'Brown',
        NULL, '1', '0');
INSERT INTO user_roles(user_id, role_id)
VALUES (1, 1),
       (1, 1),
       (1, 1),
       (1, 1),
       (1, 1);
