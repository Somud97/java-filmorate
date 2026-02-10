package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Autowired
    private ValidationService validationService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());
        try {
            validationService.validateFilm(film);
            film.setId(nextId++);
            films.put(film.getId(), film);
            log.info("Фильм успешно создан с ID: {}", film.getId());
            return film;
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
            if (film.getId() <= 0 || !films.containsKey(film.getId())) {
                throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
            }
            films.put(film.getId(), film);
            log.info("Фильм с ID {} успешно обновлен", film.getId());
            return film;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении фильма с ID {}: {}", film.getId(), e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            log.error("Фильм с ID {} не найден: {}", film.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов. Количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }
}
