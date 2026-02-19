package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
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
    private final DirectorStorage directorStorage;
    private final LikeStorage likeStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       DirectorStorage directorStorage,
                       LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
        this.likeStorage = likeStorage;
    }

    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка: фильм {}, пользователь {}", filmId, userId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        likeStorage.addLike(filmId, userId);

        log.info("Лайк успешно добавлен");
    }

    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", filmId, userId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        likeStorage.removeLike(filmId, userId);

        log.info("Лайк успешно удален");
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

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        log.info("Получение фильмов режиссёра {} с сортировкой по {}", directorId, sortBy);

        directorStorage.getById(directorId);

        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }
}

