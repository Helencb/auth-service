CREATE TABLE refresh_tokens (
                                id VARCHAR(255) PRIMARY KEY,
                                token VARCHAR(1000) UNIQUE NOT NULL,
                                revoked BOOLEAN,
                                expires_at TIMESTAMP,
                                user_id VARCHAR(255),

                                CONSTRAINT fk_refresh_user
                                    FOREIGN KEY (user_id) REFERENCES users(id)
);