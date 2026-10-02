CREATE TABLE refresh_tokens (
                                id BINARY(16) PRIMARY KEY,
                                token VARCHAR(512) NOT NULL UNIQUE,
                                user_id BINARY(16) NOT NULL,
                                expiry_date DATETIME NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);