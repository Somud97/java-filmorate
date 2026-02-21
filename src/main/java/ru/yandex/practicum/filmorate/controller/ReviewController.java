package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        log.info("Получен запрос на создание отзыва: фильм {}, пользователь {}",
            review.getFilmId(), review.getUserId());
        Review createdReview = reviewService.addReview(review);
        log.info("Отзыв успешно создан с ID: {}", createdReview.getReviewId());
        return createdReview;
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Получен запрос на обновление отзыва с ID: {}", review.getReviewId());
        Review updatedReview = reviewService.updateReview(review);
        log.info("Отзыв с ID {} успешно обновлен", updatedReview.getReviewId());
        return updatedReview;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @Positive int id) {
        log.info("Получен запрос на удаление отзыва с ID: {}", id);
        reviewService.deleteReview(id);
        log.info("Отзыв с ID {} успешно удален", id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable @Positive int id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
        @RequestParam(required = false) Integer filmId,
        @RequestParam(required = false) Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id,
                        @PathVariable @Positive int userId) {
        log.info("Получен запрос на добавление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable @Positive int id,
                           @PathVariable @Positive int userId) {
        log.info("Получен запрос на добавление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive int id,
                           @PathVariable @Positive int userId) {
        log.info("Получен запрос на удаление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable @Positive int id,
                              @PathVariable @Positive int userId) {
        log.info("Получен запрос на удаление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.removeDislike(id, userId);
    }
}
