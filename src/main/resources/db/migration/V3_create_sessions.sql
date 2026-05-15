CREATE TABLE user_sessions (
                               id VARCHAR(255) PRIMARY KEY,

                               access_token VARCHAR(2000),
                               refresh_token VARCHAR(2000),

                               ip_address VARCHAR(255),
                               user_agent VARCHAR(1000),

                               active BOOLEAN,

                               created_at TIMESTAMP,
                               expires_at TIMESTAMP,

                               user_id VARCHAR(255),
                               device_id VARCHAR(255),

                               CONSTRAINT fk_session_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id),

                               CONSTRAINT fk_session_device
                                   FOREIGN KEY (device_id)
                                       REFERENCES devices(id)
);