CREATE TABLE ticket_assignments (
                                    assignment_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
                                    ticket_id       BIGINT       NOT NULL,
                                    is_in_pool      BIT          NOT NULL DEFAULT 0,
                                    department_id   BIGINT       NULL,
                                    person_id       BIGINT       NULL,
                                    assigned_date   DATETIME2    NULL,
                                    completed_date  DATETIME2    NULL,
                                    status          NVARCHAR(20) NOT NULL DEFAULT 'OPEN'
);
GO

ALTER TABLE ticket_assignments
    ADD CONSTRAINT CK_ticket_assignments_target
        CHECK (
            (is_in_pool = 1 AND department_id IS NULL AND person_id IS NULL)
                OR
            (is_in_pool = 0 AND (
                (department_id IS NOT NULL AND person_id IS NULL) OR
                (department_id IS NULL AND person_id IS NOT NULL)
                ))
            );
GO

CREATE INDEX IX_ta_ticket     ON ticket_assignments(ticket_id);
CREATE INDEX IX_ta_department ON ticket_assignments(department_id);
CREATE INDEX IX_ta_person     ON ticket_assignments(person_id);
CREATE INDEX IX_ta_status     ON ticket_assignments(status);
GO

CREATE UNIQUE INDEX UX_ta_pool_per_ticket
    ON ticket_assignments(ticket_id)
    WHERE is_in_pool = 1;
GO

ALTER TABLE ticket_assignments
    ADD CONSTRAINT FK_ta_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
            ON DELETE CASCADE;
GO

ALTER TABLE ticket_assignments
    ADD CONSTRAINT FK_ta_department
        FOREIGN KEY (department_id) REFERENCES dbo.departments(department_id)
            ON DELETE SET NULL;
GO

ALTER TABLE ticket_assignments
    ADD CONSTRAINT FK_ta_person
        FOREIGN KEY (person_id) REFERENCES dbo.persons(person_id)
            ON DELETE SET NULL;
GO
