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
