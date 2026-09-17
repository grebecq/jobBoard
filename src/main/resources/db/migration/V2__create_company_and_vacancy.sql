CREATE TABLE company (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url   VARCHAR(512),
    website    VARCHAR(512),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vacancy (
    id              BIGSERIAL    PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT         NOT NULL,
    salary_from     INTEGER,
    salary_to       INTEGER,
    city            VARCHAR(255),
    employment_type VARCHAR(50),
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    company_id      BIGINT       NOT NULL,
    CONSTRAINT fk_vacancy_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE INDEX idx_vacancy_company_id ON vacancy(company_id);
CREATE INDEX idx_vacancy_status ON vacancy(status);
