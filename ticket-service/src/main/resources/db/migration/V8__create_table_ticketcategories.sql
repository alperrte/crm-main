CREATE TABLE categories (
                            category_id          INT IDENTITY(1,1) PRIMARY KEY,
                            category_key         NVARCHAR(64)   NOT NULL,
                            display_name         NVARCHAR(128)  NOT NULL,
                            target_department_id BIGINT         NULL,
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
    -- Ana kategoriler
    ('HR_MAIN',       N'İnsan Kaynakları',       1),
    ('FINANCE_MAIN',  N'Finans',                 2),
    ('IT_MAIN',       N'Bilgi Teknolojileri',    3),

    -- İnsan Kaynakları alt kategoriler
    ('HR_RECRUIT',    N'İK - İşe Alım',          4),
    ('HR_TRAIN',      N'İK - Eğitim',            5),
    ('HR_PAYROLL',    N'İK - Bordro',            6),

    -- Finans alt kategoriler
    ('FIN_ACCOUNT',   N'Finans - Muhasebe',      7),
    ('FIN_AUDIT',     N'Finans - Denetim',       8),
    ('FIN_BILLING',   N'Finans - Faturalama',    9),

    -- Bilgi Teknolojileri alt kategoriler
    ('IT_HELPDESK',   N'BT - Yardım Masası',     10),
    ('IT_NETWORK',    N'BT - Ağ',                11),
    ('IT_SECURITY',   N'BT - Güvenlik',          12)
) AS src(category_key, display_name, target_department_id)
ON tgt.category_key = src.category_key
WHEN NOT MATCHED THEN
    INSERT (category_key, display_name, target_department_id, is_active)
    VALUES (src.category_key, src.display_name, src.target_department_id, 1);
GO
