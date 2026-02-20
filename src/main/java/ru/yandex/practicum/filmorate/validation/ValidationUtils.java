package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ForbiddenException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ValidationUtils {

    private final JdbcTemplate jdbcTemplate;

    public void validateUser(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    public void validateFilm(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }

    public void validateFilmInTheFuture(Film film) {
        final LocalDate MIN_RELEASE_DATE =
                LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    //проверка существования пользователя
    public Review validateReview(Integer id) {
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Review review = new Review();
                review.setReviewId(rs.getInt("id"));
                review.setContent(rs.getString("content"));
                review.setIsPositive(rs.getBoolean("is_positive"));
                review.setUserId(rs.getInt("user_id"));
                review.setFilmId(rs.getInt("film_id"));
                review.setUseful(rs.getInt("useful"));
                return review;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с ID " + id + " не найден");
        }
    }

    //проверка прав пользователя на редактирование/удаление отзыва
    public void validateReviewOwnership(Integer reviewId, Integer userId) {
        String sql = "SELECT user_id FROM reviews WHERE id = ?";
        Integer ownerId = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        if (!ownerId.equals(userId)) {
            throw new ForbiddenException("Пользователь может редактировать только свои отзывы");
        }
    }
}
