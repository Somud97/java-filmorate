package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpaa.MpaaStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaaStorage mpaaStorage;
    private final FilmService filmService;
    private final ValidationService validationService;

    public FilmController(ValidationService validationService,
                          @Qualifier("filmDbStorage") FilmStorage filmStorage,
                          GenreStorage genreStorage,
                          MpaaStorage mpaaStorage,
                          FilmService filmService) {
        this.validationService = validationService;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.mpaaStorage = mpaaStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());
        validationService.validateFilm(film);
        validateMpaAndGenres(film);
        Film createdFilm = filmStorage.add(film);
        log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());
        validationService.validateFilm(film);
        validateMpaAndGenres(film);
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive int id) {
        return filmStorage.findById(id);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") @Positive int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.info("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getMostPopularFilms(count, genreId, year);
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
}
