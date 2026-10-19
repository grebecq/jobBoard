# Вакант (jobBoard)

![CI](https://github.com/grebecq/jobBoard/actions/workflows/ci.yml/badge.svg)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?logo=react&logoColor=black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

Сервис вакансий в духе hh.ru: работодатели публикуют вакансии, кандидаты ищут их по фильтрам и откликаются.

**Как устроено:** REST API на Spring Boot (Java 21) с PostgreSQL и Flyway-миграциями, вход по JWT с ролями кандидата, работодателя и администратора. Фронтенд на React собирается вместе с бэкендом в один JAR.

## Запуск

```bash
docker-compose up -d       # PostgreSQL на порту 5435
./mvnw spring-boot:run     # или конфигурация App из IntelliJ
```

Сайт: http://localhost:8080, документация API: http://localhost:8080/swagger-ui/index.html

## Авторство

Бэкенд сделан мной, фронтенд — с помощью Claude Code.

## Лицензия

[MIT](LICENSE)
