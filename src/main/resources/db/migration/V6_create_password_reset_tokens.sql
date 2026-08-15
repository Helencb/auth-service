CREATE TABLE password_reset_tokens (
                                        id VARCHAR(255) PRIMARY KEY,
                                        token VARCHAR(255) UNIQUE NOT NULL,
                                        used BOOLEAN,
                                        expires_at TIMESTAMP,
                                        created_at TIMESTAMP,
                                        user_id VARCHAR(255),

                                        CONSTRAINT fk_reset_token_user
                                            FOREIGN KEY (user_id) REFERENCES users(id)
);
