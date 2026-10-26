-- Seed data. User passwords start as LEGACY PLAINTEXT on purpose.
-- AuthService migrates each one to Argon2id on that user's first successful login.
USE motorph_payroll;

INSERT INTO employees VALUES
(10001,'Garcia','Manuel III','Chief Executive Officer','Leadership',90000,535.71,1500,2000,1000),
(10002,'Lim','Antonio','Chief Operating Officer','Leadership',60000,357.14,1500,2000,1000),
(10003,'Aquino','Bianca Sofia','Chief Finance Officer','Leadership',60000,357.14,1500,2000,1000),
(10004,'Reyes','Isabella','Chief Marketing Officer','Leadership',60000,357.14,1500,2000,1000),
(10005,'Hernandez','Eduard','IT Operations and Systems','IT',52670,313.51,1500,1000,1000),
(10006,'Villanueva','Andrea Mae','HR Manager','HR',52670,313.51,1500,1000,1000),
(10007,'San Jose','Brad','HR Team Leader','HR',42975,255.80,1500,800,800),
(10008,'Romualdez','Alice','HR Rank and File','HR',22500,133.93,1500,500,500),
(10009,'Atienza','Rosie','HR Rank and File','HR',22500,133.93,1500,500,500),
(10010,'Alvaro','Roderick','Accounting Head','Finance',52670,313.51,1500,1000,1000);

INSERT INTO users (username, password, role, employee_id) VALUES
('admin',     'admin123',   'ADMIN',    10001),
('hr.andrea', 'hrpass2026', 'HR',       10006),
('emp.alice', 'alice2026',  'EMPLOYEE', 10008);
