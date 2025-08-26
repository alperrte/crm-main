IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UX_users_email' AND object_id = OBJECT_ID('dbo.users'))
BEGIN
CREATE UNIQUE INDEX UX_users_email ON dbo.users(email);
END;

-- 3) İlk admin'i seed et (şifre: Admin123! - BCrypt)
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'alpertemiz15@gmail.com')
BEGIN
INSERT INTO dbo.users(email, password_hash, role, name, surname, phone)
VALUES (
           'alpertemiz15@gmail.com',
           '$2b$12$ItcQQOZt34kur/KqLMszuu6lYBQM/yqmiL5pBm5IWX3gL1R87wATy', -- BCrypt("admin123!")
           'ADMIN',
           'Alper',
           'Temiz',
           '5348765765'
       );
END;
