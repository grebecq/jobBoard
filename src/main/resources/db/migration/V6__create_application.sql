CREATE TABLE application (
    id            BIGSERIAL   PRIMARY KEY,
    status        VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    cover_letter  TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    candidate_id  BIGINT      NOT NULL,
    vacancy_id    BIGINT      NOT NULL,
    CONSTRAINT fk_application_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id),
    CONSTRAINT fk_application_vacancy   FOREIGN KEY (vacancy_id)   REFERENCES vacancy(id),
    CONSTRAINT uq_application_candidate_vacancy UNIQUE (candidate_id, vacancy_id)
);

CREATE INDEX idx_application_vacancy_id   ON application(vacancy_id);
CREATE INDEX idx_application_candidate_id ON application(candidate_id);
CREATE INDEX idx_application_status       ON application(status);
