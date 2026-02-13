package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.util.List;

public interface GenreStorage {
    List<GenreDto> findAll();

    GenreDto findById(int id);
}
