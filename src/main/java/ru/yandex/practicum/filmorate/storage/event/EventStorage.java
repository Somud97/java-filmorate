package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.List;

public interface EventStorage {

    List<Event> getUserFeed(int userId);

    void createLikeEvent(int userId, int entityId, Operation operation);//добавить событие EventType.LIKE

    void createFriendEvent(int userId, int entityId, Operation operation); //добавить событие EventType.FRIEND

    void createReviewEvent(int userId, int entityId, Operation operation); //добавить событие EventType.REVIEW

    boolean deleteEventByUserId(int userId); //при удалении пользователя по пути удалить события

    Event createEvent(int userId, int entityId, EventType eventType, Operation operation);
}