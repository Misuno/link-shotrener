CREATE TABLE sessions (
	id SERIAL,
	user_id bigint(20) unsigned NOT NULL,
	session_token varchar(100) NOT NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	manually_disabled bool DEFAULT false,
	PRIMARY KEY (id))
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;
