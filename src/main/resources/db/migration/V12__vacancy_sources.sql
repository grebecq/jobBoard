-- Вакансии, собранные с внешних сайтов (hh.ru и др.), хранятся рядом с обычными
ALTER TABLE vacancy ADD COLUMN source       VARCHAR(20)  NOT NULL DEFAULT 'JOBBOARD';
ALTER TABLE vacancy ADD COLUMN external_id  VARCHAR(64);
ALTER TABLE vacancy ADD COLUMN external_url VARCHAR(512);
ALTER TABLE vacancy ADD COLUMN published_at TIMESTAMP;
ALTER TABLE vacancy ADD COLUMN last_seen_at TIMESTAMP;

UPDATE vacancy SET published_at = created_at;
ALTER TABLE vacancy ALTER COLUMN published_at SET NOT NULL;

CREATE UNIQUE INDEX uq_vacancy_source_external ON vacancy(source, external_id) WHERE external_id IS NOT NULL;
CREATE INDEX idx_vacancy_source ON vacancy(source);
CREATE INDEX idx_vacancy_status_published ON vacancy(status, published_at DESC);

ALTER TABLE company ADD COLUMN source      VARCHAR(20) NOT NULL DEFAULT 'JOBBOARD';
ALTER TABLE company ADD COLUMN external_id VARCHAR(64);

CREATE UNIQUE INDEX uq_company_source_external ON company(source, external_id) WHERE external_id IS NOT NULL;
