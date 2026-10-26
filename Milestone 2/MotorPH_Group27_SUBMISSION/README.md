# MotorPH Payroll Management System (Advanced OOP Version), Group 27

Secured three-tier Java web app for MO-IT153 (IAS 2). It implements the two controls in our Implementation Log.

| Control | Where to show it |
|---|---|
| #1 Parameterized Queries (PreparedStatement) | `src/main/java/com/motorph/dao/EmployeeDAO.java`, `UserDAO.java`, `PayrollDAO.java` |
| #2 Argon2id hashing + .env config vault | `config/DatabaseConfig.java`, `service/AuthService.java`, `security/PasswordUtil.java`, `pom.xml`, `.env.example`, `sql/02_migration_expand_password.sql` |

## Architecture (three tiers)

```
Browser (index.html)
   |
Presentation tier   controller/   AuthController, EmployeeController, PayrollController
   |                              BaseController maps exceptions -> HTTP 400 / 401 / 403 / 500
Logic tier          service/      AuthService (legacy migration), EmployeeManager, PayrollCalculator
                    security/     PasswordUtil (Argon2id hash + verify), SessionManager
   |
Data access tier    dao/          UserDAO, EmployeeDAO, PayrollDAO (PreparedStatement only)
   |
DatabaseConfig (singleton, reads .env ONCE)  ->  MySQL
```

## How to run (Mac or Windows)

Requirements: JDK 17 or newer, Maven, MySQL 8.

1. Create the database (run in this order):
   ```
   mysql -u root -p < sql/01_schema.sql
   mysql -u root -p < sql/02_migration_expand_password.sql
   mysql -u root -p < sql/03_seed.sql
   ```
2. Create a limited database account for the app (do not use root):
   ```sql
   CREATE USER 'motorph_app'@'localhost' IDENTIFIED BY 'your_password_here';
   GRANT SELECT, INSERT, UPDATE ON motorph_payroll.* TO 'motorph_app'@'localhost';
   ```
3. Copy `.env.example` to `.env` and put that password in `DB_PASSWORD`.
4. Build and run from the project folder:
   ```
   mvn clean package
   java -jar target/motorph-payroll.jar
   ```
   Or open the folder in IntelliJ / NetBeans / VS Code and run `com.motorph.App`.
5. Open http://localhost:8080

## Test accounts (start as legacy plaintext, auto-upgraded to Argon2id on first login)

| Username | Password | Role | Can do |
|---|---|---|---|
| admin | admin123 | ADMIN | everything |
| hr.andrea | hrpass2026 | HR | search, compute payroll, any history |
| emp.alice | alice2026 | EMPLOYEE | own payslip history only (employee 10008) |

## Demo walkthrough

1. **Show plaintext first:** `SELECT username, password FROM motorph_payroll.users;` (all plaintext).
2. **Log in as admin**, then run the same query. The admin row is now `$argon2id$v=19$m=65536,t=3,p=1$...` (97 chars). The others stay plaintext until they log in. That is Control #2 migration.
3. **Wrong password** -> red message "Invalid username or password. (HTTP 401)". Same message for a fake username, so attackers cannot tell which usernames exist.
4. **SQL injection:** in Employee search type `' OR '1'='1' --` -> "0 record(s) found". Try `' UNION SELECT null, username, password FROM users --` -> also 0. That is Control #1.
5. **Compute payroll** for 10005, 160 hours -> payslip saved. Load history.
6. **Role check:** log in as emp.alice. Search and Compute are hidden; history is locked to 10008. Calling the API for another ID returns HTTP 403.

Optional, to reproduce the "Issue Encountered" from the log: skip step `02_migration...` and log in. The server log shows `Data too long for column 'password'` and the user gets a generic HTTP 500 (no SQL detail leaked). Run the migration and it works.

## Payroll formula (simplified monthly model)

Basic pay = hourly rate x hours. Gross = basic pay + rice, phone and clothing allowances.
SSS 5% of basic salary (max 1,750). PhilHealth 2.5% employee share (salary basis 10,000 to 100,000). Pag-IBIG 2% (max 200).
Withholding tax uses the TRAIN law monthly table on (basic pay minus SSS, PhilHealth, Pag-IBIG).
