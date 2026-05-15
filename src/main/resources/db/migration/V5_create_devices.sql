CREATE TABLE devices (
                         id VARCHAR(255) PRIMARY KEY,
                         fingerprint VARCHAR(255) NOT NULL,
                         device_name VARCHAR(255),
                         device_type VARCHAR(255),
                         operating_system VARCHAR(255),
                         browser VARCHAR(255),
                         ip_address VARCHAR(255),
                         last_login_at TIMESTAMP,
                         user_id VARCHAR(255),

                         CONSTRAINT fk_device_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
);