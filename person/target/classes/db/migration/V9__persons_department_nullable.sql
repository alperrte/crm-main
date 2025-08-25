-- V7__persons_department_nullable.sql
-- Departman ataması sonradan yapılacağı için NULL olmalı
IF EXISTS (SELECT 1 FROM sys.columns
           WHERE Name = N'department_id'
             AND Object_ID = Object_ID(N'dbo.persons'))
BEGIN
    -- Eski FK varsa kaldır
    DECLARE @fkname nvarchar(200);
SELECT @fkname = fk.NAME
FROM sys.foreign_keys fk
         JOIN sys.objects t ON fk.parent_object_id = t.object_id
WHERE t.name = 'persons' AND fk.name LIKE 'FK_persons_departments%';

IF @fkname IS NOT NULL
        EXEC('ALTER TABLE dbo.persons DROP CONSTRAINT ' + @fkname);

    -- Kolonu NULL yapılır
ALTER TABLE dbo.persons ALTER COLUMN department_id BIGINT NULL;

-- FK tekrar eklenir (NULL’a izin verir)
ALTER TABLE dbo.persons
    ADD CONSTRAINT FK_persons_departments
        FOREIGN KEY (department_id) REFERENCES dbo.departments(department_id);
END
GO
