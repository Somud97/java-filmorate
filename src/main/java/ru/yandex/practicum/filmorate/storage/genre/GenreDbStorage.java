package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<GenreDto> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new GenreDto(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public GenreDto findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<GenreDto> list = jdbcTemplate.query(sql, (rs, rowNum) ->
            new GenreDto(rs.getInt("id"), rs.getString("name")), id);
        if (list.isEmpty()) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        return list.get(0);
    }
}
