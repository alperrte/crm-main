IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'crmdb')
BEGIN
    CREATE DATABASE [crmdb];
END;
GO
