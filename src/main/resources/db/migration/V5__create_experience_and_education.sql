CREATE TABLE experience (
    id          BIGSERIAL    PRIMARY KEY,
    company     VARCHAR(255) NOT NULL,
    position    VARCHAR(255) NOT NULL,
    description TEXT,
    start_date  DATE         NOT NULL,
    end_date    DATE,
    resume_id   BIGINT       NOT NULL,
    CONSTRAINT fk_experience_resume FOREIGN KEY (resume_id) REFERENCES resume(id)
);

CREATE INDEX idx_experience_resume_id ON experience(resume_id);

CREATE TABLE education (
    id          BIGSERIAL    PRIMARY KEY,
    institution VARCHAR(255) NOT NULL,
    degree      VARCHAR(255),
    field       VARCHAR(255),
    start_date  DATE         NOT NULL,
    end_date    DATE,
    resume_id   BIGINT       NOT NULL,
    CONSTRAINT fk_education_resume FOREIGN KEY (resume_id) REFERENCES resume(id)
);

CREATE INDEX idx_education_resume_id ON education(resume_id);
