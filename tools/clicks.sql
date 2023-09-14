CREATE TABLE link_shortener.clicks (
	id SERIAL,
	short_link varchar(100) NOT NULL,
	click_data TEXT NULL,
	click_ts TIMESTAMP NOT NULL,
	PRIMARY KEY (id))
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;