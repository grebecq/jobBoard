-- Контакты для связи после отклика: переписка идёт в Telegram или почте, не внутри сервиса
ALTER TABLE company   ADD COLUMN contact_email VARCHAR(255);
ALTER TABLE company   ADD COLUMN telegram      VARCHAR(32);
ALTER TABLE candidate ADD COLUMN telegram      VARCHAR(32);

-- Сообщение работодателя при приглашении/отказе ("напишите мне в tg до пятницы")
ALTER TABLE application ADD COLUMN employer_comment TEXT;
