package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpaa.MpaaStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaaStorage mpaaStorage;
    private final FilmService filmService;
    private final DirectorService directorService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());
        validateMpaAndGenres(film);
        Film createdFilm = filmStorage.add(film);
        log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());
        validateMpaAndGenres(film);
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmStorage.findById(id);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.info("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(defaultValue = "year") String sortBy) {
        try {
            directorService.getById(directorId);
        } catch (NotFoundException e) {
            throw e;
        }
        log.info("Получен запрос на получение фильмов режиссёра {} с сортировкой по {}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer friendId) {
        log.info("Запрос общих с другом фильмов с сортировкой по их популярности: userId={}, friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpaaRatingId() != null) {
            mpaaStorage.findById(film.getMpaaRatingId());
        }
        if (film.getGenreIds() != null) {
            for (Integer genreId : film.getGenreIds()) {
                genreStorage.findById(genreId);
            }
        }
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        log.info("Удаление фильма с ID: {}", id);
        filmService.deleteById(id);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by) {
        log.info("Поиск: query='{}', by='{}'", query, by);
        return filmService.searchFilms(query, by);
    }
}