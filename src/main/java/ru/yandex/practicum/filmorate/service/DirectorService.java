package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getById(int id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        getById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        getById(id);
        directorStorage.deleteDirector(id);
    }
}
