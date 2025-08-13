IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'DB_NAME')
BEGIN
    PRINT 'Creating database DB_NAME';
    DECLARE @sql NVARCHAR(MAX) = N'CREATE DATABASE [' + REPLACE('DB_NAME', '''', '''''') + N']';
EXEC sp_executesql @sql;
END
ELSE
BEGIN
    PRINT 'Database DB_NAME already exists.';
END
