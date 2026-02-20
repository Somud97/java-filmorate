package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendLink;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDbStorage userStorage;
    private final EventService eventService;
    private final ValidationUtils validationUtils;

    private final FilmStorage filmStorage;
    private final RecommendationStorage recommendationStorage;

    public void addFriend(int userId, int friendId) {
        log.info("Добавление в друзья: пользователь {} -> {}", userId, friendId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean friendAlreadyRequested = friend.getFriendLinks().stream()
                .anyMatch(fl -> fl.getFriendId() == userId && fl.getStatus() == FriendshipStatus.UNCONFIRMED);

        if (friendAlreadyRequested) {
            friend.getFriendLinks().removeIf(fl -> fl.getFriendId() == userId);
            friend.getFriendLinks().add(new FriendLink(userId, FriendshipStatus.CONFIRMED));
            user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
            user.getFriendLinks().add(new FriendLink(friendId, FriendshipStatus.CONFIRMED));
            userStorage.update(user);
            userStorage.update(friend);
        } else {
            user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
            user.getFriendLinks().add(new FriendLink(friendId, FriendshipStatus.UNCONFIRMED));
            userStorage.update(user);
        }

        eventService.createFriendEvent(userId, friendId, Operation.ADD);
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Удаление из друзей: пользователь {} -> {}", userId, friendId);

        User user = userStorage.findById(userId);
        userStorage.findById(friendId);

        user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
        userStorage.update(user);

        eventService.createFriendEvent(userId, friendId, Operation.REMOVE);
    }

    public List<User> getFriends(int userId) {
        List<Integer> friendIds = userStorage.findById(userId).getFriendLinks().stream()
                .map(FriendLink::getFriendId)
                .toList();
        return userStorage.findByIds(friendIds);
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        validationUtils.validateUser(userId);
        validationUtils.validateUser(otherUserId);
        Set<Integer> userFriendIds = userStorage.findById(userId).getFriendLinks().stream()
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());
        Set<Integer> otherFriendIds = userStorage.findById(otherUserId).getFriendLinks().stream()
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());

        List<Integer> commonIds = userFriendIds.stream()
                .filter(otherFriendIds::contains)
                .toList();
        return userStorage.findByIds(commonIds);
    }

    public void deleteById(Integer id) {
        log.info("Удаление пользователя с ID: {}", id);
        log.info("Удаление новостей о пользователе с ID: {}", id);

        eventService.deleteEventByUserId(id);

        userStorage.deleteById(id);
    }

    public List<Film> getRecommendations(int userId) {
        userStorage.findById(userId);

        Optional<Integer> similarUserIdOpt = recommendationStorage.findMostSimilarUserId(userId);
        if (similarUserIdOpt.isEmpty()) {
            return List.of();
        }

        int similarUserId = similarUserIdOpt.get();
        List<Integer> filmIds = recommendationStorage.findRecommendedFilmIds(userId, similarUserId);

        if (filmIds.isEmpty()) {
            return List.of();
        }

        return filmStorage.findByIds(filmIds);
    }
}