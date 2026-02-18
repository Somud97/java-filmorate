package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@Qualifier("eventDbStorage")
public class EventDbStorage implements EventStorage {

    @Override
    public List<Event> getUserFeed(int userId, int limit, Long cursor) {
        return List.of();
    }

    @Override
    public String createLikeEvent(int userId, int entityId, Operation operation) {
        return "";
    }

    @Override
    public String createFriendEvent(int userId, int entityId, Operation operation) {
        return "";
    }

    @Override
    public String createReviewEvent(int userId, int entityId, Operation operation) {
        return "";
    }

    @Override
    public boolean deleteEventByUserId(int userId) {
        return false;
    }
}