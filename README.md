# MotorPH Payroll Management System (Advanced OOP Version)
### MO-IT153 Information Assurance and Security 2 | Section A4101 | Group 27

Mapúa Malayan Digital College, in collaboration with Arizona State University

This repository holds the security artifacts, evidence, and source code for our IAS 2 project. We took the MotorPH Payroll Management System and hardened it against common attacks, documenting every control from plan to tested result.

---

## Repository Contents

| Folder | What is inside |
|---|---|
| `Archive/` | IAS 1 artifacts we built on |
| `Milestone 1/` | Security Controls Audit Worksheet, Security Implementation Plan, Implementation Execution Workflow |
| `Milestone 2/` | Implementation Log, Security Solution Evaluation Package, architecture diagram, test evidence |
| `Terminal Assessment/` | Working Output for Terminal Assessment |

---

## Security Controls Implemented

| # | Control | Type | Where it lives | Tool / Library | Status |
|---|---|---|---|---|---|
| 1 | Parameterized Database Query Execution | Technical | Data Access Layer (`EmployeeDAO.java`, `UserDAO.java`, `PayrollDAO.java`) | Java JDBC `PreparedStatement` (Java SE) | Verified |
| 2 | Argon2id Password Hashing and Externalized Configuration Vault | Technical | Authentication and Connection Layer (`DatabaseConfig.java`, `AuthService.java`) | argon2-jvm 2.11, dotenv-java 3.0.0 | Verified |

### Control 1: Parameterized Queries
**Threat addressed:** SQL injection, where an attacker types database commands into a search box to steal or alter records.
**What we changed:** We replaced string concatenated SQL with `PreparedStatement` across all DAOs, so user input is always treated as plain data.
**Test:** Union based exfiltration payload (`' UNION SELECT null, username, password FROM users --`).
**Result:** The query returned an empty set. The payload was neutralized at the database driver level.
**Evidence:** `Evidence_Control1_PreparedStatement_SQLi_Blocked.png`

### Control 2: Argon2id Hashing and Externalized Configuration
**Threat addressed:** Stolen password databases and hardcoded credentials.
**What we changed:** We moved from the planned bcrypt approach to Argon2id, and moved database credentials out of the code into a `.env` file loaded once at startup. Legacy plaintext passwords are upgraded to Argon2id on first successful login.
**Test:** Login attempt with an incorrect password against an Argon2id hashed record.
**Result:** Rejected with HTTP 401 using constant time verification. No timing leak observed.
**Evidence:** `Evidence_Control2_Argon2id_Database_Hash.png`

---

## Running the Source Code

1. Extract `03_Source_Code/MotorPH_Group27_SUBMISSION.zip`.
2. Copy `.env.example` to `.env` and fill in your local database credentials.
3. Run `ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;` so the column can hold Argon2id hashes.
4. Build with Maven (`mvn clean install`) and run the application.

> The real `.env` file is never committed to this repository. Only `.env.example` is provided.

---

## Group 27 Members

| Name | Role |
|---|---|
| [Member name] | [Role] |
| [Member name] | [Role] |
| [Member name] | [Role] |
