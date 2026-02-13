package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FriendLink;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Добавление в друзья: пользователь userId отправляет запрос friendId или принимает запрос от friendId.
     * Если friendId уже отправил запрос userId — связь становится подтверждённой.
     * Иначе создаётся неподтверждённая связь (запрос от userId к friendId).
     */
    public void addFriend(int userId, int friendId) {
        log.info("Добавление в друзья: пользователь {} -> {}", userId, friendId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean friendAlreadyRequested = friend.getFriendLinks().stream()
                .anyMatch(fl -> fl.getFriendId() == userId && fl.getStatus() == FriendshipStatus.UNCONFIRMED);

        if (friendAlreadyRequested) {
            // userId принимает запрос от friendId — делаем обе связи подтверждёнными
            friend.getFriendLinks().removeIf(fl -> fl.getFriendId() == userId);
            friend.getFriendLinks().add(new FriendLink(userId, FriendshipStatus.CONFIRMED));
            user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
            user.getFriendLinks().add(new FriendLink(friendId, FriendshipStatus.CONFIRMED));
        } else {
            // userId отправляет запрос friendId — неподтверждённая связь
            user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
            user.getFriendLinks().add(new FriendLink(friendId, FriendshipStatus.UNCONFIRMED));
        }
    }

    /**
     * Удаление пользователя из друзей.
     * Связь удаляется с обеих сторон (независимо от статуса).
     */
    public void removeFriend(int userId, int friendId) {
        log.info("Удаление из друзей: пользователь {} -> {}", userId, friendId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
        friend.getFriendLinks().removeIf(fl -> fl.getFriendId() == userId);
    }

    /**
     * Получение списка друзей пользователя (только подтверждённые связи).
     */
    public List<User> getFriends(int userId) {
        return userStorage.findById(userId).getFriendLinks().stream()
                .filter(fl -> fl.getStatus() == FriendshipStatus.CONFIRMED)
                .map(fl -> userStorage.findById(fl.getFriendId()))
                .collect(Collectors.toList());
    }

    /**
     * Получение списка общих друзей двух пользователей (только подтверждённые связи).
     */
    public List<User> getCommonFriends(int userId, int otherUserId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherUserId);

        Set<Integer> userConfirmedIds = userStorage.findById(userId).getFriendLinks().stream()
                .filter(fl -> fl.getStatus() == FriendshipStatus.CONFIRMED)
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());
        Set<Integer> otherConfirmedIds = userStorage.findById(otherUserId).getFriendLinks().stream()
                .filter(fl -> fl.getStatus() == FriendshipStatus.CONFIRMED)
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());

        return userConfirmedIds.stream()
                .filter(otherConfirmedIds::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}

