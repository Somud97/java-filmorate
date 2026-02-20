package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.validation.ValidationUtils;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ValidationUtils validationUtils;

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getInt("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    };

    @Override
    public Review add(Review review) {
        //проверок мало не бывает
        validationUtils.validateUser(review.getUserId());
        validationUtils.validateFilm(review.getFilmId());
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
        if (id == null) {
            throw new RuntimeException("Не удалось получить ID созданного отзыва");
        }
        review.setReviewId(id);
        review.setUseful(0);

        return findById(id);
    }

    @Override
    public Review update(Review review) {
        validationUtils.validateUser(review.getUserId());
        validationUtils.validateFilm(review.getFilmId());
        validationUtils.validateReview(review.getReviewId());
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql,
            review.getContent(),
            review.getIsPositive(),
            review.getReviewId());

        return findById(review.getReviewId());
    }

    @Override
    public void delete(int id) {
        validationUtils.validateReview(id);
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Review findById(int id) {
        validationUtils.validateReview(id);
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, reviewRowMapper, id);
        return reviews.get(0);
    }

    @Override
    public List<Review> findByFilmId(Integer filmId, int count) {
        validationUtils.validateFilm(filmId);
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful " +
            "FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful " +
            "FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    public void addLike(int reviewId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE review_likes SET is_positive = true WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, reviewId, userId);
        } else {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, true)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }
        updateUseful(reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE review_likes SET is_positive = false WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, reviewId, userId);
        } else {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, false)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }
        updateUseful(reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, reviewId, userId);
        if (rowsAffected > 0) {
            updateUseful(reviewId);
        }
    }

    public void removeDislike(int reviewId, int userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = false";
        int rowsAffected = jdbcTemplate.update(sql, reviewId, userId);
        if (rowsAffected > 0) {
            updateUseful(reviewId);
        }
    }

    private void updateUseful(int reviewId) {
        String sql = "UPDATE reviews SET useful = " +
            "(SELECT COALESCE(SUM(CASE WHEN is_positive THEN 1 ELSE -1 END), 0) FROM review_likes WHERE review_id = ?) " +
            "WHERE id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }
}
