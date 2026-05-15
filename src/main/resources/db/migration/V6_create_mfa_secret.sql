CREATE TABLE mfa_secrets (

                             id VARCHAR(255) PRIMARY KEY,

                             type VARCHAR(255),

                             secret VARCHAR(255) NOT NULL,

                             enabled BOOLEAN,

                             backup_codes TEXT,

                             user_id VARCHAR(255) UNIQUE,

                             CONSTRAINT fk_mfa_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
);