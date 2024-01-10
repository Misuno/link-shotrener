CREATE TABLE links (
	id SERIAL,
	short varchar(100) NOT NULL,
	`long` varchar(100) NOT NULL,
	author varchar(100) NULL,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id))
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;
