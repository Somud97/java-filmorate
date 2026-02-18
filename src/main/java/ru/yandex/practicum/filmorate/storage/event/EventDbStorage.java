package ru.yandex.practicum.filmorate.storage.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.service.EventServise;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
@Qualifier("eventDbStorage")
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(EventDbStorage.class);

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Event> eventRowMapper = (ResultSet rs, int rowNum) -> {
        return new Event(
                rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getInt("entity_id"),
                EventType.valueOf(rs.getString("event_type")),
                Operation.valueOf(rs.getString("operation")),
                rs.getLong("timestamp")
        );
    };

    @Override
    public List<Event> getUserFeed(int userId) {
        log.info("Получение всех новостей пользователя с ID: {}", userId);

        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";
        List<Event> events = jdbcTemplate.query(sql, eventRowMapper, userId);

        return events;
    }

    @Override
    public String createLikeEvent(int userId, int entityId, Operation operation) {
        createEvent(userId, entityId, EventType.LIKE, operation);
        return "Событие LIKE успешно создано";
    }

    @Override
    public String createFriendEvent(int userId, int entityId, Operation operation) {
        createEvent(userId, entityId, EventType.FRIEND, operation);
        return "Событие FRIEND успешно создано";
    }

    @Override
    public String createReviewEvent(int userId, int entityId, Operation operation) {
        createEvent(userId, entityId, EventType.REVIEW, operation);
        return "Событие REVIEW успешно создано";
    }

    @Override
    public boolean deleteEventByUserId(int userId) {
        int rowsAffected = jdbcTemplate.update("DELETE FROM events WHERE user_id = ?", userId);
        return rowsAffected > 0;
    }

    @Override
    public Event createEvent(int userId, int entityId, EventType eventType, Operation operation) {
        String sql = "INSERT INTO events (user_id, entity_id, event_type, operation, timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long timestamp = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setInt(2, entityId);
            ps.setString(3, eventType.name());
            ps.setString(4, operation.name());
            ps.setLong(5, timestamp);
            return ps;
        }, keyHolder);
        int eventId = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
        return new Event(eventId, userId, entityId, eventType, operation, timestamp);
    }

}