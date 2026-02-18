-- Справочные данные: рейтинги MPA и жанры (вставка только при отсутствии)

INSERT INTO mpaa_ratings (code)
SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM mpaa_ratings WHERE code = 'G');
INSERT INTO mpaa_ratings (code)
SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM mpaa_ratings WHERE code = 'PG');
INSERT INTO mpaa_ratings (code)
SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM mpaa_ratings WHERE code = 'PG-13');
INSERT INTO mpaa_ratings (code)
SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM mpaa_ratings WHERE code = 'R');
INSERT INTO mpaa_ratings (code)
SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM mpaa_ratings WHERE code = 'NC-17');

INSERT INTO genres (name)
SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Комедия');
INSERT INTO genres (name)
SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Драма');
INSERT INTO genres (name)
SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Мультфильм');
INSERT INTO genres (name)
SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Триллер');
INSERT INTO genres (name)
SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Документальный');
INSERT INTO genres (name)
SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Боевик');

INSERT INTO events (user_id, entity_id, event_type, operation, timestamp)
VALUES
(1, 100, 'LIKE', 'ADD', 123344556),
(1, 200, 'FRIEND', 'ADD', 123344557),
(1, 300, 'REVIEW', 'UPDATE', 123344558);
