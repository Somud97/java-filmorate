package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
            // Сохраняем изменения в БД
            userStorage.update(user);
            userStorage.update(friend);
        } else {
            // userId отправляет запрос friendId — неподтверждённая связь
            user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
            user.getFriendLinks().add(new FriendLink(friendId, FriendshipStatus.UNCONFIRMED));
            // Сохраняем изменения в БД
            userStorage.update(user);
        }
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Удаление из друзей: пользователь {} -> {}", userId, friendId);

        User user = userStorage.findById(userId);
        userStorage.findById(friendId); // проверяем существование

        user.getFriendLinks().removeIf(fl -> fl.getFriendId() == friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(int userId) {
        return userStorage.findById(userId).getFriendLinks().stream()
                .map(fl -> userStorage.findById(fl.getFriendId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        Set<Integer> userFriendIds = userStorage.findById(userId).getFriendLinks().stream()
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());
        Set<Integer> otherFriendIds = userStorage.findById(otherUserId).getFriendLinks().stream()
                .map(FriendLink::getFriendId)
                .collect(Collectors.toSet());

        return userFriendIds.stream()
                .filter(otherFriendIds::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}

