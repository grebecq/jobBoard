ALTER TABLE candidate ADD COLUMN user_id BIGINT;
ALTER TABLE candidate ADD CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE candidate ADD CONSTRAINT uq_candidate_user UNIQUE (user_id);
ALTER TABLE candidate ALTER COLUMN first_name DROP NOT NULL;
ALTER TABLE candidate ALTER COLUMN last_name  DROP NOT NULL;
