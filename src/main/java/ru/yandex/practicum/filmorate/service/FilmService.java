package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getMostPopularFilms(int count) {
        log.info("Получение топ-{} самых популярных фильмов", count);

        if (count <= 0) {
            return List.of();
        }

        return filmStorage.findAll().stream()
            .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
            .limit(count)
            .collect(Collectors.toList());
    }

    public String deleteById(Integer id) {
        return filmStorage.deleteById(id);
    }
}

