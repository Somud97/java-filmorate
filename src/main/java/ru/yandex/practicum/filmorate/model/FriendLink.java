package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class FriendLink {
    private int friendId;
    private FriendshipStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendLink that = (FriendLink) o;
        return friendId == that.friendId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendId);
    }
}
