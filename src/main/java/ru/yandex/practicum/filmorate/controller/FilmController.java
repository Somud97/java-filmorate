package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    @Autowired
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
        try {
            validationService.validateFilm(film);
            validateMpaAndGenres(film);
            Film createdFilm = filmStorage.add(film);
            log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
            return createdFilm;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при создании фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());
        try {
            validationService.validateFilm(film);
            validateMpaAndGenres(film);
            Film updatedFilm = filmStorage.update(film);
            log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
            return updatedFilm;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении фильма с ID {}: {}", film.getId(), e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            log.error("Фильм с ID {} не найден: {}", film.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive int id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        return filmStorage.findById(id);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.findAll();
        log.info("Получен запрос на получение всех фильмов. Количество фильмов: {}", films.size());
        return films;
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
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") @Positive int count) {
        log.info("Получен запрос на получение топ-{} популярных фильмов", count);
        return filmService.getMostPopularFilms(count);
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
}
