-- IT-критерии вакансии по образцу hh.ru / Хабр Карьеры
ALTER TABLE vacancy ADD COLUMN specialization VARCHAR(40);
ALTER TABLE vacancy ADD COLUMN grade          VARCHAR(20);
ALTER TABLE vacancy ADD COLUMN experience     VARCHAR(20);
ALTER TABLE vacancy ADD COLUMN work_format    VARCHAR(20);

-- раньше «удалёнка» жила в employment_type — это формат работы, а не тип занятости
UPDATE vacancy SET work_format = 'REMOTE', employment_type = 'FULL_TIME' WHERE employment_type = 'REMOTE';
UPDATE vacancy SET employment_type = NULL
WHERE employment_type IS NOT NULL
  AND employment_type NOT IN ('FULL_TIME', 'PART_TIME', 'PROJECT', 'INTERNSHIP');

CREATE INDEX idx_vacancy_specialization ON vacancy(specialization);
CREATE INDEX idx_vacancy_grade          ON vacancy(grade);
CREATE INDEX idx_vacancy_status_created ON vacancy(status, created_at DESC);

-- справочник технологий
ALTER TABLE skill ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'OTHER';
CREATE INDEX idx_skill_category ON skill(category);

INSERT INTO skill (name, category) VALUES
-- языки
('Java','LANGUAGE'),('Kotlin','LANGUAGE'),('Scala','LANGUAGE'),('Groovy','LANGUAGE'),('Python','LANGUAGE'),
('Go','LANGUAGE'),('C','LANGUAGE'),('C++','LANGUAGE'),('C#','LANGUAGE'),('Rust','LANGUAGE'),
('JavaScript','LANGUAGE'),('TypeScript','LANGUAGE'),('PHP','LANGUAGE'),('Ruby','LANGUAGE'),('Swift','LANGUAGE'),
('Objective-C','LANGUAGE'),('Dart','LANGUAGE'),('Elixir','LANGUAGE'),('Erlang','LANGUAGE'),('Haskell','LANGUAGE'),
('Clojure','LANGUAGE'),('F#','LANGUAGE'),('Lua','LANGUAGE'),('Perl','LANGUAGE'),('R','LANGUAGE'),
('MATLAB','LANGUAGE'),('Julia','LANGUAGE'),('Solidity','LANGUAGE'),('1С','LANGUAGE'),('SQL','LANGUAGE'),
('Bash','LANGUAGE'),('PowerShell','LANGUAGE'),('Assembler','LANGUAGE'),('Delphi','LANGUAGE'),('COBOL','LANGUAGE'),
-- бэкенд
('Spring Boot','BACKEND'),('Spring Framework','BACKEND'),('Spring Cloud','BACKEND'),('Spring Security','BACKEND'),
('Hibernate','BACKEND'),('JPA','BACKEND'),('Quarkus','BACKEND'),('Micronaut','BACKEND'),('Ktor','BACKEND'),
('Jakarta EE','BACKEND'),('Django','BACKEND'),('Flask','BACKEND'),('FastAPI','BACKEND'),('Celery','BACKEND'),
('Node.js','BACKEND'),('Express','BACKEND'),('NestJS','BACKEND'),('Laravel','BACKEND'),('Symfony','BACKEND'),
('Ruby on Rails','BACKEND'),('ASP.NET Core','BACKEND'),('.NET','BACKEND'),('Entity Framework','BACKEND'),
('Gin','BACKEND'),('gRPC','BACKEND'),('GraphQL','BACKEND'),('REST API','BACKEND'),('WebSocket','BACKEND'),
('Microservices','BACKEND'),
-- фронтенд
('HTML','FRONTEND'),('CSS','FRONTEND'),('SASS','FRONTEND'),('Tailwind CSS','FRONTEND'),('Bootstrap','FRONTEND'),
('React','FRONTEND'),('Next.js','FRONTEND'),('Redux','FRONTEND'),('MobX','FRONTEND'),('Vue.js','FRONTEND'),
('Nuxt','FRONTEND'),('Angular','FRONTEND'),('RxJS','FRONTEND'),('Svelte','FRONTEND'),('jQuery','FRONTEND'),
('Webpack','FRONTEND'),('Vite','FRONTEND'),('Storybook','FRONTEND'),('Three.js','FRONTEND'),
-- мобильная разработка
('Android SDK','MOBILE'),('Jetpack Compose','MOBILE'),('iOS SDK','MOBILE'),('SwiftUI','MOBILE'),('UIKit','MOBILE'),
('Flutter','MOBILE'),('React Native','MOBILE'),('Kotlin Multiplatform','MOBILE'),('Xamarin','MOBILE'),
-- базы данных
('PostgreSQL','DATABASE'),('MySQL','DATABASE'),('MariaDB','DATABASE'),('Oracle','DATABASE'),('MS SQL Server','DATABASE'),
('SQLite','DATABASE'),('MongoDB','DATABASE'),('Redis','DATABASE'),('Cassandra','DATABASE'),('ClickHouse','DATABASE'),
('Elasticsearch','DATABASE'),('OpenSearch','DATABASE'),('Neo4j','DATABASE'),('Greenplum','DATABASE'),('Tarantool','DATABASE'),
-- брокеры сообщений
('Kafka','MESSAGING'),('RabbitMQ','MESSAGING'),('ActiveMQ','MESSAGING'),('NATS','MESSAGING'),
-- devops
('Docker','DEVOPS'),('Kubernetes','DEVOPS'),('Helm','DEVOPS'),('OpenShift','DEVOPS'),('Terraform','DEVOPS'),
('Ansible','DEVOPS'),('Jenkins','DEVOPS'),('GitLab CI','DEVOPS'),('GitHub Actions','DEVOPS'),('TeamCity','DEVOPS'),
('ArgoCD','DEVOPS'),('Nginx','DEVOPS'),('Linux','DEVOPS'),('Prometheus','DEVOPS'),('Grafana','DEVOPS'),
('ELK','DEVOPS'),('Zabbix','DEVOPS'),('Vault','DEVOPS'),
-- облака
('AWS','CLOUD'),('Google Cloud','CLOUD'),('Azure','CLOUD'),('Yandex Cloud','CLOUD'),('VK Cloud','CLOUD'),('Selectel','CLOUD'),
-- тестирование
('JUnit','TESTING'),('Mockito','TESTING'),('Testcontainers','TESTING'),('TestNG','TESTING'),('Selenium','TESTING'),
('Selenide','TESTING'),('Playwright','TESTING'),('Cypress','TESTING'),('Appium','TESTING'),('Postman','TESTING'),
('JMeter','TESTING'),('Gatling','TESTING'),('pytest','TESTING'),('Jest','TESTING'),('Allure','TESTING'),('k6','TESTING'),
-- data / ml
('Pandas','DATA_ML'),('NumPy','DATA_ML'),('scikit-learn','DATA_ML'),('PyTorch','DATA_ML'),('TensorFlow','DATA_ML'),
('Spark','DATA_ML'),('Hadoop','DATA_ML'),('Airflow','DATA_ML'),('dbt','DATA_ML'),('Jupyter','DATA_ML'),
('LLM','DATA_ML'),('NLP','DATA_ML'),('Computer Vision','DATA_ML'),('MLflow','DATA_ML'),('Power BI','DATA_ML'),
('Tableau','DATA_ML'),('Superset','DATA_ML'),
-- геймдев
('Unity','GAMEDEV'),('Unreal Engine','GAMEDEV'),('Godot','GAMEDEV'),
-- безопасность
('OWASP','SECURITY'),('Pentest','SECURITY'),('SIEM','SECURITY'),('Burp Suite','SECURITY'),
-- инструменты
('Git','TOOLS'),('Maven','TOOLS'),('Gradle','TOOLS'),('Jira','TOOLS'),('Confluence','TOOLS'),('Figma','TOOLS'),
-- практики
('Agile','PRACTICES'),('Scrum','PRACTICES'),('Kanban','PRACTICES'),('CI/CD','PRACTICES'),('TDD','PRACTICES'),
('DDD','PRACTICES'),('ООП','PRACTICES'),('SOLID','PRACTICES'),('Паттерны проектирования','PRACTICES'),
('System Design','PRACTICES'),('Многопоточность','PRACTICES'),('Алгоритмы и структуры данных','PRACTICES')
ON CONFLICT (name) DO UPDATE SET category = EXCLUDED.category;
