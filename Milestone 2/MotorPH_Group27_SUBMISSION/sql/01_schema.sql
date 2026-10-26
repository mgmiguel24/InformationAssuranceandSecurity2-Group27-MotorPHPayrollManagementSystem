-- MotorPH Payroll Management System: database schema
CREATE DATABASE IF NOT EXISTS motorph_payroll;
USE motorph_payroll;

DROP TABLE IF EXISTS payroll_records;
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
    employee_id    INT PRIMARY KEY,
    last_name      VARCHAR(50)  NOT NULL,
    first_name     VARCHAR(50)  NOT NULL,
    position       VARCHAR(80)  NOT NULL,
    department     VARCHAR(50)  NOT NULL,
    basic_salary   DECIMAL(12,2) NOT NULL,
    hourly_rate    DECIMAL(10,2) NOT NULL,
    rice_subsidy   DECIMAL(10,2) NOT NULL DEFAULT 1500.00,
    phone_allowance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    clothing_allowance DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

-- NOTE: password was originally VARCHAR(60) (sized for bcrypt).
-- See 02_migration_expand_password.sql for the Argon2id fix.
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(60) NOT NULL,
    role        VARCHAR(20) NOT NULL,
    employee_id INT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE payroll_records (
    payroll_id      INT AUTO_INCREMENT PRIMARY KEY,
    employee_id     INT NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    hours_worked    DECIMAL(6,2) NOT NULL,
    gross_pay       DECIMAL(12,2) NOT NULL,
    sss             DECIMAL(10,2) NOT NULL,
    philhealth      DECIMAL(10,2) NOT NULL,
    pagibig         DECIMAL(10,2) NOT NULL,
    withholding_tax DECIMAL(10,2) NOT NULL,
    net_pay         DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);
