package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final EventService eventService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user.getLogin());
        User createdUser = userService.add(user);
        log.info("Пользователь успешно создан с ID: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с ID: {}", user.getId());
        User updatedUser = userService.update(user);
        log.info("Пользователь с ID {} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Positive int id) {
        return userService.findById(id);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.findAll();
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
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive int id, @PathVariable @Positive int otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive Integer id) {
        log.info("Удаление пользователя с ID: {}", id);
        userService.deleteById(id);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(@PathVariable @Positive int id) {
        log.info("Список новостей о пользователе с ID: {}", id);
        List<Event> events = eventService.getUserFeed(id);
        log.info("Возвращаем {} событий для пользователя {}", events.size(), id);
        return events;
    }
}
