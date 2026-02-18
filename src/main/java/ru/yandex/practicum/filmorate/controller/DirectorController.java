package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Validated
@RequiredArgsConstructor
public class DirectorController {
    private static final Logger log = LoggerFactory.getLogger(DirectorController.class);

    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAllDirectors() {
        log.info("Получен запрос на получение всех режиссёров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable @Positive int id) {
        log.info("Получен запрос на получение режиссёра с ID: {}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на создание режиссёра: {}", director.getName());

        Director createdDirector = directorService.addDirector(director);
        log.info("Режиссёр успешно создан с ID: {}", createdDirector.getId());
        return createdDirector;
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на обновление режиссёра с ID: {}", director.getId());
        Director updatedDirector = directorService.updateDirector(director);
        log.info("Режиссёр с ID {} успешно обновлен", updatedDirector.getId());
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable @Positive int id) {
        log.info("Получен запрос на удаление режиссёра с ID: {}", id);
        directorService.deleteDirector(id);
        log.info("Режиссёр с ID {} успешно удален", id);
    }
}
