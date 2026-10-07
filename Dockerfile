FROM node:20-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --replace-registry-host=registry.npmmirror.com
COPY frontend .
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline -Dskip.npm -Dskip.installnodenpm
COPY src src
COPY --from=frontend /app/dist frontend/dist
RUN mvn -q -B package -DskipTests -Dskip.npm -Dskip.installnodenpm

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh .
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]
