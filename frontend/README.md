# Вакант — фронтенд (внутри jobBoard)

React + Vite (обычный JavaScript). Лежит внутри бэкенда: `jobBoard/frontend/`.
Собранный фронт раздаётся самим Spring Boot — **один проект, один JAR, без CORS**.

## Как запускать

### Вариант 1 — всё одним приложением (прод/демо)
Из корня `jobBoard`:
```bash
mvn spring-boot:run
```
Maven сам соберёт фронт (плагин `frontend-maven-plugin`) и положит его в статику.
Открой **http://localhost:8080** — там и сайт, и API.

Собрать один JAR:
```bash
mvn clean package
java -jar target/jobBoard-0.0.1-SNAPSHOT.jar
```

### Вариант 2 — разработка фронта (hot-reload)
Бэкенд запусти как обычно (порт 8080). Отдельно:
```bash
cd frontend
npm install   # один раз
npm run dev
```
Откроется http://localhost:5173. Vite проксирует `/api` на :8080 — CORS не нужен.

## Где что править

Все запросы к бэку — в **`src/api.js`**. Формы вакансии приводятся к единому виду в `mapVacancy`
(если поле в API называется иначе — правится там).

## Что уже работает с реальным API

Регистрация/логин/`me`, список/поиск/страница вакансий, публикация вакансии
(нужен существующий `companyId`), JWT в каждом запросе, роли `CANDIDATE`/`EMPLOYER`/`ADMIN`.

## Доработки бэкенда (чтобы кабинеты стали «живыми»)

Списки в кабинетах пока демо. Для реальных данных нужно: связь `User → Candidate`,
владелец у вакансии (`Vacancy → User`), эндпоинты `GET /api/applications/my`,
`GET /api/vacancies/my`.

## Структура

```
src/
  api.js            — axios + все запросы к бэку (единая точка правок)
  auth.jsx          — контекст авторизации (login/register/me/logout, роли)
  toast.jsx         — уведомления
  format.js         — форматирование зарплаты и типа занятости
  App.jsx           — маршруты и layout
  components/       — Navbar, VacancyCard, AuthModal
  pages/            — Home, VacancyDetail, CandidateDashboard, EmployerDashboard
  index.css         — стили (светлая/тёмная тема)
```
