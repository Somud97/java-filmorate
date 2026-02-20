package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecommendationDbStorage implements RecommendationStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Integer> findMostSimilarUserId(int userId) {
        String sql = """
            SELECT fl2.user_id
            FROM film_likes fl1
            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
            WHERE fl1.user_id = ?
              AND fl2.user_id <> ?
            GROUP BY fl2.user_id
            ORDER BY COUNT(*) DESC, fl2.user_id
            LIMIT 1
        """;

        List<Integer> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("user_id"), userId, userId);
        return ids.stream().findFirst();
    }

    @Override
    public List<Integer> findRecommendedFilmIds(int userId, int similarUserId) {
        String sql = """
            SELECT fl.film_id
            FROM film_likes fl
            WHERE fl.user_id = ?
              AND fl.film_id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY fl.film_id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("film_id"), similarUserId, userId);
    }
}