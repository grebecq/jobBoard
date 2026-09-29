ALTER TABLE company   ADD COLUMN contact_email VARCHAR(255);
ALTER TABLE company   ADD COLUMN telegram      VARCHAR(32);
ALTER TABLE candidate ADD COLUMN telegram      VARCHAR(32);

ALTER TABLE application ADD COLUMN employer_comment TEXT;
