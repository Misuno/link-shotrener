CREATE TABLE link_shortener.links (
	id SERIAL,
	short_link varchar(100) NOT NULL,
	long_link varchar(100) NOT NULL,
	chat varchar(100) NULL,
	PRIMARY KEY (id))
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;