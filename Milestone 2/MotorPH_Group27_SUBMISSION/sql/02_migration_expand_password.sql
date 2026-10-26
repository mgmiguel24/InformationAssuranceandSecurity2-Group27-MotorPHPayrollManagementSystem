-- Control #2 issue fix: Argon2id hashes (~97 chars) exceed VARCHAR(60).
USE motorph_payroll;
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;
