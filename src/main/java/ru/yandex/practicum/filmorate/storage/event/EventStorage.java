package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.List;

public interface EventStorage {

    List<Event> getUserFeed(int userId, int limit, Long cursor); //ktynf lheptq

    String createLikeEvent(int userId, int entityId, Operation operation);//добавить событие EventType.LIKE

    String  createFriendEvent(int userId, int entityId, Operation operation); //добавить событие EventType.FRIEND

    String  createReviewEvent(int userId, int entityId, Operation operation); //добавить событие EventType.REVIEW

    boolean deleteEventByUserId(int userId); //при удалении пользователя по пути удалить события
}