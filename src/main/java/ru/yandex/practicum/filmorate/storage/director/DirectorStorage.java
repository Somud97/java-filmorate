package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    Director add(Director director);

    Director update(Director director);

    void delete(int id);

    Director findById(int id);

    List<Director> findAll();
}
