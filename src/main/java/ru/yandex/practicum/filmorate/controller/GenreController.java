package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Validated
public class GenreController {

    private final GenreStorage genreStorage;

    public GenreController(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public List<GenreDto> getAllGenres() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable @Positive int id) {
        return genreStorage.findById(id);
    }
}
