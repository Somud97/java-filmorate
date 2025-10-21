package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user.getLogin());
        try {
            user.setId(nextId++);
            users.put(user.getId(), user);
            log.info("Пользователь успешно создан с ID: {}", user.getId());
            return user;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с ID: {}", user.getId());
        try {
            if (user.getId() <= 0 || !users.containsKey(user.getId())) {
                throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
            }
            users.put(user.getId(), user);
            log.info("Пользователь с ID {} успешно обновлен", user.getId());
            return user;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении пользователя с ID {}: {}", user.getId(), e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            log.error("Пользователь с ID {} не найден: {}", user.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }
}
