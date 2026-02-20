package ru.yandex.practicum.filmorate.storage.mpaa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.MpaaDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaaDbStorage implements MpaaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MpaaDto> findAll() {
        String sql = "SELECT id, code AS name FROM mpaa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaaDto(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public MpaaDto findById(int id) {
        String sql = "SELECT id, code AS name FROM mpaa_ratings WHERE id = ?";
        List<MpaaDto> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaaDto(rs.getInt("id"), rs.getString("name")), id);
        if (list.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
        }
        return list.get(0);
    }
}
