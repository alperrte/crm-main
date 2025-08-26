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

-- Ana ve alt departmanlar
MERGE INTO dbo.departments AS tgt
    USING (VALUES
               -- Ana departmanlar
               (N'İnsan Kaynakları',    NULL),
               (N'Finans',              NULL),
               (N'Bilgi Teknolojileri', NULL),

               -- İnsan Kaynakları alt departmanları
               (N'İK - İşe Alım',       1),
               (N'İK - Eğitim',         1),
               (N'İK - Bordro',         1),

               -- Finans alt departmanları
               (N'Finans - Muhasebe',   2),
               (N'Finans - Denetim',    2),
               (N'Finans - Faturalama', 2),

               -- Bilgi Teknolojileri alt departmanları
               (N'BT - Yardım Masası',  3),
               (N'BT - Ağ',             3),
               (N'BT - Güvenlik',       3)
    ) AS src(name, parent_department_id)
    ON (tgt.name = src.name)
    WHEN NOT MATCHED THEN
        INSERT (name, parent_department_id)
            VALUES (src.name, src.parent_department_id);
GO
