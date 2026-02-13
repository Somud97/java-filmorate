package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendLink;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }
        return user;
    };

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
        if (id == null) {
            throw new RuntimeException("Не удалось получить ID созданного пользователя");
        }
        user.setId(id);

        saveFriendLinks(user.getId(), user.getFriendLinks());

        return findById(id);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
            user.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        deleteFriendLinks(user.getId());
        saveFriendLinks(user.getId(), user.getFriendLinks());

        return findById(user.getId());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        User user = users.get(0);
        user.setFriendLinks(loadFriendLinks(id));
        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        for (User user : users) {
            user.setFriendLinks(loadFriendLinks(user.getId()));
        }
        return users;
    }

    private Set<FriendLink> loadFriendLinks(int userId) {
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
        if (friendLinks == null || friendLinks.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        for (FriendLink link : friendLinks) {
            jdbcTemplate.update(sql,
                userId,
                link.getFriendId(),
                link.getStatus().name());
        }
    }

    private void deleteFriendLinks(int userId) {
        String sql = "DELETE FROM user_friends WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
