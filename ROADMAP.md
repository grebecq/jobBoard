# jobBoard — Roadmap

Учебный pet-проект: поиск работы для разработчиков («типо hh»).
Стек: Spring Boot 4.1.1, Java 21, PostgreSQL, Flyway, JPA/Hibernate, Spring Security, springdoc-openapi (Swagger), Lombok.

Принцип: маленькие сфокусированные шаги. Каждая фаза = запустил, проверил, понял — потом дальше. Так и учится лучше, и токенов уходит меньше.

---

## Прогресс (обновлено)

- ✅ Фаза 0 — окружение и запуск
- ✅ Фаза 1 — БД + первые связи
- ✅ Фаза 2 — REST-слой (пагинацию в списке вакансий перепроверить при запуске)
- ✅ Фаза 3 — домен и все связи (по коду; + бонусом REST для Candidate/Application/Resume)
- 🔄 Фаза 4 — поиск и фильтрация (в работе: hasCity + salaryFromAtLeast готовы; осталось employmentType, навыки, объединение спецификаций, пагинация)
- ⬜ Фаза 5 — Swagger
- ⬜ Фаза 6 — Security
- ⬜ Фаза 7 — тесты (Testcontainers)
- ⬜ Фаза 8 — полировка

Примерно **3.5 из 8**.

---

## Фаза 0. Окружение и запуск
Цель: приложение стартует и подключается к Postgres.

- [x] `docker-compose.yml` с Postgres (порт, база `jobboard`, user/password)
- [x] Настроить `application.properties`: `spring.datasource.*`, `spring.jpa.hibernate.ddl-auto=validate` (схемой рулит Flyway, не Hibernate)
- [x] Профили `dev` / `prod` (`application-dev.properties`)
- [x] Пустая первая миграция или проверка, что Flyway видит `db/migration`
- [x] Запустить приложение, убедиться что коннект к БД есть

**Учим:** конфигурация Spring Boot, профили, разделение ролей Flyway vs Hibernate.

---

## Фаза 1. Первая миграция + первые сущности (ядро связей)
Цель: две связанные таблицы и работающий JPA-слой.

- [x] `V1__init.sql`: таблицы `company` и `vacancy` (FK `vacancy.company_id`)
- [x] Entity `Company` — `@OneToMany` к вакансиям
- [x] Entity `Vacancy` — `@ManyToOne` к компании
- [x] `CompanyRepository`, `VacancyRepository` (`JpaRepository`)
- [x] Проверить сохранение/чтение (в тесте или через `CommandLineRunner`)

**Учим:** Flyway-миграции, `@OneToMany`/`@ManyToOne`, `FetchType.LAZY`, **проблема N+1** (сразу увидеть и починить через `JOIN FETCH` / `@EntityGraph`).

---

## Фаза 2. REST-слой: DTO, валидация, обработка ошибок
Цель: аккуратный API для вакансий и компаний.

- [x] DTO (request/response) — не отдавать entity наружу
- [x] Маппинг entity <-> DTO (руками или MapStruct)
- [x] `VacancyController` + `VacancyService` (CRUD)
- [x] Валидация входа: `@Valid`, аннотации на DTO
- [x] Глобальный `@RestControllerAdvice` — единый формат ошибки
- [ ] Пагинация и сортировка (`Pageable`) в списке вакансий — перепроверить при запуске

**Учим:** слоистая архитектура, DTO-паттерн, Bean Validation, единая обработка ошибок, `Pageable`.

---

## Фаза 3. Расширение домена и все виды связей
Цель: полноценная модель job board.

- [x] `Skill` + `@ManyToMany` с `Vacancy` (навыки/теги)
- [x] `Candidate` + `Resume` (`@OneToOne` или `@OneToMany`)
- [x] `Experience`, `Education` — `@OneToMany` внутри резюме
- [x] `Application` (отклик) — отдельная entity: `Candidate` + `Vacancy` + `status` + сопроводительное + дата (ManyToMany с полями!)
- [x] Enum-статусы (`VacancyStatus`, `ApplicationStatus`)
- [x] Аудит: `@CreatedDate` / `@LastModifiedDate`

**Учим:** `@ManyToMany`, `@OneToOne`, join-entity с payload (почему нельзя обычным ManyToMany), enum-маппинг, JPA Auditing.

> Осталось: фактическая проверка запуском (Flyway накатывает V1–V6, Hibernate `validate` проходит по всем 8 сущностям).

---

## Фаза 4. Поиск и фильтрация
Цель: то, ради чего вообще нужен job board.

- [ ] Динамический поиск через JPA `Specification` / Criteria API — в работе
- [ ] Фильтры: город, зарплата (от/до), навыки, тип занятости — город и зарплата-от готовы
- [ ] Пагинация результатов поиска

**Учим:** Criteria API / Specifications, построение динамических запросов.

---

## Фаза 5. Документация API (Swagger)
Цель: живая документация и «пощупать» эндпоинты из браузера.

- [ ] Подобрать версию `springdoc-openapi-starter-webmvc-ui`, совместимую с Boot 4.1
- [ ] Проверить `/swagger-ui.html`
- [ ] Аннотации `@Operation`, `@Schema` на ключевых эндпоинтах

**Учим:** OpenAPI, springdoc. ⚠️ Boot 4 свежий — версия springdoc критична, подберём аккуратно.

---

## Фаза 6. Безопасность (Spring Security)
Цель: роли и защита эндпоинтов.

- [ ] Добавить `spring-boot-starter-security`
- [ ] `User` + роли: `CANDIDATE`, `EMPLOYER`, `ADMIN`
- [ ] Регистрация / логин, хеширование паролей (BCrypt)
- [ ] Ограничения: работодатель постит вакансии, кандидат откликается
- [ ] Метод-секьюрити (`@PreAuthorize`)
- [ ] (Опционально) JWT для stateless API

**Учим:** Spring Security 7 (в Boot 4), фильтры/цепочки, роли, JWT.

---

## Фаза 7. Тестирование
Цель: проверяемость на реальной БД.

- [ ] `@DataJpaTest` + **Testcontainers** (реальный Postgres в докере на время теста)
- [ ] Тесты репозиториев и связей
- [ ] Тесты сервисов и контроллеров (слайсы)

**Учим:** тестовые слайсы, Testcontainers — частый вопрос на собесах.

---

## Фаза 8. Полировка (опционально)
- [ ] Soft-delete
- [ ] Кэширование
- [ ] Полнотекстовый поиск средствами Postgres
- [ ] Докеризация самого приложения (Dockerfile)

---

## Порядок коротко
0 → окружение · 1 → БД+связи · 2 → REST · 3 → домен · 4 → поиск · 5 → Swagger · 6 → Security · 7 → тесты · 8 → полировка

> Совет по токенам: под каждую фазу (а лучше под каждый шаг) — новый чат. Называй файлы/классы вместо вставки кода, из ошибок кидай верх стектрейпа.
