
CREATE TABLE categories (
category_id          INT IDENTITY(1,1) PRIMARY KEY,
category_key         NVARCHAR(64)   NOT NULL,
display_name         NVARCHAR(128)  NOT NULL,
target_department_id BIGINT           NULL,
is_active            BIT            NOT NULL DEFAULT 1,
created_date         DATETIME2(3)   NOT NULL DEFAULT SYSUTCDATETIME(),
updated_date         DATETIME2(3)   NULL
);
GO

CREATE UNIQUE INDEX UX_categories_key ON categories(category_key);
GO

ALTER TABLE categories
    ADD CONSTRAINT FK_categories_department
        FOREIGN KEY (target_department_id) REFERENCES dbo.departments(department_id)
            ON DELETE SET NULL;
GO


MERGE INTO categories AS tgt
USING (VALUES
('CUSTOMER_SUPPORT', N'Müşteri Destek',         1),
('TECH_SUPPORT',     N'Teknik Destek',          2),
('IT_HELPDESK',      N'IT Yardım Masası',       3),
('HR',               N'İnsan Kaynakları',       4),
('FINANCE_BILLING',  N'Finans / Faturalama',    5)
) AS src(category_key, display_name, target_department_id)
ON tgt.category_key = src.category_key
WHEN NOT MATCHED THEN
    INSERT (category_key, display_name, target_department_id, is_active)
    VALUES (src.category_key, src.display_name, src.target_department_id, 1);
;
GO
