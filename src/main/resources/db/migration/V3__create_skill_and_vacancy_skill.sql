CREATE TABLE skill (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE vacancy_skill (
    vacancy_id BIGINT NOT NULL,
    skill_id   BIGINT NOT NULL,
    PRIMARY KEY (vacancy_id, skill_id),
    CONSTRAINT fk_vs_vacancy FOREIGN KEY (vacancy_id) REFERENCES vacancy(id),
    CONSTRAINT fk_vs_skill   FOREIGN KEY (skill_id)   REFERENCES skill(id)
);

CREATE INDEX idx_vacancy_skill_skill_id ON vacancy_skill(skill_id);
