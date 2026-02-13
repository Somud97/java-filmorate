package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserStorage userStorage;

    private final UserService userService;
    private final ValidationService validationService;

    @Autowired
    public UserController(ValidationService validationService,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         UserService userService) {
        this.validationService = validationService;
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user.getLogin());
        try {
            validationService.validateUser(user);
            User createdUser = userStorage.add(user);
            log.info("Пользователь успешно создан с ID: {}", createdUser.getId());
            return createdUser;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с ID: {}", user.getId());
        try {
            validationService.validateUser(user);
            User updatedUser = userStorage.update(user);
            log.info("Пользователь с ID {} успешно обновлен", updatedUser.getId());
            return updatedUser;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении пользователя с ID {}: {}", user.getId(), e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            log.error("Пользователь с ID {} не найден: {}", user.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Positive int id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        return userStorage.findById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        List<User> users = userStorage.findAll();
        log.info("Получен запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return users;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive int id, @PathVariable @Positive int friendId) {
        log.info("Получен запрос на добавление в друзья: пользователь {} -> {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable @Positive int id, @PathVariable @Positive int friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь {} -> {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable @Positive int id) {
        log.info("Получен запрос на список друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive int id, @PathVariable @Positive int otherId) {
        log.info("Получен запрос на список общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
