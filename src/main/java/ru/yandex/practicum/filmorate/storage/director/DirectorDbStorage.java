package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;

@Component
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Director> directorRowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    };

    @Override
    public Director add(Director director) {
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
        return findById(id);
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Режиссёр с ID " + director.getId() + " не найден");
        }
        return findById(director.getId());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Режиссёр с ID " + id + " не найден");
        }
    }

    @Override
    public Director findById(int id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, directorRowMapper, id);
        if (directors.isEmpty()) {
            throw new NotFoundException("Режиссёр с ID " + id + " не найден");
        }
        return directors.get(0);
    }

    @Override
    public List<Director> findAll() {
        String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, directorRowMapper);
    }
}
