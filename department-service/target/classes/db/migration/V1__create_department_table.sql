CREATE TABLE departments (
                             department_id BIGINT PRIMARY KEY IDENTITY(1,1),
                             name NVARCHAR(255) NOT NULL,
                             parent_department_id BIGINT NULL,
                             is_active BIT DEFAULT 1,
                             is_deleted BIT DEFAULT 0,
                             deleted_user_id BIGINT NULL,
                             is_updated BIT DEFAULT 0,
                             updated_user_id BIGINT NULL
);

MERGE INTO dbo.departments AS tgt
    USING (VALUES
               (N'CUSTOMER SUPPORT',       NULL),
               (N'TECH_SUPPORT',        NULL),
               (N'IT_HELPDESK',     NULL),
               (N'HR',     NULL),
               (N'FINANCE_BILLING',  NULL)
    ) AS src(name, parent_department_id)
    ON (tgt.name = src.name)
    WHEN NOT MATCHED THEN
        INSERT (name, parent_department_id)
            VALUES (src.name, src.parent_department_id)
;
GO