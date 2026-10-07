-- Демо-компании и вакансии для витрины
INSERT INTO company (name, description) VALUES
('Северный код',     'Финтех-продукты для малого бизнеса: платежи, счета и онлайн-касса.'),
('Облачные решения', 'Облачная платформа: managed Kubernetes, базы данных и объектное хранилище.'),
('Логистик Тех',     'Сервисы для доставки и складов: маршрутизация, трекинг, учёт грузов.'),
('Учи.Код',          'Онлайн-школа программирования для школьников и студентов.'),
('Пиксель Плей',     'Мобильные игры и приложения.'),
('ДатаЛаб',          'Аналитика и машинное обучение для ретейла.');

INSERT INTO vacancy (company_id, title, specialization, grade, experience, work_format, employment_type, city, salary_from, salary_to, created_at, description)
SELECT c.id, v.title, v.spec, v.grade, v.exp, v.fmt, v.emp, v.city, v.sf, v.st, now() - v.age * interval '1 hour', v.descr
FROM (VALUES
('Северный код', 'Стажёр Java-разработчик', 'BACKEND', 'INTERN', 'NO_EXPERIENCE', 'HYBRID', 'INTERNSHIP', 'Санкт-Петербург', 60000, 80000, 1,
 E'Чем заниматься:\n• писать небольшие фичи в сервисе счетов под присмотром ментора\n• покрывать код тестами на JUnit\n\nЧто ждём:\n• Java Core, базовый SQL\n• пет-проект на Spring Boot\n\nУсловия: стажировка 3 месяца с наймом в штат по итогам.'),
('Логистик Тех', 'Junior Java-разработчик', 'BACKEND', 'JUNIOR', 'NO_EXPERIENCE', 'OFFICE', 'FULL_TIME', 'Екатеринбург', 90000, 130000, 2,
 E'Чем заниматься:\n• развивать REST API сервиса трекинга грузов\n• писать запросы и миграции для PostgreSQL\n\nЧто ждём:\n• Java, Spring Boot, Hibernate\n• понимание HTTP и REST\n\nУсловия: ментор на первые полгода, ДМС.'),
('Северный код', 'Java-разработчик (платежи)', 'BACKEND', 'MIDDLE', 'FROM_1_TO_3', 'HYBRID', 'FULL_TIME', 'Санкт-Петербург', 220000, 300000, 3,
 E'Чем заниматься:\n• писать микросервисы платёжного шлюза\n• настраивать обмен событиями через Kafka\n\nЧто ждём:\n• от 2 лет на Java\n• Spring Boot, Kafka, PostgreSQL\n\nУсловия: гибрид, квартальные премии.'),
('Облачные решения', 'Senior Java-разработчик', 'BACKEND', 'SENIOR', 'FROM_3_TO_6', 'REMOTE', 'FULL_TIME', 'Москва', 350000, 450000, 5,
 E'Чем заниматься:\n• развивать control plane облачной платформы\n• менторить команду\n\nЧто ждём:\n• от 5 лет на Java, опыт highload\n• Kubernetes, распределённые системы\n\nУсловия: удалёнка, опцион.'),
('Северный код', 'Руководитель группы Java-разработки', 'BACKEND', 'LEAD', 'MORE_THAN_6', 'HYBRID', 'FULL_TIME', 'Санкт-Петербург', 450000, NULL, 8,
 E'Чем заниматься:\n• руководить командой из 6 разработчиков\n• отвечать за архитектуру направления\n\nЧто ждём:\n• от 6 лет в разработке, от 2 лет тимлидом\n\nУсловия: годовой бонус, ДМС для семьи.'),
('Логистик Тех', 'Kotlin-разработчик', 'BACKEND', 'MIDDLE', 'FROM_1_TO_3', 'REMOTE', 'FULL_TIME', 'Екатеринбург', 200000, 260000, 12,
 E'Чем заниматься:\n• писать сервис расчёта маршрутов курьеров\n\nЧто ждём:\n• от года на Kotlin или Java\n• Spring Boot, PostgreSQL\n\nУсловия: удалёнка по России.'),
('Облачные решения', 'Go-разработчик', 'BACKEND', 'MIDDLE', 'FROM_1_TO_3', 'REMOTE', 'FULL_TIME', 'Москва', 250000, 320000, 18,
 E'Чем заниматься:\n• писать операторы Kubernetes для управляемых баз данных\n\nЧто ждём:\n• от 2 лет на Go\n\nУсловия: удалёнка.'),
('Учи.Код', 'Fullstack-разработчик (Java + React)', 'FULLSTACK', 'MIDDLE', 'FROM_1_TO_3', 'REMOTE', 'FULL_TIME', 'Москва', 200000, 250000, 24,
 E'Чем заниматься:\n• делать фичи платформы обучения от API до интерфейса\n\nЧто ждём:\n• Java и Spring Boot, React и TypeScript\n\nУсловия: удалёнка, курсы школы бесплатно.'),
('Учи.Код', 'Frontend-разработчик (React)', 'FRONTEND', 'JUNIOR', 'FROM_1_TO_3', 'HYBRID', 'FULL_TIME', 'Москва', 120000, 160000, 30,
 E'Чем заниматься:\n• развивать личный кабинет ученика\n\nЧто ждём:\n• JavaScript, TypeScript, React от года\n\nУсловия: гибрид.'),
('Северный код', 'Инженер по тестированию (Java)', 'QA', 'JUNIOR', 'NO_EXPERIENCE', 'HYBRID', 'FULL_TIME', 'Санкт-Петербург', 80000, 110000, 36,
 E'Чем заниматься:\n• писать автотесты UI и API\n\nЧто ждём:\n• основы Java и теории тестирования\n\nУсловия: обучение автоматизации внутри команды.'),
('Облачные решения', 'DevOps-инженер', 'DEVOPS', 'MIDDLE', 'FROM_3_TO_6', 'REMOTE', 'FULL_TIME', 'Москва', 260000, 330000, 48,
 E'Чем заниматься:\n• поддерживать CI/CD для 40+ сервисов\n• описывать инфраструктуру в Terraform\n\nЧто ждём:\n• от 3 лет в DevOps\n\nУсловия: удалёнка, дежурства оплачиваются.'),
('Пиксель Плей', 'Android-разработчик', 'MOBILE', 'MIDDLE', 'FROM_1_TO_3', 'OFFICE', 'FULL_TIME', 'Казань', 180000, 240000, 60,
 E'Чем заниматься:\n• разрабатывать приложение-компаньон для игр\n\nЧто ждём:\n• от 2 лет под Android на Kotlin\n\nУсловия: офис, релокация.'),
('Пиксель Плей', 'iOS-разработчик', 'MOBILE', 'SENIOR', 'FROM_3_TO_6', 'HYBRID', 'FULL_TIME', 'Казань', 300000, 380000, 72,
 E'Чем заниматься:\n• вести iOS-версию приложения\n\nЧто ждём:\n• от 4 лет под iOS, SwiftUI и UIKit\n\nУсловия: гибрид.'),
('Пиксель Плей', 'Unity-разработчик', 'GAMEDEV', 'JUNIOR', 'NO_EXPERIENCE', 'OFFICE', 'FULL_TIME', 'Казань', 90000, 120000, 84,
 E'Чем заниматься:\n• делать игровые механики в Unity\n\nЧто ждём:\n• C# и своя небольшая игра в портфолио\n\nУсловия: офис, наставник.'),
('ДатаЛаб', 'Data Engineer', 'DATA_ENGINEER', 'MIDDLE', 'FROM_1_TO_3', 'REMOTE', 'FULL_TIME', 'Новосибирск', 230000, 280000, 96,
 E'Чем заниматься:\n• строить пайплайны данных о продажах\n\nЧто ждём:\n• Python, уверенный SQL, Airflow\n\nУсловия: удалёнка.'),
('ДатаЛаб', 'Аналитик данных', 'DATA_ANALYST', 'JUNIOR', 'NO_EXPERIENCE', 'HYBRID', 'FULL_TIME', 'Новосибирск', 90000, 120000, 120,
 E'Чем заниматься:\n• считать метрики и собирать дашборды\n\nЧто ждём:\n• SQL с JOIN и оконными функциями, Python\n\nУсловия: гибрид.'),
('Логистик Тех', 'Системный аналитик', 'SYSTEM_ANALYST', 'MIDDLE', 'FROM_1_TO_3', 'OFFICE', 'FULL_TIME', 'Екатеринбург', 170000, 220000, 144,
 E'Чем заниматься:\n• описывать интеграции и проектировать API\n\nЧто ждём:\n• от 2 лет системным аналитиком, SQL\n\nУсловия: офис, ДМС.'),
('ДатаЛаб', 'ML-инженер', 'DATA_SCIENCE', 'SENIOR', 'FROM_3_TO_6', 'REMOTE', 'FULL_TIME', 'Новосибирск', NULL, NULL, 168,
 E'Чем заниматься:\n• обучать модели прогноза спроса\n\nЧто ждём:\n• от 3 лет в ML, PyTorch\n\nУсловия: зарплата по итогам собеседования.')
) AS v(company, title, spec, grade, exp, fmt, emp, city, sf, st, age, descr)
JOIN company c ON c.name = v.company;

INSERT INTO vacancy_skill (vacancy_id, skill_id)
SELECT v.id, s.id
FROM (VALUES
('Стажёр Java-разработчик',              ARRAY['Java', 'Spring Boot', 'PostgreSQL', 'SQL', 'Git']),
('Junior Java-разработчик',              ARRAY['Java', 'Spring Boot', 'Hibernate', 'PostgreSQL', 'REST API', 'Docker']),
('Java-разработчик (платежи)',           ARRAY['Java', 'Spring Boot', 'Kafka', 'PostgreSQL', 'Microservices', 'Kubernetes']),
('Senior Java-разработчик',              ARRAY['Java', 'Spring Cloud', 'Kafka', 'Kubernetes', 'gRPC', 'System Design']),
('Руководитель группы Java-разработки',  ARRAY['Java', 'Spring Boot', 'Microservices', 'System Design']),
('Kotlin-разработчик',                   ARRAY['Kotlin', 'Spring Boot', 'PostgreSQL', 'Redis']),
('Go-разработчик',                       ARRAY['Go', 'gRPC', 'PostgreSQL', 'Kubernetes']),
('Fullstack-разработчик (Java + React)', ARRAY['Java', 'Spring Boot', 'React', 'TypeScript']),
('Frontend-разработчик (React)',         ARRAY['React', 'TypeScript', 'Redux', 'CSS']),
('Инженер по тестированию (Java)',       ARRAY['Java', 'Selenide', 'JUnit', 'Postman']),
('DevOps-инженер',                       ARRAY['Kubernetes', 'Terraform', 'Ansible', 'GitLab CI', 'Grafana']),
('Android-разработчик',                  ARRAY['Kotlin', 'Jetpack Compose', 'Android SDK']),
('iOS-разработчик',                      ARRAY['Swift', 'SwiftUI', 'UIKit']),
('Unity-разработчик',                    ARRAY['Unity', 'C#']),
('Data Engineer',                        ARRAY['Python', 'Airflow', 'Spark', 'ClickHouse', 'SQL']),
('Аналитик данных',                      ARRAY['SQL', 'Python', 'Pandas', 'Superset']),
('Системный аналитик',                   ARRAY['SQL', 'REST API', 'Confluence', 'Jira']),
('ML-инженер',                           ARRAY['Python', 'PyTorch', 'LLM', 'MLflow'])
) AS m(title, skills)
JOIN vacancy v ON v.title = m.title
JOIN skill s ON s.name = ANY (m.skills);
