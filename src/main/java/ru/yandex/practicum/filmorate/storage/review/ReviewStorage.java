package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    void delete(int id);

    Review findById(int id);

    List<Review> findByFilmId(Integer filmId, int count);

    List<Review> findAll(int count);
}
