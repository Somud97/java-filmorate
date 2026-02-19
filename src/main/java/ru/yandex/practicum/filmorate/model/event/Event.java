package ru.yandex.practicum.filmorate.model.event;

import lombok.Data;

@Data
public class Event {
    private int eventId;
    private int userId;
    private int entityId;
    private EventType eventType;
    private Operation operation;
    private long timestamp;

    public Event(int eventId, int userId, int entityId, EventType eventType, Operation operation, long timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
        this.timestamp = timestamp;
    }
}