package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventServise;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, EventService eventServise) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.eventServise = eventServise;
    }

    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        film.getLikes().add(userId);
        filmStorage.update(film);

        eventServise.createLikeEvent(userId, filmId, Operation.ADD);
    }

    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        film.getLikes().remove(userId);
        filmStorage.update(film);

        eventServise.createLikeEvent(userId, filmId, Operation.REMOVE);
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        log.info("Получение топ-{} самых популярных фильмов по жанру id={} и году={}",
                count, genreId, year);
        if (count <= 0) {
            return List.of();
        }

        Stream<Film> filmStream = filmStorage.findAll().stream();

        if (genreId != null) {
            filmStream = filmStream.filter(film ->
                    film.getGenreIds().contains(genreId) ||
                            film.getGenres().stream()
                                    .anyMatch(genre -> genre.ordinal() + 1 == genreId)
            );
        }
        if (year != null) {
            filmStream = filmStream.filter(film ->
                    film.getReleaseDate() != null &&
                            film.getReleaseDate().getYear() == year
            );
        }
        return filmStream
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public String deleteById(Integer id) {
        return filmStorage.deleteById(id);
    }
}

