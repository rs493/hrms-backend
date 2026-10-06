CREATE TABLE IF NOT EXISTS user_activation_tokens (
	token_id INT NOT NULL AUTO_INCREMENT,
	user_id INT NOT NULL,
	token_hash VARCHAR(64) NOT NULL,
	expires_at TIMESTAMP NOT NULL,
	used_at TIMESTAMP NULL,
	created_at TIMESTAMP NOT NULL,
	PRIMARY KEY (token_id),
	UNIQUE KEY uk_user_activation_tokens_token_hash (token_hash),
	KEY idx_user_activation_tokens_user_id (user_id),
	CONSTRAINT fk_user_activation_tokens_user
		FOREIGN KEY (user_id) REFERENCES users (user_id)
);
