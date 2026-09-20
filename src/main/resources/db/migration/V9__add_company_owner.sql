-- nullable: у компаний, заведённых до этой миграции, владельца нет
ALTER TABLE company ADD COLUMN owner_id BIGINT;
ALTER TABLE company ADD CONSTRAINT fk_company_owner FOREIGN KEY (owner_id) REFERENCES users(id);

CREATE INDEX idx_company_owner_id ON company(owner_id);
