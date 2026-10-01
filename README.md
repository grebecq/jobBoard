# Вакант (jobBoard)

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
