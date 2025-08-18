-- src/main/resources/db/migration/V6__create_table_tickets.sql
CREATE TABLE tickets(
                        ticket_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                        issue NVARCHAR(MAX) NOT NULL,
                        priority NVARCHAR(50) NOT NULL,
                        is_active BIT DEFAULT 1,
                        created_date DATETIME2,
                        closed_date DATETIME2,
                        is_employee BIT NOT NULL DEFAULT 0,

                        creator_customer_id BIGINT NULL,
                        creator_person_id BIGINT NULL
);
ALTER TABLE tickets
    ADD CONSTRAINT CK_tickets_creator
        CHECK(
            (is_employee=1 AND creator_person_id IS NOT NULL AND creator_customer_id IS NULL) OR
            (is_employee=0 AND creator_customer_id IS NOT NULL AND creator_person_id IS NULL)
            );
GO

ALTER TABLE tickets
    ADD CONSTRAINT FK_ticket_creator_customers
        FOREIGN KEY (creator_customer_id) REFERENCES customers(customer_id);
GO

ALTER TABLE tickets
    ADD CONSTRAINT Fk_ticket_creator_persons
        FOREIGN KEY (creator_person_id) REFERENCES dbo.persons(person_id);
GO
