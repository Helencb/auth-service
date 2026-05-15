CREATE TABLE roles (
                       id VARCHAR(255) PRIMARY KEY,
                       name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE users_roles (
                             user_id VARCHAR(255),
                             role_id VARCHAR(255),

                             CONSTRAINT fk_user
                                 FOREIGN KEY (user_id) REFERENCES users(id),

                             CONSTRAINT fk_role
                                 FOREIGN KEY (role_id) REFERENCES roles(id)
);