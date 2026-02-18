package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.List;

public interface EventStorage {

    Event add(Event event); //добавить

    Event findById(int eventId); //найти по id

    List<Event> getUserFeed(int userId, int limit, Long cursor); //ktynf lheptq

    default Event createLikeEvent(int userId, int entityId, Operation operation) {
        return new Event(0, userId, entityId, EventType.LIKE, operation, System.currentTimeMillis());
    } //добавить событие EventType.LIKE

    default Event createFriendEvent(int userId, int entityId, Operation operation) {
        return new Event(0, userId, entityId, EventType.FRIEND, operation, System.currentTimeMillis());
    } //добавить событие EventType.FRIEND

    default Event createReviewEvent(int userId, int entityId, Operation operation) {
        return new Event(0, userId, entityId, EventType.REVIEW, operation, System.currentTimeMillis());
    } //добавить событие EventType.REVIEW

    boolean deleteEventByUserId(int userId); //при удалении пользователя по пути удалить события
}