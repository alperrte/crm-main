CREATE TABLE persons(
                        person_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                        name NVARCHAR(255) NOT NULL,
                        surname NVARCHAR(255) NOT NULL,
                        email NVARCHAR(255) NOT NULL,
                        phone NVARCHAR(255) NULL,
                        is_active BIT NOT NULL DEFAULT 1,
                        department_id BIGINT NOT NULL
);

-- Department foreign key
ALTER TABLE persons
    ADD CONSTRAINT FK_persons_departments
        FOREIGN KEY (department_id)
            REFERENCES departments(department_id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;


