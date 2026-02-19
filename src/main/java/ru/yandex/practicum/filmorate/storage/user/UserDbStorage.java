package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendLink;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dal.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.intValue());
        }
        return user;
    }

    @Override
    public User update(User user) {
        validateUserExists(user.getId());
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
            user.getId());

        deleteFriendLinks(user.getId());
        saveFriendLinks(user.getId(), user.getFriendLinks());

        return findById(user.getId());
    }

    @Override
    public void deleteById(int id) {
        validateUserExists(id);
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public User findById(int id) {
        validateUserExists(id);
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    private Map<Integer, Set<FriendLink>> loadAllFriendLinks() {
        String sql = "SELECT user_id, friend_id, status FROM user_friends";
        Map<Integer, Set<FriendLink>> result = new HashMap<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int userId = rs.getInt("user_id");
            int friendId = rs.getInt("friend_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(new FriendLink(friendId, status));
            return null;
        });
        return result;
    }

    private Set<FriendLink> loadFriendLinks(int userId) {
        validateUserExists(userId);
        String sql = "SELECT friend_id, status FROM user_friends WHERE user_id = ?";
        List<FriendLink> links = jdbcTemplate.query(sql,
            (rs, rowNum) -> {
                int friendId = rs.getInt("friend_id");
                String statusStr = rs.getString("status");
                FriendshipStatus status = FriendshipStatus.valueOf(statusStr);
                return new FriendLink(friendId, status);
            },
            userId);
        return new HashSet<>(links);
    }

    private void saveFriendLinks(int userId, Set<FriendLink> friendLinks) {
        validateUserExists(userId);
        if (friendLinks == null || friendLinks.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(friendLinks.size());
        for (FriendLink link : friendLinks) {
            batchArgs.add(new Object[]{userId, link.getFriendId(), link.getStatus().name()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void deleteFriendLinks(int userId) {
        validateUserExists(userId);
        String sql = "DELETE FROM user_friends WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    private void validateUserExists(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}
