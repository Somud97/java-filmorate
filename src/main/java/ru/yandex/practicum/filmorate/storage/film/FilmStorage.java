package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    void deleteById(int id);

    Film findById(int id);

    List<Film> findAll();

    List<Film> getFilmsByDirector(int directorId, String sortBy);
}

