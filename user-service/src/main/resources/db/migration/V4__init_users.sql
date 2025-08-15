CREATE TABLE users(
                      user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                      user_name NVARCHAR(255) NOT NULL,
                      password_hash NVARCHAR(255) NOT NULL,
                      refresh_token_hash NVARCHAR(255),
                      refresh_token_expires_at DATETIME2,
                      role NVARCHAR(255),
                      person_id BIGINT
);

ALTER TABLE users
    ADD CONSTRAINT FK_users_persons
        FOREIGN KEY (person_id)
            REFERENCES persons(person_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;