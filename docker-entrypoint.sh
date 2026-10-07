#!/bin/sh
# Render отдаёт адрес базы как postgresql://user:pass@host:port/db, а Spring нужен jdbc-формат
case "$DATABASE_URL" in
  postgres://*|postgresql://*) export DATABASE_URL="jdbc:postgresql://${DATABASE_URL#*@}" ;;
esac
exec java $JAVA_OPTS -jar app.jar
