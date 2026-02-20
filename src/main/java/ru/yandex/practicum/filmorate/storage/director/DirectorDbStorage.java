package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Director> directorRowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    };

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
        if (id == null) {
            throw new RuntimeException("Не удалось получить ID созданного режиссёра");
        }
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE id = ?",
                director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Режиссёр с ID " + id + " не найден");
        }
    }

    @Override
    public Optional<Director> getById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM directors WHERE id = ?", directorRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, directorRowMapper);
    }
}
