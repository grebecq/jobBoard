# «Вакант» — сервис вакансий в духе hh.ru

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

## Что это

Сервис вакансий: работодатели публикуют вакансии, кандидаты ищут их по фильтрам и откликаются.
Работодатель ведёт отклики по воронке (новый, просмотрен, приглашён, отказ), кандидат видит статус каждого своего отклика.
Бэкенд, API и сайт живут в одном приложении и собираются в один JAR.

## Зачем

Учебный проект для портфолио. Показывает, как устроен backend «как в жизни»: роли и права доступа,
сложный поиск с фильтрами, миграции БД, тесты на настоящем PostgreSQL, CI и деплой.

## Что умеет

- регистрация и вход по JWT, три роли: кандидат, работодатель, администратор;
- вакансии: создание, закрытие, список «мои вакансии» для работодателя;
- поиск и фильтры: текст, город, минимальная зарплата, специализация, грейд, опыт, формат работы,
  тип занятости, навыки, компания, сортировка;
- компании: карточка компании и её вакансии;
- отклики с воронкой статусов `PENDING → VIEWED → INVITED / REJECTED`, список откликов по вакансии для работодателя;
- справочники и навыки отдаются через API, на старте база заполняется демо-данными;
- интерактивная документация API (Swagger UI);
- React-интерфейс для кандидата и работодателя.

## Права доступа

| Действие | Кандидат | Работодатель | Админ |
|---|:-:|:-:|:-:|
| Смотреть вакансии, компании, справочники | да | да | да |
| Откликнуться на вакансию | да | нет | нет |
| Создавать и менять компании и вакансии | нет | да | да |
| Смотреть отклики на вакансию и менять их статус | нет | да | да |
| Свои отклики | да | нет | нет |
| Управление навыками и кандидатами | нет | нет | да |

## Стек

Java 21, Spring Boot 4, Spring Security + JWT (jjwt), Spring Data JPA / Hibernate, PostgreSQL, Flyway,
MapStruct, Lombok, springdoc-openapi (Swagger), React 18 + Vite, Docker, GitHub Actions.

## Как запустить

Нужны JDK 21 и Docker.

```bash
docker-compose up -d       # PostgreSQL на порту 5435
./mvnw spring-boot:run     # Maven сам соберёт фронтенд и положит его в приложение
```

- Сайт: http://localhost:8080
- Документация API: http://localhost:8080/swagger-ui/index.html

Разработка фронтенда с hot-reload: `cd frontend && npm install && npm run dev`, сайт на http://localhost:5173
(запросы `/api` проксируются на бэкенд).

Всё одним контейнером: `docker build -t jobboard .`. Образ собирает фронт и бэкенд, работает с профилем `prod`
и берёт настройки из переменных `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET`.

## Тесты

```bash
mvn verify
```

Тесты ходят в настоящий PostgreSQL (нужна запущенная база, как выше) и проверяют вход и права доступа, поиск
вакансий с фильтрами, воронку откликов. GitHub Actions запускает их на каждый push и pull request.

## Деплой

**Render, бесплатно:** Dashboard → New → Blueprint → этот репозиторий. Сервис и база описаны в `render.yaml`,
`JWT_SECRET` создаётся автоматически.

## Структура

```
src/main/java/fedoseev/jobboard/
  controller/     REST-контроллеры
  service/        бизнес-логика
  repository/     Spring Data JPA
  entity/ dto/    модели и объекты запросов/ответов
  specifications/ динамические фильтры поиска
  config/         безопасность, JWT, OpenAPI
src/main/resources/db/migration/   Flyway-миграции V1–V12
frontend/                          React-приложение
```

## Авторство

Бэкенд написан мной, веб-интерфейс сделан с помощью ИИ-ассистента.

## Лицензия

[MIT](LICENSE)
