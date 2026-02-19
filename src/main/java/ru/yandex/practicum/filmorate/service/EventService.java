package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventStorage eventStorage;

    public List<Event> getUserFeed(int userId) {
        log.info("Получение всех новостей пользователя с ID: {}", userId);
        List<Event> events = eventStorage.getUserFeed(userId);
        return events;
    }

    public void createLikeEvent(int userId, int entityId, Operation operation) {
        log.info("Создание события LIKE");
        eventStorage.createLikeEvent(userId, entityId, operation);
    }

    public void createFriendEvent(int userId, int entityId, Operation operation) {
        log.info("Создание события FRIEND");
        eventStorage.createFriendEvent(userId, entityId, operation);
    }

    public void createReviewEvent(int userId, int entityId, Operation operation) {
        log.info("Создание события REVIEW");
        eventStorage.createReviewEvent(userId, entityId, operation);
    }

    public boolean deleteEventByUserId(int userId) {
        log.info("Удаление всех новостей пользователя с ID: {}", userId);
        return eventStorage.deleteEventByUserId(userId);
    }
}
