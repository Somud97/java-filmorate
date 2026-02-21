package ru.yandex.practicum.filmorate.storage.recommendation;

import java.util.List;
import java.util.Optional;

public interface RecommendationStorage {

    Optional<Integer> findMostSimilarUserId(int userId);

    List<Integer> findRecommendedFilmIds(int userId, int similarUserId);
}
