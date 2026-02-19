package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final int DEFAULT_COUNT = 10;

    private final ReviewStorage reviewStorage;
    private final ReviewDbStorage reviewDbStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    public Review addReview(Review review) {
        log.info("Добавление отзыва: пользователь {}, фильм {}", review.getUserId(), review.getFilmId());

        // Проверка существования пользователя и фильма
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());

        Review addReview = reviewStorage.add(review);
        //пришлось создать переменную, потому что id отзыва создается после метода add
        eventService.createReviewEvent(addReview.getUserId(), addReview.getReviewId(), Operation.ADD);
        return addReview;
    }

    public Review updateReview(Review review) {
        log.info("Обновление отзыва с ID: {}", review.getReviewId());

        Review existingReview = reviewStorage.findById(review.getReviewId());

        // Проверка, что пользователь может редактировать только свой отзыв
        if (!existingReview.getUserId().equals(review.getUserId())) {
            throw new IllegalArgumentException("Пользователь может редактировать только свои отзывы");
        }

        eventService.createReviewEvent(review.getUserId(), review.getReviewId(), Operation.UPDATE);

        return reviewStorage.update(review);
    }

    public void deleteReview(int reviewId) {
        log.info("Удаление отзыва с ID: {}", reviewId);

        eventService.createReviewEvent(getReviewById(reviewId).getUserId(), reviewId, Operation.REMOVE);

        reviewStorage.delete(reviewId);
    }

    public Review getReviewById(int reviewId) {
        return reviewStorage.findById(reviewId);
    }

    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count != null && count > 0) ? count : DEFAULT_COUNT;

        if (filmId != null) {
            log.info("Получение отзывов для фильма {} (лимит: {})", filmId, limit);
            return reviewStorage.findByFilmId(filmId, limit);
        } else {
            log.info("Получение всех отзывов (лимит: {})", limit);
            return reviewStorage.findAll(limit);
        }
    }

    public void addLike(int reviewId, int userId) {
        log.info("Добавление лайка отзыву {} от пользователя {}", reviewId, userId);

        reviewStorage.findById(reviewId);
        userStorage.findById(userId);

        reviewDbStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        log.info("Добавление дизлайка отзыву {} от пользователя {}", reviewId, userId);

        reviewStorage.findById(reviewId);
        userStorage.findById(userId);

        reviewDbStorage.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        log.info("Удаление лайка отзыву {} от пользователя {}", reviewId, userId);

        reviewStorage.findById(reviewId);
        userStorage.findById(userId);

        reviewDbStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        log.info("Удаление дизлайка отзыву {} от пользователя {}", reviewId, userId);

        reviewStorage.findById(reviewId);
        userStorage.findById(userId);

        reviewDbStorage.removeDislike(reviewId, userId);
    }
}
